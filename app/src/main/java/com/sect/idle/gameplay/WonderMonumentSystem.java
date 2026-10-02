package com.sect.idle.gameplay;

import com.sect.idle.utils.ErrorCode;
import com.sect.idle.utils.ExceptionManager;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * WonderMonumentSystem - Civilization Style Wonders of the World & Victory Conditions.
 * Allows construction of grand ancient monuments, unlocking empire-wide miracles,
 * and triggering Civilization victory conditions (Conquest, Ascension, Cultural, Economic).
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class WonderMonumentSystem {

    private static volatile WonderMonumentSystem instance;
    private static final Object LOCK = new Object();

    // Victory Types
    public static final int VICTORY_NONE = 0;
    public static final int VICTORY_MARTIAL_CONQUEST = 1;  // Conquered all 6 rival factions
    public static final int VICTORY_CELESTIAL_ASCENSION = 2;// Built Tower of 9 Heavens & Researched Godhood
    public static final int VICTORY_CULTURAL_ENLIGHTENMENT = 3;// 10,000 Dao Insight & Grand Lotus Monument
    public static final int VICTORY_ECONOMIC_JADE = 4;     // 1,000,000 Spirit Stones & Celestial Bank Wonder

    public static class AncientWonder {
        public String id;
        public String name;
        public String description;
        public long costStones;
        public long costOres;
        public long costJade;
        public int constructionTurnsRequired;
        public int constructionTurnsCompleted;
        public boolean isCompleted;
        public String globalBuffDescription;

        public AncientWonder(String id, String name, String desc, long stones, long ores, long jade, int turns, String buffDesc) {
            this.id = id;
            this.name = name;
            this.description = desc;
            this.costStones = stones;
            this.costOres = ores;
            this.costJade = jade;
            this.constructionTurnsRequired = turns;
            this.constructionTurnsCompleted = 0;
            this.isCompleted = false;
            this.globalBuffDescription = buffDesc;
        }

        public float getProgressFraction() {
            if (constructionTurnsRequired == 0) return 1.0f;
            return (float) constructionTurnsCompleted / constructionTurnsRequired;
        }
    }

    public final ArrayList<AncientWonder> wonders;
    private final HashMap<String, AncientWonder> wonderMap;
    public String currentUnderConstructionId = null;

    public static WonderMonumentSystem getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new WonderMonumentSystem();
                }
            }
        }
        return instance;
    }

    private WonderMonumentSystem() {
        this.wonders = new ArrayList<AncientWonder>(4);
        this.wonderMap = new HashMap<String, AncientWonder>(8);
        initWonders();
    }

    private void initWonders() {
        wonders.clear();
        wonderMap.clear();

        registerWonder(new AncientWonder("WONDER_NINE_HEAVENS", "Tower of Nine Heavens", "A 10,000-meter celestial pillar connecting the mortal world to the Immortal Realm.", 50000L, 20000L, 500L, 20, "Enables Celestial Ascension Victory & +100% Cultivation EXP"));
        registerWonder(new AncientWonder("WONDER_DRAGON_SPRING", "Primordial Dragon Jade Spring", "The crystallized eye of an ancient Azure Dragon flowing with liquid essence.", 30000L, 15000L, 300L, 15, "Grants +50 Lifespan to all disciples and +200 Max HP"));
        registerWonder(new AncientWonder("WONDER_GREAT_ARRAY", "Great Wall of Dao Formations", "A defensive ring of 108 levitating mountain peaks guarding the sect perimeter.", 40000L, 35000L, 200L, 18, "Immunity to rival surprise invasions and +50% War DEF"));
        registerWonder(new AncientWonder("WONDER_LOTUS_PAGODA", "Grand Lotus Enlightenment Pagoda", "A sacred 100-petaled jade pagoda transmitting infinite Daoist wisdom.", 35000L, 10000L, 400L, 16, "Enables Cultural Enlightenment Victory & Triples Dao Insight"));
    }

    private void registerWonder(AncientWonder w) {
        wonders.add(w);
        wonderMap.put(w.id, w);
    }

    public AncientWonder getWonder(String id) {
        return wonderMap.get(id);
    }

    public boolean startConstruction(SectData sect, String wonderId) {
        AncientWonder wonder = getWonder(wonderId);
        if (wonder == null || wonder.isCompleted || currentUnderConstructionId != null) {
            return false;
        }

        if (sect != null && sect.spend(wonder.costStones, 0, wonder.costOres)) {
            if (sect.jade >= wonder.costJade) {
                sect.jade -= wonder.costJade;
                currentUnderConstructionId = wonderId;
                ExceptionManager.get().logUserAction("WONDER_START", wonder.name, "Commenced Construction");
                return true;
            } else {
                // Refund spent stones and ores
                sect.earn(wonder.costStones, 0, wonder.costOres);
            }
        }
        return false;
    }

    public boolean advanceConstructionTurn() {
        if (currentUnderConstructionId == null) return false;
        AncientWonder wonder = getWonder(currentUnderConstructionId);
        if (wonder == null || wonder.isCompleted) return false;

        wonder.constructionTurnsCompleted++;
        if (wonder.constructionTurnsCompleted >= wonder.constructionTurnsRequired) {
            wonder.isCompleted = true;
            currentUnderConstructionId = null;
            ExceptionManager.get().logUserAction("WONDER_COMPLETE", wonder.name, "Wonder Completed: " + wonder.name);
            return true;
        }
        return false;
    }

    /**
     * Checks if any 4X Victory Condition is met.
     */
    public int checkVictoryCondition(SectData sect, FactionDiplomacySystem diplomacy) {
        if (sect == null) return VICTORY_NONE;

        // 1. Celestial Ascension Victory
        AncientWonder tower = getWonder("WONDER_NINE_HEAVENS");
        if (tower != null && tower.isCompleted && DaoTechTreeSystem.getInstance().getTech("TECH_IMMORTAL_ASCENSION") != null && DaoTechTreeSystem.getInstance().getTech("TECH_IMMORTAL_ASCENSION").unlocked) {
            return VICTORY_CELESTIAL_ASCENSION;
        }

        // 2. Cultural Enlightenment Victory
        AncientWonder lotus = getWonder("WONDER_LOTUS_PAGODA");
        if (lotus != null && lotus.isCompleted && sect.sectExp >= 10000L) {
            return VICTORY_CULTURAL_ENLIGHTENMENT;
        }

        // 3. Economic Jade Monopoly Victory
        if (sect.spiritStones >= 1000000L && sect.jade >= 5000L) {
            return VICTORY_ECONOMIC_JADE;
        }

        // 4. Martial Conquest Victory
        if (diplomacy != null) {
            boolean allDefeatedOrVassal = true;
            for (int i = 0; i < diplomacy.factions.size(); i++) {
                FactionDiplomacySystem.RivalFaction f = diplomacy.factions.get(i);
                if (f != null && !f.isVassal && f.relationStatus != FactionDiplomacySystem.RELATION_ALLIED && f.militaryPower > 2000L) {
                    allDefeatedOrVassal = false;
                    break;
                }
            }
            if (allDefeatedOrVassal && diplomacy.factions.size() > 0) {
                return VICTORY_MARTIAL_CONQUEST;
            }
        }

        return VICTORY_NONE;
    }
}
