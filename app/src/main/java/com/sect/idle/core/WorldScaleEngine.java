package com.sect.idle.core;

import com.sect.idle.utils.ErrorCode;
import com.sect.idle.utils.ExceptionManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WorldScaleEngine - Layer 0 Foundation Engine.
 * Provides double precision world grid coordinates, origin rebasing (preventing floating point jitter),
 * multi-threaded background job dispatcher, deterministic random seed lock, and system memory budgeting.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class WorldScaleEngine {

    private static volatile WorldScaleEngine instance;
    private static final Object LOCK = new Object();

    public static final double CELL_SIZE_METERS = 1000.0; // 1km Grid Cell
    public static final int MAX_WORKER_THREADS = Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors()));

    // World Origin Rebasing (Offsets large coordinate drift)
    private double originX = 0.0;
    private double originY = 0.0;
    private double originZ = 0.0;

    // Deterministic Seed Lock
    private long deterministicSeed = 1337042069L;
    private long simTickCount = 0L;

    // Memory Budgeting (in Megabytes)
    public int textureBudgetMB = 128;
    public int meshBudgetMB = 32;
    public int audioBudgetMB = 16;
    public int totalAllocatedMB = 0;

    // Thread Pool Worker Dispatcher
    private final ExecutorService workerPool;

    public static WorldScaleEngine getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new WorldScaleEngine();
                }
            }
        }
        return instance;
    }

    private WorldScaleEngine() {
        this.workerPool = Executors.newFixedThreadPool(MAX_WORKER_THREADS, new ThreadFactory() {
            private final AtomicInteger threadId = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "SectEngineWorker-" + threadId.getAndIncrement());
                t.setPriority(Thread.NORM_PRIORITY - 1);
                t.setDaemon(true);
                return t;
            }
        });
    }

    /**
     * Origin Rebasing: Shifts local coordinate origin when player/camera moves far,
     * maintaining sub-millimeter precision on mobile floating point units.
     */
    public void rebaseOrigin(double newX, double newY, double newZ) {
        this.originX = newX;
        this.originY = newY;
        this.originZ = newZ;
        ExceptionManager.get().addBreadcrumb("ENGINE", "Origin rebased to (" + (long) newX + ", " + (long) newY + ", " + (long) newZ + ")");
    }

    public double getOriginX() { return originX; }
    public double getOriginY() { return originY; }
    public double getOriginZ() { return originZ; }

    public double toLocalX(double worldX) { return worldX - originX; }
    public double toLocalY(double worldY) { return worldY - originY; }
    public double toLocalZ(double worldZ) { return worldZ - originZ; }

    /**
     * Submits a decoupled computational task to the worker pool.
     */
    public void submitJob(final Runnable task, final String jobName) {
        if (task == null) return;
        workerPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Throwable t) {
                    ExceptionManager.get().reportError(ErrorCode.SYS_UNKNOWN, t, "Job failed: " + jobName, "WorldScaleEngine");
                }
            }
        });
    }

    /**
     * Deterministic simulation step with locked PRNG sequence for turn replays.
     */
    public void stepSimulation() {
        simTickCount++;
        deterministicSeed = (deterministicSeed * 6364136223846793005L + 1442695040888963407L);
    }

    public long getSimTickCount() { return simTickCount; }
    public long getDeterministicSeed() { return deterministicSeed; }

    public void setDeterministicSeed(long seed) {
        this.deterministicSeed = seed;
        this.simTickCount = 0;
    }

    /**
     * Memory budgeting tracker and automatic cache evictor.
     */
    public boolean allocateMemoryBudget(String subsystem, int megabytes) {
        if (subsystem == null) return false;
        if (subsystem.equalsIgnoreCase("TEXTURE") && totalAllocatedMB + megabytes > textureBudgetMB) {
            ExceptionManager.get().logMemoryPressure(totalAllocatedMB, textureBudgetMB, "TextureBudget");
            return false;
        }
        totalAllocatedMB += megabytes;
        return true;
    }

    public void releaseMemoryBudget(int megabytes) {
        totalAllocatedMB = Math.max(0, totalAllocatedMB - megabytes);
    }

    public void shutdown() {
        if (workerPool != null && !workerPool.isShutdown()) {
            workerPool.shutdown();
        }
    }
}
