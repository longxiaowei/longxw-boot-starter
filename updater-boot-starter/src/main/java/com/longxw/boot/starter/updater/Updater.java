package com.longxw.boot.starter.updater;

import com.longxw.boot.starter.updater.tool.DbTool;
import com.longxw.boot.starter.updater.tool.FileTool;
import com.longxw.boot.starter.updater.tool.IOTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;

@Slf4j
public class Updater {

    private String defaultPath = "classpath:script/sql";

    private DbTool dbTool;

    public Updater(Connection connection){
        this.dbTool = new DbTool(connection);
    }

    public void update() throws SQLException,IOException{
        String version = dbTool.getCurrentVersion();
        if(version == null){
            version = "0";
        }

        Map<String,String> map = this.getScript(version);
        if(!map.isEmpty()){
            for(String key : map.keySet()){
                String[] sqls = map.get(key).trim().split(";");
                try {
                    dbTool.setAutoCommit(false);
                    for(String sql : sqls){
                        log.info("executor sql:{},version:{}",sql,key);
                        dbTool.executeUpdate(sql);
                    }
                    dbTool.updateVersion(key);
                    dbTool.commit();
                }catch (SQLException e){
                    dbTool.rollback();
                }
            }
        }
    }

    private int compareVersion(String version1,String version2){
        return version1.compareTo(version2);
    }

    private Map<String,String> getScript(String version) throws IOException {
        URL url = ResourceUtils.getURL(defaultPath);
        if(ResourceUtils.isJarURL(url)){
            log.info("read jar files");
            Enumeration<JarEntry> entries = ((JarURLConnection) url.openConnection()).getJarFile().entries();
            if(entries != null){
                String sqlPath = defaultPath.split(":")[1];
                Map<String,String> map = new HashMap<>();
                while (entries.hasMoreElements()){
                    JarEntry entry = entries.nextElement();
                    if(entry.getName().startsWith(sqlPath)){
                        int index = entry.getName().lastIndexOf(".sql");
                        if(index>0){
                            String[] arr = entry.getName().split("/");
                            int versionIndex = arr.length-2;
                            String sqlVersion = arr[versionIndex];
                            if(compareVersion(sqlVersion,version)>0){
                                log.info("jar file:{}",entry.getName());
                                String value = map.get(sqlVersion);
                                if(value == null || "".equals(value)){
                                    value = entry.getName();
                                }else{
                                    value = ","+entry.getName();
                                }
                                map.put(sqlVersion,value);
                            }
                        }
                    }
                }
                if(map.size() == 0){
                    return map;
                }else{
                    Map<String,String> result = new HashMap<>();
                    map.forEach((k,v)->{
                        StringBuilder sb = new StringBuilder();
                        Arrays.asList(v.split(",")).stream().forEach(string -> {
                            log.info("read file:{}",string);
                            try (InputStream is =  this.getClass().getClassLoader().getResourceAsStream(string)){
                                sb.append(IOTool.readToString(is));
                            }catch (Exception e){

                            }
                        });
                        result.put(k,sb.toString());
                    });
                    return result;
                }
            }else{
                return new HashMap<>();
            }
        }else{
            File sqlDir = new File(url.getPath()).getCanonicalFile();
            //获取该目录下所有文件和目录的绝对路径
            File[] files = sqlDir.listFiles();

            //找出符合的版本号的文件夹目录
            List<File> fileList = Arrays.stream(files)
                    .filter(file -> file.isDirectory())
                    .filter(file -> compareVersion(file.getName(),version)>0 )
                    .collect(Collectors.toList());
            Map<String,String> map = new TreeMap();
            fileList.forEach( file -> {
                StringBuffer sb =new StringBuffer();
                Arrays.stream(file.listFiles()).forEach(sqlFile -> {
                    try {
                        FileTool.readLines(sqlFile).forEach(line -> {
                            if( !line.startsWith("#")){
                                sb.append(line);
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                });
                map.put(file.getName(),sb.toString());

            });
            return map;
        }
    }
}
