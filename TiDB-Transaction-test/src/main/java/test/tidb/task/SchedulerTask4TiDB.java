package test.tidb.task;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import test.tidb.lock.dao.TaskLockMapper;
import test.tidb.lock.dao.TiDBCommonMapper;
import test.tidb.lock.entity.TaskLock;
import test.tidb.lock.service.impl.BaseLockService;
import test.tidb.util.CommonUtil;

/**
 * @author Adrninistrator
 * @date 2022/6/15
 * @description:
 */
@Component("test.tidb.task.SchedulerTask4TiDB")
public class SchedulerTask4TiDB implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerTask4TiDB.class);

    public static final String TRANSACTION_MODE_KEY = "transaction.mode";

    public static final String TASK_NAME = SchedulerTask4TiDB.class.getSimpleName();

    @Autowired
    private DruidDataSource druidDataSource;

    @Autowired
    private TaskLockMapper taskLockMapper;

    @Autowired
    private TiDBCommonMapper tiDBCommonMapper;

    private ApplicationContext applicationContext;

    private BaseLockService lockService;

    private boolean useTiDB = false;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

        logger.info("连接的数据库信息 {}", druidDataSource.getRawJdbcUrl());

        String dbVersion = tiDBCommonMapper.selectDbVersion();
        logger.info("数据库版本 {}", dbVersion);
        useTiDB = StringUtils.containsIgnoreCase(dbVersion, "TiDB");
        if (useTiDB) {
            logger.info("TiDB数据库版本 {}", tiDBCommonMapper.selectTiDBVersion());
            logger.info("事务隔离级别 {}", tiDBCommonMapper.selectTiDBTransactionIsolation());
        }

        String transactionMode = System.getProperty(TRANSACTION_MODE_KEY);
        if (StringUtils.isBlank(transactionMode)) {
            logger.error("请通过 -D{}=xxx 指定事务的模式 {}", TRANSACTION_MODE_KEY, getBeanNames());
            return;
        }

        setLockService(transactionMode);

        // 执行前先修改数据库锁记录为未锁定
        TaskLock tmpTaskLock = new TaskLock();
        tmpTaskLock.setTaskName(TASK_NAME);
        tmpTaskLock.setLockFlag(BaseLockService.FLAG_UNLOCK);
        taskLockMapper.updateUnlockFlagNoCheck(tmpTaskLock);
    }

    public String[] getBeanNames() {
        return applicationContext.getBeanNamesForType(BaseLockService.class);
    }

    public void setLockService(String transactionMode) {
        lockService = applicationContext.getBean(transactionMode, BaseLockService.class);

        logger.info("当前使用的事务的模式 {} [{}]", transactionMode, lockService.getTransactionMode());
    }

    public void process() {
        if (lockService == null) {
            logger.error("服务为空");
            return;
        }

        if (!lockService.lock(TASK_NAME)) {
            return;
        }

        try {
            customHandle();
        } finally {
            lockService.unlock(TASK_NAME);
        }
    }

    protected void customHandle() {
        logger.info("开始执行");

        if (useTiDB) {
            // 查询TiDB自动重试开关
            String tiDBDisableAutoRetry = tiDBCommonMapper.selectTiDBDisableAutoRetry();
            logger.info("tiDBDisableAutoRetry {}", tiDBDisableAutoRetry);
        }

        CommonUtil.sleep(2000L);
        logger.info("执行结束");
    }
}
