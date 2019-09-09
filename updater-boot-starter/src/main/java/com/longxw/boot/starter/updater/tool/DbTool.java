package com.longxw.boot.starter.updater.tool;

import java.sql.*;

public class DbTool {

    private Connection connection;

    String tableName = "UPDATER_VERISON";

    private String initialVersion = "1.0.0";

    public String getInitialVersion() {
        return initialVersion;
    }

    String initTableSql = "CREATE TABLE IF NOT EXISTS {} ( "+tableName +
            "  VERSION varchar(255) DEFAULT NULL," +
            "  UPDATE_TIME datetime DEFAULT NULL," +
            "  PRIMARY KEY (VERSION)" +
            ")";

    String selectVersionSql = "SELECT VERSION FROM "+tableName;

    public DbTool(Connection connection){
        this.connection = connection;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();
        statement.close();
        return resultSet;
    }

    public ResultSet executeQuery(String sql,Object... args) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement(sql);
        this.bindArgs(statement, args);
        ResultSet resultSet = statement.executeQuery();
        statement.close();
        return resultSet;
    }

    public void executeUpdate(String sql) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement(sql);
        statement.executeUpdate();
        statement.close();
    }

    public void executeUpdate(String sql, Object... args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        this.bindArgs(statement, args);
        statement.executeUpdate();
        statement.close();
    }

    private void bindArgs(PreparedStatement statement, Object[] args) throws SQLException {
        if (args != null && args.length > 0){
            for (int i = 0, length = args.length; i < length; i ++){
                statement.setObject(i + 1, args[i]);
            }
        }
    }

    public String getCurrentVersion() throws SQLException{
        this.initVersion();
        ResultSet resultSet = this.executeQuery(selectVersionSql);
        if (resultSet.next() && resultSet.getMetaData().getColumnCount() > 0){
            return (String)resultSet.getObject(1);
        } else {
            return null;
        }
    }

    private void initVersion() throws SQLException{
        this.executeUpdate(initTableSql);
    }

    public void insertVersion() throws SQLException{
        String initVersionSql = "INSERT INTO UPDATER_VERISON (VERSION,UPDATE_TIME) VALUES(?,?)";
        this.executeUpdate(initVersionSql,initialVersion,new Timestamp(System.currentTimeMillis()));
    }

    public void updateVersion(String version) throws SQLException{
        String updatesql = "UPDATE UPDATER_VERISON SET VERSION = ?";
        this.executeUpdate(updatesql,version);
    }
}
