package test.tidb.lock.service.impl;

import org.springframework.stereotype.Service;
import test.tidb.lock.entity.TaskLock;

/**
 * @author adrninistrator
 * @date 2022/7/1
 * @description:
 */
@Service(LockService_4b_OptimisticAutoRetryNoTransactionWithCheck.TRANSACTION_MODE)
public class LockService_4b_OptimisticAutoRetryNoTransactionWithCheck extends BaseLockService {
    public static final String TRANSACTION_MODE = "LockService_4b_OptimisticAutoRetryNoTransactionWithCheck";

    @Override
    protected boolean lockRecord(String taskName, TaskLock taskLock) {
        return lockRecordWithCheck(taskName, taskLock);
    }

    @Override
    protected void beforeDoLock(String taskName) {
        useTransactionModePessimistic();

        tiDBDisableAutoRetryOff();
    }

    @Override
    public String getTransactionMode() {
        return "使用乐观事务，使用自动重试，不使用事务，update操作判断修改前的值";
    }

    @Override
    // 是否使用事务
    protected boolean useTransaction() {
        return false;
    }
}
