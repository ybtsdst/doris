/*
 *  Created on: 2025-08-28
 *      Author: bingtao.yin@transwarp.io
 */

#include "pipeline/exec/argo_scan_operator.h"

#include "vec/exec/scan/argo_scanner.h"

namespace doris::pipeline {

Status ArgoScanLocalState::_init_scanners(std::list<vectorized::VScannerSPtr>* scanners) {
    auto& p = _parent->cast<ArgoScanOperatorX>();
    std::unique_ptr<vectorized::ArgoScanner> scanner =
            vectorized::ArgoScanner::create_unique(state(), this, p._limit, _scanner_profile.get());
    RETURN_IF_ERROR(scanner->prepare(state(), _conjuncts));
    scanners->push_back(std::move(scanner));
    return Status::OK();
}

std::string ArgoScanLocalState::name_suffix() const {
    return fmt::format(" (id={}. nereids_id={}. argo table name= {})",
                       std::to_string(_parent->node_id()), std::to_string(_parent->nereids_id()),
                       _parent->cast<ArgoScanOperatorX>()._table_name);
};

ArgoScanOperatorX::ArgoScanOperatorX(ObjectPool* pool, const TPlanNode& tnode, int operator_id,
                                     const DescriptorTbl& descs, int parallel_tasks)
        : ScanOperatorX<ArgoScanLocalState>(pool, tnode, operator_id, descs, parallel_tasks),
          _tuple_id(tnode.argo_scan_node.tuple_id),
          _table_name(tnode.argo_scan_node.table_name) {
    _output_tuple_id = tnode.argo_scan_node.tuple_id;
}

}; // namespace doris::pipeline
