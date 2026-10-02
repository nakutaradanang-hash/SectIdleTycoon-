package com.sect.idle.core;

import android.graphics.Canvas;
import org.junit.Assert;
import org.junit.Test;

public class GameLoopLifecycleTest {

    @Test
    public void testGameLoopLifecycleAndTargetFps() {
        final int[] updateCount = {0};
        final int[] renderCount = {0};

        GameLoop.GameLoopCallback callback = new GameLoop.GameLoopCallback() {
            @Override
            public void onGameUpdate(float dt) {
                updateCount[0]++;
            }

            @Override
            public void onGameRender(Canvas canvas) {
                renderCount[0]++;
            }

            @Override
            public void onSurfaceChanged(int width, int height) {}

            @Override
            public void onSurfaceCreated() {}

            @Override
            public void onSurfaceDestroyed() {}
        };

        GameLoop loop = new GameLoop(null, callback);
        loop.setTargetFps(60);
        Assert.assertEquals(60, loop.getTargetFps());

        loop.setTargetFps(10); // Below min (15) -> clamps to 15
        Assert.assertEquals(15, loop.getTargetFps());

        loop.setTargetFps(150); // Above max (120) -> clamps to 120
        Assert.assertEquals(120, loop.getTargetFps());

        // Test dimensions
        Assert.assertEquals(720, loop.getSurfaceWidth());
        Assert.assertEquals(1280, loop.getSurfaceHeight());

        // Test pause/resume flags
        loop.pause();
        Assert.assertTrue(loop.isPaused());
        loop.resume();
        Assert.assertFalse(loop.isPaused());

        // Test direct stop
        loop.stop();
        Assert.assertFalse(loop.isRunning());
    }
}
