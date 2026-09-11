package com.sect.idle.core;

/**
 * GameConfig v5.2.1
 * Ultra-optimized for Android with adaptive quality control.
 */
public final class GameConfig {
    private GameConfig() {}

    // ========== DEVICE OPTIMIZATION ==========
    public static final boolean DEBUG = false;
    public static final int TARGET_FPS = 60;
    public static final int LOGIC_FPS = 30;
    public static final int TICK_MS = 1000;
    public static final int DAY_TICKS = 60;
    public static final int SAVE_INTERVAL = 7;

    public static final int MAX_DISCIPLES = 50;
    public static final int MAX_ITEMS = 500;
    public static final int MAX_BUILDINGS = 20;
    public static final int MAX_BATTLE_UNITS = 40;
    public static final int MAX_LOG_LINES = 100;
    public static final int MAX_EFFECTS = 300;
    public static final int MAX_SKILLS_PER_UNIT = 8;

    // ========== VISUAL QUALITY SETTINGS ==========
    public static final int QUALITY_LOW = 0;
    public static final int QUALITY_MEDIUM = 1;
    public static final int QUALITY_HIGH = 2;
    public static final int QUALITY_ULTRA = 3;

    public static final int TIER_LOW = QUALITY_LOW;
    public static final int TIER_MED = QUALITY_MEDIUM;
    public static final int TIER_HIGH = QUALITY_HIGH;

    public static int currentQuality = QUALITY_LOW;
    public static int deviceTier = QUALITY_LOW;

    // Feature toggles - optimized defaults for low-entry devices (1GB RAM & low CPU/GPU)
    public static boolean ENABLE_SHADOWS = false;
    public static boolean ENABLE_LIGHTING = false;
    public static boolean ENABLE_PARTICLES = false;
    public static boolean ENABLE_POST_PROCESS = false;
    public static boolean ENABLE_BLOOM = false;
    public static boolean ENABLE_REFLECTION = false;
    public static boolean ENABLE_VIGNETTE = false;
    public static boolean ENABLE_SPECULAR = false;
    public static boolean ENABLE_FAKE_3D = false;
    public static boolean ENABLE_ANIMATION = true;
    public static boolean ENABLE_DAMAGE_NUMBERS = true;
    public static boolean ENABLE_GROUND_RING = false;
    public static boolean ENABLE_TRAILS = false;
    public static boolean ENABLE_SMOOTH_CAMERA = true;
    public static final boolean FORCE_EFFECTS = false;

    // Render limits
    public static int MAX_LIGHTS = 0;
    public static int MAX_PARTICLES_RENDER = 0;
    public static int MAX_LAYER_BUFFERS = 5;
    public static float SHADOW_QUALITY = 0.5f;

    public static final float CAM_SHAKE_DECAY = 0.9f;
    public static final int TILE_WATER = 3;
    public static final int TILE_LAVA = 6;
    public static final int TILE_FIRE = 99;
    public static final float MINIMAP_Y = 10f;
    
    public static final int SPRITE_COUNT = 4;
    public static final int ANIM_FPS = 8;

    // ========== REALMS ==========
    public static final String[] REALMS = {
        "Mortal", "Qi Refining", "Foundation Establishment", "Core Formation", "Nascent Soul",
        "Soul Transformation", "Void Refinement", "Body Integration",
        "Tribulation Transcendence", "True Immortal", "Golden Immortal",
        "Primordial Immortal", "Dao Ancestor", "Heavenly Dao"
    };
    public static final int REALM_MAX = REALMS.length;
    public static final int REALM_EXP_CAP = 1000;
    public static final int REALM_SUB_STARS = 9;

    // ========== STATS ==========
    public static final int STAT_MIN = 1;
    public static final int STAT_MAX = 100;
    public static final int AGE_MAX = 1000;
    public static final int LIFESPAN_BASE = 100;
    public static final int LIFESPAN_VARIANCE = 200;

    // ========== RESOURCES ==========
    public static final int RES_SPIRIT_STONE = 0;
    public static final int RES_JADE = 1;
    public static final int RES_ESSENCE = 2;
    public static final int RES_REPUTATION = 3;
    public static final int RES_FAME = 4;
    public static final int RES_MERIT = 5;
    public static final int RES_COUNT = 6;

    // ========== TASKS ==========
    public static final int TASK_NONE = 0;
    public static final int TASK_FARMING = 1;
    public static final int TASK_CRAFTING = 2;
    public static final int TASK_ALCHEMY = 3;
    public static final int TASK_CULTIVATION = 4;
    public static final int TASK_MINING = 5;
    public static final int TASK_TRAINING = 6;
    public static final int TASK_GUARD = 7;
    public static final int TASK_RESEARCH = 8;
    public static final int TASK_TRADING = 9;
    public static final int TASK_EXPLORING = 10;
    public static final int TASK_COUNT = 11;

    public static final String[] TASK_NAMES = {
        "Idle", "Farming", "Crafting", "Alchemy", "Cultivation",
        "Mining", "Training", "Guard", "Research", "Trading", "Exploring"
    };
    public static final int[] TASK_COLORS = {
        0xFF888888, 0xFF4CAF50, 0xFFFF9800, 0xFF448AFF,
        0xFFE040FB, 0xFF795548, 0xFFFF5722, 0xFF607D8B,
        0xFF009688, 0xFFFFEB3B, 0xFF9C27B0
    };
    public static final int[] TASK_BASE_INCOME = {0, 50, 80, 120, 0, 60, 0, 0, 0, 150, 30};

    // ========== BUILDINGS ==========
    public static final int BUILD_HALL = 0;
    public static final int BUILD_LIBRARY = 1;
    public static final int BUILD_ALCHEMY = 2;
    public static final int BUILD_FORGE = 3;
    public static final int BUILD_GARDEN = 4;
    public static final int BUILD_MINE = 5;
    public static final int BUILD_MARKET = 6;
    public static final int BUILD_ARENA = 7;
    public static final int BUILD_SPIRIT_POOL = 8;
    public static final int BUILD_TOWER = 9;
    public static final int BUILD_COUNT = 10;
    public static final String[] BUILD_NAMES = {
        "Main Hall", "Library", "Alchemy Lab", "Forge", "Spirit Garden",
        "Mine", "Market", "Arena", "Spirit Pool", "Tower"
    };

    // ========== PERSONALITY ==========
    public static final int PER_NONE = 0;
    public static final int PER_ARROGANT = 1;
    public static final int PER_DILIGENT = 2;
    public static final int PER_MYSTERIOUS = 3;
    public static final int PER_MERCHANT = 4;
    public static final int PER_LOYAL = 5;
    public static final int PER_LAZY = 6;
    public static final int PER_COLDBLOOD = 7;
    public static final int PER_FIGHTER = 8;
    public static final int PER_SCHOLAR = 9;
    public static final int PER_ORPHAN = 10;
    public static final int PER_KIND = 11;
    public static final int PER_RUTHLESS = 12;
    public static final int PER_WISE = 13;
    public static final int PER_CURIOUS = 14;
    public static final int PER_PROUD = 15;
    public static final int PER_COUNT = 16;
    public static final String[] PERSONALITY_NAMES = {
        "None", "Arrogant", "Diligent", "Mysterious", "Merchant", "Loyal",
        "Lazy", "Cold-blooded", "Fighter", "Scholar", "Orphan", "Kind",
        "Ruthless", "Wise", "Curious", "Proud"
    };

    // ========== TALENT ==========
    public static final int TAL_NONE = 0;
    public static final int TAL_BODY = 1;
    public static final int TAL_MIND = 2;
    public static final int TAL_SPIRIT = 3;
    public static final int TAL_LUCK = 4;
    public static final int TAL_DUAL = 5;
    public static final int TAL_CHAOS = 6;
    public static final int TAL_HEAVEN = 7;
    public static final int TAL_COUNT = 8;
    public static final String[] TALENT_NAMES = {
        "None", "Body", "Mind", "Spirit", "Luck", "Dual", "Chaos", "Heaven"
    };

    // ========== ELEMENT ==========
    public static final int ELEM_NONE = 0;
    public static final int ELEM_FIRE = 1;
    public static final int ELEM_WATER = 2;
    public static final int ELEM_WOOD = 3;
    public static final int ELEM_METAL = 4;
    public static final int ELEM_EARTH = 5;
    public static final int ELEM_LIGHT = 6;
    public static final int ELEM_SHADOW = 7;
    public static final int ELEM_WIND = 8;
    public static final int ELEM_ICE = 9;
    public static final int ELEM_LIGHTNING = 10;
    public static final int ELEM_COUNT = 11;
    public static final String[] ELEMENT_NAMES = {
        "None", "Fire", "Water", "Wood", "Metal", "Earth",
        "Light", "Shadow", "Wind", "Ice", "Lightning"
    };
    public static final int[] ELEMENT_COLORS = {
        0xFFAAAAAA, 0xFFFF5722, 0xFF2196F3, 0xFF4CAF50, 0xFF607D8B,
        0xFF795548, 0xFFFFFDE7, 0xFF212121, 0xFF00BCD4, 0xFF03A9F4, 0xFFFFEB3B
    };

    // ========== BATTLE ==========
    public static final int BATTLE_PVE = 0;
    public static final int BATTLE_PVP = 1;
    public static final int BATTLE_SECT_WAR = 2;
    public static final int BATTLE_BOSS = 3;

    // ========== RARITY ==========
    public static final int RARITY_COMMON = 0;
    public static final int RARITY_UNCOMMON = 1;
    public static final int RARITY_RARE = 2;
    public static final int RARITY_EPIC = 3;
    public static final int RARITY_LEGEND = 4;
    public static final int RARITY_MYTHIC = 5;
    public static final int RARITY_COUNT = 6;
    public static final int[] RARITY_COLORS = {
        0xFFAAAAAA, 0xFF4CAF50, 0xFF2196F3, 0xFF9C27B0, 0xFFFFD700, 0xFFFF3D71
    };
    public static final String[] RARITY_NAMES = {
        "Common", "Uncommon", "Rare", "Epic", "Legend", "Mythic"
    };

    // ========== SEASONS ==========
    public static final int SEASON_SPRING = 0;
    public static final int SEASON_SUMMER = 1;
    public static final int SEASON_AUTUMN = 2;
    public static final int SEASON_WINTER = 3;
    public static final String[] SEASONS = {"Spring", "Summer", "Autumn", "Winter"};

    // ========== SAVE KEYS ==========
    public static final String PREFS_NAME = "IdleSectPro_v5";
    public static final String KEY_SAVE = "save_v5";
    public static final String KEY_SETTINGS = "settings_v5";
    public static final String KEY_LOGS = "logs_v5";

    // ========== CAMERA ==========
    public static final float CAM_MIN_ZOOM = 0.5f;
    public static final float CAM_MAX_ZOOM = 2.5f;
    public static final float CAM_SPEED = 0.18f;
    public static final float CAM_LERP = 0.12f;

    // ========== UI ==========
    public static final float UI_SAFE_TOP = 60f;
    public static final float UI_SAFE_BOTTOM = 40f;
    public static final int UI_BG_COLOR = 0xDD1A1A2E;
    public static final int UI_TEXT_COLOR = 0xFFFFFFFF;
    public static final int UI_GOLD_COLOR = 0xFFFFD700;
    public static final int UI_JADE_COLOR = 0xFF00E5FF;
    public static final int UI_ESSENCE_COLOR = 0xFFB388FF;

    // ========== LIGHTING COLORS ==========
    public static final int LIGHT_AMBIENT_DAY = 0xFF1A1A2E;
    public static final int LIGHT_AMBIENT_NIGHT = 0xFF0A0A1A;
    public static final int LIGHT_TORCH = 0xFFFFA726;
    public static final int LIGHT_MAGIC = 0xFF448AFF;
    public static final int LIGHT_HEAL = 0xFF66BB6A;
    public static final int LIGHT_FIRE = 0xFFFF5722;
    public static final int LIGHT_ICE = 0xFF00E5FF;
    public static final int LIGHT_GOLD = 0xFFFFD700;

    // ========== UTILS ==========
    public static String getRealmName(int id) {
        return (id >= 0 && id < REALM_MAX) ? REALMS[id] : REALMS[0];
    }
    public static String getTaskName(int id) {
        return (id >= 0 && id < TASK_COUNT) ? TASK_NAMES[id] : TASK_NAMES[0];
    }
    public static String getSeasonName(int id) {
        return (id >= 0 && id < SEASONS.length) ? SEASONS[id] : SEASONS[0];
    }
    public static String getPersonalityName(int id) {
        return (id >= 0 && id < PER_COUNT) ? PERSONALITY_NAMES[id] : PERSONALITY_NAMES[0];
    }
    public static String getElementName(int id) {
        return (id >= 0 && id < ELEM_COUNT) ? ELEMENT_NAMES[id] : ELEMENT_NAMES[0];
    }
    public static String getTalentName(int id) {
        return (id >= 0 && id < TAL_COUNT) ? TALENT_NAMES[id] : TALENT_NAMES[0];
    }
    public static String getRarityName(int id) {
        return (id >= 0 && id < RARITY_COUNT) ? RARITY_NAMES[id] : RARITY_NAMES[0];
    }
    public static int getElementColor(int id) {
        return (id >= 0 && id < ELEM_COUNT) ? ELEMENT_COLORS[id] : ELEMENT_COLORS[0];
    }
    public static int clamp(int val, int min, int max) {
        return val < min ? min : (val > max ? max : val);
    }
    public static float clamp(float val, float min, float max) {
        return val < min ? min : (val > max ? max : val);
    }
    public static int lerpInt(int a, int b, float t) {
        return a + (int)((b - a) * clamp(t, 0f, 1f));
    }

    public static String getBuildingName(int id) {
        return (id >= 0 && id < BUILD_COUNT) ? BUILD_NAMES[id] : "Building";
    }

    public static void setQuality(int q) {
        applyQuality(q);
    }

    public static void applyQuality(int quality) {
        currentQuality = quality;
        deviceTier = quality;
        switch (quality) {
            case QUALITY_LOW:
                ENABLE_SHADOWS = false; ENABLE_LIGHTING = false; ENABLE_PARTICLES = false;
                ENABLE_POST_PROCESS = false; ENABLE_BLOOM = false; ENABLE_REFLECTION = false;
                ENABLE_VIGNETTE = false; ENABLE_SPECULAR = false; ENABLE_FAKE_3D = false;
                ENABLE_ANIMATION = true; ENABLE_DAMAGE_NUMBERS = true; ENABLE_GROUND_RING = false;
                ENABLE_TRAILS = false; MAX_LIGHTS = 0; MAX_PARTICLES_RENDER = 0;
                break;
            case QUALITY_MEDIUM:
                ENABLE_SHADOWS = true; ENABLE_LIGHTING = true; ENABLE_PARTICLES = true;
                ENABLE_POST_PROCESS = true; ENABLE_BLOOM = false; ENABLE_REFLECTION = false;
                ENABLE_VIGNETTE = true; ENABLE_SPECULAR = true; ENABLE_FAKE_3D = true;
                ENABLE_ANIMATION = true; ENABLE_DAMAGE_NUMBERS = true; ENABLE_GROUND_RING = true;
                ENABLE_TRAILS = true; MAX_LIGHTS = 12; MAX_PARTICLES_RENDER = 200;
                break;
            case QUALITY_HIGH:
                ENABLE_SHADOWS = true; ENABLE_LIGHTING = true; ENABLE_PARTICLES = true;
                ENABLE_POST_PROCESS = true; ENABLE_BLOOM = true; ENABLE_REFLECTION = true;
                ENABLE_VIGNETTE = true; ENABLE_SPECULAR = true; ENABLE_FAKE_3D = true;
                ENABLE_ANIMATION = true; ENABLE_DAMAGE_NUMBERS = true; ENABLE_GROUND_RING = true;
                ENABLE_TRAILS = true; MAX_LIGHTS = 20; MAX_PARTICLES_RENDER = 400;
                break;
            case QUALITY_ULTRA:
                ENABLE_SHADOWS = true; ENABLE_LIGHTING = true; ENABLE_PARTICLES = true;
                ENABLE_POST_PROCESS = true; ENABLE_BLOOM = true; ENABLE_REFLECTION = true;
                ENABLE_VIGNETTE = true; ENABLE_SPECULAR = true; ENABLE_FAKE_3D = true;
                ENABLE_ANIMATION = true; ENABLE_DAMAGE_NUMBERS = true; ENABLE_GROUND_RING = true;
                ENABLE_TRAILS = true; MAX_LIGHTS = 32; MAX_PARTICLES_RENDER = 600;
                break;
        }
    }
}
