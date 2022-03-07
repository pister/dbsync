# dbsync
一个sql层面的数据库同步工具，支持如下功能：
+ 支持全量迁移和增量迁移
+ 支持来源和目标表异构（表名不一致，字段不一致）
+ 支持目标表从不同的源表中获取数据
+ 支持单表迁移到分库分库
+ 支持远程迁移
+ 支持断点续传

## 使用限制
+ 表内必须要带有一个最后修改时间字段，更新修改都会更新该字段
+ 最好带有一个逻辑删除字段，用于感知数据被删除


## 基本使用方法
1. 创建服务端 SyncServer
```
// 创建本地服务端
SyncServer syncServer = new DefaultSyncServer();

// 注册需要被迁移的源数据库

// 第一个源数据库
syncServer.registerDbConfig(0, "192.168.1.123:3306/mydb0", "my_user0", "my_password0");
// 第二个源数据库
syncServer.registerDbConfig(1, "192.168.1.123:3306/mydb1", "my_user1", "my_password1");
// 初始化
syncServer.init();
```

2. 创建客户端 
```
SyncClient syncClient = new SyncClient();
// 设置对应的服务端
syncClient.setSyncServer(syncServer);
// 设置目标数据库
syncClient.registerLocalDb(0, "127.0.0.1:3306/sample2", "root", "123456");

// 添加一个任务配置
syncClient.addTableTaskConfig(TableTaskConfig.makeSingle("my_sample_task", 0, "sample_pen", 0, "sample_pen"));

// 初始化
syncClient.init();

```

3. 实行同步迁移
```
// 实行迁移，可调用多次
syncClient.exec("my_sample_task");
```

## 远程迁移服务端配置
1. 配置ServiceExporterServlet

```
<servlet>
    <servlet-name>ServiceExporterServlet</servlet-name>
    <servlet-class>com.github.pister.dbsync.endpoint.remoting.server.ServiceExporterServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
    <init-param>
        <param-name>as.server</param-name>
        <param-value>true</param-value>
    </init-param>
    <init-param>
        <param-name>app.secret</param-name>
        <param-value>appId1=secret123;appId2=secret456</param-value>
    </init-param>
    <init-param>
        <param-name>db.config</param-name>
        <param-value>0=my_user0:my_password0@192.168.1.123:3306/mydb0;1=my_user1:my_password1@192.168.1.123:3306/mydb1</param-value>
    </init-param>
</servlet>

<servlet-mapping>
    <servlet-name>ServiceExporterServlet</servlet-name>
    <url-pattern>sync.api</url-pattern>
</servlet-mapping>
```

2. 启动web容器

3. 创建远程服务端，后面的使用方法和基础的本地服务端一样
```
// 创建远程服务端
RemoteSyncServerFactory factory = new RemoteSyncServerFactory();
factory.setAppId("appId1");
factory.setAppSecret("secret123");
factory.setRemotingUrl("http://192.168.1.199/sync.api");

SyncServer syncServer = factory.createSyncServer();

```

## 进阶使用方法
```
// 创建单表到单表同步
TableTaskConfig tableTaskConfig = TableTaskConfig.makeSingle("my_sample_task", 0, "sample_pen", 0, "sample_pen");

// 创建单表到多表同步
TableTaskConfig tableTaskConfig = TableTaskConfig.makeOneTooManyShard("sports", 0, "my_source_table", "my_dest_table_%04d", "my_route_column", 2, 4);

// 把一个字段映射到另一个字段
tableTaskConfig.mappingColumn(...)

// 只跑全量，不跑增量
tableTaskConfig.onlyFullDump(...)

// 行数据拦截修改
tableTaskConfig.rowInterceptor(...)

// 设置批量修改拦截器
tableTaskConfig.batchInterceptor(...)

// 设置更新时间字段
tableTaskConfig.setUpdatedField(...)

// 设置分库分表路由策略
tableTaskConfig.setShardStrategy(...);

// 设置数据源额外查询条件
tableTaskConfig.sourceExtCondition(...)


// 忽略检查（建议不要忽略）
syncClient.setIgnoreCheck(...)

// 创建任务
syncClient.addTableTaskConfig(tableTaskConfig);



```

