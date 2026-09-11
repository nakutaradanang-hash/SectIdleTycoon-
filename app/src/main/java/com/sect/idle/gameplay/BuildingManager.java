package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Building;

import java.util.ArrayList;

/**
 * BuildingManager - Single-responsibility manager for Sect building infrastructure,
 * construction costs, leveling, and spatial layout.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BuildingManager {

    private static volatile BuildingManager instance;
    private static final Object LOCK = new Object();

    private BuildingManager() {}

    public static BuildingManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new BuildingManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes default sect buildings configuration.
     */
    public ArrayList<Building> createDefaultBuildings() {
        ArrayList<Building> list = new ArrayList<Building>();

        // Main Hall
        Building mainHall = new Building(GameConfig.BUILD_HALL, "Main Hall", 10, 500L, 5);
        mainHall.posX = 2000; mainHall.posY = 2000;
        mainHall.build();
        list.add(mainHall);

        // Cultivation Chamber / Library
        Building library = new Building(GameConfig.BUILD_LIBRARY, "Library", 10, 300L, 4);
        library.posX = 2200; library.posY = 2000;
        list.add(library);

        // Herb Garden
        Building herbGarden = new Building(GameConfig.BUILD_GARDEN, "Spirit Garden", 10, 200L, 6);
        herbGarden.posX = 1800; herbGarden.posY = 2000;
        list.add(herbGarden);

        // Alchemy Furnace
        Building alchemy = new Building(GameConfig.BUILD_ALCHEMY, "Alchemy Lab", 10, 400L, 4);
        alchemy.posX = 2000; alchemy.posY = 1800;
        list.add(alchemy);

        // Blacksmith / Armory
        Building forge = new Building(GameConfig.BUILD_FORGE, "Forge", 10, 400L, 4);
        forge.posX = 2000; forge.posY = 2200;
        list.add(forge);

        // Spirit Pool
        Building spring = new Building(GameConfig.BUILD_SPIRIT_POOL, "Spirit Pool", 10, 600L, 3);
        spring.posX = 1800; spring.posY = 1800;
        list.add(spring);

        // Martial Arena
        Building arena = new Building(GameConfig.BUILD_ARENA, "Arena", 10, 500L, 8);
        arena.posX = 2200; arena.posY = 2200;
        list.add(arena);

        return list;
    }

    /**
     * Upgrades a sect building if resources are sufficient.
     */
    public boolean upgradeBuilding(Building b, SectData data) {
        if (b == null || data == null) return false;
        long cost = b.upgradeCost;
        if (data.spiritStones >= cost) {
            data.spend(cost, 0, 0);
            b.upgrade();
            data.recalculateEconomy();
            return true;
        }
        return false;
    }

    /**
     * Constructs an unbuilt building.
     */
    public boolean constructBuilding(Building b, SectData data) {
        if (b == null || data == null) return false;
        if (b.isBuilt) return false;
        long cost = b.cost;
        if (data.spiritStones >= cost) {
            data.spend(cost, 0, 0);
            b.build();
            data.recalculateEconomy();
            return true;
        }
        return false;
    }
}
