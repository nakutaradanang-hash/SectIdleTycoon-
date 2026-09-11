package com.sect.idle.gameplay;

import android.content.Context;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;

import java.util.ArrayList;

/**
 * SectLifecycleManager - Single-responsibility manager for Sect startup, save/load lifecycle,
 * and decoupling core game initialization from UI activities like MainActivity.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class SectLifecycleManager {

    private static volatile SectLifecycleManager instance;
    private static final Object LOCK = new Object();

    public interface InitCallback {
        void onProgress(int percent, String description);
        void onComplete();
    }

    private SectLifecycleManager() {}

    public static SectLifecycleManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new SectLifecycleManager();
                }
            }
        }
        return instance;
    }

    /**
     * Loads existing sect data or initializes a brand-new immortal cultivation sect.
     */
    public boolean loadOrInitializeSect(Context context) {
        if (context == null) return false;

        SectData data = SectData.getInstance();
        SaveManager saveManager = new SaveManager(context);
        boolean loaded = saveManager.load();

        if (!loaded) {
            initializeNewSect(data);
            saveManager.save();
        }

        return true;
    }

    /**
     * Initializes default immortal sect state with disciples, buildings, and initial economy.
     */
    public void initializeNewSect(SectData data) {
        if (data == null) return;

        data.reset();

        // Seed initial disciples via DiscipleManager
        ArrayList<Disciple> initialDisciples = DiscipleManager.getInstance().generateInitialDisciples(3);
        for (int i = 0; i < initialDisciples.size(); i++) {
            data.addDisciple(initialDisciples.get(i));
        }

        // Activate starter Main Hall
        if (data.buildings != null && !data.buildings.isEmpty()) {
            Building mainHall = data.buildings.get(0);
            if (mainHall != null) {
                mainHall.isBuilt = true;
            }
        }

        // Compute starting economy
        EconomyManager.getInstance().recalculateEconomy(data);
    }
}
