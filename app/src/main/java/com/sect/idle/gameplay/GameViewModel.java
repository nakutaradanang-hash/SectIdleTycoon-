package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import java.util.ArrayList;

/**
 * GameViewModel - Centralized state manager and lifecycle observer.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class GameViewModel {
    private final SectData data;
    private final SaveManager saveManager;

    public interface StateObserver {
        void onDataChanged();
    }

    private final ArrayList<StateObserver> observers = new ArrayList<StateObserver>();

    public GameViewModel(android.content.Context context) {
        this.data = SectData.getInstance();
        this.saveManager = new SaveManager(context);
    }

    public void addObserver(StateObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(StateObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        for (int i = 0; i < observers.size(); i++) {
            StateObserver o = observers.get(i);
            if (o != null) o.onDataChanged();
        }
    }

    public SectData getData() {
        return data;
    }

    public void recruitDisciple(Disciple d) {
        if (d != null) {
            data.addDisciple(d);
            notifyObservers();
        }
    }

    public void assignTask(Disciple d, int task) {
        if (d != null) {
            d.currentTask = task;
            d.updateEfficiency();
            data.recalculateEconomy();
            notifyObservers();
        }
    }

    public void saveGame() {
        if (saveManager != null) {
            saveManager.save();
        }
    }

    public boolean loadGame() {
        if (saveManager != null) {
            boolean success = saveManager.load();
            if (success) notifyObservers();
            return success;
        }
        return false;
    }
}
