package test.gen_shell;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import test.base.TestBase;
import test.tidb.lock.service.impl.BaseLockService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author adrninistrator
 * @date 2022/6/19
 * @description:
 */
public class GenerateShellFiles extends TestBase implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(GenerateShellFiles.class);

    public static final String SHELL_FILE_NAME_HEAD = "run-";

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    // 删除现有脚本
    private void deleteFiles(String dirName, String fileExt) {
        File dir = new File(dirName);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }

                String fileName = file.getName();
                if (fileName.startsWith(SHELL_FILE_NAME_HEAD) && fileName.endsWith(fileExt)) {
                    logger.info("删除文件 {} {}", dirName, fileName);
                    file.delete();
                }
            }
        }
    }

    // 生成新脚本
    private void generateFile(String dirName, String fileExt, String command, String beanName, String instanceNum) {
        logger.info("生成新脚本 {} {} {} {} {}", dirName, fileExt, command, beanName, instanceNum);

        String filePath = dirName + File.separator + SHELL_FILE_NAME_HEAD + beanName + "-[" + instanceNum + "]" + fileExt;
        String fileContent = command + " " + beanName + " " + instanceNum;
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            out.write(fileContent);
        } catch (Exception e) {
            logger.error("error ", e);
        }
    }

    private void generate(String dirName, String fileExt, String command) {
        logger.info("开始生成 {} {} {}", dirName, fileExt, command);

        // 删除现有脚本
        deleteFiles(dirName, fileExt);

        for (String beanName : applicationContext.getBeanNamesForType(BaseLockService.class)) {
            for (int i = 1; i <= 2; i++) {
                // 生成新脚本
                generateFile(dirName, fileExt, command, beanName, String.valueOf(i));
            }
        }
    }

    // 检查类名与服务名称是否相符
    private void checkClassAndServiceName() {
        for (String beanName : applicationContext.getBeanNamesForType(BaseLockService.class)) {
            BaseLockService baseLockService = applicationContext.getBean(beanName, BaseLockService.class);
            String className = baseLockService.getClass().getSimpleName();
            if (!className.equals(beanName)) {
                logger.error("类名与服务名不相符 {} {}", className, beanName);
                Assert.fail("类名与服务名不相符");
            }
        }
    }

    @Test
    public void test() {
        checkClassAndServiceName();

        generate("bat", ".bat", "run.bat");
        generate("shell", ".sh", "sh run.sh");
    }
}
