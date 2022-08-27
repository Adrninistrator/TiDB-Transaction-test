package test.task;

import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import test.base.TestSpringTaskDisabledBase;
import test.tidb.lock.dao.TaskLockMapper;
import test.tidb.lock.entity.TaskLock;
import test.tidb.starter.Starter;
import test.tidb.task.SchedulerTask4TiDB;

import java.util.Calendar;

/**
 * @author adrninistrator
 * @date 2022/6/21
 * @description:
 */
public abstract class TestTaskBase extends TestSpringTaskDisabledBase {

    @Autowired
    protected SchedulerTask4TiDB schedulerTask4TiDB;

    @Autowired
    protected TaskLockMapper taskLockMapper;

    @BeforeClass
    public static void beforeClassBaseTestTask() {
        System.setProperty(Starter.INSTANCE_NUM, "1");
    }

    // 将数据库中的记录的开始时间设成很久以前
    protected void setTaskTimeLongAgo(String taskName) {
        TaskLock taskLock = new TaskLock();
        taskLock.setTaskName(taskName);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2000);
        taskLock.setBeginTime(calendar.getTime());
        taskLockMapper.updateByPrimaryKeySelective(taskLock);
    }

    protected void doTest() {
        taskLockMapper.deleteByPrimaryKey(SchedulerTask4TiDB.TASK_NAME);
        schedulerTask4TiDB.process();

        setTaskTimeLongAgo(SchedulerTask4TiDB.TASK_NAME);
        schedulerTask4TiDB.process();
    }
}
