package org.apache.doris.datasource.argo;

import org.apache.doris.common.DdlException;
import org.apache.doris.datasource.CatalogProperty;
import org.apache.doris.datasource.ExternalCatalog;
import org.apache.doris.datasource.InitCatalogLog;
import org.apache.doris.datasource.SessionContext;
import org.apache.doris.datasource.argo.util.Auth;
import org.apache.doris.datasource.jdbc.client.JdbcArgoClient;
import org.apache.doris.datasource.jdbc.client.JdbcClientConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/26.
 */
public class ArgoExternalCatalog extends ExternalCatalog {

    private static final Logger log = LogManager.getLogger(ArgoExternalCatalog.class);
    private static final String ARGO_DIR = System.getenv("DORIS_HOME") + "/argo";

    public ArgoExternalCatalog(long catalogId, String name, String resource, Map<String, String> props, String comment) {
        super(catalogId, name, InitCatalogLog.Type.ARGO, comment);

        this.catalogProperty = new CatalogProperty(resource, props);
    }

    @Override
    public void checkProperties() throws DdlException {
        super.checkProperties();
        final Map<String, String> properties = catalogProperty.getProperties();
        if (!properties.containsKey("jdbc.url")) {
            throw new DdlException("[jdbc.url] is required");
        }
        if (!properties.containsKey("driver.url")) {
            throw new DdlException("[driver.url] is required");
        }

        String authType = catalogProperty.getOrDefault("auth.type", "plain").toLowerCase(Locale.ROOT);
        switch (authType) {
            case "kerberos":
                if (!properties.containsKey("auth.kerberos.principal")) {
                    throw new DdlException("For kerberos authentication, [auth.kerberos.principal] is required");
                }
                if (!properties.containsKey("auth.kerberos.kuser")) {
                    throw new DdlException("For kerberos authentication, [auth.kerberos.kuser] is required");
                }
                if (!properties.containsKey("auth.kerberos.keytab")) {
                    throw new DdlException("For kerberos authentication, [auth.kerberos.keytab] is required");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void setDefaultPropsIfMissing(boolean isReplay) {
        super.setDefaultPropsIfMissing(isReplay);
    }

    @Override
    public void onClose() {
        if (metadataOps != null) {
            metadataOps.close();
            metadataOps = null;
        }
    }

    @Override
    public List<String> listTableNames(SessionContext ctx, String dbName) {
        return getMetadataOps().listTableNames(dbName);
    }

    @Override
    public boolean tableExist(SessionContext ctx, String dbName, String tblName) {
        return getMetadataOps().tableExist(dbName, tblName);
    }

    @Override
    protected void initLocalObjectsImpl() {
        final Map<String, String> properties = catalogProperty.getProperties();
        JdbcClientConfig jdbcClientConfig = new JdbcClientConfig();
        jdbcClientConfig.setCatalog(name).setUser("").setPassword("")
            .setDriverClass("org.apache.hive.jdbc.HiveDriver")
            .setDriverUrl(properties.get("driver.url"));

        String authType = catalogProperty.getOrDefault("auth.type", "plain").toLowerCase(Locale.ROOT);
        Auth auth = null;
        switch (authType) {
            case "plain":
                auth = new Auth.PlainAuth();
                break;
            case "ldap": {
                final String username = catalogProperty.getOrDefault("auth.plain.username", "");
                final String password = catalogProperty.getOrDefault("auth.plain.password", "");

                Auth.LdapAuth ldapAuth = new Auth.LdapAuth();
                ldapAuth.setUsername(username);
                ldapAuth.setPassword(password);
                auth = ldapAuth;

                jdbcClientConfig.setUser(username).setPassword(password);
            }
            break;
            case "kerberos": {
                Auth.KerberosAuth kerberosAuth = new Auth.KerberosAuth();
                kerberosAuth.setPrincipal(properties.get("auth.kerberos.principal"));
                kerberosAuth.setKuser(properties.get("auth.kerberos.kuser"));
                String keytab = properties.get("auth.kerberos.keytab");
                String keytabAbs = ARGO_DIR + "/" + keytab;
                if (!new File(keytabAbs).exists()) {
                    throw new RuntimeException("keytab file does not exist: " + keytabAbs);
                }
                kerberosAuth.setKeytab(keytabAbs);

                String krb5Conf = catalogProperty.getOrDefault("auth.kerberos.krb5conf", "krb5.conf");
                final String krb5ConfAbs = ARGO_DIR + "/" + krb5Conf;
                if (!new File(krb5ConfAbs).exists()) {
                    throw new RuntimeException("krb5conf file does not exist: " + krb5ConfAbs);
                }
                kerberosAuth.setKrb5conf(krb5ConfAbs);
                if (System.getProperty("java.security.krb5.conf") != null) {
                    log.warn("java.security.krb5.conf is already set to {}, it will be overridden to {}",
                        System.getProperty("java.security.krb5.conf"), krb5ConfAbs);
                }
                // TODO: also set -Dsun.security.krb5.debug=true?
                System.setProperty("java.security.krb5.conf", krb5ConfAbs);

                auth = kerberosAuth;
            }
            break;
            default:
                throw new RuntimeException("Unknown auth type: " + authType);
        }

        jdbcClientConfig.setJdbcUrl(auth.getJdbcUrl(properties.get("jdbc.url")));
        metadataOps = new ArgoMetadataOps(new JdbcArgoClient(jdbcClientConfig));
    }
}
