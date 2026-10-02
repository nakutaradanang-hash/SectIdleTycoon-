package com.sect.idle.gameplay;

import com.sect.idle.systems.RNG;
import com.sect.idle.utils.ErrorCode;
import com.sect.idle.utils.ExceptionManager;
import java.util.ArrayList;

/**
 * RealmHexGridMap - Civilization Series 4X Style Hex Grid World Map.
 * Provides territory exploration (Fog of War), spirit vein resource nodes,
 * outpost construction, border expansion, and hex tile yields.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class RealmHexGridMap {

    public static final int MAP_WIDTH = 12;
    public static final int MAP_HEIGHT = 12;
    public static final int TOTAL_HEX_TILES = MAP_WIDTH * MAP_HEIGHT;

    // Tile Terrain Types
    public static final int TERRAIN_PLAINS = 0;       // Balanced Food & Spirit Stones
    public static final int TERRAIN_MOUNTAIN = 1;     // High Spirit Ore & Defense
    public static final int TERRAIN_SPIRIT_LAKE = 2;  // High Spirit Qi & Herbs
    public static final int TERRAIN_ANCIENT_RUINS = 3;// Research / Dao Insight
    public static final int TERRAIN_DRAGON_VEIN = 4;  // Extreme Multi-Resource Yield
    public static final int TERRAIN_VOLCANO = 5;      // Fire Essence & Crafting

    // Tile Ownership
    public static final int OWNER_UNCLAIMED = 0;
    public static final int OWNER_PLAYER_SECT = 1;
    public static final int OWNER_RIVAL_SECT = 2;
    public static final int OWNER_DEMONIC_HORDE = 3;

    public static class HexTile {
        public int q, r; // Axial hex coordinates
        public int terrainType;
        public int owner;
        public boolean explored;
        public boolean hasOutpost;
        public int spiritStonesYield;
        public int herbsYield;
        public int oresYield;
        public int daoInsightYield;
        public String name;

        public HexTile(int q, int r, int terrainType, String name) {
            this.q = q;
            this.r = r;
            this.terrainType = terrainType;
            this.name = name;
            this.owner = OWNER_UNCLAIMED;
            this.explored = false;
            this.hasOutpost = false;
            calculateYields();
        }

        public void calculateYields() {
            switch (terrainType) {
                case TERRAIN_PLAINS:
                    spiritStonesYield = 20;
                    herbsYield = 15;
                    oresYield = 5;
                    daoInsightYield = 2;
                    break;
                case TERRAIN_MOUNTAIN:
                    spiritStonesYield = 10;
                    herbsYield = 5;
                    oresYield = 30;
                    daoInsightYield = 5;
                    break;
                case TERRAIN_SPIRIT_LAKE:
                    spiritStonesYield = 25;
                    herbsYield = 35;
                    oresYield = 5;
                    daoInsightYield = 10;
                    break;
                case TERRAIN_ANCIENT_RUINS:
                    spiritStonesYield = 15;
                    herbsYield = 10;
                    oresYield = 15;
                    daoInsightYield = 25;
                    break;
                case TERRAIN_DRAGON_VEIN:
                    spiritStonesYield = 60;
                    herbsYield = 40;
                    oresYield = 50;
                    daoInsightYield = 30;
                    break;
                case TERRAIN_VOLCANO:
                    spiritStonesYield = 15;
                    herbsYield = 0;
                    oresYield = 45;
                    daoInsightYield = 15;
                    break;
                default:
                    spiritStonesYield = 10;
                    herbsYield = 10;
                    oresYield = 10;
                    daoInsightYield = 5;
                    break;
            }
        }
    }

    private static volatile RealmHexGridMap instance;
    private static final Object LOCK = new Object();

    public final HexTile[] grid;

    public static RealmHexGridMap getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new RealmHexGridMap();
                }
            }
        }
        return instance;
    }

    private RealmHexGridMap() {
        this.grid = new HexTile[TOTAL_HEX_TILES];
        generateWorldMap();
    }

    private void generateWorldMap() {
        String[] names = {
            "Mist Plains", "Jade Peak", "Lotus Lake", "Sword Grave", "Dragon Spine",
            "Thunder Ridge", "Golden Valley", "Primordial Cavern", "Heaven Fall", "Moon Well"
        };

        for (int r = 0; r < MAP_HEIGHT; r++) {
            for (int q = 0; q < MAP_WIDTH; q++) {
                int index = r * MAP_WIDTH + q;
                int terrain = (q == 5 && r == 5) ? TERRAIN_DRAGON_VEIN : RNG.nextInt(6);
                String name = names[RNG.nextInt(names.length)] + " [" + q + "," + r + "]";
                HexTile tile = new HexTile(q, r, terrain, name);

                // Spawn player starting sect capital at (0,0) and surrounding tiles
                if (q <= 1 && r <= 1) {
                    tile.explored = true;
                    tile.owner = OWNER_PLAYER_SECT;
                    if (q == 0 && r == 0) tile.hasOutpost = true;
                }
                grid[index] = tile;
            }
        }
    }

    public HexTile getTile(int q, int r) {
        if (q < 0 || q >= MAP_WIDTH || r < 0 || r >= MAP_HEIGHT) return null;
        return grid[r * MAP_WIDTH + q];
    }

    /**
     * Explores a target hex tile, clearing Fog of War.
     */
    public boolean exploreTile(int q, int r) {
        HexTile tile = getTile(q, r);
        if (tile == null) return false;
        tile.explored = true;
        return true;
    }

    /**
     * Claims an explored tile and builds an outpost to harvest resources per turn.
     */
    public boolean claimTerritory(SectData sect, int q, int r) {
        HexTile tile = getTile(q, r);
        if (tile == null || !tile.explored || tile.owner == OWNER_PLAYER_SECT) {
            return false;
        }

        int cost = 300 + (tile.terrainType == TERRAIN_DRAGON_VEIN ? 500 : 0);
        if (sect != null && sect.spiritStones >= cost) {
            sect.spend(cost, 50, 50);
            tile.owner = OWNER_PLAYER_SECT;
            tile.hasOutpost = true;
            ExceptionManager.get().logUserAction("HEX_MAP_CLAIM", tile.name, "Constructed Outpost");
            return true;
        }
        return false;
    }

    /**
     * Ticks all player-controlled hex tiles and gathers resources.
     */
    public void collectTurnHarvest(SectData sect) {
        if (sect == null) return;
        long totalStones = 0;
        long totalHerbs = 0;
        long totalOres = 0;
        long totalDaoInsight = 0;

        for (int i = 0; i < TOTAL_HEX_TILES; i++) {
            HexTile tile = grid[i];
            if (tile != null && tile.owner == OWNER_PLAYER_SECT) {
                totalStones += tile.spiritStonesYield;
                totalHerbs += tile.herbsYield;
                totalOres += tile.oresYield;
                totalDaoInsight += tile.daoInsightYield;
            }
        }
        sect.earn(totalStones, totalHerbs, totalOres);
        sect.sectExp += totalDaoInsight;
    }

    public int getPlayerControlledTileCount() {
        int count = 0;
        for (int i = 0; i < TOTAL_HEX_TILES; i++) {
            if (grid[i] != null && grid[i].owner == OWNER_PLAYER_SECT) {
                count++;
            }
        }
        return count;
    }
}
