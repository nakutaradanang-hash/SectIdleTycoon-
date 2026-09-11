package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.core.GameTime;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;
import com.sect.idle.models.Item;
import com.sect.idle.models.Quest;
import java.util.ArrayList;

/**
 * SectData - Core singleton model holding all game data.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class SectData {
    private static SectData instance;

    public static synchronized SectData getInstance() {
        if (instance == null) {
            instance = new SectData();
        }
        return instance;
    }

    // Currency & Resources
    public String sectName = "Cloud Mist Sect";
    public long spiritStones = 1000;
    public long spiritHerbs = 100;
    public long spiritPills = 10;
    public long spiritOres = 50;
    public long jade = 50;
    public long essence = 20;
    public long sectQi = 100;
    public long sectPower = 1000;
    public long sectExp = 0;
    public int sectRealm = 0;
    public int sectRealmExp = 0;
    public int highestRealm = 0;
    public int sectRank = 1;
    public int maxDisciples = 10;
    public int mapProgression = 1;

    // Time
    public GameTime time;
    public boolean autoSave = true;
    private long lastSaveTime = 0;

    // Lists
    public final ArrayList<Disciple> disciples;
    public final ArrayList<Building> buildings;
    public final ArrayList<Item> inventory;
    public final ArrayList<Quest> activeQuests;

    // Economy cache
    public long dailyIncomeSS = 0;
    public long dailyExpenseSS = 0;
    public long netDailySS = 0;
    public long netProfit = 0;
    public boolean economyDirty = true;

    private SectData() {
        this.time = new GameTime();
        this.disciples = new ArrayList<Disciple>();
        this.buildings = new ArrayList<Building>();
        this.inventory = new ArrayList<Item>();
        this.activeQuests = new ArrayList<Quest>();
        initDefaultBuildings();
    }

    public void reset() {
        spiritStones = 1000;
        spiritHerbs = 100;
        spiritPills = 10;
        spiritOres = 50;
        jade = 50;
        essence = 20;
        sectPower = 1000;
        sectExp = 0;
        sectRealm = 0;
        sectRealmExp = 0;
        sectRank = 1;
        maxDisciples = 10;
        time.set(1, 1, 1, 6);
        disciples.clear();
        buildings.clear();
        inventory.clear();
        activeQuests.clear();
        initDefaultBuildings();
        recalculateEconomy();
        try {
            ResourceManager.getInstance().syncFromSectData(this);
        } catch (Exception ignored) {}
    }

    private void initDefaultBuildings() {
        // Main Hall
        Building mainHall = new Building(GameConfig.BUILD_HALL, "Main Hall", 10, 500L, 5);
        mainHall.posX = 2000; mainHall.posY = 2000;
        mainHall.build();
        buildings.add(mainHall);

        // Cultivation Chamber / Library
        Building library = new Building(GameConfig.BUILD_LIBRARY, "Library", 10, 300L, 4);
        library.posX = 2200; library.posY = 2000;
        buildings.add(library);

        // Herb Garden
        Building garden = new Building(GameConfig.BUILD_GARDEN, "Spirit Garden", 10, 200L, 6);
        garden.posX = 2000; garden.posY = 2200;
        buildings.add(garden);

        // Alchemy Pavilion
        Building alchemy = new Building(GameConfig.BUILD_ALCHEMY, "Alchemy Lab", 10, 400L, 3);
        alchemy.posX = 1800; alchemy.posY = 2000;
        buildings.add(alchemy);

        // Mine
        Building mine = new Building(GameConfig.BUILD_MINE, "Spirit Mine", 10, 350L, 6);
        mine.posX = 2000; mine.posY = 1800;
        buildings.add(mine);

        // Training Ground / Arena
        Building arena = new Building(GameConfig.BUILD_ARENA, "Martial Arena", 10, 600L, 8);
        arena.posX = 2200; arena.posY = 2200;
        buildings.add(arena);
    }

    public void addDisciple(Disciple d) {
        if (d != null) {
            disciples.add(d);
            markEconomyDirty();
        }
    }

    public void removeDisciple(Disciple d) {
        if (d != null) {
            disciples.remove(d);
            markEconomyDirty();
        }
    }

    public int getActiveTaskCount(int task) {
        int count = 0;
        for (int i = 0; i < disciples.size(); i++) {
            Disciple d = disciples.get(i);
            if (d != null && d.currentTask == task) {
                count++;
            }
        }
        return count;
    }

    public void earn(long stones, long herbs, long ores) {
        this.spiritStones += stones;
        this.spiritHerbs += herbs;
        this.spiritOres += ores;
        try {
            ResourceManager.getInstance().syncFromSectData(this);
        } catch (Exception ignored) {}
    }

    public boolean spend(long stones, long herbs, long ores) {
        if (this.spiritStones >= stones && this.spiritHerbs >= herbs && this.spiritOres >= ores) {
            this.spiritStones -= stones;
            this.spiritHerbs -= herbs;
            this.spiritOres -= ores;
            try {
                ResourceManager.getInstance().syncFromSectData(this);
            } catch (Exception ignored) {}
            return true;
        }
        return false;
    }

    public void markEconomyDirty() {
        this.economyDirty = true;
    }

    public void recalculateEconomy() {
        dailyIncomeSS = 0;
        dailyExpenseSS = 0;
        sectPower = 0;

        for (int i = 0; i < disciples.size(); i++) {
            Disciple d = disciples.get(i);
            if (d != null) {
                dailyIncomeSS += d.getTaskIncome();
                dailyExpenseSS += d.dailyWage;
                sectPower += (long) d.getPowerRating();
            }
        }

        for (int i = 0; i < buildings.size(); i++) {
            Building b = buildings.get(i);
            if (b != null && b.isBuilt) {
                dailyIncomeSS += b.getDailyIncome();
            }
        }

        netDailySS = dailyIncomeSS - dailyExpenseSS;
        netProfit = netDailySS;
        economyDirty = false;
    }

    public boolean shouldAutoSave() {
        long now = System.currentTimeMillis();
        if (now - lastSaveTime > 60000) {
            lastSaveTime = now;
            return true;
        }
        return false;
    }

    public void checkQuests(int type, int amount) {
        for (int i = 0; i < activeQuests.size(); i++) {
            Quest q = activeQuests.get(i);
            if (q != null && !q.completed && q.type == type) {
                q.addProgress(amount);
                if (q.completed) {
                    earn(q.rewardSS, 0, 0);
                    jade += q.rewardJade;
                }
            }
        }
    }
}
