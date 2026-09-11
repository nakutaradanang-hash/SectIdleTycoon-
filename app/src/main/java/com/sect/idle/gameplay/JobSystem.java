package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;

/**
 * JobSystem - Manages disciple occupations and titles.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class JobSystem {
    private JobSystem() {}

    public static String getJobName(Disciple d) {
        if (d == null) return "None";
        switch (d.currentTask) {
            case GameConfig.TASK_FARMING: return "Herbalist";
            case GameConfig.TASK_CRAFTING: return "Blacksmith";
            case GameConfig.TASK_ALCHEMY: return "Alchemist";
            case GameConfig.TASK_CULTIVATION: return "Cultivator";
            case GameConfig.TASK_MINING: return "Miner";
            case GameConfig.TASK_TRAINING: return "Martial Artist";
            case GameConfig.TASK_GUARD: return "Sect Guard";
            case GameConfig.TASK_RESEARCH: return "Scholar";
            case GameConfig.TASK_TRADING: return "Merchant";
            case GameConfig.TASK_EXPLORING: return "Scout";
            default: return "Idle Disciple";
        }
    }

    public static String getTaskDescription(int task) {
        switch (task) {
            case GameConfig.TASK_FARMING: return "Tends to the herb garden, producing medicinal ingredients.";
            case GameConfig.TASK_CRAFTING: return "Forges weapons, armor, and spiritual treasures.";
            case GameConfig.TASK_ALCHEMY: return "Refines spiritual pills and miraculous elixirs.";
            case GameConfig.TASK_CULTIVATION: return "Meditates to absorb celestial Qi and advance realm.";
            case GameConfig.TASK_MINING: return "Excavates spirit veins for rare ores and crystals.";
            case GameConfig.TASK_TRAINING: return "Hones combat techniques and builds physical strength.";
            case GameConfig.TASK_GUARD: return "Patrols sect gates and defends against invaders.";
            case GameConfig.TASK_RESEARCH: return "Deciphers ancient scriptures and sect formations.";
            case GameConfig.TASK_TRADING: return "Conducts commerce with mortal kingdoms and rogue cultivators.";
            case GameConfig.TASK_EXPLORING: return "Ventures into forbidden zones seeking hidden treasures.";
            default: return "Resting and wandering the sect grounds.";
        }
    }
}
