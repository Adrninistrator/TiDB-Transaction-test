package test.tidb.lock.dao;

public interface TiDBCommonMapper {
    String selectDbVersion();

    String selectNow();

    String selectX();

    String selectAutocommit();

    //
    String selectTiDBVersion();

    String selectTiDBTransactionIsolation();

    int tiDBDisableAutoRetryOff();

    int tiDBDisableAutoRetryOn();

    String selectTiDBDisableAutoRetry();

    int setTiDBDisableAutoRetry(String tidbDisableAutoRetry);

    int enableTiDBAutoRetryTimes();

    String selectTiDBAutoRetryTimes();

    int beginTidbPessimistic();

    int useTiDBTransactionModePessimistic();
}