package org.apache.doris.datasource.argo;

import org.apache.doris.datasource.ExternalCatalog;
import org.apache.doris.datasource.ExternalDatabase;
import org.apache.doris.datasource.InitDatabaseLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/26.
 */
public class ArgoExternalDatabase extends ExternalDatabase<ArgoExternalTable> {

    private static final Logger log = LogManager.getLogger(ArgoExternalDatabase.class);

    /**
     * Create external database.
     *
     * @param extCatalog The catalog this database belongs to.
     * @param id         Database id.
     * @param name       Database name.
     * @param remoteName Remote database name.
     */
    public ArgoExternalDatabase(ExternalCatalog extCatalog, long id, String name, String remoteName) {
        super(extCatalog, id, name, remoteName, InitDatabaseLog.Type.ARGO);
    }

    @Override
    protected ArgoExternalTable buildTableInternal(String remoteTableName, String localTableName, long tblId, ExternalCatalog catalog, ExternalDatabase db) {
        return new ArgoExternalTable(tblId, localTableName, remoteTableName, (ArgoExternalCatalog) extCatalog, (ArgoExternalDatabase) db);
    }
}
