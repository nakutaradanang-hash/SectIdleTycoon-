package com.sect.idle.core;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import com.sect.idle.utils.ExceptionManager;

/**
 * GameLoop - High-Performance Double-Buffered Threaded Game Engine Loop.
 *
 * Technical Specifications:
 * - Implements {@link SurfaceHolder.Callback} and {@link Runnable}.
 * - Dedicated background thread with strict frame-pacing (target 30-60 FPS).
 * - Zero GC allocations during update and render cycles to prevent GC pauses.
 * - Synchronized lock management for thread-safe pause, resume, and teardown.
 * - Hardware buffer swapping with adaptive frame-lag compensation for low-end Android 5.0+ devices.
 * - 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible (Zero Lambdas, Zero Streams).
 */
public final class GameLoop implements SurfaceHolder.Callback, Runnable {

    /**
     * Callback interface for game update and render ticks.
     */
    public interface GameLoopCallback {
        void onGameUpdate(float dt);
        void onGameRender(Canvas canvas);
        void onSurfaceChanged(int width, int height);
        void onSurfaceCreated();
        void onSurfaceDestroyed();
    }

    // Thread & Synchronization
    private Thread gameThread;
    private SurfaceHolder surfaceHolder;
    private GameLoopCallback callback;
    private final Object pauseLock = new Object();
    private final Object threadLock = new Object();

    // State Flags
    private volatile boolean isRunning = false;
    private volatile boolean isPaused = false;
    private volatile boolean isSurfaceReady = false;

    // Timing & Frame Pacing Constants
    public static final int DEFAULT_TARGET_FPS = 60;
    public static final int LOW_POWER_FPS = 30;
    private static final int MAX_FRAME_SKIPS = 5;
    private static final float MAX_DELTA_TIME = 0.1f; // 100ms cap to avoid physics jumps

    private int targetFps = DEFAULT_TARGET_FPS;
    private long targetFrameNanos = 1_000_000_000L / DEFAULT_TARGET_FPS;

    // Frame Statistics (Primitive fields, zero allocation)
    private float currentFps = 60.0f;
    private int frameCount = 0;
    private long fpsTimer = 0L;

    // Screen Dimensions
    private int surfaceWidth = 720;
    private int surfaceHeight = 1280;

    public GameLoop(SurfaceHolder holder, GameLoopCallback callback) {
        this.surfaceHolder = holder;
        this.callback = callback;
        if (this.surfaceHolder != null) {
            this.surfaceHolder.addCallback(this);
        }
    }

    public void setCallback(GameLoopCallback cb) {
        this.callback = cb;
    }

    public void setTargetFps(int fps) {
        if (fps < 15) fps = 15;
        if (fps > 120) fps = 120;
        this.targetFps = fps;
        this.targetFrameNanos = 1_000_000_000L / fps;
    }

    public int getTargetFps() {
        return targetFps;
    }

    public float getCurrentFps() {
        return currentFps;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public int getSurfaceWidth() {
        return surfaceWidth;
    }

    public int getSurfaceHeight() {
        return surfaceHeight;
    }

    // ========================================================================
    // SurfaceHolder.Callback Implementation
    // ========================================================================

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surfaceHolder = holder;
        this.isSurfaceReady = true;
        if (callback != null) {
            callback.onSurfaceCreated();
        }
        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.surfaceHolder = holder;
        this.surfaceWidth = width > 0 ? width : 720;
        this.surfaceHeight = height > 0 ? height : 1280;
        this.isSurfaceReady = true;
        if (callback != null) {
            callback.onSurfaceChanged(surfaceWidth, surfaceHeight);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.isSurfaceReady = false;
        if (callback != null) {
            callback.onSurfaceDestroyed();
        }
        stop();
    }

    // ========================================================================
    // Lifecycle Management (Thread Safe)
    // ========================================================================

    public void start() {
        synchronized (threadLock) {
            if (isRunning) return;
            isRunning = true;
            isPaused = false;
            gameThread = new Thread(this, "Sect-GameLoopThread");
            gameThread.setPriority(Thread.MAX_PRIORITY);
            gameThread.start();
        }
    }

    public void stop() {
        synchronized (threadLock) {
            isRunning = false;
            resume(); // Wake up thread if waiting
            if (gameThread != null) {
                try {
                    gameThread.join(500);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                gameThread = null;
            }
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }

    // ========================================================================
    // Non-Blocking Core Engine Execution Loop (Zero GC In Loop)
    // ========================================================================

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        fpsTimer = System.currentTimeMillis();
        frameCount = 0;

        while (isRunning) {
            // 1. Thread-safe Pause Management
            if (isPaused) {
                synchronized (pauseLock) {
                    while (isPaused && isRunning) {
                        try {
                            pauseLock.wait(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                lastTime = System.nanoTime(); // Reset delta after resume
            }

            if (!isRunning) break;

            // 2. High-precision Timing & Delta Clamping
            long now = System.nanoTime();
            long elapsedNanos = now - lastTime;
            lastTime = now;

            float dt = (float) elapsedNanos / 1_000_000_000.0f;
            if (dt > MAX_DELTA_TIME) {
                dt = MAX_DELTA_TIME;
            } else if (dt < 0.0001f) {
                dt = 0.0001f;
            }

            // 3. Game Logic Simulation Step
            if (callback != null) {
                try {
                    callback.onGameUpdate(dt);
                } catch (Throwable t) {
                    ExceptionManager.get().reportException(t, "GameLoop", "Error during onGameUpdate", ExceptionManager.LEVEL_WARN);
                }
            }

            // 4. Double-Buffered Surface Render Step
            if (isSurfaceReady && surfaceHolder != null) {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null && callback != null) {
                        callback.onGameRender(canvas);
                    }
                } catch (Throwable t) {
                    ExceptionManager.get().reportException(t, "GameLoop", "Error during onGameRender", ExceptionManager.LEVEL_ERROR);
                } finally {
                    if (canvas != null && surfaceHolder != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        } catch (Throwable ignored) {}
                    }
                }
            }

            // 5. FPS Statistics Tracking
            frameCount++;
            long currentMillis = System.currentTimeMillis();
            if (currentMillis - fpsTimer >= 1000L) {
                currentFps = (float) frameCount * 1000.0f / (float) (currentMillis - fpsTimer);
                frameCount = 0;
                fpsTimer = currentMillis;
            }

            // 6. Precise Frame-Pacing Sleep Calculation
            long frameDurationNanos = System.nanoTime() - now;
            long sleepNanos = targetFrameNanos - frameDurationNanos;

            if (sleepNanos > 1_000_000L) { // More than 1ms left
                long sleepMs = sleepNanos / 1_000_000L;
                int sleepRemNanos = (int) (sleepNanos % 1_000_000L);
                try {
                    Thread.sleep(sleepMs, sleepRemNanos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else if (sleepNanos < -targetFrameNanos) {
                // Adaptive frame skip on heavy frame drop
                int skips = 0;
                while (sleepNanos < 0 && skips < MAX_FRAME_SKIPS) {
                    if (callback != null) {
                        try {
                            callback.onGameUpdate(0.0166f);
                        } catch (Throwable ignored) {}
                    }
                    sleepNanos += targetFrameNanos;
                    skips++;
                }
            }
        }
    }
}
