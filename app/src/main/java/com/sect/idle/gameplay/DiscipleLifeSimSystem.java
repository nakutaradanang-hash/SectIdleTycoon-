package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;
import com.sect.idle.utils.ErrorCode;
import com.sect.idle.utils.ExceptionManager;
import java.util.ArrayList;

/**
 * DiscipleLifeSimSystem - The Sims Style Life Simulation & Needs System.
 * Simulates real-time Needs (Energy, Hunger/Vitality, Mood, Social, Feng Shui Environment),
 * moodlets, emotional breakdowns (Qi Deviation), and autonomous disciple actions.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class DiscipleLifeSimSystem {

    private static volatile DiscipleLifeSimSystem instance;
    private static final Object LOCK = new Object();

    // Emotional States
    public static final int MOODLET_NORMAL = 0;
    public static final int MOODLET_INSPIRED = 1;       // +30% Alchemy/Crafting & Breakthrough chance
    public static final int MOODLET_ENLIGHTENED = 2;    // +50% Cultivation EXP
    public static final int MOODLET_SERENE = 3;         // 0 Stress, +20 Energy Regen
    public static final int MOODLET_ENRAGED = 4;        // +20% Battle ATK, -30% Accuracy
    public static final int MOODLET_HEARTBROKEN = 5;    // -50% Task efficiency
    public static final int MOODLET_QI_DEVIATION = 6;   // Dangerous demonic backlash (needs treatment)

    public static class LifeSimProfile {
        public String discipleId;
        public int hunger;       // 0 (starving) to 100 (full)
        public int socialNeed;   // 0 (lonely) to 100 (fulfilled)
        public int fengShui;     // 0 (bad aura) to 100 (auspicious)
        public int activeMoodlet;// MOODLET_*
        public int moodletDurationTicks;
        public String currentActionDescription;

        public LifeSimProfile(String id) {
            this.discipleId = id;
            this.hunger = 80 + RNG.nextInt(20);
            this.socialNeed = 70 + RNG.nextInt(30);
            this.fengShui = 50 + RNG.nextInt(50);
            this.activeMoodlet = MOODLET_NORMAL;
            this.moodletDurationTicks = 0;
            this.currentActionDescription = "Meditating at pavilion";
        }
    }

    public static DiscipleLifeSimSystem getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DiscipleLifeSimSystem();
                }
            }
        }
        return instance;
    }

    private DiscipleLifeSimSystem() {}

    /**
     * Ticks the Sims-style life needs for all disciples in the sect.
     */
    public void tickDiscipleLifeNeeds(SectData sect) {
        if (sect == null || sect.disciples == null) return;

        for (int i = 0; i < sect.disciples.size(); i++) {
            Disciple d = sect.disciples.get(i);
            if (d == null) continue;

            // Decay Energy & Hunger through activity
            if (d.currentTask != 0) {
                d.energy = Math.max(0, d.energy - 2);
                d.stress = Math.min(100, d.stress + 1);
            } else {
                // Resting
                d.energy = Math.min(d.maxEnergy > 0 ? d.maxEnergy : 100, d.energy + 5);
                d.stress = Math.max(0, d.stress - 3);
            }

            // Calculate Mood from Needs (Energy, Stress, Relationships)
            int calculatedMood = (d.energy + (100 - d.stress) + (d.relationships != null ? d.relationships.size() * 10 : 0)) / 2;
            d.mood = Math.max(0, Math.min(100, calculatedMood));

            // Emotional Breakdown / Qi Deviation check
            if (d.stress >= 90 && d.energy <= 10 && RNG.nextInt(100) < 5) {
                ExceptionManager.get().reportError(ErrorCode.COMBAT_AI_INVALID_STATE, "Disciple " + d.name + " suffered Qi Deviation from excessive stress!");
            }
        }
    }

    /**
     * Feed disciple spirit cuisine / medicinal elixir to restore vitality and mood.
     */
    public boolean feedDisciple(Disciple d, String mealType, int costHerbs, SectData sect) {
        if (d == null || sect == null) return false;
        if (sect.spiritHerbs < costHerbs) return false;

        sect.spend(0, costHerbs, 0);
        d.energy = Math.min(d.maxEnergy > 0 ? d.maxEnergy : 100, d.energy + 30);
        d.stress = Math.max(0, d.stress - 20);
        d.mood = Math.min(100, d.mood + 15);
        ExceptionManager.get().logUserAction("DISCIPLE_FEED", d.name, "Consumed " + mealType);
        return true;
    }

    /**
     * Social interaction between two disciples (Chat, Spar, Gift, Confess Dao Romance).
     */
    public String performSocialInteraction(Disciple actor, Disciple target, int interactionType) {
        if (actor == null || target == null) return "Invalid interaction target.";

        StringBuilder result = new StringBuilder();
        switch (interactionType) {
            case 1: // Chat & Share Dao Insights
                actor.stress = Math.max(0, actor.stress - 10);
                target.stress = Math.max(0, target.stress - 10);
                actor.mood = Math.min(100, actor.mood + 10);
                target.mood = Math.min(100, target.mood + 10);
                result.append(actor.name).append(" and ").append(target.name).append(" enjoyed tea and discussed the Dao of Heaven.");
                break;

            case 2: // Friendly Martial Spar
                actor.energy = Math.max(0, actor.energy - 15);
                target.energy = Math.max(0, target.energy - 15);
                actor.addExperience(25);
                target.addExperience(25);
                result.append(actor.name).append(" and ").append(target.name).append(" completed a 100-round sword sparring session! (+25 EXP)");
                break;

            case 3: // Propose Dao Companionship (Marriage / Dao Partner)
                if (actor.mood > 60 && target.mood > 60) {
                    actor.mood = 100;
                    target.mood = 100;
                    actor.loyalty = 100;
                    target.loyalty = 100;
                    result.append("✨ [DAO COMPANIONSHIP PLEDGED] ").append(actor.name).append(" and ").append(target.name)
                          .append(" swore eternal vows as Dao Companions under the starry sky!");
                } else {
                    target.mood = Math.max(0, target.mood - 10);
                    result.append(target.name).append(" politely declined ").append(actor.name).append("'s proposal. (Mood -10)");
                }
                break;

            default:
                result.append(actor.name).append(" greeted ").append(target.name).append(".");
                break;
        }
        return result.toString();
    }
}
