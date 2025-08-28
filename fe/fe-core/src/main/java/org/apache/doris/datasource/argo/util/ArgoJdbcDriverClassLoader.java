package org.apache.doris.datasource.argo.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/27.
 */
public class ArgoJdbcDriverClassLoader extends ClassLoader {

    private static final Logger log = LogManager.getLogger(ArgoJdbcDriverClassLoader.class);

    private static final String[] PACKAGES_LOAD_BY_JAR = {
        "org.apache.thrift",
        "org.apache.hive",
        "org.apache.hadoop",
        "io.transwarp"
    };

    public ArgoJdbcDriverClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (Arrays.stream(PACKAGES_LOAD_BY_JAR).anyMatch(name::startsWith)) {
            return null;
        }

        return super.loadClass(name, resolve);
    }
}
