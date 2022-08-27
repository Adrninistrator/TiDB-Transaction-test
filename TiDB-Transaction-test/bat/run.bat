SETLOCAL ENABLEDELAYEDEXPANSION
set CLASSPATH=
FOR %%C IN (..\lib\*.jar) DO set CLASSPATH=!CLASSPATH!;%%C
echo %CLASSPATH%
java -Dtransaction.mode=%1 -Dinstance.num=%2 -Dfile.encoding=UTF-8 -cp .;../config;../jar/TiDB-Transaction-test.jar;%CLASSPATH% -Ddruid.mysql.usePingMethod=false -Xms1024m -Xmx1024m -XX:MetaspaceSize=64M test.tidb.starter.Starter

pause...