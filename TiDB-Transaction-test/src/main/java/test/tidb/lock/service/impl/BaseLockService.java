package test.tidb.lock.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionTemplate;
import test.tidb.lock.dao.TaskLockMapper;
import test.tidb.lock.dao.TiDBCommonMapper;
import test.tidb.lock.entity.TaskLock;
import test.tidb.lock.service.LockService;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;

/**
 * @author Adrninistrator
 * @date 2022/6/16
 * @description:
 */
public abstract class BaseLockService implements LockService {
    private static final Logger logger = LoggerFactory.getLogger(BaseLockService.class);

    // 已锁定
    public static final int FLAG_LOCKED = 1;

    // 未锁定
    public static final int FLAG_UNLOCK = 0;

    public static final String[] KNOWN_ERROR_MESSAGES = {"can not retry select for update statement", "Write conflict"};

    // 两次定时任务执行的最小时间间隔，单位秒
    public static final int TASK_INTERVAL_SECONDS = 5;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @Autowired
    protected TaskLockMapper taskLockMapper;

    @Autowired
    protected TiDBCommonMapper tiDBCommonMapper;

    protected String origTidbDisableAutoRetry;

    protected String processInfo;

    protected boolean exceptionOccurred = false;

    public BaseLockService() {
        processInfo = ManagementFactory.getRuntimeMXBean().getName();
        logger.info("当前进程信息 {}", processInfo);
    }

    @Override
    public boolean lock(String taskName) {
        logger.info("开始尝试获取锁 [{}]", getTransactionMode());
        boolean success = false;
        try {
            exceptionOccurred = false;

            if (useTransaction()) {
                // 获取锁，使用事务
                success = Boolean.TRUE.equals(transactionTemplate.execute(status ->
                        doLock(taskName)
                ));
            } else {
                // 获取锁，不使用事务
                success = doLock(taskName);
            }
            return success;
        } catch (Exception e) {
            exceptionOccurred = true;
            if (e instanceof TransactionSystemException &&
                    e.getCause() instanceof SQLException &&
                    StringUtils.containsAny(e.getCause().getMessage(), KNOWN_ERROR_MESSAGES)) {
                logger.warn("并发执行时，未获取到锁的事务执行失败，已知的正常现象，不需要关注 {} {}", taskName, e.getCause().getMessage());
            }
            logger.error("获取锁出现异常 {} ", taskName, e);
            return false;
        } finally {
            logger.info("尝试获取锁结束 {}", success ? "获取到锁" : "未获取到锁");

            // 在执行lock方法获取锁结束前的操作
            beforeLockDone();
        }
    }

    @Override
    public void unlock(String taskName) {
        try {
            TaskLock tmpTaskLock = new TaskLock();
            tmpTaskLock.setTaskName(taskName);
            tmpTaskLock.setLockFlag(FLAG_UNLOCK);

            int row = taskLockMapper.updateUnlockFlagNoCheck(tmpTaskLock);
            logger.info("修改记录状态为解锁，更新行数 {} {}", row, taskName);
        } catch (Exception e) {
            logger.error("error ", e);
        }
    }

    // 锁定记录
    protected abstract boolean lockRecord(String taskName, TaskLock taskLock);

    // 执行锁定操作前的自定义处理
    protected abstract void beforeDoLock(String taskName);

    // 在执行doLock方法获取锁结束前的操作
    protected void beforeDoLockDone() {
    }

    // 在执行lock方法获取锁结束前的操作
    protected void beforeLockDone() {
    }

    // 当前使用的事务模式
    public abstract String getTransactionMode();

    // 是否使用事务
    protected boolean useTransaction() {
        return true;
    }

    // 记录TiDB禁用自动重试设置
    protected void recordTidbDisableAutoRetry() {
        origTidbDisableAutoRetry = tiDBCommonMapper.selectTiDBDisableAutoRetry();
        logger.info("TiDB禁用自动重试设置 {}", origTidbDisableAutoRetry);
    }

    // 恢复TiDB禁用自动重试设置
    protected void recoveryTidbDisableAutoRetry() {
        if (origTidbDisableAutoRetry == null) {
            return;
        }
        logger.info("恢复TiDB禁用自动重试设置 {}", origTidbDisableAutoRetry);

        tiDBCommonMapper.setTiDBDisableAutoRetry(origTidbDisableAutoRetry);
    }

    private Boolean doLock(String taskName) {
        try {
            // 执行锁定操作前的自定义处理
            beforeDoLock(taskName);

            TaskLock taskLock = taskLockMapper.selectForUpdate(taskName);
            // 判断锁记录
            if (taskLock == null) {
                // 锁记录不存在
                // 插入锁记录
                return insertLock(taskName);
            }

            // 锁记录已存在
            // 判断锁状态
            if (FLAG_LOCKED == taskLock.getLockFlag()) {
                logger.info("定时任务状态为已锁定，获取锁失败 {}", taskLock);
                return false;
            }

            // 状态是已解锁
            // 判断是否达到两次定时任务之间允许执行的时间隔
            if (taskLock.getDbCurrentDate().getTime() - taskLock.getBeginTime().getTime() < TASK_INTERVAL_SECONDS * 1000L) {
                logger.info("上次定时任务执行完毕后未到{}秒，不允许执行 数据库当前时间 {} 数据库记录的begin_time {}", TASK_INTERVAL_SECONDS, taskLock.getDbCurrentDate(), taskLock.getBeginTime());
                return false;
            }

            // 锁定记录
            if (!lockRecord(taskName, taskLock)) {
                logger.info("定时任务状态为未锁定，更新锁记录时因为并发导致加锁不成功 {}", taskName);
                return false;
            }

            logger.info("定时任务状态为未锁定 {}", taskName);
            // 获取到锁
            return true;
        } catch (Exception e) {
            logger.error("定时任务执行出现异常 {} ", taskName, e);

            return false;
        } finally {
            // 在执行doLock方法获取锁结束前的操作
            beforeDoLockDone();
        }
    }

    // 插入锁记录
    private boolean insertLock(String taskName) {
        try {
            TaskLock taskLock = new TaskLock();
            taskLock.setTaskName(taskName);
            taskLock.setLockFlag(FLAG_LOCKED);
            taskLock.setProcessInfo(processInfo);

            int i = taskLockMapper.insertIgnore(taskLock);
            if (i == 1) {
                logger.info("插入锁记录成功 {}", taskLock);
                return true;
            }

            logger.info("插入锁记录失败 {}", taskName);
            return false;
        } catch (Exception e) {
            logger.error("插入锁记录异常 {} ", taskName, e);
            return false;
        }
    }

    // 禁用TiDB自动重试
    protected void tiDBDisableAutoRetryOn() {
        // 查询TiDB自动重试开关
        String tiDBDisableAutoRetryBefore = tiDBCommonMapper.selectTiDBDisableAutoRetry();
        tiDBCommonMapper.tiDBDisableAutoRetryOn();
        String tiDBDisableAutoRetryAfter = tiDBCommonMapper.selectTiDBDisableAutoRetry();

        logger.info("tiDBDisableAutoRetry before {}", tiDBDisableAutoRetryBefore);
        logger.info("tiDBDisableAutoRetry after {}", tiDBDisableAutoRetryAfter);
    }

    // 启用TiDB自动重试
    protected void tiDBDisableAutoRetryOff() {
        // 使用的TiDB默认系统变量已满足以下要求，可不设置，执行以下代码后会增加耗时，可能导致没有并发执行事务
//        String tiDBDisableAutoRetryBefore = tiDBCommonMapper.selectTiDBDisableAutoRetry();
//        String tiDBAutoRetryTimesBefore = tiDBCommonMapper.selectTiDBAutoRetryTimes();
//        tiDBCommonMapper.tiDBDisableAutoRetryOff();
//        tiDBCommonMapper.enableTiDBAutoRetryTimes();
//        String tiDBDisableAutoRetryAfter = tiDBCommonMapper.selectTiDBDisableAutoRetry();
//        String tiDBAutoRetryTimesAfter = tiDBCommonMapper.selectTiDBAutoRetryTimes();
//
//        logger.info("tiDBDisableAutoRetry before {}", tiDBDisableAutoRetryBefore);
//        logger.info("tiDBAutoRetryTimesBefore {}", tiDBAutoRetryTimesBefore);
//        logger.info("tiDBDisableAutoRetry after {}", tiDBDisableAutoRetryAfter);
//        logger.info("tiDBAutoRetryTimesAfter {}", tiDBAutoRetryTimesAfter);
    }

    protected boolean lockRecordNoCheck(String taskName) {
        TaskLock tmpTaskLock = new TaskLock();
        tmpTaskLock.setTaskName(taskName);
        tmpTaskLock.setLockFlag(FLAG_LOCKED);
        tmpTaskLock.setProcessInfo(processInfo);

        int row = taskLockMapper.updateLockFlagNoCheck(tmpTaskLock);
        logger.info("修改记录状态为锁定，更新行数 {} {}", row, taskName);
        return row > 0;
    }

    protected boolean lockRecordWithCheck(String taskName, TaskLock taskLock) {
        TaskLock tmpTaskLock = new TaskLock();
        tmpTaskLock.setTaskName(taskName);
        tmpTaskLock.setLockFlag(FLAG_LOCKED);
        tmpTaskLock.setProcessInfo(processInfo);

        int row = taskLockMapper.updateLockFlagWithCheck(tmpTaskLock, taskLock);
        logger.info("修改记录状态为锁定，更新行数 {} {}", row, taskName);
        return row > 0;
    }

    // 开启TiDB悲观事务
    protected void beginTidbPessimistic() {
        tiDBCommonMapper.beginTidbPessimistic();
    }

    // 使用乐观事务
    protected void useTransactionModePessimistic() {
        // TiDB默认系统变量已满足以下要求，可不设置，执行以下代码后会增加耗时，可能导致没有并发执行事务
//        tiDBCommonMapper.useTiDBTransactionModePessimistic();
    }
}
