package test.show;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import test.base.TestBase;
import test.tidb.lock.service.impl.BaseLockService;

/**
 * @author adrninistrator
 * @date 2022/7/2
 * @description:
 */
public class ShowTestInfo extends TestBase implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ShowTestInfo.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void show() {
        StringBuilder testInfo = new StringBuilder();
        for (String beanName : applicationContext.getBeanNamesForType(BaseLockService.class)) {
            BaseLockService baseLockService = applicationContext.getBean(beanName, BaseLockService.class);
            testInfo.append("\n|").append(beanName).append("|").append(baseLockService.getTransactionMode()).append("|");
        }
        logger.info("测试信息如下: {}", testInfo);
    }
}
