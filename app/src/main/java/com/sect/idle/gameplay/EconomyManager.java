package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;

/**
 * EconomyManager - Single-responsibility manager for Sect income, expenses,
 * wages, resource balances, and economic tick cycles.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class EconomyManager {

    private static volatile EconomyManager instance;
    private static final Object LOCK = new Object();

    private EconomyManager() {}

    public static EconomyManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new EconomyManager();
                }
            }
        }
        return instance;
    }

    /**
     * Recalculates total sect income, expenses, and net profit per day.
     */
    public void recalculateEconomy(SectData data) {
        if (data == null) return;

        long income = 0;
        long expense = 0;

        // Building base income
        if (data.buildings != null) {
            for (int i = 0; i < data.buildings.size(); i++) {
                Building b = data.buildings.get(i);
                if (b != null && b.isBuilt) {
                    if (b.type == GameConfig.BUILD_SPIRIT_POOL) {
                        income += b.level * 80L;
                    } else if (b.type == GameConfig.BUILD_GARDEN) {
                        income += b.level * 50L;
                    }
                }
            }
        }

        // Disciple tasks and wages
        if (data.disciples != null) {
            for (int i = 0; i < data.disciples.size(); i++) {
                Disciple d = data.disciples.get(i);
                if (d != null) {
                    expense += d.dailyWage;
                    if (d.currentTask == GameConfig.TASK_MINING) {
                        income += (long)(15L * (d.taskEfficiency / 100f));
                    } else if (d.currentTask == GameConfig.TASK_FARMING) {
                        income += (long)(10L * (d.taskEfficiency / 100f));
                    }
                }
            }
        }

        data.dailyIncomeSS = income;
        data.dailyExpenseSS = expense;
        data.netDailySS = income - expense;
        data.netProfit = data.netDailySS;
        data.economyDirty = false;
    }

    /**
     * Executes daily economic tick.
     */
    public void processDailyTick(SectData data) {
        if (data == null) return;
        recalculateEconomy(data);

        data.spiritStones += data.netDailySS;
        if (data.spiritStones < 0) {
            data.spiritStones = 0;
        }
    }
}
