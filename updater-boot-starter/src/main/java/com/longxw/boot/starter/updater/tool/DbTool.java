package com.longxw.boot.starter.updater.tool;

import java.sql.*;

public class DbTool {

    private Connection connection;

    String initTableSql = "CREATE TABLE IF NOT EXISTS UPDATER_VERISON" +
            "(  VERSION varchar(255)," +
            "  UPDATE_TIME datetime DEFAULT NULL," +
            "  PRIMARY KEY (VERSION)" +
            ")";

    String selectVersionSql = "SELECT VERSION FROM UPDATER_VERISON";

    public DbTool(Connection connection){
        this.connection = connection;
    }

    /**获取第一行第一列数据
     * @author longxw
     * @since 2019-9-9
     */
    public Object queryObject(String sql) throws SQLException {
        try(PreparedStatement statement = this.connection.prepareStatement(sql)){
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next() && resultSet.getMetaData().getColumnCount() > 0){
                return resultSet.getObject(1);
            } else {
                return null;
            }
        }
    }

    /**获取第一行第一列数据
     * @author longxw
     * @since 2019-9-9
     */
    public Object queryObject(String sql,Object... args) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement(sql)){
            this.bindArgs(statement, args);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next() && resultSet.getMetaData().getColumnCount() > 0){
                return resultSet.getObject(1);
            } else {
                return null;
            }
        }
    }

    public void executeUpdate(String sql) throws SQLException {
        try(PreparedStatement statement = this.connection.prepareStatement(sql)){
            statement.executeUpdate();
        }
    }

    public void executeUpdate(String sql, Object... args) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);){
            this.bindArgs(statement, args);
            statement.executeUpdate();
        }
    }

    private void bindArgs(PreparedStatement statement, Object[] args) throws SQLException {
        if (args != null && args.length > 0){
            for (int i = 0, length = args.length; i < length; i ++){
                statement.setObject(i + 1, args[i]);
            }
        }
    }

    public String getCurrentVersion() throws SQLException{
        this.initTable();
        return (String)this.queryObject(selectVersionSql);
    }

    private void initTable() throws SQLException{
        this.executeUpdate(initTableSql);
    }


    public void insertVersion(String version)throws SQLException {
        String initVersionSql = "INSERT INTO UPDATER_VERISON (VERSION,UPDATE_TIME) VALUES(?,?)";
        this.executeUpdate(initVersionSql,version,new Timestamp(System.currentTimeMillis()));
    }

    public void updateVersion(String version) throws SQLException{
        String delql = "DELETE FROM UPDATER_VERISON";
        this.executeUpdate(delql);
        this.insertVersion(version);
    }
}
