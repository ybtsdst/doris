package org.apache.doris.datasource.argo;

import org.apache.doris.catalog.Column;
import org.apache.doris.datasource.SchemaCacheValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/28.
 */
public class ArgoSchemaCacheValue extends SchemaCacheValue {

    private static final Logger log = LogManager.getLogger(ArgoSchemaCacheValue.class);

    public ArgoSchemaCacheValue(List<Column> schema) {
        super(schema);
    }
}
