package com.sect.idle.gameplay;

import android.view.View;
import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.systems.RNG;
import com.sect.idle.utils.HapticManager;
import java.util.ArrayList;

/**
 * TrainingSystem - Manages disciple martial training, body tempering,
 * meditation, and cultivation Realm Breakthrough mechanics requiring
 * both Realm Experience and Spirit Stones.
 * Fully integrates procedurally generated Disciple Talents (Fast Learner, Dao Heart,
 * Iron Bones, Sword Heart, etc.) for dynamic training rate multipliers, breakthrough discounts,
 * and breakthrough success rate bonuses.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class TrainingSystem {
    private static volatile TrainingSystem instance;
    private static final Object LOCK = new Object();

    public static TrainingSystem getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new TrainingSystem();
                }
            }
        }
        return instance;
    }

    public static final int TYPE_MEDITATION = 0;
    public static final int TYPE_BODY_TEMPERING = 1;
    public static final int TYPE_TECHNIQUE_SPAR = 2;
    public static final int TYPE_DAO_COMPREHENSION = 3;

    public static class BreakthroughResult {
        public final boolean success;
        public final int oldRealm;
        public final int newRealm;
        public final int expCost;
        public final long stoneCost;
        public final String realmName;
        public final String message;

        public BreakthroughResult(boolean success, int oldRealm, int newRealm, int expCost, long stoneCost, String realmName, String message) {
            this.success = success;
            this.oldRealm = oldRealm;
            this.newRealm = newRealm;
            this.expCost = expCost;
            this.stoneCost = stoneCost;
            this.realmName = realmName;
            this.message = message;
        }
    }

    /**
     * Calculates the required realm experience to attempt breakthrough at given realm.
     */
    public int getRequiredExp(int currentRealm) {
        if (currentRealm < 0) currentRealm = 0;
        return (currentRealm + 1) * 300 + (currentRealm * currentRealm * 120);
    }

    /**
     * Calculates the required spirit stones to attempt breakthrough at given realm.
     */
    public long getRequiredSpiritStones(int currentRealm) {
        return getRequiredSpiritStones(currentRealm, null);
    }

    /**
     * Calculates the required spirit stones with disciple talent discount applied.
     */
    public long getRequiredSpiritStones(int currentRealm, Disciple d) {
        if (currentRealm < 0) currentRealm = 0;
        long base = (currentRealm + 1) * 200L + (currentRealm * currentRealm * 100L);
        if (d != null) {
            float discount = TalentSystem.getBreakthroughStoneDiscount(d);
            return Math.max(50L, (long)(base * discount));
        }
        return base;
    }

    /**
     * Checks if a disciple is eligible for breakthrough.
     */
    public boolean canBreakthrough(Disciple d, SectData s) {
        if (d == null || s == null || !d.isAlive()) return false;
        if (d.realm >= GameConfig.REALM_MAX - 1) return false;
        int reqExp = getRequiredExp(d.realm);
        long reqStones = getRequiredSpiritStones(d.realm, d);
        return d.realmExp >= reqExp && s.spiritStones >= reqStones;
    }

    /**
     * Calculates the success rate percentage (40% to 98%) for a breakthrough attempt.
     */
    public int getBreakthroughSuccessRate(Disciple d, SectData s) {
        if (d == null) return 50;
        int baseChance = 75 - (d.realm * 4);
        if (baseChance < 45) baseChance = 45;

        // Bonus from disciple attributes
        int attrBonus = (d.wis / 5) + (d.lck / 6) + (d.intel / 8);

        // Building facility bonus (e.g. Cultivation Library, Spirit Pool)
        int facilityBonus = 0;
        if (s != null && s.buildings != null) {
            for (int i = 0; i < s.buildings.size(); i++) {
                Building b = s.buildings.get(i);
                if (b != null && b.isBuilt && b.type == GameConfig.BUILD_LIBRARY) {
                    facilityBonus += b.level * 2;
                }
            }
        }

        // Talent innate breakthrough bonus
        int talentBonus = TalentSystem.getBreakthroughRateBonus(d);

        int total = baseChance + attrBonus + facilityBonus + talentBonus;
        if (total > 98) total = 98;
        if (total < 40) total = 40;
        return total;
    }

    /**
     * Attempts a breakthrough with tactile haptic feedback, talent bonuses, and resource consumption.
     */
    public BreakthroughResult attemptBreakthrough(Disciple d, SectData s, View sourceView) {
        if (d == null || s == null || !d.isAlive()) {
            return new BreakthroughResult(false, 0, 0, 0, 0, "Unknown", "Disciple or Sect data unavailable.");
        }
        if (d.realm >= GameConfig.REALM_MAX - 1) {
            return new BreakthroughResult(false, d.realm, d.realm, 0, 0, GameConfig.getRealmName(d.realm), "Disciple has already attained the Pinnacle Immortal Dao realm!");
        }

        int reqExp = getRequiredExp(d.realm);
        long reqStones = getRequiredSpiritStones(d.realm, d);

        if (d.realmExp < reqExp) {
            if (sourceView != null) HapticManager.getInstance().performFailure(sourceView);
            return new BreakthroughResult(false, d.realm, d.realm, reqExp, reqStones, GameConfig.getRealmName(d.realm),
                    "Insufficient Realm EXP! (" + d.realmExp + " / " + reqExp + ")");
        }

        if (s.spiritStones < reqStones) {
            if (sourceView != null) HapticManager.getInstance().performFailure(sourceView);
            return new BreakthroughResult(false, d.realm, d.realm, reqExp, reqStones, GameConfig.getRealmName(d.realm),
                    "Insufficient Spirit Stones! Required: " + reqStones + " SS (Current: " + s.spiritStones + ")");
        }

        // Deduct spirit stones via SectData spend
        s.spend(reqStones, 0, 0);
        d.realmExp -= reqExp;

        int successRate = getBreakthroughSuccessRate(d, s);
        boolean success = RNG.chance(successRate);

        int oldRealm = d.realm;

        if (success) {
            d.realm++;
            d.realmTier = 1;
            
            // Major attribute surge upon breaking through heavenly bottlenecks
            int realmMultiplier = d.realm + 1;
            d.str += 4 + realmMultiplier * 2;
            d.vit += 4 + realmMultiplier * 2;
            d.agi += 3 + realmMultiplier;
            d.intel += 3 + realmMultiplier;
            d.wis += 2 + realmMultiplier;
            d.maxHp += 120 + realmMultiplier * 40;
            d.hp = d.maxHp;
            d.maxMp += 60 + realmMultiplier * 25;
            d.mp = d.maxMp;
            d.recalculateStats();
            d.recalcCombat();
            d.updateEfficiency();

            if (d.realm > s.highestRealm) {
                s.highestRealm = d.realm;
            }
            if (s.highestRealm > s.sectRealm) {
                s.sectRealm = s.highestRealm;
            }
            s.markEconomyDirty();

            String newRealmName = GameConfig.getRealmName(d.realm);

            // Tactile and visual feedback
            if (sourceView != null) {
                HapticManager.getInstance().performBreakthroughSuccess(sourceView);
            }
            GameplayFeedbackDispatcher.getInstance().onDiscipleBreakthrough(d, newRealmName);

            return new BreakthroughResult(true, oldRealm, d.realm, reqExp, reqStones, newRealmName,
                    "⚡ Heavenly Tribulation Overcome! " + d.name + " broke through to " + newRealmName + "!");
        } else {
            // Cultivation setback
            d.realmExp = (int)(d.realmExp * 0.5f);
            if (sourceView != null) {
                HapticManager.getInstance().performFailure(sourceView);
            }
            return new BreakthroughResult(false, oldRealm, oldRealm, reqExp, reqStones, GameConfig.getRealmName(oldRealm),
                    "Bottleneck resisted the breakthrough! Consumed " + reqStones + " Spirit Stones. Meditate and try again!");
        }
    }

    public void processDiscipleTraining(Disciple d, SectData s, int trainingType) {
        if (d == null || s == null || !d.isAlive()) return;

        float efficiencyBonus = 1.0f + (d.taskEfficiency / 100f);

        for (int i = 0; i < s.buildings.size(); i++) {
            Building b = s.buildings.get(i);
            if (b != null && b.isBuilt) {
                if (b.type == GameConfig.BUILD_ARENA && trainingType == TYPE_TECHNIQUE_SPAR) {
                    efficiencyBonus += b.level * 0.15f;
                } else if (b.type == GameConfig.BUILD_LIBRARY && (trainingType == TYPE_MEDITATION || trainingType == TYPE_DAO_COMPREHENSION)) {
                    efficiencyBonus += b.level * 0.15f;
                }
            }
        }

        int extraStat = TalentSystem.getSparringStatBonus(d);

        switch (trainingType) {
            case TYPE_MEDITATION:
                float expMult = TalentSystem.getTrainingExpMultiplier(d);
                int expGain = (int)((25 + d.wis * 2 + d.intel * 2) * efficiencyBonus * expMult);
                d.realmExp += expGain;
                d.addExperience(expGain / 2);
                break;

            case TYPE_BODY_TEMPERING:
                d.str += RNG.nextInt(1, 3) + extraStat;
                d.vit += RNG.nextInt(1, 3) + extraStat;
                d.bodyRefiningStage++;
                d.recalculateStats();
                break;

            case TYPE_TECHNIQUE_SPAR:
                d.agi += RNG.nextInt(1, 3) + extraStat;
                d.accuracy += 1;
                d.recalculateStats();
                break;

            case TYPE_DAO_COMPREHENSION:
                d.wis += RNG.nextInt(1, 3) + extraStat;
                d.intel += RNG.nextInt(1, 3) + extraStat;
                d.recalculateStats();
                break;
        }

        // Automatic breakthrough if conditions are satisfied during meditation
        if (canBreakthrough(d, s)) {
            attemptBreakthrough(d, s, null);
        }
    }

    public void processAllTrainingDisciples(SectData s) {
        if (s == null || s.disciples == null) return;
        for (int i = 0; i < s.disciples.size(); i++) {
            Disciple d = s.disciples.get(i);
            if (d != null && d.currentTask == GameConfig.TASK_TRAINING) {
                processDiscipleTraining(d, s, TYPE_TECHNIQUE_SPAR);
            } else if (d != null && d.currentTask == GameConfig.TASK_CULTIVATION) {
                processDiscipleTraining(d, s, TYPE_MEDITATION);
            }
        }
    }
}
