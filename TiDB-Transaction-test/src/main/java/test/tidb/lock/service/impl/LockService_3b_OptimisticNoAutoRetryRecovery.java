package test.tidb.lock.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import test.tidb.lock.entity.TaskLock;

/**
 * @author Adrninistrator
 * @date 2022/6/19
 * @description:
 */
@Service(LockService_3b_OptimisticNoAutoRetryRecovery.TRANSACTION_MODE)
public class LockService_3b_OptimisticNoAutoRetryRecovery extends BaseLockService {
    private static final Logger logger = LoggerFactory.getLogger(LockService_3b_OptimisticNoAutoRetryRecovery.class);

    public static final String TRANSACTION_MODE = "LockService_3b_OptimisticNoAutoRetryRecovery";

    @Override
    protected boolean lockRecord(String taskName, TaskLock taskLock) {
        return lockRecordNoCheck(taskName);
    }

    @Override
    protected void beforeDoLock(String taskName) {
        // 记录TiDB禁用自动重试设置
        recordTidbDisableAutoRetry();

        useTransactionModePessimistic();

        tiDBDisableAutoRetryOn();
    }

    // 在执行doLock方法获取锁结束前的操作
    @Override
    protected void beforeDoLockDone() {
        // 恢复TiDB禁用自动重试设置
        recoveryTidbDisableAutoRetry();
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
        return "使用乐观事务，禁用自动重试，事务开始前记录自动重试设置，事务结束前恢复自动重试设置";
    }
}
