package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;

/**
 * AlchemySystem - Pill concoction and refining mechanics.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class AlchemySystem {
    private AlchemySystem() {}

    public static final String[] RECIPES = {
        "Qi Gathering Pill",
        "Foundation Establishment Pill",
        "Body Tempering Elixir",
        "Spirit Cleansing Pill",
        "Nascent Soul Pill",
        "Void Transcending Pill",
        "Immortal Ascension Pellet"
    };

    public static final long[] HERB_COSTS = {
        20, 50, 100, 250, 600, 1500, 4000
    };

    public static final int[] SUCCESS_RATES = {
        85, 75, 65, 55, 45, 35, 25
    };

    public static class PillResult {
        public boolean success;
        public int quality;
        public int pillsMade;
        public String pillName;
        public int expGained;
        public String message;

        public PillResult(boolean success, int quality, int pillsMade, String pillName, int expGained) {
            this.success = success;
            this.quality = quality;
            this.pillsMade = pillsMade;
            this.pillName = pillName;
            this.expGained = expGained;
            if (success) {
                this.message = "Successfully crafted " + pillsMade + "x " + pillName + " (Quality " + quality + ")!";
            } else {
                this.message = "Alchemy failed. The cauldron blew up into black smoke!";
            }
        }
    }

    public static PillResult craft(Disciple worker, int recipeIndex, float bonusSuccessRate) {
        if (recipeIndex < 0 || recipeIndex >= RECIPES.length) {
            recipeIndex = 0;
        }

        String name = RECIPES[recipeIndex];
        long cost = HERB_COSTS[recipeIndex];
        int baseChance = SUCCESS_RATES[recipeIndex];

        SectData data = SectData.getInstance();
        if (data.spiritHerbs < cost) {
            return new PillResult(false, 0, 0, name, 0);
        }
        data.spend(0, cost, 0);

        int workerBonus = 0;
        if (worker != null) {
            workerBonus = (worker.intel / 3) + (worker.lck / 4);
        }

        float totalChance = baseChance + workerBonus + (bonusSuccessRate * 100f);
        boolean success = RNG.chance((int)totalChance);

        int quality = 1;
        int pillsMade = 0;
        int exp = 0;

        if (success) {
            pillsMade = 1;
            if (RNG.chance(30 + (worker != null ? worker.lck : 0))) {
                quality = 2; // High grade
                pillsMade = 2;
            }
            if (RNG.chance(10 + (worker != null ? worker.lck / 2 : 0))) {
                quality = 3; // Supreme grade
                pillsMade = 3;
            }
            exp = (recipeIndex + 1) * 25 * quality;
            if (worker != null) {
                worker.addExp(exp);
            }
        } else {
            exp = (recipeIndex + 1) * 5;
            if (worker != null) {
                worker.addExp(exp);
            }
        }

        return new PillResult(success, quality, pillsMade, name, exp);
    }
}
