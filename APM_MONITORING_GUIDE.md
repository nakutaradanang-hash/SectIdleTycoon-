# 📊 Application Performance Monitoring (APM) Guide & Architecture

## 1. Overview & Objectives
The Idle Sect Cultivation application features an in-house, zero-dependency, real-time **Application Performance Monitoring (APM)** engine engineered specifically for Android. It operates with 100% pure Java 7 and Sketchware Pro v7.0.0 compatibility while delivering telemetry comparable to enterprise APM solutions.

The APM engine continuously observes application health, flags performance regressions, captures runtime anomalies (NPE, OOM, FC, GC pressure), detects memory leaks via weak-reference lifecycles, and provides both an in-app interactive diagnostic dashboard and a real-time floating developer HUD.

---

## 2. Core Architecture

```
                                  ┌────────────────────────┐
                                  │       MainActivity     │
                                  │   (Lifecycle Anchor)   │
                                  └───────────┬────────────┘
                                              │
                                              ▼
                                 ┌──────────────────────────┐
                                 │        ApmManager        │
                                 │   (Central Coordinator)  │
                                 └────────────┬─────────────┘
                ┌──────────────────┬──────────┴───────────┬───────────────────┐
                ▼                  ▼                      ▼                   ▼
       ┌─────────────────┐┌──────────────────┐  ┌──────────────────┐┌──────────────────┐
       │   CpuMonitor    ││  MemoryWatchdog  │  │FrameLatencyTrack ││ ErrorRateTracker │
       │ (/proc/stat &   ││ (Heap, GC, OOM,  │  │  (FPS, Jank ms,  ││ (Velocity, NPE,  │
       │  Cgroup Parser) ││  Native Memory)  │  │  Frozen Frames)  ││  FC, Crash Bread)│
       └─────────────────┘└──────────────────┘  └──────────────────┘└──────────────────┘
                │                  │                      │                   │
                └──────────────────┼──────────────────────┼───────────────────┘
                                   │
                                   ▼
                      ┌────────────────────────────┐
                      │    LeakDetector Engine     │
                      │ (ReferenceQueue & WeakRef) │
                      └────────────┬───────────────┘
                                   │
                                   ▼
                  ┌─────────────────────────────────┐
                  │    Early Warning System         │
                  │ (Anomaly Evaluation & Cooldown) │
                  └────────┬───────────────┬────────┘
                           │               │
            ┌──────────────▼──────┐  ┌─────▼────────────────┐
            │ ApmDashboardDialog  │  │  ApmFloatingOverlay  │
            │  (Visual Inspector) │  │  (Live Screen HUD)   │
            └─────────────────────┘  └──────────────────────┘
```

---

## 3. Monitored Telemetry & Metrics

### A. CPU Utilization (`CpuMonitor`)
- **Metric**: Process CPU usage percentage and Total System CPU load.
- **Mechanism**: Parses `/proc/stat` and `/proc/self/stat` differential clock ticks (`utime + stime`) against system total jiffies across available core threads.
- **Warning Threshold**: CPU > **75%** sustained over 3 sampling cycles.
- **Critical Threshold**: CPU > **90%** sustained over 3 sampling cycles.

### B. Memory & Garbage Collection (`MemoryWatchdog`)
- **Metric**: Used Java Heap (MB), Allocated Heap (MB), Max Allowed Heap (MB), Heap Utilization Percentage (%), Native Allocated Heap (MB), and GC Frequency.
- **Mechanism**: Samples `Runtime.getRuntime()` and `Debug.getNativeHeapAllocatedSize()`. Tracks sudden heap drops to calculate GC trigger frequency and pause pressure.
- **Warning Threshold**: Heap Usage > **80%**.
- **Critical Threshold**: Heap Usage > **90%** (Risk of imminent `OutOfMemoryError`).

### C. Rendering & Frame Latency (`FrameLatencyTracker`)
- **Metric**: Real-time FPS (Frames Per Second), Average Frame Duration (ms), 95th-Percentile Latency, Jank Frame Count (>32ms / 2 missed vsyncs), Frozen Frame Count (>300ms).
- **Mechanism**: High-resolution nanosecond hardware timestamp recording via `Choreographer.FrameCallback` / game rendering loop timer.
- **Warning Threshold**: FPS < **45 FPS** or Jank Rate > **15%**.
- **Critical Threshold**: FPS < **25 FPS** or Frozen Frame detected.

### D. Error Velocity & Anomaly Tracking (`ErrorRateTracker`)
- **Metric**: Total Uncaught Crashes (FC), NullPointerExceptions (NPE), OutOfMemory (OOM), Total Caught Exceptions, and 60-second sliding-window Error Velocity (Errors/Min).
- **Mechanism**: Intercepts unhandled crashes via `CrashHandler.java` and internal `ExceptionManager.java` breadcrumbs.
- **Warning Threshold**: Error Rate > **5 errors / minute**.
- **Critical Threshold**: Any fatal unhandled crash or Error Rate > **15 errors / minute**.

### E. Memory Leak Detection (`LeakDetector`)
- **Metric**: Retained activity/dialog candidate instances, retained duration (seconds), and reference leakage paths.
- **Mechanism**: Watches destroyed UI contexts using `WeakReference` paired with a background `ReferenceQueue`. If an object remains unreachable for garbage collection > 5.0 seconds after destruction, it is classified as a leak candidate.

---

## 4. Anomaly Alert System & Threshold Matrix

| Alert Type | Severity | Condition | Suggested Remediation |
| :--- | :--- | :--- | :--- |
| `HIGH_CPU` | ⚠️ WARNING | Process CPU > 75% | Check background thread loops, reduce particle update rate in `CultivationAiEngine`. |
| `HIGH_CPU` | 🚨 CRITICAL | Process CPU > 90% | Infinite calculation loop detected; throttle game simulation tick rate. |
| `HIGH_MEMORY` | ⚠️ WARNING | Heap Usage > 80% | Clear cached bitmap drawables and invoke soft cache cleanup in `DialogManager`. |
| `HIGH_MEMORY` | 🚨 CRITICAL | Heap Usage > 90% | Imminent OOM; flush non-essential disciple battle histories and purge audio buffers. |
| `HIGH_ERROR_RATE` | ⚠️ WARNING | > 5 err / min | Check missing resource IDs or invalid state deserialization. |
| `HIGH_ERROR_RATE` | 🚨 CRITICAL | > 15 err / min or Crash | Inspect `CrashHandler` breadcrumbs and isolate failing subsystem. |
| `JANK_DETECTED` | ⚠️ WARNING | FPS < 45 | Optimize UI layout hierarchies and eliminate on-draw object allocations. |
| `MEMORY_LEAK_CANDIDATE` | ⚠️ WARNING | Retained View > 5s | Unregister static listeners, remove active Handler callbacks on `onDestroy`. |

*Note: All alerts are throttled with a 10-second debounce cooldown (`ALERT_COOLDOWN_MS`) to prevent alert flooding.*

---

## 5. How to Access & Use APM in the App

1. **Accessing the Full Diagnostic Dashboard**:
   - Open the game and tap the **⚙️ Settings** icon in the HUD.
   - Click the **"📊 APM Performance Monitor"** button.
   - The interactive dialog displays:
     - Real-time gauge bars for CPU and Heap Memory.
     - Live FPS counter, Jank frame metrics, and freeze indicators.
     - Error rate counter, NPE/OOM logs, and active leak candidates.
     - Recent alert history stream.
     - **Toggle Floating HUD** button for persistent on-screen monitoring.
     - **Export Diagnostics JSON** button for automated bug reporting.

2. **Using the Real-Time Floating HUD**:
   - Tap "Toggle Floating HUD" to display a minimalist overlay (`FPS: 60 | CPU: 12% | RAM: 34MB`).
   - The overlay stays pinned above game screens during live gameplay and turns amber/red when performance breaches critical thresholds.

3. **Exporting Diagnostic Snapshots**:
   - Tap "Export Diagnostics JSON" or call `ApmManager.getInstance().exportJsonDiagnostics()` to get a structured payload containing the complete hardware profile, memory stats, thread counts, and historical alert log.

---

## 6. Incident Response Runbook for Developers

### Case 1: High CPU Alert (> 80%)
1. Open the APM Dashboard to verify whether CPU load is isolated to the process or the whole system.
2. Check `BattleEngine.java` and `DiscipleManager.java` for tight `while` loops.
3. Verify that background thread handlers in `CultivationAiEngine` are sleeping or using scheduled delays.

### Case 2: Memory Leak / High Memory Alert
1. Inspect the "Leak Candidates" section in the APM Dashboard.
2. If `MainActivity` or `DialogManager` is listed, verify that all dialogs call `dismiss()` and unregister their `ResourceChangeListener` / `ApmAlertListener`.
3. Call `ApmManager.getInstance().triggerGarbageCollection()` and observe if heap memory recovers.

### Case 3: Error Burst / NPE Surge
1. Check `ApmSnapshot.errorRate` and inspect `CrashHandler.java` log output.
2. Review nullability of disciple attributes or building slot indexes before executing operations.
