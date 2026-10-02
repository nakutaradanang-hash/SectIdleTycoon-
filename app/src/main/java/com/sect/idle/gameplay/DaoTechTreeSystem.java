package com.sect.idle.gameplay;

import com.sect.idle.utils.ErrorCode;
import com.sect.idle.utils.ExceptionManager;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * DaoTechTreeSystem - Civilization Style Tech / Dao Research Tree.
 * Allows unlocking ancient techniques, formation blueprints, economic institutions,
 * and military doctrines across 4 major Eras.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class DaoTechTreeSystem {

    private static volatile DaoTechTreeSystem instance;
    private static final Object LOCK = new Object();

    // Eras
    public static final int ERA_FOUNDATION = 1;
    public static final int ERA_GOLDEN_CORE = 2;
    public static final int ERA_VOID_ASCENSION = 3;
    public static final int ERA_CELESTIAL_DAO = 4;

    public static class DaoTech {
        public String id;
        public String name;
        public String description;
        public int era;
        public long requiredResearchPoints;
        public long currentProgress;
        public boolean unlocked;
        public String prerequisiteTechId;
        public String bonusDescription;

        public DaoTech(String id, String name, String description, int era, long reqPoints, String prereqId, String bonusDesc) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.era = era;
            this.requiredResearchPoints = reqPoints;
            this.currentProgress = 0L;
            this.unlocked = false;
            this.prerequisiteTechId = prereqId;
            this.bonusDescription = bonusDesc;
        }

        public boolean isResearched() {
            return unlocked;
        }
    }

    public final ArrayList<DaoTech> techList;
    private final HashMap<String, DaoTech> techMap;
    public String currentResearchTechId = null;

    public static DaoTechTreeSystem getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DaoTechTreeSystem();
                }
            }
        }
        return instance;
    }

    private DaoTechTreeSystem() {
        this.techList = new ArrayList<DaoTech>(12);
        this.techMap = new HashMap<String, DaoTech>(16);
        initTechTree();
    }

    private void initTechTree() {
        techList.clear();
        techMap.clear();

        // Era 1: Foundation Era
        registerTech(new DaoTech("TECH_BREATHING", "Qi Condensation Breathing", "Standardized breathing techniques that boost disciple cultivation speed.", ERA_FOUNDATION, 100L, null, "+20% Cultivation EXP gain"));
        registerTech(new DaoTech("TECH_HERB_FARMING", "Spiritual Horticulture", "Advanced irrigation and soil enrichment for the spirit garden.", ERA_FOUNDATION, 200L, "TECH_BREATHING", "+30% Spirit Herb harvest"));
        registerTech(new DaoTech("TECH_MINE_REFINING", "Spirit Ore Smelting", "High temperature bellows that extract purer minerals from mine veins.", ERA_FOUNDATION, 250L, "TECH_BREATHING", "+30% Spirit Ore harvest"));

        // Era 2: Golden Core Era
        registerTech(new DaoTech("TECH_FLYING_SWORD", "Flying Sword Formations", "Sword telekinesis arrays that strike enemies from afar.", ERA_GOLDEN_CORE, 600L, "TECH_MINE_REFINING", "Unlocks Flying Sword Arrays & +25% Battle ATK"));
        registerTech(new DaoTech("TECH_ALCHEMY_STILLS", "Grand Pill Cauldrons", "Multi-chambered alchemy cauldrons for top-grade pill synthesis.", ERA_GOLDEN_CORE, 800L, "TECH_HERB_FARMING", "+50% Pill output & Unlocks Tier 3 Elixirs"));
        registerTech(new DaoTech("TECH_SECT_COMMERCE", "Silk Road Guild Hall", "Establish licensed merchant guilds across neighboring mortal empires.", ERA_GOLDEN_CORE, 900L, "TECH_BREATHING", "+40% Daily Spirit Stone Income"));

        // Era 3: Void Ascension Era
        registerTech(new DaoTech("TECH_VOID_ARRAY", "Spatial Void Gateways", "Dimensional folding formations that connect distant realm outposts instantaneously.", ERA_VOID_ASCENSION, 2000L, "TECH_FLYING_SWORD", "+50% Hex Grid Harvest Yield"));
        registerTech(new DaoTech("TECH_CELESTIAL_WARD", "Myriad Thunder Ward", "A sect-wide protective dome powered by celestial lightning.", ERA_VOID_ASCENSION, 2500L, "TECH_ALCHEMY_STILLS", "Increases Sect Defense by 5000"));

        // Era 4: Celestial Dao Era
        registerTech(new DaoTech("TECH_IMMORTAL_ASCENSION", "Ascension to Godhood", "Transcend mortality and construct the Path of Nine Heavens.", ERA_CELESTIAL_DAO, 6000L, "TECH_VOID_ARRAY", "Triggers Celestial Ascension Victory Condition"));

        // Auto unlock the first foundation tech
        unlockTechDirectly("TECH_BREATHING");
        currentResearchTechId = "TECH_HERB_FARMING";
    }

    private void registerTech(DaoTech t) {
        techList.add(t);
        techMap.put(t.id, t);
    }

    public DaoTech getTech(String id) {
        return techMap.get(id);
    }

    public void unlockTechDirectly(String techId) {
        DaoTech tech = getTech(techId);
        if (tech != null) {
            tech.unlocked = true;
            tech.currentProgress = tech.requiredResearchPoints;
        }
    }

    /**
     * Sets active research technology.
     */
    public boolean selectResearch(String techId) {
        DaoTech tech = getTech(techId);
        if (tech == null || tech.unlocked) return false;

        if (tech.prerequisiteTechId != null) {
            DaoTech prereq = getTech(tech.prerequisiteTechId);
            if (prereq == null || !prereq.unlocked) {
                return false; // Prerequisite not researched
            }
        }
        this.currentResearchTechId = techId;
        ExceptionManager.get().logUserAction("TECH_RESEARCH_SELECT", tech.name, "Researching");
        return true;
    }

    /**
     * Ticks research points generated by library buildings and disciple studies.
     */
    public boolean addResearchProgress(long points) {
        if (currentResearchTechId == null || points <= 0) return false;
        DaoTech current = getTech(currentResearchTechId);
        if (current == null || current.unlocked) return false;

        current.currentProgress += points;
        if (current.currentProgress >= current.requiredResearchPoints) {
            current.currentProgress = current.requiredResearchPoints;
            current.unlocked = true;
            ExceptionManager.get().logUserAction("TECH_UNLOCKED", current.name, "Discovered Technology: " + current.name);
            return true;
        }
        return false;
    }

    public int getUnlockedTechCount() {
        int count = 0;
        for (int i = 0; i < techList.size(); i++) {
            if (techList.get(i).unlocked) count++;
        }
        return count;
    }
}
