package test.tidb.lock.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import test.tidb.lock.entity.TaskLock;

/**
 * @author Adrninistrator
 * @date 2022/6/16
 * @description:
 */
@Service(LockService_3a_OptimisticNoAutoRetry.TRANSACTION_MODE)
public class LockService_3a_OptimisticNoAutoRetry extends BaseLockService {
    private static final Logger logger = LoggerFactory.getLogger(LockService_3a_OptimisticNoAutoRetry.class);

    public static final String TRANSACTION_MODE = "LockService_3a_OptimisticNoAutoRetry";

    @Override
    protected boolean lockRecord(String taskName, TaskLock taskLock) {
        return lockRecordNoCheck(taskName);
    }

    @Override
    protected void beforeDoLock(String taskName) {
        useTransactionModePessimistic();

        tiDBDisableAutoRetryOn();
    }

    // 在执行lock方法获取锁结束前的操作
    @Override
    protected void beforeLockDone() {
        if (exceptionOccurred) {
            // 事务执行失败时，查询TiDB自动重试开关
            String tiDBDisableAutoRetry = tiDBCommonMapper.selectTiDBDisableAutoRetry();
            logger.info("tiDBDisableAutoRetry {}", tiDBDisableAutoRetry);
        }
    }

    @Override
    public String getTransactionMode() {
        return "使用乐观事务，禁用自动重试";
    }
}
