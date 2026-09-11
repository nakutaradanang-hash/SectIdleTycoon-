package com.sect.idle.gameplay;

import android.os.Handler;
import android.os.Looper;
import com.sect.idle.utils.DataValidator;
import java.util.ArrayList;

/**
 * ResourceManager - Centralized reactive resource monitoring class that tracks
 * Spirit Stone, Herb, Pill, Ore, and Jade counts.
 *
 * Implements strict input validation, overflow protection, and non-negative bounds checking.
 * Automatically notifies registered UI listeners and HUD elements when any resource
 * value is updated, earned, spent, or modified across the game.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class ResourceManager {

    public interface ResourceChangeListener {
        /**
         * Invoked whenever any core resource value changes.
         * Guaranteed to execute safely on Android's Main Looper / UI thread.
         */
        void onResourcesChanged(long stones, long herbs, long pills, long ores, long jade);
    }

    private static volatile ResourceManager instance;
    private static final Object LOCK = new Object();
    private static final long MAX_RESOURCE_CAP = 1000000000000L; // 1 Trillion Hard Cap

    private final ArrayList<ResourceChangeListener> listeners;
    private final Handler mainHandler;

    // Tracked resource balances
    private long spiritStones = 1000;
    private long spiritHerbs = 100;
    private long spiritPills = 10;
    private long spiritOres = 50;
    private long jade = 50;

    private ResourceManager() {
        this.listeners = new ArrayList<ResourceChangeListener>();
        Handler h = null;
        try {
            if (Looper.getMainLooper() != null) {
                h = new Handler(Looper.getMainLooper());
            }
        } catch (Throwable ignored) {
            h = null;
        }
        this.mainHandler = h;
    }

    public static ResourceManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ResourceManager();
                }
            }
        }
        return instance;
    }

    /**
     * Synchronizes the ResourceManager with a given SectData instance with bounds validation.
     */
    public synchronized void syncFromSectData(SectData data) {
        if (data == null) return;
        this.spiritStones = DataValidator.clampLong(data.spiritStones, 0L, MAX_RESOURCE_CAP);
        this.spiritHerbs = DataValidator.clampLong(data.spiritHerbs, 0L, MAX_RESOURCE_CAP);
        this.spiritPills = DataValidator.clampLong(data.spiritPills, 0L, MAX_RESOURCE_CAP);
        this.spiritOres = DataValidator.clampLong(data.spiritOres, 0L, MAX_RESOURCE_CAP);
        this.jade = DataValidator.clampLong(data.jade, 0L, MAX_RESOURCE_CAP);
        notifyListeners();
    }

    /**
     * Pushes current resource values into SectData.
     */
    public synchronized void syncToSectData(SectData data) {
        if (data == null) return;
        data.spiritStones = this.spiritStones;
        data.spiritHerbs = this.spiritHerbs;
        data.spiritPills = this.spiritPills;
        data.spiritOres = this.spiritOres;
        data.jade = this.jade;
    }

    // =========================================================================
    // LISTENER REGISTRATION & REACTIVE DISPATCH
    // =========================================================================

    public synchronized void registerListener(ResourceChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            // Immediately dispatch current balances to newly registered listener
            final ResourceChangeListener target = listener;
            final long s = spiritStones, h = spiritHerbs, p = spiritPills, o = spiritOres, j = jade;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    target.onResourcesChanged(s, h, p, o, j);
                }
            };
            if (mainHandler != null) {
                mainHandler.post(r);
            } else {
                r.run();
            }
        }
    }

    public synchronized void unregisterListener(ResourceChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public synchronized void clearListeners() {
        listeners.clear();
    }

    public synchronized void notifyListeners() {
        final long s = spiritStones;
        final long h = spiritHerbs;
        final long p = spiritPills;
        final long o = spiritOres;
        final long j = jade;

        final ArrayList<ResourceChangeListener> copy = new ArrayList<ResourceChangeListener>(listeners);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < copy.size(); i++) {
                    ResourceChangeListener l = copy.get(i);
                    if (l != null) {
                        try {
                            l.onResourcesChanged(s, h, p, o, j);
                        } catch (Exception ignored) {}
                    }
                }
            }
        };

        if (mainHandler != null) {
            mainHandler.post(r);
        } else {
            r.run();
        }
    }

    // =========================================================================
    // RESOURCE MUTATIONS WITH ARITHMETIC SAFETY & BOUNDS CHECKING
    // =========================================================================

    public synchronized void addSpiritStones(long amount) {
        if (amount <= 0) return;
        this.spiritStones = DataValidator.clampLong(DataValidator.safeAdd(this.spiritStones, amount), 0L, MAX_RESOURCE_CAP);
        SectData data = SectData.getInstance();
        if (data != null) data.spiritStones = this.spiritStones;
        notifyListeners();
    }

    public synchronized boolean spendSpiritStones(long amount) {
        if (amount <= 0) return true;
        if (this.spiritStones >= amount) {
            this.spiritStones = DataValidator.safeSubtractNonNegative(this.spiritStones, amount);
            SectData data = SectData.getInstance();
            if (data != null) data.spiritStones = this.spiritStones;
            notifyListeners();
            return true;
        }
        return false;
    }

    public synchronized void addSpiritHerbs(long amount) {
        if (amount <= 0) return;
        this.spiritHerbs = DataValidator.clampLong(DataValidator.safeAdd(this.spiritHerbs, amount), 0L, MAX_RESOURCE_CAP);
        SectData data = SectData.getInstance();
        if (data != null) data.spiritHerbs = this.spiritHerbs;
        notifyListeners();
    }

    public synchronized boolean spendSpiritHerbs(long amount) {
        if (amount <= 0) return true;
        if (this.spiritHerbs >= amount) {
            this.spiritHerbs = DataValidator.safeSubtractNonNegative(this.spiritHerbs, amount);
            SectData data = SectData.getInstance();
            if (data != null) data.spiritHerbs = this.spiritHerbs;
            notifyListeners();
            return true;
        }
        return false;
    }

    public synchronized void addSpiritPills(long amount) {
        if (amount <= 0) return;
        this.spiritPills = DataValidator.clampLong(DataValidator.safeAdd(this.spiritPills, amount), 0L, MAX_RESOURCE_CAP);
        SectData data = SectData.getInstance();
        if (data != null) data.spiritPills = this.spiritPills;
        notifyListeners();
    }

    public synchronized boolean spendSpiritPills(long amount) {
        if (amount <= 0) return true;
        if (this.spiritPills >= amount) {
            this.spiritPills = DataValidator.safeSubtractNonNegative(this.spiritPills, amount);
            SectData data = SectData.getInstance();
            if (data != null) data.spiritPills = this.spiritPills;
            notifyListeners();
            return true;
        }
        return false;
    }

    public synchronized void earn(long stones, long herbs, long ores) {
        if (stones > 0) this.spiritStones = DataValidator.clampLong(DataValidator.safeAdd(this.spiritStones, stones), 0L, MAX_RESOURCE_CAP);
        if (herbs > 0) this.spiritHerbs = DataValidator.clampLong(DataValidator.safeAdd(this.spiritHerbs, herbs), 0L, MAX_RESOURCE_CAP);
        if (ores > 0) this.spiritOres = DataValidator.clampLong(DataValidator.safeAdd(this.spiritOres, ores), 0L, MAX_RESOURCE_CAP);

        SectData data = SectData.getInstance();
        if (data != null) {
            data.spiritStones = this.spiritStones;
            data.spiritHerbs = this.spiritHerbs;
            data.spiritOres = this.spiritOres;
        }
        notifyListeners();
    }

    public synchronized boolean spend(long stones, long herbs, long ores) {
        long validStones = Math.max(0, stones);
        long validHerbs = Math.max(0, herbs);
        long validOres = Math.max(0, ores);

        if (this.spiritStones >= validStones && this.spiritHerbs >= validHerbs && this.spiritOres >= validOres) {
            this.spiritStones = DataValidator.safeSubtractNonNegative(this.spiritStones, validStones);
            this.spiritHerbs = DataValidator.safeSubtractNonNegative(this.spiritHerbs, validHerbs);
            this.spiritOres = DataValidator.safeSubtractNonNegative(this.spiritOres, validOres);

            SectData data = SectData.getInstance();
            if (data != null) {
                data.spiritStones = this.spiritStones;
                data.spiritHerbs = this.spiritHerbs;
                data.spiritOres = this.spiritOres;
            }
            notifyListeners();
            return true;
        }
        return false;
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public synchronized long getSpiritStones() {
        return spiritStones;
    }

    public synchronized long getSpiritHerbs() {
        return spiritHerbs;
    }

    public synchronized long getSpiritPills() {
        return spiritPills;
    }

    public synchronized long getSpiritOres() {
        return spiritOres;
    }

    public synchronized long getJade() {
        return jade;
    }
}
