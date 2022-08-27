# 结束相关进程
ps -ef | grep TiDB-Transaction-test.jar | grep -v grep | awk '{print $2}' | xargs kill -9