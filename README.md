# dbsync
一个sql层面的数据库同步工具，支持如下功能：
+ 支持全量迁移和增量迁移
+ 支持来源和目标表异构（表名不一样，字段不一致）
+ 支持多张表合并到一张表
+ 支持单表迁移到分库分库，或分库分库扩容
+ 支持远程迁移

## 使用限制
+ 表内必须要带有一个最后修改时间字段，更新修改都会更新该字段
+ 最好带有一个逻辑删除字段，用于感知数据被删除


## 使用方法
1. 创建服务端 SyncServer
```
// 创建本地服务端
SyncServer syncServer = new DefaultSyncServer();
// 或是创建远程服务端
RemoteSyncServerFactory factory = new RemoteSyncServerFactory();
factory.setAppId("xxx");
factory.setAppSecret("xxx");
factory.setRemotingUrl("http://xxxxx");
SyncServer syncServer = factory.createSyncServer();


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

