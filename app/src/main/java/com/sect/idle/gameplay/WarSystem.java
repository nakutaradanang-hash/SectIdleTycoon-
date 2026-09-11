package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * WarSystem - Manages sect territory conquests, defensive wars against rival sects,
 * demonic horde invasions, and generates comprehensive round-by-round battle logs
 * (Critical Strike, Damage Dealt, Status Effects, Qi Barrier).
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class WarSystem {
    private static volatile WarSystem instance;
    private static final Object LOCK = new Object();

    public static WarSystem getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new WarSystem();
                }
            }
        }
        return instance;
    }

    public static class WarBattleLogEntry {
        public int round;
        public String tag;       // "Critical Strike", "Damage Dealt", "Status Effects", "Qi Barrier", "Decisive Strike"
        public String actor;
        public String target;
        public int damage;
        public String detail;
        public int tagColor;     // ARGB Color int

        public WarBattleLogEntry(int round, String tag, String actor, String target, int damage, String detail, int tagColor) {
            this.round = round;
            this.tag = tag;
            this.actor = actor;
            this.target = target;
            this.damage = damage;
            this.detail = detail;
            this.tagColor = tagColor;
        }

        public String getFormattedLog() {
            StringBuilder sb = new StringBuilder();
            sb.append("[Round ").append(round).append("] [").append(tag).append("] ");
            if (actor != null && target != null && !actor.isEmpty()) {
                sb.append(actor).append(" ➔ ").append(target).append(": ");
            }
            sb.append(detail);
            return sb.toString();
        }
    }

    public static class WarResult {
        public boolean won;
        public int repChange;
        public int stoneReward;
        public int herbReward;
        public int oreReward;
        public int jadeReward;
        public String targetSectName;
        public String message;
        public int sectPowerTotal;
        public int enemyPowerTotal;
        public final ArrayList<WarBattleLogEntry> roundLogs = new ArrayList<WarBattleLogEntry>();
    }

    public static final WarResult RESULT_POOL = new WarResult();

    public static final String[] RIVAL_SECTS = {
        "Blood Demon Sect",
        "Nine Nether Palace",
        "Celestial Sword Sect",
        "Thunder Cloud Pavilion",
        "Asura Valley",
        "Myriad Beast Manor"
    };

    private static final String[] ATTACK_SKILLS = {
        "Nine Heavens Sword Ray", "Celestial Thunder Palm", "Void Rifting Slash",
        "Solar Phoenix Flame", "Dragon Subduing Strike", "Asura Blood Severing Edge",
        "Absolute Zero Frost Wave", "Primordial Chaos Beam"
    };

    private static final String[] STATUS_EFFECTS = {
        "Demonic Qi Shackles (DEF -30%)", "Soul Burn (Continuous Fire Damage)",
        "Spiritual Stun (Action Delayed)", "Poison Mist Infliction (ATK -25%)",
        "Frostbite Slow (SPD -40%)", "Qi Deviation Weakness"
    };

    public WarResult launchSectCampaign(SectData s, int difficulty) {
        RESULT_POOL.roundLogs.clear();
        if (s == null) {
            RESULT_POOL.won = false;
            RESULT_POOL.message = "No sect data.";
            return RESULT_POOL;
        }

        String rival = RIVAL_SECTS[RNG.nextInt(RIVAL_SECTS.length)];
        RESULT_POOL.targetSectName = rival;

        int totalSectPower = 0;
        ArrayList<Disciple> activeFighters = new ArrayList<Disciple>();
        for (int i = 0; i < s.disciples.size(); i++) {
            Disciple d = s.disciples.get(i);
            if (d != null && d.isAlive()) {
                totalSectPower += (int) d.getPowerRating();
                activeFighters.add(d);
            }
        }

        if (activeFighters.isEmpty()) {
            Disciple elder = new Disciple("Sect Elder");
            elder.initStats(40, 35, 30, 25, 35, 30, 20);
            elder.recalcCombat();
            activeFighters.add(elder);
            totalSectPower += (int) elder.getPowerRating();
        }

        int enemyPower = 450 + (difficulty * 650) + (s.sectRealm * 320) + RNG.nextInt(-80, 180);
        boolean victory = totalSectPower >= enemyPower;
        RESULT_POOL.won = victory;
        RESULT_POOL.sectPowerTotal = totalSectPower;
        RESULT_POOL.enemyPowerTotal = enemyPower;

        // =========================================================================
        // GENERATE ROUND-BY-ROUND COMBAT SIMULATION & DETAILED BATTLE LOGS
        // =========================================================================
        int numRounds = RNG.nextInt(4, 6);
        String rivalLeader = rival + " Elder " + RNG.pick(new String[]{"Mo Xie", "Xuan Tian", "Yin Sha", "Kui Ba", "Feng Hen"});

        for (int round = 1; round <= numRounds; round++) {
            Disciple hero = activeFighters.get(RNG.nextInt(activeFighters.size()));
            String skill = RNG.pick(ATTACK_SKILLS);

            // 1. Damage Dealt Event (Our turn)
            int baseHeroDmg = (int) (hero.atk * (1.2f + (round * 0.15f)) + RNG.nextInt(80, 250));
            boolean isHeroCrit = RNG.chance(25 + hero.lck / 3);

            if (isHeroCrit) {
                int critDmg = (int) (baseHeroDmg * 2.2f + RNG.nextInt(150, 400));
                RESULT_POOL.roundLogs.add(new WarBattleLogEntry(
                    round,
                    "Critical Strike",
                    hero.name,
                    rivalLeader,
                    critDmg,
                    "Channels Dao Qi into '" + skill + "' landing a DEVASTATING CRITICAL STRIKE for " + critDmg + " Holy Damage!",
                    0xFFFFD700 // Gold
                ));
            } else {
                RESULT_POOL.roundLogs.add(new WarBattleLogEntry(
                    round,
                    "Damage Dealt",
                    hero.name,
                    rivalLeader,
                    baseHeroDmg,
                    "Executes '" + skill + "' dealing " + baseHeroDmg + " elemental damage!",
                    0xFFFF5252 // Red / Coral
                ));
            }

            // 2. Status Effects or Qi Barrier
            if (RNG.chance(45)) {
                String status = RNG.pick(STATUS_EFFECTS);
                RESULT_POOL.roundLogs.add(new WarBattleLogEntry(
                    round,
                    "Status Effects",
                    hero.name,
                    rivalLeader,
                    0,
                    "Afflicted target with " + status + "!",
                    0xFFE040FB // Purple / Magenta
                ));
            } else if (RNG.chance(40)) {
                int barrierAbsorb = (int) (hero.def * 1.8f + 120);
                RESULT_POOL.roundLogs.add(new WarBattleLogEntry(
                    round,
                    "Qi Barrier",
                    hero.name,
                    "Sect Vanguard",
                    barrierAbsorb,
                    "Erects 'Nine Heavens Spirit Shield' absorbing " + barrierAbsorb + " incoming demonic backlash!",
                    0xFF00E5FF // Cyan
                ));
            }

            // 3. Enemy Retaliation Damage Dealt Event
            int enemyDmg = (int) ((enemyPower / (numRounds * 1.5f)) + RNG.nextInt(60, 200));
            boolean isEnemyCrit = RNG.chance(18);
            if (isEnemyCrit) {
                int enemyCritDmg = (int) (enemyDmg * 1.8f);
                RESULT_POOL.roundLogs.add(new WarBattleLogEntry(
                    round,
                    "Critical Strike",
                    rivalLeader,
                    hero.name,
                    enemyCritDmg,
                    "Unleashes Blood Frenzy counter-strike for " + enemyCritDmg + " critical damage!",
                    0xFFFF9100 // Orange-Red
                ));
            } else {
                RESULT_POOL.roundLogs.add(new WarBattleLogEntry(
                    round,
                    "Damage Dealt",
                    rivalLeader,
                    hero.name,
                    enemyDmg,
                    "Strikes back with Demonic Soul Curse dealing " + enemyDmg + " damage!",
                    0xFFFF7043 // Deep Orange
                ));
            }
        }

        // Final Decisive Round Entry
        if (victory) {
            RESULT_POOL.roundLogs.add(new WarBattleLogEntry(
                numRounds,
                "Decisive Strike",
                "Grand Sect Formation",
                rivalLeader,
                totalSectPower * 2,
                "Sect Array activates Holy Dragon Beam! Shattered " + rival + "'s mountain barrier into dust!",
                0xFF4CAF50 // Green
            ));
        } else {
            RESULT_POOL.roundLogs.add(new WarBattleLogEntry(
                numRounds,
                "Status Effects",
                rivalLeader,
                "Sect Forces",
                0,
                "Demonic Qi overwhelming! Sect commanders ordered a tactical retreat to preserve disciple foundations.",
                0xFFFF5252 // Red
            ));
        }

        // Rewards and economy updates
        if (victory) {
            RESULT_POOL.repChange = 60 + (difficulty * 30);
            RESULT_POOL.stoneReward = 600 * (difficulty + 1);
            RESULT_POOL.herbReward = 120 * (difficulty + 1);
            RESULT_POOL.oreReward = 60 * (difficulty + 1);
            RESULT_POOL.jadeReward = 18 * (difficulty + 1);

            s.earn(RESULT_POOL.stoneReward, RESULT_POOL.herbReward, RESULT_POOL.oreReward);
            s.jade += RESULT_POOL.jadeReward;
            s.sectRealmExp += 35 * (difficulty + 1);
            s.sectRank = Math.min(100, s.sectRank + 1);
            RESULT_POOL.message = "Conquest Victory! Plundered " + RESULT_POOL.stoneReward + " Spirit Stones, "
                    + RESULT_POOL.herbReward + " Herbs, and " + RESULT_POOL.jadeReward + " Immortal Jade.";
        } else {
            RESULT_POOL.repChange = -15;
            RESULT_POOL.stoneReward = 0;
            RESULT_POOL.herbReward = 0;
            RESULT_POOL.oreReward = 0;
            RESULT_POOL.jadeReward = 0;
            RESULT_POOL.message = "War campaign halted. Demonic forces resisted our advance. Disciples safely retreated.";
        }

        // Apply disciple battle stats
        for (int i = 0; i < activeFighters.size(); i++) {
            Disciple d = activeFighters.get(i);
            if (d != null && d.isAlive()) {
                d.reputation = Math.max(0, Math.min(10000, d.reputation + RESULT_POOL.repChange));
                d.totalBattles++;
                if (RESULT_POOL.won) {
                    d.battlesWon++;
                    d.totalBattlesWon++;
                    d.addExperience(90 * (difficulty + 1));
                } else {
                    d.addExperience(25);
                }
            }
        }

        s.recalculateEconomy();
        GameplayFeedbackDispatcher.getInstance().onWarCampaignCompleted(RESULT_POOL);

        if (victory && !activeFighters.isEmpty()) {
            Disciple leader = activeFighters.get(0);
            if (leader != null) {
                int critDmg = (int) (leader.getPowerRating() * 3.5f + RNG.nextInt(100, 500));
                GameplayFeedbackDispatcher.getInstance().onCriticalStrike(
                    leader.position.x, leader.position.y, leader.name, rivalLeader, critDmg, RESULT_POOL.repChange, true);
            }
        }

        return RESULT_POOL;
    }
}
