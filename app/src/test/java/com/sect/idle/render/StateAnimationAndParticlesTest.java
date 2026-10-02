package com.sect.idle.render;

import android.graphics.Canvas;
import com.sect.idle.systems.CameraSystem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class StateAnimationAndParticlesTest {

    private StateAnimationSystem animSystem;
    private ParticleEngineV2 particleEngine;
    private CameraSystem cameraSystem;

    @Before
    public void setUp() {
        animSystem = StateAnimationSystem.getInstance();
        animSystem.clear();
        particleEngine = new ParticleEngineV2(1080, 1920);
        cameraSystem = new CameraSystem(1080, 1920);
    }

    @Test
    public void testStateAnimationTriggers() {
        Assert.assertEquals(0, animSystem.getActiveCount());

        // Trigger various animated state effects
        animSystem.triggerCriticalStrike(100f, 200f, "Li Chen", "Shadow Beast", 9500, 50, false);
        Assert.assertTrue(animSystem.getActiveCount() >= 1);

        animSystem.triggerTournamentClash(500f, 600f, "Han Li", "Elder Wang", true, 3, 500);
        animSystem.triggerWarConquest("Heavenly Demon Sect", true, 120, 2000, 150);
        animSystem.triggerBreakthrough(300f, 400f, "Xiao Yan", "Golden Core Stage");
        animSystem.triggerStatSurge(200f, 200f, "+500 QI", 0xFF00FF00);

        Assert.assertTrue(animSystem.getActiveCount() <= 16);
    }

    @Test
    public void testStateAnimationLifecycleUpdateAndRender() {
        animSystem.triggerCriticalStrike(200f, 300f, "Hero", "Boss", 10000, 100, true);
        Canvas canvas = new Canvas();

        // Simulate ENTER phase
        animSystem.update(0.1f);
        animSystem.render(canvas, cameraSystem, 1080, 1920);

        // Simulate PEAK phase
        animSystem.update(0.3f);
        animSystem.render(canvas, cameraSystem, 1080, 1920);

        // Simulate HOLD phase
        animSystem.update(0.5f);
        animSystem.render(canvas, cameraSystem, 1080, 1920);

        // Simulate EXIT phase
        animSystem.update(0.8f);
        animSystem.render(canvas, cameraSystem, 1080, 1920);

        // Simulate full expiration
        animSystem.update(2.5f);
        Assert.assertEquals(0, animSystem.getActiveCount());
    }

    @Test
    public void testParticleEngineV2SpawningAndStepping() {
        Assert.assertEquals(0, particleEngine.getActiveCount());

        // Spawn individual particles
        int id1 = particleEngine.spawn(100f, 100f, 10f, -10f, 1.0f, 5f, 0xFFFF0000, ParticleEngineV2.TYPE_FIRE);
        int id2 = particleEngine.spawn(100f, 100f, 5f, 5f, 1.5f, 4f, 0xFF00FFFF, ParticleEngineV2.TYPE_QI_PETAL);
        int id3 = particleEngine.spawn(100f, 100f, 0f, 0f, 0.8f, 6f, 0xFFFFFF00, ParticleEngineV2.TYPE_LIGHTNING);

        Assert.assertTrue(id1 >= 0);
        Assert.assertTrue(id2 >= 0);
        Assert.assertTrue(id3 >= 0);
        Assert.assertTrue(particleEngine.getActiveCount() >= 3);

        // Burst & Trail
        particleEngine.spawnBurst(200f, 200f, 20, 10f, 50f, 0.5f, 1.2f, 2f, 6f, 0xFFFF8800, ParticleEngineV2.TYPE_SPARKLE);
        particleEngine.spawnTrail(250f, 250f, 15f, -15f, 5, 0xFF00FF88);

        // Update physics
        particleEngine.gravityY = 15f;
        particleEngine.windX = 5f;
        particleEngine.turbulence = 2f;
        particleEngine.update(0.016f);

        Canvas canvas = new Canvas();
        particleEngine.render(canvas, cameraSystem);
        particleEngine.renderScreenSpace(canvas);

        // Clear
        particleEngine.clear();
        Assert.assertEquals(0, particleEngine.getActiveCount());
    }
}
