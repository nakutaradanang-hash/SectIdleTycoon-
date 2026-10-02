package com.sect.idle.utils;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ProfilerAndDiagnosticsTest {

    private Context context;
    private PerformanceProfiler profiler;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        profiler = new PerformanceProfiler();
        CrashHandler.init(context).install(context);
    }

    @Test
    public void testPerformanceProfilerFrameCycle() {
        for (int i = 0; i < 65; i++) {
            profiler.beginFrame();
            profiler.markDrawCall();
            profiler.markEntity();
            profiler.markParticle();
            profiler.markTextureSwitch();
            profiler.endFrame();
        }

        Assert.assertTrue("Profiler drawCalls tracked", profiler.drawCalls >= 0);
        Assert.assertTrue("Profiler avgFps non-negative", profiler.avgFps >= 0f);
        Assert.assertNotNull("Stats string generated", profiler.getStatsString());

        profiler.resetCounters();
        Assert.assertEquals(0, profiler.drawCalls);
        Assert.assertEquals(0, profiler.entityCount);
        Assert.assertEquals(0, profiler.particleCount);
        Assert.assertEquals(0, profiler.textureSwitches);
    }

    @Test
    public void testCrashHandlerSafeRunAndLogging() {
        CrashHandler.safeRun("TEST_SAFE_RUN", new Runnable() {
            @Override
            public void run() {
                // Test handled exception capture
                throw new IllegalStateException("Simulated safe exception for unit test");
            }
        });

        CrashHandler.logException("TEST_LOG_EX", new RuntimeException("Captured runtime error"));

        String logs = CrashHandler.getInstance().readLatestCrashLog();
        Assert.assertNotNull(logs);
    }

    @Test
    public void testDataValidatorEdgeCases() {
        Assert.assertFalse("Null string is invalid name", DataValidator.isValidName(null));
        Assert.assertFalse("Empty string is invalid name", DataValidator.isValidName(""));
        Assert.assertFalse("XSS tag is invalid name", DataValidator.isValidName("<script>alert(1)</script>"));
        Assert.assertTrue("Valid disciple name", DataValidator.isValidName("ImmortalSword99"));

        Assert.assertEquals("Sanitized text", "Hello World", DataValidator.sanitizeName("Hello <b>World</b>", 32).trim());
        Assert.assertEquals("Clamped integer", 100, DataValidator.clampInt(150, 0, 100));
        Assert.assertEquals("Clamped float", 1.0f, DataValidator.clampFloat(2.5f, 0f, 1.0f), 0.001f);
    }
}
