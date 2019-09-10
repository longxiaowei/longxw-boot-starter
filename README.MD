## sql 脚本控制器

　　程序启动时触发监听器，读取 `classpath:script/sql` 下的脚本，脚本目录结构如下：

```
resources
 └────script
    └───── sql
        └───── 1.0.0
            └──── xxx.sql
```

　　在 resources 目录下新建 script/sql 目录，sql目录下一级目录即版本号，程序启动时读取库中的版本号。获取sql目录下所有大于数据库版本的脚本执行，执行完毕后将数据库版本号更新为最新的版本号，目前只支持mysql。