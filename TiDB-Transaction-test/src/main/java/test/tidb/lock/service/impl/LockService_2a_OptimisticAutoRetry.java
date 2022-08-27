package test.tidb.lock.service.impl;

import org.springframework.stereotype.Service;
import test.tidb.lock.entity.TaskLock;

/**
 * @author Adrninistrator
 * @date 2022/6/16
 * @description:
 */
@Service(LockService_2a_OptimisticAutoRetry.TRANSACTION_MODE)
public class LockService_2a_OptimisticAutoRetry extends BaseLockService {
    public static final String TRANSACTION_MODE = "LockService_2a_OptimisticAutoRetry";

    @Override
    protected boolean lockRecord(String taskName, TaskLock taskLock) {
        return lockRecordNoCheck(taskName);
    }

    @Override
    protected void beforeDoLock(String taskName) {
        useTransactionModePessimistic();

        tiDBDisableAutoRetryOff();
    }

    @Override
    public String getTransactionMode() {
        return "使用乐观事务，启用自动重试";
    }
}
