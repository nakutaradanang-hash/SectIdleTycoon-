package com.sect.idle.models;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.sect.idle.core.GameConfig;
import com.sect.idle.systems.CameraSystem;

public final class Building {
    public int type;
    public String name;
    public int level;
    public int maxLevel;
    public long upgradeCost;
    public long cost;
    public int workers;
    public int maxWorkers;
    public float efficiency;
    public boolean isBuilt;
    public int posX, posY;
    public int width, height;
    public int spriteId;
    public int incomeBonus;
    public int storageBonus;

    private static final float UPGRADE_COST_MULT = 1.5f;
    private static final float EFFICIENCY_PER_LEVEL = 0.2f;
    private static final int WORKERS_PER_UPGRADE = 2;

    public Building(int type, String name, int maxLevel, long cost, int maxWorkers) {
        this.type = type;
        this.name = name != null ? name : GameConfig.BUILD_NAMES[type >= 0 && type < GameConfig.BUILD_COUNT ? type : 0];
        this.maxLevel = Math.max(1, maxLevel);
        this.upgradeCost = Math.max(0L, cost);
        this.cost = Math.max(0L, cost);
        this.maxWorkers = Math.max(1, maxWorkers);
        this.level = 0;
        this.workers = 0;
        this.efficiency = 1.0f;
        this.isBuilt = false;
        this.incomeBonus = 0;
        this.storageBonus = 0;
        this.width = 80;
        this.height = 80;
    }
    
    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (!isBuilt || canvas == null || cam == null || paint == null) return;
        float px = cam.worldToScreenX(posX);
        float py = cam.worldToScreenY(posY);
        float size = 90f * cam.zoom;
        float half = size * 0.5f;
        paint.setColor(0xFF3A2A60);
        canvas.drawRoundRect(px - half, py - half, px + half, py + half, 10f * cam.zoom, 10f * cam.zoom, paint);
        paint.setColor(0xFFFFFFFF);
        paint.setTextSize(14f * cam.zoom);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(name + " Lv." + level, px, py - half - 8f * cam.zoom, paint);
    }

    public void upgrade() {
        if (level < maxLevel) {
            level++;
            efficiency += EFFICIENCY_PER_LEVEL;
            upgradeCost = (long)(upgradeCost * UPGRADE_COST_MULT);
            maxWorkers += WORKERS_PER_UPGRADE;
            incomeBonus += level * 5;
            storageBonus += level * 10;
        }
    }

    public long getUpgradeCost() {
        return isBuilt ? upgradeCost : upgradeCost / 2;
    }

    public boolean canUpgrade(long ss) {
        return isBuilt && level < maxLevel && ss >= getUpgradeCost();
    }

    public long getDailyIncome() {
        return isBuilt ? (long)(level * 20 * efficiency) + incomeBonus : 0L;
    }

    public int getStorageCapacity() {
        return isBuilt ? storageBonus : 0;
    }

    public boolean assignWorker() {
        if (workers < maxWorkers) {
            workers++;
            efficiency += 0.05f;
            return true;
        }
        return false;
    }

    public boolean removeWorker() {
        if (workers > 0) {
            workers--;
            efficiency = Math.max(1.0f, efficiency - 0.05f);
            return true;
        }
        return false;
    }

    public void build() {
        if (!isBuilt) {
            isBuilt = true;
            level = 1;
            efficiency = 1.2f;
        }
    }
}
