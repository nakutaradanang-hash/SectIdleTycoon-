package com.sect.idle.utils;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import com.sect.idle.core.GameConfig;
import com.sect.idle.systems.AssetManager;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

/**
 * MemoryMonitor v1.0 - Centralized memory monitoring & asset reclamation system.
 * Designed for low-entry mobile devices (RAM 2GB+, itel A70, Android 5.0+).
 * 
 * Features:
 * 1. Tracks JVM runtime heap, threshold levels, and real-time pressure.
 * 2. Provides proactive memory watchdog to prevent OOM errors before OS kill signals.
 * 3. Coordinates cache evictions across BitmapCache, AssetManager, MemoryPool, and Scenes.
 * 4. 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class MemoryMonitor {
    public static final int PRESSURE_NORMAL = 0;
    public static final int PRESSURE_MODERATE = 1;
    public static final int PRESSURE_HIGH = 2;
    public static final int PRESSURE_CRITICAL = 3;

    private static volatile MemoryMonitor instance;
    private final Context appContext;
    private final ArrayList<WeakReference<MemoryReclaimable>> listeners;
    private final boolean lowRamDevice;
    private final long maxHeapBytes;

    private long lastCheckTimeMs = 0L;
    private static final long CHECK_INTERVAL_MS = 3000L; // Check at most every 3 seconds

    public interface MemoryReclaimable {
        /**
         * Called when memory pressure level changes or trim is requested.
         * @param pressureLevel PRESSURE_NORMAL, PRESSURE_MODERATE, PRESSURE_HIGH, or PRESSURE_CRITICAL
         */
        void onTrimMemory(int pressureLevel);

        /**
         * Called during critical OOM hazard to immediately purge all volatile caches.
         */
        void onEmergencyMemoryRelease();
    }

    private MemoryMonitor(Context context) {
        this.appContext = context != null ? context.getApplicationContext() : null;
        this.listeners = new ArrayList<WeakReference<MemoryReclaimable>>();
        
        boolean isLow = false;
        if (appContext != null) {
            ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                isLow = am.isLowRamDevice();
            }
        }
        this.lowRamDevice = isLow || (GameConfig.deviceTier == GameConfig.TIER_LOW);
        this.maxHeapBytes = Runtime.getRuntime().maxMemory();
    }

    public static MemoryMonitor get(Context context) {
        if (instance == null) {
            synchronized (MemoryMonitor.class) {
                if (instance == null) {
                    instance = new MemoryMonitor(context);
                }
            }
        }
        return instance;
    }

    public static MemoryMonitor get() {
        return instance;
    }

    public synchronized void registerReclaimable(MemoryReclaimable reclaimable) {
        if (reclaimable == null) return;
        // Avoid duplicate registrations
        for (int i = 0; i < listeners.size(); i++) {
            MemoryReclaimable existing = listeners.get(i).get();
            if (existing == reclaimable) return;
        }
        listeners.add(new WeakReference<MemoryReclaimable>(reclaimable));
    }

    public synchronized void unregisterReclaimable(MemoryReclaimable reclaimable) {
        if (reclaimable == null) return;
        Iterator<WeakReference<MemoryReclaimable>> it = listeners.iterator();
        while (it.hasNext()) {
            MemoryReclaimable existing = it.next().get();
            if (existing == null || existing == reclaimable) {
                it.remove();
            }
        }
    }

    /**
     * Handles Android OS ComponentCallbacks2 trim memory callbacks.
     */
    public void handleTrimMemory(int level) {
        int pressure;
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL || level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            pressure = PRESSURE_CRITICAL;
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW || level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            pressure = PRESSURE_HIGH;
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE || level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND || level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            pressure = PRESSURE_MODERATE;
        } else {
            pressure = PRESSURE_NORMAL;
        }

        ExceptionManager.get().addBreadcrumb("MemoryMonitor", "handleTrimMemory level=" + level + " -> pressure=" + pressure);
        dispatchReclamation(pressure);
    }

    /**
     * Handles Android OS onLowMemory callbacks.
     */
    public void handleLowMemory() {
        ExceptionManager.get().addBreadcrumb("MemoryMonitor", "handleLowMemory emergency triggered");
        dispatchEmergencyRelease();
    }

    /**
     * Proactive memory watchdog. Periodically evaluates heap metrics and cleans up if needed.
     */
    public int checkMemoryPressure() {
        long now = System.currentTimeMillis();
        if (now - lastCheckTimeMs < CHECK_INTERVAL_MS) {
            return PRESSURE_NORMAL;
        }
        lastCheckTimeMs = now;

        float usage = getMemoryUsageRatio();
        float criticalThreshold = lowRamDevice ? 0.80f : 0.88f;
        float highThreshold = lowRamDevice ? 0.68f : 0.76f;
        float moderateThreshold = lowRamDevice ? 0.55f : 0.65f;

        int pressure = PRESSURE_NORMAL;
        if (usage >= criticalThreshold) {
            pressure = PRESSURE_CRITICAL;
            dispatchEmergencyRelease();
        } else if (usage >= highThreshold) {
            pressure = PRESSURE_HIGH;
            dispatchReclamation(PRESSURE_HIGH);
        } else if (usage >= moderateThreshold) {
            pressure = PRESSURE_MODERATE;
            dispatchReclamation(PRESSURE_MODERATE);
        }
        return pressure;
    }

    public synchronized void dispatchReclamation(int pressureLevel) {
        if (pressureLevel <= PRESSURE_NORMAL) return;

        // 1. Core utilities trim
        BitmapCache bmpCache = BitmapCache.get();
        if (bmpCache != null) {
            if (pressureLevel >= PRESSURE_CRITICAL) {
                bmpCache.clear();
            } else if (pressureLevel == PRESSURE_HIGH) {
                bmpCache.trimToSize((int) (maxHeapBytes / 1024 / 24));
            } else {
                bmpCache.trimToSize((int) (maxHeapBytes / 1024 / 16));
            }
        }

        if (appContext != null) {
            AssetManager am = AssetManager.get(appContext);
            if (am != null) {
                if (pressureLevel >= PRESSURE_HIGH) {
                    am.unloadUnused();
                }
            }
        }

        MemoryPool.reset();

        // 2. Notify all registered listeners
        ArrayList<MemoryReclaimable> activeListeners = new ArrayList<MemoryReclaimable>();
        Iterator<WeakReference<MemoryReclaimable>> it = listeners.iterator();
        while (it.hasNext()) {
            MemoryReclaimable r = it.next().get();
            if (r == null) {
                it.remove();
            } else {
                activeListeners.add(r);
            }
        }

        for (int i = 0; i < activeListeners.size(); i++) {
            try {
                activeListeners.get(i).onTrimMemory(pressureLevel);
            } catch (Exception e) {
                ExceptionManager.get().report(e, "MemoryMonitor", "dispatchReclamation");
            }
        }

        if (pressureLevel >= PRESSURE_CRITICAL) {
            forceGarbageCollection();
        }
    }

    public synchronized void dispatchEmergencyRelease() {
        // 1. Clear all volatile caches
        BitmapCache bmpCache = BitmapCache.get();
        if (bmpCache != null) {
            bmpCache.clear();
        }

        if (appContext != null) {
            AssetManager am = AssetManager.get(appContext);
            if (am != null) {
                am.onLowMemory();
            }
        }

        MemoryPool.reset();

        // 2. Notify all registered listeners for emergency release
        ArrayList<MemoryReclaimable> activeListeners = new ArrayList<MemoryReclaimable>();
        Iterator<WeakReference<MemoryReclaimable>> it = listeners.iterator();
        while (it.hasNext()) {
            MemoryReclaimable r = it.next().get();
            if (r == null) {
                it.remove();
            } else {
                activeListeners.add(r);
            }
        }

        for (int i = 0; i < activeListeners.size(); i++) {
            try {
                activeListeners.get(i).onEmergencyMemoryRelease();
            } catch (Exception e) {
                ExceptionManager.get().report(e, "MemoryMonitor", "dispatchEmergencyRelease");
            }
        }

        forceGarbageCollection();
    }

    public void forceGarbageCollection() {
        try {
            System.gc();
            System.runFinalization();
        } catch (Exception ignored) {}
    }

    public long getUsedMemoryBytes() {
        Runtime r = Runtime.getRuntime();
        return r.totalMemory() - r.freeMemory();
    }

    public long getMaxMemoryBytes() {
        return maxHeapBytes;
    }

    public long getFreeMemoryBytes() {
        Runtime r = Runtime.getRuntime();
        return maxHeapBytes - (r.totalMemory() - r.freeMemory());
    }

    public float getMemoryUsageRatio() {
        if (maxHeapBytes <= 0) return 0f;
        return (float) getUsedMemoryBytes() / (float) maxHeapBytes;
    }

    public int getMemoryUsagePercent() {
        return (int) (getMemoryUsageRatio() * 100f);
    }

    public boolean isLowRamDevice() {
        return lowRamDevice;
    }

    public String getMemorySummary() {
        long usedMB = getUsedMemoryBytes() / (1024 * 1024);
        long maxMB = maxHeapBytes / (1024 * 1024);
        int percent = getMemoryUsagePercent();
        return String.format(Locale.US, "Mem: %dMB / %dMB (%d%%) LowRam: %s", usedMB, maxMB, percent, lowRamDevice ? "YES" : "NO");
    }
}
