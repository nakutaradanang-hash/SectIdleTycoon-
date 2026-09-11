package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * CultivationAiEngine - Autonomous AI task assigning, cultivation guidance, and decision making.
 * Zero-allocation, pure Java 7, and Sketchware Pro v7.0.0 compatible.
 */
public final class CultivationAiEngine {
    private CultivationAiEngine() {}

    public static void autoAssignOptimalTask(Disciple d) {
        if (d == null || !d.isAlive()) return;

        // Choose task based on highest stat and talent
        int highestStat = Math.max(d.str, Math.max(d.intel, Math.max(d.vit, d.lck)));
        if (highestStat == d.intel) {
            d.currentTask = d.intel > 30 ? GameConfig.TASK_ALCHEMY : GameConfig.TASK_RESEARCH;
        } else if (highestStat == d.str) {
            d.currentTask = GameConfig.TASK_CRAFTING;
        } else if (highestStat == d.vit) {
            d.currentTask = GameConfig.TASK_MINING;
        } else {
            d.currentTask = GameConfig.TASK_FARMING;
        }
        d.updateEfficiency();
    }

    public static void optimizeSectWorkforce(ArrayList<Disciple> disciples) {
        if (disciples == null) return;
        for (int i = 0; i < disciples.size(); i++) {
            Disciple d = disciples.get(i);
            if (d != null && d.isAlive() && d.currentTask == GameConfig.TASK_NONE) {
                autoAssignOptimalTask(d);
            }
        }
    }

    public static String suggestCultivationTip(Disciple d) {
        if (d == null) return "Cultivate with a calm heart.";
        if (d.stress > 70) return d.name + " is overly stressed. Assign a leisure hobby or let them rest.";
        if (d.energy < 30) return d.name + " is exhausted. Needs meditation to recover Qi.";
        if (d.realmExp > GameConfig.REALM_EXP_CAP * 0.8f) return d.name + " is nearing a breakthrough! Prepare tribulation pills.";
        return d.name + " is progressing smoothly along the Heavenly Dao.";
    }
}
