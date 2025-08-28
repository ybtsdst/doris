package org.apache.doris.datasource.argo;

import com.alibaba.google.common.collect.Lists;
import org.apache.doris.analysis.CreateDbStmt;
import org.apache.doris.analysis.CreateTableStmt;
import org.apache.doris.analysis.DropDbStmt;
import org.apache.doris.analysis.DropTableStmt;
import org.apache.doris.catalog.Column;
import org.apache.doris.common.DdlException;
import org.apache.doris.common.UserException;
import org.apache.doris.datasource.jdbc.client.JdbcArgoClient;
import org.apache.doris.datasource.operations.ExternalMetadataOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by bingtao.yin@transwarp.io on 2025/8/26.
 */
public class ArgoMetadataOps implements ExternalMetadataOps {

    private static final Logger log = LogManager.getLogger(ArgoMetadataOps.class);

    private JdbcArgoClient jdbcArgoClient;

    public ArgoMetadataOps(JdbcArgoClient jdbcArgoClient) {
        this.jdbcArgoClient = jdbcArgoClient;
    }

    @Override
    public void createDb(CreateDbStmt stmt) throws DdlException {
        throw new DdlException("Argo does not support create database");
    }

    @Override
    public void dropDb(DropDbStmt stmt) throws DdlException {
        throw new DdlException("Argo does not support drop database");
    }

    @Override
    public boolean createTable(CreateTableStmt stmt) throws UserException {
        throw new UserException("Argo does not support create table");
    }

    @Override
    public void dropTable(DropTableStmt stmt) throws DdlException {
        throw new DdlException("Argo does not support drop table");
    }

    @Override
    public void truncateTable(String dbName, String tblName, List<String> partitions) throws DdlException {
        throw new DdlException("Argo does not support truncate table");
    }

    @Override
    public List<String> listDatabaseNames() {
        return jdbcArgoClient.getDatabaseNameList();
    }

    @Override
    public List<String> listTableNames(String db) {
        return jdbcArgoClient.getTablesNameList(db);
    }

    @Override
    public boolean tableExist(String dbName, String tblName) {
        return listTableNames(dbName).contains(tblName);
    }

    @Override
    public boolean databaseExist(String dbName) {
        return listDatabaseNames().contains(dbName);
    }

    public List<Column> describeTable(String database, String table) {
        List<Column> columns = jdbcArgoClient.getColumnsFromJdbc(database, table);
        // TODO: post process
        return columns;
    }

    @Override
    public void close() {
        if (jdbcArgoClient != null) {
            jdbcArgoClient.closeClient();
            jdbcArgoClient = null;
        }
    }
}
