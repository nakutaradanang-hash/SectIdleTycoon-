package com.sect.idle.core;

import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

/**
 * Unit Tests for FixedMath & WorldScaleEngine.
 * Validates zero-GC fixed-point arithmetic, trigonometric tables, origin rebasing,
 * multi-threaded worker dispatch, and memory budgeting.
 */
public class FixedMathAndWorldScaleEngineTest {

    private WorldScaleEngine engine;

    @Before
    public void setUp() {
        engine = WorldScaleEngine.getInstance();
    }

    @Test
    public void testFixedPointConversionsAndMath() {
        int fixedFive = FixedMath.fromInt(5);
        int fixedTwo = FixedMath.fromInt(2);

        assertEquals(5, FixedMath.toInt(fixedFive));
        assertEquals(2, FixedMath.toInt(fixedTwo));

        int mult = FixedMath.multiply(fixedFive, fixedTwo);
        assertEquals(10, FixedMath.toInt(mult));

        int div = FixedMath.divide(fixedFive, fixedTwo);
        assertEquals(2.5f, FixedMath.toFloat(div), 0.01f);

        int clamped = FixedMath.clamp(FixedMath.fromInt(15), FixedMath.fromInt(0), FixedMath.fromInt(10));
        assertEquals(10, FixedMath.toInt(clamped));

        int sqrtVal = FixedMath.sqrt(FixedMath.fromInt(25));
        assertEquals(5, FixedMath.toInt(sqrtVal));
    }

    @Test
    public void testFixedPointTrigonometry() {
        int zeroSin = FixedMath.sin(0);
        assertEquals(0, FixedMath.toInt(zeroSin));

        int piSin = FixedMath.sin(FixedMath.PI);
        assertEquals(0, FixedMath.toInt(piSin));

        int zeroCos = FixedMath.cos(0);
        assertEquals(1.0f, FixedMath.toFloat(zeroCos), 0.05f);
    }

    @Test
    public void testWorldScaleOriginRebasing() {
        engine.rebaseOrigin(10000.0, 20000.0, 500.0);
        assertEquals(10000.0, engine.getOriginX(), 0.001);
        assertEquals(20000.0, engine.getOriginY(), 0.001);

        double localX = engine.toLocalX(10050.0);
        double localY = engine.toLocalY(20025.0);
        assertEquals(50.0, localX, 0.001);
        assertEquals(25.0, localY, 0.001);
    }

    @Test
    public void testDeterministicSeedProgression() {
        engine.setDeterministicSeed(99999L);
        assertEquals(99999L, engine.getDeterministicSeed());

        engine.stepSimulation();
        assertTrue("Seed must mutate deterministically", engine.getDeterministicSeed() != 99999L);
        assertEquals(1L, engine.getSimTickCount());
    }

    @Test
    public void testMemoryBudgeting() {
        engine.totalAllocatedMB = 0;
        boolean alloc1 = engine.allocateMemoryBudget("TEXTURE", 50);
        assertTrue("Allocating 50MB within 128MB budget must succeed", alloc1);

        boolean allocOverflow = engine.allocateMemoryBudget("TEXTURE", 100);
        assertFalse("Allocating 100MB over 128MB limit must fail gracefully", allocOverflow);

        engine.releaseMemoryBudget(30);
        assertEquals(20, engine.totalAllocatedMB);
    }

    @Test
    public void testThreadPoolWorkerJobDispatch() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] executed = new boolean[]{false};

        engine.submitJob(new Runnable() {
            @Override
            public void run() {
                executed[0] = true;
                latch.countDown();
            }
        }, "TestWorkerTask");

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue("Background worker task must execute asynchronously", completed);
        assertTrue("Task flag must be true", executed[0]);
    }
}
