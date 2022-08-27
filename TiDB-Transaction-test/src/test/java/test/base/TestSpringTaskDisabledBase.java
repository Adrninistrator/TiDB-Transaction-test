package test.base;

import org.junit.BeforeClass;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @author adrninistrator
 * @date 2022/6/20
 * @description:
 */
@PrepareForTest({ScheduledTaskRegistrar.class})
public class TestSpringTaskDisabledBase extends TestMockBase {

    @BeforeClass
    public static void beforeClassTestSpringTaskDisabledBase() {
        PowerMockito.suppress(PowerMockito.method(ScheduledTaskRegistrar.class, "scheduleTasks"));
    }
}
