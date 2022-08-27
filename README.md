# 1. 前言

当前项目用于模拟使用数据库进行锁控制的场景，对TiDB的乐观事务、悲观事务模型不同条件下事务执行情况进行验证。

以下示例项目的使用说明，相关文档可参考以下内容：

“TiDB乐观事务、悲观事务模型验证”[https://blog.csdn.net/a82514921/article/details/126563502](https://blog.csdn.net/a82514921/article/details/126563502)

# 2. 配置TiDB数据库信息

在TiDB-Transaction-test/src/main/resources/application.properties配置文件中配置TiDB数据库信息。

# 3. 构建可以执行的程序

打开命令行，进入示例项目根目录，执行以下命令：

```
gradlew gen_run_jar
```

执行完毕后，会在output_dir目录中生成可以执行的程序。

# 4. 执行示例项目

使用Windows操作系统时，需要进入“output_dir/run-bat”目录，使用Linux操作系统时，需要进入“output_dir/run-shell”目录。

在以上目录，有用于执行不同场景示例项目的脚本，每个场景都有两个脚本，分别为“run-xxx-\[1\].yyy”“run-xxx-\[2\].yyy”，脚本文件名及验证的场景如下：

|脚本名|验证的场景|
|---|---|
|run-LockService_1_Pessimistic|使用悲观事务|
|run-LockService_2a_OptimisticAutoRetry|使用乐观事务，启用自动重试|
|run-LockService_3a_OptimisticNoAutoRetry|使用乐观事务，禁用自动重试|
|run-LockService_3b_OptimisticNoAutoRetryRecovery|使用乐观事务，禁用自动重试，事务开始前记录自动重试设置，事务结束前恢复自动重试设置|
|run-LockService_4a_OptimisticAutoRetryNoTransactionNoCheck|使用乐观事务，使用自动重试，不使用事务，update操作不判断修改前的值|
|run-LockService_4b_OptimisticAutoRetryNoTransactionWithCheck|使用乐观事务，使用自动重试，不使用事务，update操作判断修改前的值|

每次在执行示例项目时，需要执行某个场景对应的两个脚本，会分别启动两个进程，执行相同的代码，验证对应的场景下TiDB事务执行情况。

执行时的日志会生成在log目录中，日志文件名与脚本文件名前缀相同。

# 5. 调整定时任务执行时间间隔

定时任务信息在applicationContext.xml文件中配置，默认配置如下：

```xml
<task:scheduled ref="test.tidb.task.SchedulerTask4TiDB" method="process" cron="*/10 * * * * *"/>
```

可以通过修改以上crontab表达式调整定时任务执行时间间隔。
