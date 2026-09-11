package com.sect.idle.apm;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.sect.idle.utils.CrashHandler;
import com.sect.idle.utils.ExceptionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ApmManager - Central Application Performance Monitoring (APM) Coordinator.
 * Tracks CPU, Memory, Frame Latency / Jank, Errors, and Memory Leaks in real-time.
 * Emits early-warning alerts for performance anomalies.
 */
public final class ApmManager {

    private static final String TAG = "ApmManager";
    private static volatile ApmManager instance;

    public interface ApmAlertListener {
        void onAlertTriggered(ApmAlert alert);
    }

    public interface ApmSnapshotListener {
        void onSnapshotUpdated(ApmSnapshot snapshot);
    }

    // Components
    private final CpuMonitor cpuMonitor;
    private final MemoryWatchdog memoryWatchdog;
    private final FrameLatencyTracker frameTracker;
    private final ErrorRateTracker errorTracker;
    private final LeakDetector leakDetector;

    // Alert & Snapshot History
    private static final int MAX_ALERTS = 50;
    private static final int MAX_SNAPSHOT_HISTORY = 60;
    private final List<ApmAlert> alertHistory = new CopyOnWriteArrayList<ApmAlert>();
    private final List<ApmSnapshot> snapshotHistory = new CopyOnWriteArrayList<ApmSnapshot>();

    // Listeners
    private final List<ApmAlertListener> alertListeners = new CopyOnWriteArrayList<ApmAlertListener>();
    private final List<ApmSnapshotListener> snapshotListeners = new CopyOnWriteArrayList<ApmSnapshotListener>();

    // Thresholds (Configurable)
    public float thresholdHeapPercentWarning = 80.0f;
    public float thresholdHeapPercentCritical = 90.0f;
    public float thresholdCpuPercentWarning = 75.0f;
    public float thresholdCpuPercentCritical = 90.0f;
    public float thresholdErrorsPerMinWarning = 5.0f;
    public float thresholdErrorsPerMinCritical = 15.0f;

    // Cooldown timers to prevent alert floods
    private long lastMemAlertTime = 0L;
    private long lastCpuAlertTime = 0L;
    private long lastErrorAlertTime = 0L;
    private long lastJankAlertTime = 0L;
    private static final long ALERT_COOLDOWN_MS = 10_000L;

    // Background Sampling Loop
    private final Handler mainHandler;
    private final AtomicBoolean isMonitoring = new AtomicBoolean(false);
    private final Runnable sampleRunnable = new Runnable() {
        @Override
        public void run() {
            if (isMonitoring.get()) {
                sampleAndEvaluate();
                if (mainHandler != null) {
                    mainHandler.postDelayed(this, 1000L); // Sample every 1 second
                }
            }
        }
    };

    private ApmManager(Context context) {
        Handler h = null;
        try {
            if (Looper.getMainLooper() != null) {
                h = new Handler(Looper.getMainLooper());
            }
        } catch (Throwable ignored) {
            h = null;
        }
        this.mainHandler = h;

        this.cpuMonitor = new CpuMonitor();
        this.memoryWatchdog = new MemoryWatchdog();
        this.frameTracker = new FrameLatencyTracker();
        this.errorTracker = new ErrorRateTracker();
        this.leakDetector = new LeakDetector();

        if (context instanceof Application) {
            registerActivityLifecycleTracker((Application) context);
        }
    }

    public static synchronized ApmManager init(Context context) {
        if (instance == null) {
            instance = new ApmManager(context != null ? context.getApplicationContext() : null);
            instance.startMonitoring();
        }
        return instance;
    }

    public static ApmManager getInstance() {
        if (instance == null) {
            synchronized (ApmManager.class) {
                if (instance == null) {
                    instance = new ApmManager(null);
                    instance.startMonitoring();
                }
            }
        }
        return instance;
    }

    public void startMonitoring() {
        if (isMonitoring.compareAndSet(false, true)) {
            if (mainHandler != null) {
                mainHandler.post(sampleRunnable);
            }
            Log.i(TAG, "APM Monitoring started successfully.");
        }
    }

    public void stopMonitoring() {
        if (isMonitoring.compareAndSet(true, false)) {
            if (mainHandler != null) {
                mainHandler.removeCallbacks(sampleRunnable);
            }
            Log.i(TAG, "APM Monitoring stopped.");
        }
    }

    public void registerActivityLifecycleTracker(Application app) {
        if (app == null) return;
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
            @Override
            public void onActivityStarted(Activity activity) {}
            @Override
            public void onActivityResumed(Activity activity) {}
            @Override
            public void onActivityPaused(Activity activity) {}
            @Override
            public void onActivityStopped(Activity activity) {}
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override
            public void onActivityDestroyed(Activity activity) {
                // Automatically watch destroyed activities for memory retention leaks
                watchForMemoryLeak(activity, "Activity:" + activity.getClass().getSimpleName());
            }
        });
    }

    public void recordFrame(long frameDurationNs) {
        frameTracker.recordFrame(frameDurationNs);
    }

    public void onFrameRendered() {
        frameTracker.onFrameRendered();
    }

    public void recordException(Throwable t, boolean isFatal) {
        errorTracker.recordException(t, isFatal);
        if (isFatal) {
            dispatchAlert(new ApmAlert(
                    ApmAlert.AlertType.HIGH_ERROR_RATE,
                    ApmAlert.Severity.CRITICAL,
                    "Application Crash Detected",
                    t.getClass().getSimpleName() + ": " + t.getMessage(),
                    "Review CrashHandler log dump and resolve fatal exception.",
                    1, 1
            ));
        }
    }

    public void watchForMemoryLeak(Object object, String description) {
        leakDetector.watch(object, description);
    }

    public synchronized ApmSnapshot sampleAndEvaluate() {
        cpuMonitor.sample();
        memoryWatchdog.sample();
        
        long now = System.currentTimeMillis();
        float processCpu = cpuMonitor.getProcessCpuUsage();
        float systemCpu = cpuMonitor.getSystemCpuUsage();
        long usedHeapMb = memoryWatchdog.getUsedHeapMb();
        long maxHeapMb = memoryWatchdog.getMaxHeapMb();
        float heapPercent = memoryWatchdog.getHeapUsagePercent();
        long nativeHeapMb = memoryWatchdog.getNativeHeapAllocatedMb();
        int gcCount = memoryWatchdog.getGcCount();
        long gcPause = memoryWatchdog.getGcEstimatedPauseMs();
        
        float fps = frameTracker.getFps();
        float avgFrameMs = frameTracker.getAvgFrameMs();
        float maxFrameMs = frameTracker.getMaxFrameMs();
        int jankCount = frameTracker.getJankFrameCount();
        int frozenCount = frameTracker.getFrozenFrameCount();

        int totalErrors = errorTracker.getTotalExceptions();
        int npeCount = errorTracker.getNpeCount();
        int oomCount = errorTracker.getOomCount();
        int crashCount = errorTracker.getCrashCount();
        float errorsPerMin = errorTracker.getErrorsPerMinute();

        int activeLeaks = leakDetector.getActiveLeakCandidateCount();
        int activeThreads = Thread.activeCount();

        ApmSnapshot snapshot = new ApmSnapshot(
                now, processCpu, systemCpu, usedHeapMb, maxHeapMb, heapPercent,
                nativeHeapMb, gcCount, gcPause, fps, avgFrameMs, maxFrameMs,
                jankCount, frozenCount, totalErrors, npeCount, oomCount, crashCount,
                errorsPerMin, activeLeaks, activeThreads
        );

        // Store snapshot in rolling history
        snapshotHistory.add(snapshot);
        while (snapshotHistory.size() > MAX_SNAPSHOT_HISTORY) {
            snapshotHistory.remove(0);
        }

        // Notify snapshot listeners
        for (ApmSnapshotListener l : snapshotListeners) {
            try { l.onSnapshotUpdated(snapshot); } catch (Throwable ignored) {}
        }

        // Evaluate Anomalies & Triggers
        evaluateAlertRules(snapshot, now);

        return snapshot;
    }

    private void evaluateAlertRules(ApmSnapshot snap, long now) {
        // 1. Memory Warnings
        if (snap.heapUsagePercent >= thresholdHeapPercentCritical && (now - lastMemAlertTime > ALERT_COOLDOWN_MS)) {
            lastMemAlertTime = now;
            dispatchAlert(new ApmAlert(
                    ApmAlert.AlertType.HIGH_MEMORY,
                    ApmAlert.Severity.CRITICAL,
                    "Critical Memory Pressure",
                    String.format(Locale.US, "Heap at %.1f%% (%dMB / %dMB)", snap.heapUsagePercent, snap.usedHeapMb, snap.maxHeapMb),
                    "Trigger immediate cache eviction or reduce particle/texture quality.",
                    snap.heapUsagePercent, thresholdHeapPercentCritical
            ));
        } else if (snap.heapUsagePercent >= thresholdHeapPercentWarning && (now - lastMemAlertTime > ALERT_COOLDOWN_MS)) {
            lastMemAlertTime = now;
            dispatchAlert(new ApmAlert(
                    ApmAlert.AlertType.HIGH_MEMORY,
                    ApmAlert.Severity.WARNING,
                    "High Memory Usage",
                    String.format(Locale.US, "Heap at %.1f%% (%dMB / %dMB)", snap.heapUsagePercent, snap.usedHeapMb, snap.maxHeapMb),
                    "Monitor background tasks and prune cached bitmaps.",
                    snap.heapUsagePercent, thresholdHeapPercentWarning
            ));
        }

        // 2. CPU Spikes
        if (snap.processCpuPercent >= thresholdCpuPercentCritical && (now - lastCpuAlertTime > ALERT_COOLDOWN_MS)) {
            lastCpuAlertTime = now;
            dispatchAlert(new ApmAlert(
                    ApmAlert.AlertType.HIGH_CPU,
                    ApmAlert.Severity.CRITICAL,
                    "Severe CPU Bottleneck",
                    String.format(Locale.US, "App CPU at %.1f%% across %d processors", snap.processCpuPercent, cpuMonitor.getAvailableProcessors()),
                    "Throttle intensive background calculations or lower simulation tick rate.",
                    snap.processCpuPercent, thresholdCpuPercentCritical
            ));
        }

        // 3. Frame Jank / Freezes
        if (snap.frozenFrameCount > 0 && (now - lastJankAlertTime > ALERT_COOLDOWN_MS)) {
            lastJankAlertTime = now;
            dispatchAlert(new ApmAlert(
                    ApmAlert.AlertType.FRAME_JANK,
                    ApmAlert.Severity.WARNING,
                    "Main Thread Freeze Detected",
                    String.format(Locale.US, "Max frame render time reached %.1fms", snap.maxFrameMs),
                    "Offload heavy computations, I/O, or parsing off the UI main thread.",
                    snap.maxFrameMs, 250.0
            ));
            frameTracker.resetCounters();
        }

        // 4. Error Velocity
        if (snap.errorsPerMinute >= thresholdErrorsPerMinCritical && (now - lastErrorAlertTime > ALERT_COOLDOWN_MS)) {
            lastErrorAlertTime = now;
            dispatchAlert(new ApmAlert(
                    ApmAlert.AlertType.HIGH_ERROR_RATE,
                    ApmAlert.Severity.CRITICAL,
                    "High Error Rate Anomaly",
                    String.format(Locale.US, "%.1f errors per minute detected", snap.errorsPerMinute),
                    "Check ExceptionManager logs and inspect recent state mutations.",
                    snap.errorsPerMinute, thresholdErrorsPerMinCritical
            ));
        }

        // 5. Memory Leaks
        if (snap.activeLeakCandidates > 0) {
            List<LeakDetector.LeakCandidate> leaks = leakDetector.checkLeaks();
            if (!leaks.isEmpty()) {
                LeakDetector.LeakCandidate first = leaks.get(0);
                if (first.retainedGcPasses == 1) { // Only alert once when confirmed
                    dispatchAlert(new ApmAlert(
                            ApmAlert.AlertType.MEMORY_LEAK_CANDIDATE,
                            ApmAlert.Severity.WARNING,
                            "Potential Memory Leak",
                            first.className + " retained for " + (first.getRetainedDurationMs() / 1000) + "s",
                            "Ensure unregistering listeners and clearing static references on destroy.",
                            snap.activeLeakCandidates, 1.0
                    ));
                }
            }
        }
    }

    public void dispatchAlert(final ApmAlert alert) {
        if (alert == null) return;
        alertHistory.add(alert);
        while (alertHistory.size() > MAX_ALERTS) {
            alertHistory.remove(0);
        }

        Log.w(TAG, "APM Alert: " + alert.getDisplaySummary());

        Runnable notifyRunnable = new Runnable() {
            @Override
            public void run() {
                for (ApmAlertListener l : alertListeners) {
                    try { l.onAlertTriggered(alert); } catch (Throwable ignored) {}
                }
            }
        };

        if (mainHandler != null) {
            mainHandler.post(notifyRunnable);
        } else {
            notifyRunnable.run();
        }
    }

    public void addAlertListener(ApmAlertListener listener) {
        if (listener != null && !alertListeners.contains(listener)) {
            alertListeners.add(listener);
        }
    }

    public void removeAlertListener(ApmAlertListener listener) {
        alertListeners.remove(listener);
    }

    public void addSnapshotListener(ApmSnapshotListener listener) {
        if (listener != null && !snapshotListeners.contains(listener)) {
            snapshotListeners.add(listener);
        }
    }

    public void removeSnapshotListener(ApmSnapshotListener listener) {
        snapshotListeners.remove(listener);
    }

    public List<ApmAlert> getAlertHistory() {
        return Collections.unmodifiableList(new ArrayList<ApmAlert>(alertHistory));
    }

    public List<ApmSnapshot> getSnapshotHistory() {
        return Collections.unmodifiableList(new ArrayList<ApmSnapshot>(snapshotHistory));
    }

    public CpuMonitor getCpuMonitor() { return cpuMonitor; }
    public MemoryWatchdog getMemoryWatchdog() { return memoryWatchdog; }
    public FrameLatencyTracker getFrameTracker() { return frameTracker; }
    public ErrorRateTracker getErrorTracker() { return errorTracker; }
    public LeakDetector getLeakDetector() { return leakDetector; }

    public String exportJsonDiagnostics() {
        ApmSnapshot snap = sampleAndEvaluate();
        StringBuilder sb = new StringBuilder(1024);
        sb.append("{\n");
        sb.append("  \"timestamp\": ").append(snap.timestamp).append(",\n");
        sb.append("  \"cpu\": {\n");
        sb.append("    \"process_percent\": ").append(snap.processCpuPercent).append(",\n");
        sb.append("    \"system_percent\": ").append(snap.systemCpuPercent).append("\n");
        sb.append("  },\n");
        sb.append("  \"memory\": {\n");
        sb.append("    \"used_heap_mb\": ").append(snap.usedHeapMb).append(",\n");
        sb.append("    \"max_heap_mb\": ").append(snap.maxHeapMb).append(",\n");
        sb.append("    \"heap_usage_percent\": ").append(snap.heapUsagePercent).append(",\n");
        sb.append("    \"native_heap_mb\": ").append(snap.nativeHeapAllocatedMb).append(",\n");
        sb.append("    \"gc_count\": ").append(snap.gcCountTotal).append("\n");
        sb.append("  },\n");
        sb.append("  \"rendering\": {\n");
        sb.append("    \"fps\": ").append(snap.fps).append(",\n");
        sb.append("    \"avg_frame_ms\": ").append(snap.avgFrameMs).append(",\n");
        sb.append("    \"max_frame_ms\": ").append(snap.maxFrameMs).append(",\n");
        sb.append("    \"jank_frames\": ").append(snap.jankFrameCount).append("\n");
        sb.append("  },\n");
        sb.append("  \"errors\": {\n");
        sb.append("    \"total\": ").append(snap.totalExceptions).append(",\n");
        sb.append("    \"npe\": ").append(snap.npeCount).append(",\n");
        sb.append("    \"oom\": ").append(snap.oomCount).append(",\n");
        sb.append("    \"crashes\": ").append(snap.crashCount).append(",\n");
        sb.append("    \"rate_per_min\": ").append(snap.errorsPerMinute).append("\n");
        sb.append("  },\n");
        sb.append("  \"leaks\": {\n");
        sb.append("    \"active_candidates\": ").append(snap.activeLeakCandidates).append("\n");
        sb.append("  }\n");
        sb.append("}");
        return sb.toString();
    }
}
