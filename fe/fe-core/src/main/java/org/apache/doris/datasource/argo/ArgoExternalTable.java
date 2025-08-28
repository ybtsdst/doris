package org.apache.doris.datasource.argo;

import org.apache.doris.catalog.Column;
import org.apache.doris.datasource.ExternalTable;
import org.apache.doris.datasource.SchemaCacheValue;
import org.apache.doris.thrift.TArgoTable;
import org.apache.doris.thrift.TTableDescriptor;
import org.apache.doris.thrift.TTableType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/26.
 */
public class ArgoExternalTable extends ExternalTable {

    private static final Logger log = LogManager.getLogger(ArgoExternalTable.class);

    public ArgoExternalTable() {
    }

    public ArgoExternalTable(long id, String name, String remoteName, ArgoExternalCatalog catalog, ArgoExternalDatabase db) {
        super(id, name, remoteName, catalog, db, TableType.ARGO_EXTERNAL_TABLE);
    }

    @Override
    public Optional<SchemaCacheValue> initSchema() {
        ArgoExternalCatalog catalog = (ArgoExternalCatalog) getCatalog();
        ArgoMetadataOps metadataOps = (ArgoMetadataOps) catalog.getMetadataOps();

        List<Column> columns = metadataOps.describeTable(dbName, name);
        return Optional.of(new ArgoSchemaCacheValue(columns));
    }

    @Override
    public TTableDescriptor toThrift() {
        TArgoTable argoTable = new TArgoTable();
        argoTable.setDbName(dbName);
        argoTable.setTableName(name);

        ArgoSchemaCacheValue schemaCacheValue = (ArgoSchemaCacheValue) initSchema().orElse(
            new ArgoSchemaCacheValue(Collections.EMPTY_LIST));

        TTableDescriptor tableDescriptor = new TTableDescriptor(getId(), TTableType.ARGO_TABLE, schemaCacheValue.getSchema().size(), 0,
            getName(), "");

        tableDescriptor.setArgoTable(argoTable);
        return tableDescriptor;
    }
}
