package org.apache.doris.datasource.argo.util;

import java.util.Arrays;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/26.
 */
public abstract class Auth {

    public String getJdbcUrl(String value) {
        return value;
    }

    public static class PlainAuth extends Auth {
    }

    public static class LdapAuth extends Auth {
        private String username;
        private String password;

        public LdapAuth() {
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class KerberosAuth extends Auth {
        private String principal;
        private String keytab;
        private String kuser;
        private String krb5conf;

        public KerberosAuth() {
        }

        public String getPrincipal() {
            return principal;
        }

        public void setPrincipal(String principal) {
            this.principal = principal;
        }

        public String getKeytab() {
            return keytab;
        }

        public void setKeytab(String keytab) {
            this.keytab = keytab;
        }

        public String getKuser() {
            return kuser;
        }

        public void setKuser(String kuser) {
            this.kuser = kuser;
        }

        public String getKrb5conf() {
            return krb5conf;
        }

        public void setKrb5conf(String krb5conf) {
            this.krb5conf = krb5conf;
        }

        /**
         * reference: https://www.transwarp.cn/doc/argodb/5.2/_%E5%BA%94%E7%94%A8%E5%BC%80%E5%8F%91--application-development--development-jdbc#kerberos-auth
         *
         * @param value
         * @return
         */
        @Override
        public String getJdbcUrl(String value) {
            // append kerberos info to jdbc url
            final String[] params = {"principal=", "authentication=", "kuser=", "keytab=", "krb5conf="};
            final String[] values = {principal, "kerberos", kuser, keytab, krb5conf};
            if (Arrays.stream(params).anyMatch(value::contains)) {
                return value;
            }
            if (!value.endsWith(";")) {
                value += ";";
            }
            StringBuilder valueBuilder = new StringBuilder(value);
            for (int i = 0; i < params.length; i++) {
                valueBuilder.append(params[i]).append(values[i]).append(";");
            }
            value = valueBuilder.toString();
            return value;
        }
    }
}
