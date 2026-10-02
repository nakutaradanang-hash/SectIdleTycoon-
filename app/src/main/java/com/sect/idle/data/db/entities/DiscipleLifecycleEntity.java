package com.sect.idle.data.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * DiscipleLifecycleEntity - Robust Room Entity representing disciple identity,
 * lifecycle state, Sims-style survival needs (Hunger, Energy, Mood, Qi Satiety),
 * and Xianxia Cultivation progress (Realms, Tribulations, Qi Deviation, Dao Heart).
 * Fully compatible with Sketchware Pro v7.0.0 and Android 5.0+ (API 21+).
 */
@Entity(tableName = "disciple_lifecycles")
public class DiscipleLifecycleEntity {

    // ==========================================
    // LIFECYCLE STAGES
    // ==========================================
    public static final int STAGE_MORTAL_RECRUIT = 0;
    public static final int STAGE_OUTER_DISCIPLE = 1;
    public static final int STAGE_INNER_DISCIPLE = 2;
    public static final int STAGE_CORE_DISCIPLE = 3;
    public static final int STAGE_ELDER = 4;
    public static final int STAGE_GRAND_ELDER = 5;
    public static final int STAGE_SECT_MASTER = 6;
    public static final int STAGE_ANCESTOR = 7;

    // ==========================================
    // STATE MACHINE ACTIVE STATES
    // ==========================================
    public static final int STATE_IDLE = 0;
    public static final int STATE_RESTING = 1;
    public static final int STATE_EATING = 2;
    public static final int STATE_CULTIVATING = 3;
    public static final int STATE_SECT_DUTY = 4;
    public static final int STATE_BREAKTHROUGH = 5;
    public static final int STATE_TRIBULATION = 6;
    public static final int STATE_QI_DEVIATION = 7;
    public static final int STATE_INJURED = 8;
    public static final int STATE_EXPEDITION = 9;
    public static final int STATE_ASCENDED = 10;
    public static final int STATE_DECEASED = 11;

    // ==========================================
    // CULTIVATION REALMS
    // ==========================================
    public static final int REALM_QI_CONDENSATION = 0;
    public static final int REALM_FOUNDATION_ESTABLISHMENT = 1;
    public static final int REALM_CORE_FORMATION = 2;
    public static final int REALM_NASCENT_SOUL = 3;
    public static final int REALM_SOUL_FORMATION = 4;
    public static final int REALM_VOID_REFINEMENT = 5;
    public static final int REALM_BODY_INTEGRATION = 6;
    public static final int REALM_GREAT_MAHAYANA = 7;
    public static final int REALM_TRIBULATION_TRANSCENDENCE = 8;
    public static final int REALM_TRUE_IMMORTAL = 9;

    // Sub-stages
    public static final int SUB_EARLY = 1;
    public static final int SUB_MID = 2;
    public static final int SUB_LATE = 3;
    public static final int SUB_PEAK = 4;

    // Elements
    public static final int ELEMENT_METAL = 0;
    public static final int ELEMENT_WOOD = 1;
    public static final int ELEMENT_WATER = 2;
    public static final int ELEMENT_FIRE = 3;
    public static final int ELEMENT_EARTH = 4;
    public static final int ELEMENT_LIGHTNING = 5;
    public static final int ELEMENT_WIND = 6;
    public static final int ELEMENT_YIN_YANG = 7;

    // ==========================================
    // PRIMARY KEY & IDENTITY
    // ==========================================
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "disciple_id")
    public String discipleId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "gender")
    public int gender; // 0 = Female, 1 = Male

    @ColumnInfo(name = "age")
    public int age;

    @ColumnInfo(name = "lifespan")
    public int lifespan;

    @ColumnInfo(name = "generation")
    public int generation;

    @ColumnInfo(name = "element")
    public int element;

    @ColumnInfo(name = "talent_grade")
    public int talentGrade; // 1 to 9 (Heavenly Spiritual Roots)

    // ==========================================
    // LIFECYCLE & STATE MACHINE TRACKING
    // ==========================================
    @ColumnInfo(name = "lifecycle_stage")
    public int lifecycleStage;

    @ColumnInfo(name = "current_state")
    public int currentState;

    @ColumnInfo(name = "state_timer_seconds")
    public int stateTimerSeconds;

    @ColumnInfo(name = "assigned_duty")
    public String assignedDuty;

    @ColumnInfo(name = "last_state_change_time")
    public long lastStateChangeTime;

    // ==========================================
    // SURVIVAL & VITALITY NEEDS (0 to 100)
    // ==========================================
    @ColumnInfo(name = "hunger")
    public int hunger; // 100 = Full, 0 = Starving

    @ColumnInfo(name = "energy")
    public int energy; // 100 = Rested, 0 = Exhausted

    @ColumnInfo(name = "mood")
    public int mood; // 100 = Blissful, 0 = Depressed

    @ColumnInfo(name = "health")
    public int health; // 100 = Peak Physical, 0 = Critical

    @ColumnInfo(name = "qi_satiety")
    public int qiSatiety; // 100 = Qi Saturated, 0 = Qi Depleted

    @ColumnInfo(name = "comfort")
    public int comfort;

    @ColumnInfo(name = "social")
    public int social;

    @ColumnInfo(name = "hygiene")
    public int hygiene;

    // ==========================================
    // CULTIVATION PROGRESSION
    // ==========================================
    @ColumnInfo(name = "realm")
    public int realm;

    @ColumnInfo(name = "sub_stage")
    public int subStage;

    @ColumnInfo(name = "cultivation_exp")
    public long cultivationExp;

    @ColumnInfo(name = "max_cultivation_exp")
    public long maxCultivationExp;

    @ColumnInfo(name = "dao_heart_stability")
    public float daoHeartStability; // 0.0 to 100.0%

    @ColumnInfo(name = "qi_purity")
    public float qiPurity; // 0.0 to 1.0 (Higher reduces breakthrough bottleneck)

    @ColumnInfo(name = "is_bottleneck")
    public boolean isBottleneck;

    @ColumnInfo(name = "tribulation_counter")
    public int tribulationCounter; // Number of heavenly lightning strikes survived

    @ColumnInfo(name = "karmic_sin")
    public int karmicSin;

    @ColumnInfo(name = "merit_points")
    public int meritPoints;

    @ColumnInfo(name = "total_breakthrough_attempts")
    public int totalBreakthroughAttempts;

    @ColumnInfo(name = "last_tick_time")
    public long lastTickTime;

    /**
     * Default Constructor for Room reflection.
     */
    public DiscipleLifecycleEntity() {
        this.discipleId = "disciple_" + System.currentTimeMillis();
        this.name = "Unknown Disciple";
        this.gender = 1;
        this.age = 16;
        this.lifespan = 100;
        this.generation = 1;
        this.element = ELEMENT_WOOD;
        this.talentGrade = 1;

        this.lifecycleStage = STAGE_OUTER_DISCIPLE;
        this.currentState = STATE_IDLE;
        this.stateTimerSeconds = 0;
        this.assignedDuty = "General Sect Caretaker";
        this.lastStateChangeTime = System.currentTimeMillis();

        this.hunger = 100;
        this.energy = 100;
        this.mood = 100;
        this.health = 100;
        this.qiSatiety = 50;
        this.comfort = 80;
        this.social = 80;
        this.hygiene = 90;

        this.realm = REALM_QI_CONDENSATION;
        this.subStage = SUB_EARLY;
        this.cultivationExp = 0L;
        this.maxCultivationExp = calculateMaxExp(REALM_QI_CONDENSATION, SUB_EARLY);
        this.daoHeartStability = 100.0f;
        this.qiPurity = 0.5f;
        this.isBottleneck = false;
        this.tribulationCounter = 0;
        this.karmicSin = 0;
        this.meritPoints = 0;
        this.totalBreakthroughAttempts = 0;
        this.lastTickTime = System.currentTimeMillis();
    }

    /**
     * Full Parameterized Constructor for custom creation.
     */
    @Ignore
    public DiscipleLifecycleEntity(@NonNull String discipleId, String name, int gender, int age, int element, int talentGrade) {
        this();
        this.discipleId = discipleId;
        this.name = name != null ? name : "Disciple " + discipleId;
        this.gender = gender;
        this.age = Math.max(12, age);
        this.element = element;
        this.talentGrade = Math.max(1, Math.min(9, talentGrade));
        this.lifespan = calculateBaseLifespan(this.realm, this.talentGrade);
    }

    /**
     * Calculate required EXP for realm and substage.
     */
    public static long calculateMaxExp(int realm, int subStage) {
        long base = 100L;
        for (int i = 0; i < realm; i++) {
            base = base * 3L + 150L;
        }
        return base * subStage;
    }

    /**
     * Calculate lifespan based on realm.
     */
    public static int calculateBaseLifespan(int realm, int talentGrade) {
        int base = 100 + (talentGrade * 5);
        switch (realm) {
            case REALM_QI_CONDENSATION: return base + 20;
            case REALM_FOUNDATION_ESTABLISHMENT: return base + 150;
            case REALM_CORE_FORMATION: return base + 400;
            case REALM_NASCENT_SOUL: return base + 900;
            case REALM_SOUL_FORMATION: return base + 1900;
            case REALM_VOID_REFINEMENT: return base + 4900;
            case REALM_BODY_INTEGRATION: return base + 9900;
            case REALM_GREAT_MAHAYANA: return base + 29900;
            case REALM_TRIBULATION_TRANSCENDENCE: return base + 99900;
            case REALM_TRUE_IMMORTAL: return 9999999;
            default: return base;
        }
    }

    public static String getRealmName(int realm, int subStage) {
        String stageStr;
        switch (subStage) {
            case SUB_EARLY: stageStr = "Early"; break;
            case SUB_MID: stageStr = "Mid"; break;
            case SUB_LATE: stageStr = "Late"; break;
            case SUB_PEAK: stageStr = "Peak"; break;
            default: stageStr = "Initial"; break;
        }

        String realmStr;
        switch (realm) {
            case REALM_QI_CONDENSATION: realmStr = "Qi Condensation"; break;
            case REALM_FOUNDATION_ESTABLISHMENT: realmStr = "Foundation Establishment"; break;
            case REALM_CORE_FORMATION: realmStr = "Core Formation"; break;
            case REALM_NASCENT_SOUL: realmStr = "Nascent Soul"; break;
            case REALM_SOUL_FORMATION: realmStr = "Soul Formation"; break;
            case REALM_VOID_REFINEMENT: realmStr = "Void Refinement"; break;
            case REALM_BODY_INTEGRATION: realmStr = "Body Integration"; break;
            case REALM_GREAT_MAHAYANA: realmStr = "Great Mahayana"; break;
            case REALM_TRIBULATION_TRANSCENDENCE: realmStr = "Tribulation Transcendence"; break;
            case REALM_TRUE_IMMORTAL: return "True Immortal (Ascended)";
            default: realmStr = "Mortal"; break;
        }
        return realmStr + " (" + stageStr + ")";
    }

    public static String getStateName(int state) {
        switch (state) {
            case STATE_IDLE: return "Idle";
            case STATE_RESTING: return "Resting / Sleeping";
            case STATE_EATING: return "Dining / Spirit Feast";
            case STATE_CULTIVATING: return "Meditating Cultivation";
            case STATE_SECT_DUTY: return "Sect Duty";
            case STATE_BREAKTHROUGH: return "Realm Breakthrough";
            case STATE_TRIBULATION: return "Heavenly Tribulation";
            case STATE_QI_DEVIATION: return "Qi Deviation (Demonic Backlash)";
            case STATE_INJURED: return "Severely Injured";
            case STATE_EXPEDITION: return "Mortal Realm Expedition";
            case STATE_ASCENDED: return "Ascended to Immortal Realm";
            case STATE_DECEASED: return "Perished (Reincarnated)";
            default: return "Unknown";
        }
    }

    public static String getStageName(int stage) {
        switch (stage) {
            case STAGE_MORTAL_RECRUIT: return "Mortal Aspirant";
            case STAGE_OUTER_DISCIPLE: return "Outer Sect Disciple";
            case STAGE_INNER_DISCIPLE: return "Inner Sect Disciple";
            case STAGE_CORE_DISCIPLE: return "Core True Disciple";
            case STAGE_ELDER: return "Hall Elder";
            case STAGE_GRAND_ELDER: return "Grand Supreme Elder";
            case STAGE_SECT_MASTER: return "Sect Master / Patriarch";
            case STAGE_ANCESTOR: return "Supreme Ancestor";
            default: return "Novice";
        }
    }

    public boolean isAlive() {
        return currentState != STATE_DECEASED && currentState != STATE_ASCENDED;
    }

    public boolean isStarving() {
        return hunger <= 15;
    }

    public boolean isExhausted() {
        return energy <= 15;
    }

    public boolean isDepressed() {
        return mood <= 20;
    }

    public boolean canAttemptBreakthrough() {
        return isAlive() && cultivationExp >= maxCultivationExp && !isBottleneck && currentState != STATE_QI_DEVIATION;
    }
}
