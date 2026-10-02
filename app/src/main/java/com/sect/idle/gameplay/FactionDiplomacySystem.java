package com.sect.idle.gameplay;

import com.sect.idle.systems.RNG;
import com.sect.idle.utils.ErrorCode;
import com.sect.idle.utils.ExceptionManager;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * FactionDiplomacySystem - Romance of the Three Kingdoms Style Grand Strategy Diplomacy.
 * Manages inter-sect relations, pacts, espionage, alliances, tributes, false flags, and grand coalitions.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class FactionDiplomacySystem {

    private static volatile FactionDiplomacySystem instance;
    private static final Object LOCK = new Object();

    // Relation Status Constants
    public static final int RELATION_AT_WAR = 0;
    public static final int RELATION_HOSTILE = 1;
    public static final int RELATION_NEUTRAL = 2;
    public static final int RELATION_FRIENDLY = 3;
    public static final int RELATION_ALLIED = 4;
    public static final int RELATION_VASSAL = 5;

    // Stratagem Types
    public static final int STRATAGEM_INFILTRATE_SPY = 1;
    public static final int STRATAGEM_FALSE_FLAG_SLANDER = 2;
    public static final int STRATAGEM_BRIBE_OFFICER = 3;
    public static final int STRATAGEM_SEND_TRIBUTE = 4;
    public static final int STRATAGEM_FORM_COALITION = 5;
    public static final int STRATAGEM_TRADE_PACT = 6;
    public static final int STRATAGEM_NON_AGGRESSION = 7;

    public static class RivalFaction {
        public String id;
        public String name;
        public String leaderName;
        public int relationStatus; // 0..5
        public int favorScore;     // -100 to +100
        public long militaryPower;
        public long treasuryStones;
        public int territoryCount;
        public boolean nonAggressionPact;
        public int pactRemainingTurns;
        public int tradePactTurns;
        public boolean isVassal;
        public boolean inCoalitionAgainstPlayer;

        public RivalFaction(String id, String name, String leaderName, int initialFavor, long power, int territories) {
            this.id = id;
            this.name = name;
            this.leaderName = leaderName;
            this.favorScore = initialFavor;
            this.militaryPower = power;
            this.territoryCount = territories;
            this.treasuryStones = 5000L + RNG.nextInt(10000);
            updateRelationStatus();
        }

        public void updateRelationStatus() {
            if (isVassal) {
                relationStatus = RELATION_VASSAL;
            } else if (favorScore <= -60) {
                relationStatus = RELATION_AT_WAR;
            } else if (favorScore <= -20) {
                relationStatus = RELATION_HOSTILE;
            } else if (favorScore < 40) {
                relationStatus = RELATION_NEUTRAL;
            } else if (favorScore < 80) {
                relationStatus = RELATION_FRIENDLY;
            } else {
                relationStatus = RELATION_ALLIED;
            }
        }
    }

    public static class DiplomaticActionResult {
        public boolean success;
        public String stratagemName;
        public String targetFaction;
        public String report;
        public int favorDelta;
        public int stoneCost;

        public DiplomaticActionResult(boolean success, String stratagemName, String targetFaction, String report, int favorDelta, int stoneCost) {
            this.success = success;
            this.stratagemName = stratagemName;
            this.targetFaction = targetFaction;
            this.report = report;
            this.favorDelta = favorDelta;
            this.stoneCost = stoneCost;
        }
    }

    public final ArrayList<RivalFaction> factions;
    private final HashMap<String, RivalFaction> factionMap;

    public static FactionDiplomacySystem getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new FactionDiplomacySystem();
                }
            }
        }
        return instance;
    }

    private FactionDiplomacySystem() {
        this.factions = new ArrayList<RivalFaction>(6);
        this.factionMap = new HashMap<String, RivalFaction>(8);
        initDefaultFactions();
    }

    private void initDefaultFactions() {
        factions.clear();
        factionMap.clear();

        addFaction(new RivalFaction("FAC_WUDANG", "Wudang Mountain Sect", "Grandmaster Zhang", 20, 12000L, 4));
        addFaction(new RivalFaction("FAC_SHAOLIN", "Shaolin Dharma Temple", "Abbot Xuan Ci", 15, 15000L, 5));
        addFaction(new RivalFaction("FAC_DEMON", "Blood Demon Sanctuary", "Lord Yanluo", -70, 18000L, 6));
        addFaction(new RivalFaction("FAC_HEAVEN", "Heavenly Sword Pavilion", "Sword Saint Ye", 0, 14000L, 4));
        addFaction(new RivalFaction("FAC_KUNLUN", "Kunlun Jade Mountain", "Matriarch Ling", 30, 9000L, 3));
        addFaction(new RivalFaction("FAC_ASURA", "Asura Blood Valley", "General Ba", -50, 11000L, 3));
    }

    private void addFaction(RivalFaction f) {
        factions.add(f);
        factionMap.put(f.id, f);
    }

    public RivalFaction getFaction(String id) {
        return factionMap.get(id);
    }

    /**
     * Executes a Three Kingdoms style diplomatic stratagem.
     */
    public DiplomaticActionResult executeStratagem(SectData sect, String factionId, int stratagemType, int investmentStones) {
        RivalFaction target = getFaction(factionId);
        if (target == null) {
            return new DiplomaticActionResult(false, "Unknown", "Unknown", "Target faction does not exist.", 0, 0);
        }

        if (sect != null && sect.spiritStones < investmentStones) {
            return new DiplomaticActionResult(false, "Insufficient Funds", target.name, "Not enough spirit stones to execute stratagem.", 0, 0);
        }

        if (sect != null && investmentStones > 0) {
            sect.spend(investmentStones, 0, 0);
        }

        switch (stratagemType) {
            case STRATAGEM_SEND_TRIBUTE: {
                int favorGain = Math.min(30, 5 + (investmentStones / 500));
                target.favorScore = Math.min(100, target.favorScore + favorGain);
                target.updateRelationStatus();
                ExceptionManager.get().logUserAction("DIPLOMACY_TRIBUTE", target.name, "Favor +" + favorGain);
                return new DiplomaticActionResult(true, "Send Tribute", target.name,
                        "Sent " + investmentStones + " Spirit Stones. " + target.leaderName + " expressed satisfaction. Favor +" + favorGain,
                        favorGain, investmentStones);
            }

            case STRATAGEM_NON_AGGRESSION: {
                if (target.favorScore < 0) {
                    return new DiplomaticActionResult(false, "Non-Aggression Pact", target.name,
                            target.name + " rejected the pact due to poor relations.", 0, investmentStones);
                }
                target.nonAggressionPact = true;
                target.pactRemainingTurns = 10;
                target.favorScore = Math.min(100, target.favorScore + 10);
                target.updateRelationStatus();
                return new DiplomaticActionResult(true, "Non-Aggression Pact", target.name,
                        "Signed 10-turn non-aggression pact with " + target.name + ".", 10, investmentStones);
            }

            case STRATAGEM_TRADE_PACT: {
                target.tradePactTurns = 12;
                target.favorScore = Math.min(100, target.favorScore + 15);
                target.updateRelationStatus();
                return new DiplomaticActionResult(true, "Trade Agreement", target.name,
                        "Established bilateral trade route. Generates +20% market income for 12 turns.", 15, investmentStones);
            }

            case STRATAGEM_INFILTRATE_SPY: {
                boolean spySuccess = RNG.nextInt(100) < 70;
                if (spySuccess) {
                    return new DiplomaticActionResult(true, "Espionage Infiltration", target.name,
                            "Spy successfully embedded! Intel reveals: Military Power " + target.militaryPower + ", Treasury: " + target.treasuryStones + " Stones.", 0, investmentStones);
                } else {
                    target.favorScore = Math.max(-100, target.favorScore - 25);
                    target.updateRelationStatus();
                    return new DiplomaticActionResult(false, "Espionage Compromised", target.name,
                            "Spy was captured! " + target.leaderName + " is outraged. Favor -25.", -25, investmentStones);
                }
            }

            case STRATAGEM_FALSE_FLAG_SLANDER: {
                boolean slanderSuccess = RNG.nextInt(100) < 60;
                if (slanderSuccess) {
                    target.militaryPower = Math.max(1000L, target.militaryPower - 2000L);
                    return new DiplomaticActionResult(true, "False Flag Slander", target.name,
                            "Fabricated internal discord within " + target.name + ". Their military morale dropped by 2000!", 0, investmentStones);
                } else {
                    target.favorScore = Math.max(-100, target.favorScore - 30);
                    target.updateRelationStatus();
                    return new DiplomaticActionResult(false, "Slander Exposed", target.name,
                            "Your agents were unmasked. " + target.name + " declared you a treacherous enemy! Favor -30.", -30, investmentStones);
                }
            }

            case STRATAGEM_FORM_COALITION: {
                if (target.favorScore >= 50) {
                    target.isVassal = false;
                    target.favorScore = 100;
                    target.relationStatus = RELATION_ALLIED;
                    return new DiplomaticActionResult(true, "Grand Alliance Coalition", target.name,
                            target.name + " entered into an eternal defensive pact against the Demonic Factions!", 30, investmentStones);
                } else {
                    return new DiplomaticActionResult(false, "Coalition Rejected", target.name,
                            target.leaderName + " refused your invitation. Requires at least 50 Favor.", 0, investmentStones);
                }
            }

            default:
                return new DiplomaticActionResult(false, "Unknown Stratagem", target.name, "Invalid stratagem command.", 0, 0);
        }
    }

    /**
     * Advances diplomatic turns (decay pacts, tick trade bonuses, simulate AI faction wars).
     */
    public void advanceTurn(SectData sect) {
        for (int i = 0; i < factions.size(); i++) {
            RivalFaction f = factions.get(i);
            if (f != null) {
                if (f.pactRemainingTurns > 0) {
                    f.pactRemainingTurns--;
                    if (f.pactRemainingTurns == 0) {
                        f.nonAggressionPact = false;
                    }
                }
                if (f.tradePactTurns > 0) {
                    f.tradePactTurns--;
                    if (sect != null) {
                        sect.earn(150, 20, 10);
                    }
                }
                // Small dynamic power growth
                f.militaryPower += RNG.nextInt(100);
            }
        }
    }
}
