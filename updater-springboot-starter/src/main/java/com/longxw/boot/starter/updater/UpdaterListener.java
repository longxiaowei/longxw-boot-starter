package com.longxw.boot.starter.updater;
import com.longxw.boot.starter.updater.tool.DbTool;
import com.longxw.boot.starter.updater.tool.FileTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@Slf4j
public class UpdaterListener implements ApplicationListener<ContextRefreshedEvent> {

    private String defaultPath = "classpath:script/sql";

    @Autowired
    UpdaterDataSourceProperties updaterDataSourceProperties;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        try {
            doUpdater(applicationContext);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void doUpdater(ApplicationContext applicationContext) throws SQLException,IOException{
        Connection connection = getConnection(applicationContext);
        DbTool dbTool = new DbTool(connection);
        String version = dbTool.getCurrentVersion();
        if(version == null){
            dbTool.insertVersion();//先插入一个默认
            version = dbTool.getInitialVersion();
        }

        Map<String,String> map = getScript(version);
        String lastVersion = null;
        for(String key : map.keySet()){
            String[] sqls = map.get(key).split(";");
            for(String sql : sqls){
                dbTool.executeUpdate(sql);
                lastVersion = key;
            }
        }
        dbTool.updateVersion(lastVersion);
        if(connection != null){
            connection.close();
        }

    }

    /** 获取符合条件的 sql, map 的 key=version，value=scripts
     * @author longxw
     * @since 2019-8-12
     */
    private Map<String,String> getScript(String version) {
        try {
            URL url = ResourceUtils.getURL(defaultPath);
            if(ResourceUtils.isJarURL(url)){
                Enumeration<JarEntry> entries = ((JarURLConnection) url.openConnection()).getJarFile().entries();
                if(entries != null){
                    String sqlPath = defaultPath.split(":")[0];
                    List<String> list = new ArrayList<>();
                    while (entries.hasMoreElements()){
                        JarEntry entry = entries.nextElement();
                        if(entry.getName().startsWith(sqlPath)){
                            int index = entry.getName().lastIndexOf(".sql");
                            if(index>0){
                                list.add(entry.getName());
                            }
                        }
                    }
                }
            }
            File sqlDir = new File(url.getPath()).getCanonicalFile();
            File[] files = sqlDir.listFiles();
            List<File> fileList = Arrays.stream(files)
                    .filter(file -> compareVersion(file.getName(),version)>0 )
                    .collect(Collectors.toList());
            Map<String,String> map = new TreeMap();
            fileList.forEach( file -> {
                StringBuffer sb =new StringBuffer();
                try{
                    FileTool.readLines(file).forEach(line -> {
                        if( !line.startsWith("#")){
                            sb.append(line);
                        }
                    });
                    map.put(file.getName(),sb.toString());
                }catch (IOException e){
                    e.printStackTrace();
                }

            });
            return map;
        }catch (Exception e){
            e.printStackTrace();
            return new TreeMap();
        }
    }

    private Connection getConnection(ApplicationContext applicationContext) throws SQLException{
        if(updaterDataSourceProperties.isUpdater()){
            log.debug("use updater datasource,url:{},username:{},password:{}");
            return DriverManager.getConnection(updaterDataSourceProperties.getUrl(), updaterDataSourceProperties.getUsername(), updaterDataSourceProperties.getPassword());
        }
        return applicationContext.getBean(DataSource.class).getConnection();
    }

    private int compareVersion(String t1,String t2){
        int result = t1.compareTo(t2);
        log.debug("{} compare to {} is {}", t1, t2, result);
        return result;
    }
}
