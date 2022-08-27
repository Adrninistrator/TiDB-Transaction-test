package test.tidb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Adrninistrator
 * @date 2022/6/16
 * @description:
 */
public class CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error("error ", e);
            Thread.currentThread().interrupt();
        }
    }

    private CommonUtil() {
        throw new IllegalStateException("illegal");
    }
}
