package org.apache.doris.datasource.jdbc.client;

import org.apache.doris.catalog.Type;
import org.apache.doris.datasource.jdbc.util.JdbcFieldSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/26.
 */
public class JdbcArgoClient extends JdbcClient {

    private static final Logger log = LogManager.getLogger(JdbcArgoClient.class);

    public JdbcArgoClient(JdbcClientConfig jdbcClientConfig) {
        super(jdbcClientConfig);
    }

    @Override
    protected Type jdbcTypeToDoris(JdbcFieldSchema fieldSchema) {
        String dataTypeName = fieldSchema.getDataTypeName().orElse("unknown").toLowerCase(Locale.ROOT);
        Type dorisType = null;
        switch (dataTypeName) {
            case "int":
                dorisType = Type.INT;
                break;
            case "string":
                dorisType = Type.STRING;
                break;
            default:
                throw new RuntimeException("unknown data type: " + dataTypeName);
        }

        return dorisType;
    }
}
