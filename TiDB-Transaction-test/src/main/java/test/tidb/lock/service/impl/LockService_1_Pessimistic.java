package test.tidb.lock.service.impl;

import org.springframework.stereotype.Service;
import test.tidb.lock.entity.TaskLock;

/**
 * @author Adrninistrator
 * @date 2022/6/16
 * @description:
 */
@Service(LockService_1_Pessimistic.TRANSACTION_MODE)
public class LockService_1_Pessimistic extends BaseLockService {
    public static final String TRANSACTION_MODE = "LockService_1_Pessimistic";

    @Override
    protected boolean lockRecord(String taskName, TaskLock taskLock) {
        return lockRecordNoCheck(taskName);
    }

    @Override
    protected void beforeDoLock(String taskName) {
        beginTidbPessimistic();
    }

    @Override
    public String getTransactionMode() {
        return "使用悲观事务";
    }
}
