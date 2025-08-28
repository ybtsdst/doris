/*
 *  Created on: 2025-08-28
 *      Author: bingtao.yin@transwarp.io
 */

#include "vec/exec/scan/argo_scanner.h"

#if defined(__clang__)
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wnon-virtual-dtor"
#pragma clang diagnostic ignored "-Wunused-result"
#elif defined(__GNUC__)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wnon-virtual-dtor"
#pragma GCC diagnostic ignored "-Wunused-result"
#endif

#include "shiva/client/client.h"
#include "shiva/client/table_deleter.h"
#include "shiva/util/status.h"

#if defined(__clang__)
#pragma clang diagnostic pop
#elif defined(__GNUC__)
#pragma GCC diagnostic pop
#endif

#include "common/logging.h"
#include "common/status.h"

#define CHECK_SHIVA_STATUS(status)                                                            \
    do {                                                                                      \
        const shiva::Status& tmp = status;                                                    \
        if (!tmp.ok()) {                                                                      \
            return doris::Status::InternalError("shiva client exception: " + tmp.ToString()); \
        }                                                                                     \
    } while (0)

namespace {

const uint32_t dim = 4; // dimension
doris::Status create_shiva_client(const std::string& master_group,
                                  std::shared_ptr<shiva::client::ShivaClient>& client) {
    const std::string username = "shiva";
    const std::string password = "shiva";

    shiva::Status status;
    shiva::client::ShivaClientBuilder builder;
    status = builder.master_group(master_group).PlainAuth(username, password).Build(&client);
    if (!status.ok()) {
        LOG(ERROR) << "create shiva client failed, status: " << status.ToString();
        return doris::Status::InternalError("create shiva client failed");
    }
    return doris::Status::OK();
}

doris::Status create_table(shiva::client::ShivaClient* client, std::string& table_id) {
    const std::string table_name = "table_from_doris2";

    std::shared_ptr<shiva::client::ShivaTableDeleter> deleter;
    CHECK_SHIVA_STATUS(client->NewTableDeleter(&deleter));
    deleter->table_name(table_name).drop_directly(true).wait(true).Delete();

    std::shared_ptr<shiva::client::ShivaTableCreator> creator;
    CHECK_SHIVA_STATUS(client->NewTableCreator(&creator));

    shiva::client::ShivaSchema schema;
    shiva::client::ShivaSchemaBuilder schema_builder;
    schema_builder.AddColumn("uid", shiva::client::ShivaColumnSchema::BIGINT)->NotNull();
    schema_builder.AddColumn("content", shiva::client::ShivaColumnSchema::STRING)->NotNull();
    schema_builder.AddColumn("embedding", shiva::client::ShivaColumnSchema::FLOAT_VECTOR)
            ->NotNull()
            ->Dimensions({dim});
    schema_builder.SetPrimaryKey({"uid"});
    CHECK_SHIVA_STATUS(schema_builder.Build(&schema););
    shiva::Status status = creator->add_primary_hash_partitions({"uid"}, 3)
                                   .schema(&schema)
                                   .engine_type(shiva::client::TAB_ENGINE)
                                   .num_replicas(1)
                                   .timeout(kutil::MonoDelta::FromSeconds(300))
                                   .table_name(table_name)
                                   .Create();
    CHECK_SHIVA_STATUS(status);
    table_id = creator->created_table().table_id();
    VLOG_CRITICAL << "shiva table created: " << table_id;

    std::shared_ptr<shiva::client::ShivaTableAlterer> alterer;
    CHECK_SHIVA_STATUS(client->NewTableAlterer(&alterer));
    alterer->table_id(table_id).AddEmbeddingIndex("embedding_index_0", "embedding",
                                                  shiva::client::ShivaEmbeddingMetricType::L2,
                                                  shiva::client::ShivaEmbeddingIndexType::FLAT);
    CHECK_SHIVA_STATUS(alterer->Alter());
    VLOG_CRITICAL << "embedding index created: " << table_id;

    return doris::Status::OK();
}

doris::Status load_data(shiva::client::ShivaClient* client, const std::string& table_id) {
    // open table
    std::shared_ptr<shiva::client::ShivaTable> table;
    CHECK_SHIVA_STATUS(client->OpenTable(table_id, &table));
    auto shiva_indexes = table->indexes();

    // insert some data
    int length = dim * sizeof(float);
    auto session = client->NewSession();
    CHECK_SHIVA_STATUS(session->SetFlushMode(shiva::client::ShivaSession::AUTO_FLUSH_BACKGROUND));
    std::vector<float> data {0.1, 0.2, 0.3, 0.4};

    for (int i = 0; i < 10; ++i) {
        auto* op = table->NewInsert();
        auto* row = op->mutable_row();
        CHECK_SHIVA_STATUS(row->SetInt64("uid", i));
        CHECK_SHIVA_STATUS(row->SetString("content", "value from doris: " + std::to_string(i)));
        CHECK_SHIVA_STATUS(row->SetFloatVector("embedding",
                                               shiva::Slice((const uint8_t*)data.data(), length)));
        CHECK_SHIVA_STATUS(session->Apply(op));
    }
    auto ts = session->Flush();
    if (!ts.ok()) {
        std::vector<shiva::client::ShivaError*> errors;
        bool overflowed;
        session->GetPendingErrors(&errors, &overflowed);
        LOG(WARNING) << "errors size : " << errors.size();
        for (auto* error : errors) {
            LOG(WARNING) << "insert [" << error->failed_op().ToString() << "] failed with error "
                         << error->status();
            delete error;
        }
    }
    VLOG_CRITICAL << "data inserted";

    // rebuild index
    auto* job = new shiva::client::ShivaTabRebuildEmbeddingIndexJob(
            table_id, shiva_indexes->embedding_indexes().cbegin()->second->id());
    auto submitter = client->NewJobSubmitter();
    CHECK_SHIVA_STATUS(
            submitter->job(job).wait(true).timeout(kutil::MonoDelta::FromSeconds(300)).Submit());
    VLOG_CRITICAL << "embedding index rebuilt";

    return doris::Status::OK();
}

doris::Status query(shiva::client::ShivaClient* client, const std::string& table_id) {
    // open table
    std::shared_ptr<shiva::client::ShivaTable> table;
    CHECK_SHIVA_STATUS(client->OpenTable(table_id, &table));

    // ann search
    int k = 3;
    auto* searcher = table->BuildAnnSearcher();
    shiva::client::ShivaAnnSearchRequest* ann_request;
    CHECK_SHIVA_STATUS(table->NewAnnSearchRequest("embedding", k, &ann_request));
    float query[] = {0, 0, 0, 0};
    ann_request->add_vector(query);
    CHECK_SHIVA_STATUS(searcher->SetAnnSearchRequest(ann_request));

    std::vector<shiva::client::ShivaAnnSearchResult*> results;
    CHECK_SHIVA_STATUS(searcher->Search(&results));
    for (auto* result : results) {
        for (int i = 0; i < k; ++i) {
            auto row_ptr = result->Row(i);
            float score;
            CHECK_SHIVA_STATUS(row_ptr->GetAnnScore(&score));
            VLOG_CRITICAL << "embedding result " << i << ", ann score : " << score
                          << ", value : " << result->Row(i)->ToString();
        }
    }

    return doris::Status::OK();
}

} // namespace

namespace doris::vectorized {

ArgoScanner::ArgoScanner(RuntimeState* state, doris::pipeline::ArgoScanLocalState* local_state,
                         int64_t limit, RuntimeProfile* profile)
        : VScanner(state, local_state, limit, profile) {}

Status ArgoScanner::open(RuntimeState* state) {
    VLOG_CRITICAL << "ArgoScanner::open";
    RETURN_IF_CANCELLED(state);
    RETURN_IF_ERROR(VScanner::open(state));
    return Status::OK();
}

Status ArgoScanner::close(RuntimeState* state) {
    VLOG_CRITICAL << "ArgoScanner::close";
    RETURN_IF_ERROR(VScanner::close(state));
    return Status::OK();
}

Status ArgoScanner::prepare(RuntimeState* state, const VExprContextSPtrs& conjuncts) {
    VLOG_CRITICAL << "ArgoScanner::prepare";
    RETURN_IF_ERROR(VScanner::prepare(state, conjuncts));
    // TODO: tuple descriptor
    return Status::OK();
}

Status ArgoScanner::_get_block_impl(RuntimeState* state, Block* block, bool* eos) {
    *eos = true;

    std::shared_ptr<shiva::client::ShivaClient> client;
    RETURN_IF_ERROR(create_shiva_client("172.17.120.15:18650", client));
    VLOG_CRITICAL << "shiva client created";

    std::string table_id;
    RETURN_IF_ERROR(create_table(client.get(), table_id));
    RETURN_IF_ERROR(load_data(client.get(), table_id));
    RETURN_IF_ERROR(query(client.get(), table_id));

    return Status::OK();
}

} // namespace doris::vectorized
