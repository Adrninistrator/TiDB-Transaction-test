package test.tidb.lock.service;

/**
 * @author Adrninistrator
 * @date 2022/6/16
 * @description:
 */
public interface LockService {

    boolean lock(String taskName);

    void unlock(String taskName);
}
