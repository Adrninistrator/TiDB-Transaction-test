package test.task;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Adrninistrator
 * @date 2022/6/18
 * @description:
 */

public class TestAllTask extends TestTaskBase {
    private static final Logger logger = LoggerFactory.getLogger(TestAllTask.class);

    @Test
    public void test() {
        for (String beanName : schedulerTask4TiDB.getBeanNames()) {
            logger.info("开始测试 {}", beanName);
            schedulerTask4TiDB.setLockService(beanName);

            doTest();
        }
    }
}
