package com.longxw.boot.starter.updater;

import lombok.Data;

@Data
public class SQLEntry implements Comparable<SQLEntry>{

    private String path;

    private String version;

    private String name;

    private String[] sqls;

    public SQLEntry(String path,String version,String name){
        this.path = path;
        this.version = version;
        this.name = name;
    }
    public SQLEntry(){

    }

    @Override
    public int compareTo(SQLEntry o) {
        return this.getPath().compareTo(o.getPath());
    }
}
