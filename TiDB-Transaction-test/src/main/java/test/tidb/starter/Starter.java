package test.tidb.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import test.tidb.util.CommonUtil;

/**
 * @author Adrninistrator
 * @date 2022/6/16
 * @description:
 */
public class Starter {
    private static final Logger logger = LoggerFactory.getLogger(Starter.class);

    public static final String INSTANCE_NUM = "instance.num";

    private static int instanceNum;

    private ClassPathXmlApplicationContext context;

    static {
        String instanceNumStr = System.getProperty(INSTANCE_NUM);
        if (instanceNumStr == null) {
            logger.error("请通过 -D{}=xxx 指定当前实例序号", INSTANCE_NUM);
            System.exit(0);
        }

        try {
            instanceNum = Integer.parseInt(instanceNumStr);
            if (instanceNum <= 0) {
                logger.error("通过 -D{}=xxx 指定的当前实例序号应大于0 {}", INSTANCE_NUM, instanceNumStr);
                System.exit(0);
            }
        } catch (Exception e) {
            logger.error("通过 -D{}=xxx 指定的当前实例序号非法 {}", INSTANCE_NUM, instanceNumStr);
            System.exit(0);
        }
    }

    public static int getInstanceNum() {
        return instanceNum;
    }

    public void start() {
        try {
            logger.info("start begin.....");

            context = new ClassPathXmlApplicationContext();
            context.setConfigLocation("classpath:applicationContext.xml");
            context.refresh();
            context.start();

            logger.info("start success!");

            while (true) {
                CommonUtil.sleep(1000L);
            }
        } catch (Exception e) {
            System.err.println("Server exception : " + e.getMessage());
            logger.error("error ", e);
        }
    }

    public static void main(String[] args) {
        Starter starter = new Starter();
        starter.start();
    }
}
