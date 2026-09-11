package com.sect.idle.models;

import com.sect.idle.core.GameConfig;
import com.sect.idle.core.Vector2;
import com.sect.idle.gameplay.TalentSystem;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

public final class Disciple {
    // ====== IDENTITY ======
    public String id;
    public String name;
    public String title;
    public boolean isMale;
    public int age;
    public int lifespan;
    public int birthDay;
    
    public int animFrame = 0;
    public float animTimer = 0f;
    public boolean isMoving = false;
    
    // ====== STATS ======
    public int str, agi, intel, lck, vit, wis, cha;
    public final int[] stats = new int[7];
    private static final int STAT_STR = 0, STAT_AGI = 1, STAT_INT = 2, STAT_LCK = 3, STAT_VIT = 4, STAT_WIS = 5, STAT_CHA = 6;

    // ====== REALM ======
    public int realm;
    public int realmExp;
    public int realmTier;

    // ====== ELEMENT ======
    public int element;
    public int elementMastery;

    // ====== TALENT ======
    public Talent talent;
    public int talentType;
    public int talentGrade;
    public String talentName;

    // ====== PERSONALITY ======
    public int personality;
    public int behaviorState;
    public int mood;
    public int stress;
    public int energy;
    public int maxEnergy;

    // ====== SOCIAL ======
    public int loyalty;
    public int reputation;
    public int relationshipSect;
    public final ArrayList<String> relationships;
    public final ArrayList<String> grudges;

    // ====== ECONOMY ======
    public int dailyWage;
    public long totalContribution;
    public long totalEarnings;

    // ====== TASK ======
    public int currentTask;
    public int taskEfficiency;
    public int taskProgress;
    public int taskTarget;
    public int taskLevel;

    // ====== SKILLS ======
    public final ArrayList<Skill> skills;
    public int skillPoints;

    // ====== EQUIPMENT ======
    public Equipment weapon;
    public Equipment armor;
    public Equipment accessory;
    public Equipment artifact;

    // ====== COMBAT ======
    public int hp, maxHp;
    public int mp, maxMp;
    public int atk, def, spd;
    public int critRate, critDmg;
    public int dodge, accuracy;

    // ====== POSITION ======
    public final Vector2 position;
    public final Vector2 targetPos;
    public float moveSpeed;
    public int facing;

    // ====== HOBBY & JOB & LEVELING ======
    public int level = 1;
    public long exp = 0L;
    public long maxExp = 100L;
    public int alchemySkill = 1;
    public int bodyRefiningStage = 1;
    public int totalBattles = 0;
    public int battlesWon = 0;
    public int totalBattlesWon = 0;
    public int hobby;
    public int jobClass;
    public int jobLevel;
    public int jobExp;

    public boolean addExperience(long gain) {
        if (gain <= 0) return false;
        exp += gain;
        boolean leveledUp = false;
        while (exp >= maxExp && level < 100) {
            exp -= maxExp;
            level++;
            maxExp = (long)(maxExp * 1.35f + 50);
            str += 3;
            agi += 2;
            intel += 3;
            vit += 3;
            wis += 2;
            recalculateStats();
            leveledUp = true;
        }
        return leveledUp;
    }

    public void recalculateStats() {
        maxHp = BASE_HP + vit * HP_PER_VIT + str * HP_PER_STR + realm * HP_PER_REALM + (level * 25);
        if (hp > maxHp || hp <= 0) hp = maxHp;
        maxMp = BASE_MP + intel * MP_PER_INT + wis * MP_PER_WIS + realm * MP_PER_REALM + (level * 15);
        if (mp > maxMp || mp <= 0) mp = maxMp;
        atk = str * ATK_PER_STR + agi * ATK_PER_AGI + (realm * 15) + (level * 5);
        def = vit * DEF_PER_VIT + str * DEF_PER_STR + (realm * 10) + (level * 3);
        spd = agi * SPD_PER_AGI + (realm * 5) + (level * 2);
    }

    // ====== FAIRY ======
    public Fairy fairy;
    public boolean hasFairy;

    // ====== STATUS ======
    public final ArrayList<StatusEffect> statusEffects;

    // ====== HISTORY ======
    public final ArrayList<String> battleHistory;
    public int kills;
    public int deaths;
    public int tournamentsWon;

    // ====== VISUAL ======
    public int spriteId;
    public int avatarId;
    public int colorTint;

    // ====== CONSTANTS ======
    private static final int BASE_HP = 100;
    private static final int BASE_MP = 50;
    private static final int HP_PER_VIT = 10;
    private static final int HP_PER_STR = 5;
    private static final int HP_PER_REALM = 50;
    private static final int MP_PER_INT = 8;
    private static final int MP_PER_WIS = 5;
    private static final int MP_PER_REALM = 20;
    private static final int ATK_PER_STR = 3;
    private static final int ATK_PER_AGI = 1;
    private static final int DEF_PER_VIT = 2;
    private static final int DEF_PER_STR = 1;
    private static final int SPD_PER_AGI = 2;
    private static final int CRIT_PER_LCK = 2;
    private static final int BASE_CRIT_DMG = 150;
    private static final int ACC_PER_WIS = 2;
    private static final int ACC_PER_AGI = 1;
    private static final int DODGE_PER_AGI = 1;
    private static final int DODGE_PER_LCK = 1;
    private static final int MIN_EFFICIENCY = 10;
    private static final int MAX_EFFICIENCY = 100;
    private static final float MOVE_SPEED_BASE = 1.0f;
    private static final float MOVE_SPEED_VAR = 1.5f;
    private static final int AGE_MIN = 16;
    private static final int AGE_RANGE = 20;
    private static final int ENERGY_REGEN = 20;
    private static final int STRESS_RELIEF = 5;
    private static final int MOOD_LOYALTY_BONUS = 5;
    private static final int MOOD_LOYALTY_PENALTY = -3;
    private static final int ENERGY_WORK_COST = 15;
    private static final int STRESS_WORK_GAIN = 5;
    private static final int ENERGY_THRESHOLD_LOW = 30;
    private static final int STRESS_THRESHOLD_HIGH = 70;
    private static final int MOOD_THRESHOLD_LOW = 30;

    public Disciple() {
        this.id = "D" + System.currentTimeMillis() + RNG.nextInt(10000);
        this.relationships = new ArrayList<String>(4);
        this.grudges = new ArrayList<String>(4);
        this.skills = new ArrayList<Skill>(GameConfig.MAX_SKILLS_PER_UNIT);
        this.statusEffects = new ArrayList<StatusEffect>(4);
        this.battleHistory = new ArrayList<String>(8);
        this.position = new Vector2();
        this.targetPos = new Vector2();
        this.age = AGE_MIN + RNG.nextInt(AGE_RANGE);
        this.lifespan = GameConfig.LIFESPAN_BASE + RNG.nextInt(GameConfig.LIFESPAN_VARIANCE);
        this.birthDay = 0;
        this.energy = 100;
        this.maxEnergy = 100;
        this.mood = 50;
        this.stress = 0;
        this.hp = BASE_HP;
        this.maxHp = BASE_HP;
        this.mp = BASE_MP;
        this.maxMp = BASE_MP;
        this.moveSpeed = MOVE_SPEED_BASE + RNG.nextFloat() * MOVE_SPEED_VAR;
        this.facing = 1;
        this.colorTint = 0xFFFFFFFF;
        this.currentTask = GameConfig.TASK_NONE;
        this.taskEfficiency = 50;
        this.efficiency = 1.0f;
        this.loyalty = 50;
        this.reputation = 0;
        this.relationshipSect = 50;
    }

    public float efficiency = 1.0f;

    public Disciple(String name) {
        this();
        this.name = name != null ? name : "Disciple";
    }

    public void initStats(int s, int a, int i, int l, int v, int w, int c) {
        str = s; agi = a; intel = i; lck = l; vit = v; wis = w; cha = c;
        stats[STAT_STR] = s; stats[STAT_AGI] = a; stats[STAT_INT] = i;
        stats[STAT_LCK] = l; stats[STAT_VIT] = v; stats[STAT_WIS] = w; stats[STAT_CHA] = c;
        recalcCombat();
    }

    public void recalcCombat() {
        maxHp = BASE_HP + vit * HP_PER_VIT + str * HP_PER_STR + realm * HP_PER_REALM;
        maxMp = BASE_MP + intel * MP_PER_INT + wis * MP_PER_WIS + realm * MP_PER_REALM;

        float atkMult = talent != null ? talent.atkMultiplier : 1.0f;
        float defMult = talent != null ? talent.defMultiplier : 1.0f;
        float spdMult = talent != null ? talent.spdMultiplier : 1.0f;
        int critBonus = talent != null ? talent.critRateBonus : 0;
        int critDmgBonus = talent != null ? talent.critDmgBonus : 0;
        int dodgeBonus = talent != null ? talent.dodgeBonus : 0;
        int accBonus = talent != null ? talent.accuracyBonus : 0;

        atk = (int)((str * ATK_PER_STR + agi * ATK_PER_AGI + (weapon != null ? weapon.atk : 0)) * atkMult);
        def = (int)((vit * DEF_PER_VIT + str * DEF_PER_STR + (armor != null ? armor.def : 0)) * defMult);
        spd = (int)((agi * SPD_PER_AGI + (accessory != null ? accessory.spd : 0)) * spdMult);
        critRate = lck * CRIT_PER_LCK + (artifact != null ? artifact.crit : 0) + critBonus;
        critDmg = BASE_CRIT_DMG + str + critDmgBonus;
        dodge = agi * DODGE_PER_AGI + lck * DODGE_PER_LCK + dodgeBonus;
        accuracy = wis * ACC_PER_WIS + agi * ACC_PER_AGI + accBonus;
        hp = Math.min(hp, maxHp);
        mp = Math.min(mp, maxMp);
        if (hp <= 0) hp = 1;
    }

    public String getRealmDisplay() {
        int sub = Math.min(GameConfig.REALM_SUB_STARS, (realmExp / 100) + 1);
        return GameConfig.getRealmName(realm) + " · " + sub + "-Star";
    }

    public String getTaskName() {
        return GameConfig.getTaskName(currentTask);
    }

    public int getTaskColor() {
        return GameConfig.TASK_COLORS[GameConfig.clamp(currentTask, 0, GameConfig.TASK_COUNT - 1)];
    }

    public int getTaskIncome() {
        int base = GameConfig.TASK_BASE_INCOME[GameConfig.clamp(currentTask, 0, GameConfig.TASK_BASE_INCOME.length - 1)];
        return (base * taskEfficiency * (100 + taskLevel * 5)) / 10000;
    }

    public void updateEfficiency() {
        int base = 50;
        switch (currentTask) {
            case GameConfig.TASK_FARMING: base += str / 2 + vit / 4; break;
            case GameConfig.TASK_CRAFTING: base += agi / 2 + intel / 4; break;
            case GameConfig.TASK_ALCHEMY: base += intel / 2 + wis / 4; break;
            case GameConfig.TASK_CULTIVATION: base += (intel + wis) / 4; break;
            case GameConfig.TASK_MINING: base += str / 2 + vit / 3; break;
            case GameConfig.TASK_TRAINING: base += str / 3 + agi / 3 + vit / 3; break;
            case GameConfig.TASK_GUARD: base += str / 3 + vit / 3 + cha / 4; break;
            case GameConfig.TASK_RESEARCH: base += intel / 2 + wis / 3; break;
            case GameConfig.TASK_TRADING: base += cha / 2 + lck / 3 + intel / 4; break;
            case GameConfig.TASK_EXPLORING: base += agi / 2 + lck / 3 + wis / 4; break;
        }
        base += getPersonalityBonus();
        base += getTalentBonus();
        base += getElementBonus();
        if (energy < ENERGY_THRESHOLD_LOW) base -= 20;
        if (stress > STRESS_THRESHOLD_HIGH) base -= 15;
        if (mood < MOOD_THRESHOLD_LOW) base -= 10;
        if (fairy != null) base += fairy.efficiencyBonus;
        taskEfficiency = GameConfig.clamp(base, MIN_EFFICIENCY, MAX_EFFICIENCY);
    }

    private int getPersonalityBonus() {
        switch (personality) {
            case GameConfig.PER_ARROGANT: return currentTask == GameConfig.TASK_CULTIVATION ? 20 : -10;
            case GameConfig.PER_DILIGENT: return 15;
            case GameConfig.PER_MYSTERIOUS: return currentTask == GameConfig.TASK_CULTIVATION ? 25 : 5;
            case GameConfig.PER_MERCHANT: return currentTask == GameConfig.TASK_TRADING ? 25 : currentTask == GameConfig.TASK_CRAFTING ? 15 : 0;
            case GameConfig.PER_LOYAL: return loyalty > 70 ? 20 : 0;
            case GameConfig.PER_LAZY: return -20;
            case GameConfig.PER_COLDBLOOD: return currentTask == GameConfig.TASK_ALCHEMY ? 20 : 10;
            case GameConfig.PER_FIGHTER: return currentTask == GameConfig.TASK_TRAINING ? 25 : currentTask == GameConfig.TASK_FARMING ? -15 : 5;
            case GameConfig.PER_SCHOLAR: return currentTask == GameConfig.TASK_ALCHEMY || currentTask == GameConfig.TASK_RESEARCH ? 30 : -5;
            case GameConfig.PER_ORPHAN: return age > 30 ? 15 : 5;
            case GameConfig.PER_KIND: return currentTask == GameConfig.TASK_FARMING ? 15 : 5;
            case GameConfig.PER_RUTHLESS: return currentTask == GameConfig.TASK_GUARD || currentTask == GameConfig.TASK_MINING ? 20 : 0;
            case GameConfig.PER_WISE: return currentTask == GameConfig.TASK_RESEARCH || currentTask == GameConfig.TASK_CULTIVATION ? 20 : 5;
            case GameConfig.PER_CURIOUS: return currentTask == GameConfig.TASK_EXPLORING ? 25 : 10;
            case GameConfig.PER_PROUD: return currentTask == GameConfig.TASK_GUARD ? 20 : -5;
            default: return 0;
        }
    }

    private int getTalentBonus() {
        if (talent != null) {
            return TalentSystem.getTaskEfficiencyBonus(this, currentTask);
        }
        switch (talentType) {
            case GameConfig.TAL_BODY: return currentTask == GameConfig.TASK_FARMING || currentTask == GameConfig.TASK_MINING || currentTask == GameConfig.TASK_TRAINING ? 20 : 5;
            case GameConfig.TAL_MIND: return currentTask == GameConfig.TASK_ALCHEMY || currentTask == GameConfig.TASK_RESEARCH ? 20 : 5;
            case GameConfig.TAL_SPIRIT: return currentTask == GameConfig.TASK_CULTIVATION ? 25 : 10;
            case GameConfig.TAL_LUCK: return lck / 3;
            case GameConfig.TAL_DUAL: return 10;
            case GameConfig.TAL_CHAOS: return RNG.nextInt(40) - 10;
            case GameConfig.TAL_HEAVEN: return 15;
            default: return 0;
        }
    }

    private int getElementBonus() {
        if (element == GameConfig.ELEM_NONE) return 0;
        if (currentTask == GameConfig.TASK_ALCHEMY && (element == GameConfig.ELEM_FIRE || element == GameConfig.ELEM_WATER)) return 10;
        if (currentTask == GameConfig.TASK_FARMING && element == GameConfig.ELEM_WOOD) return 15;
        if (currentTask == GameConfig.TASK_MINING && element == GameConfig.ELEM_EARTH) return 15;
        if (currentTask == GameConfig.TASK_CRAFTING && element == GameConfig.ELEM_METAL) return 15;
        return 0;
    }

    public int getExpectedWage() {
        int base = 50 + realm * 30 + (str + agi + intel + lck + vit + wis + cha) / 7;
        switch (personality) {
            case GameConfig.PER_ARROGANT: base = (int)(base * 1.5f); break;
            case GameConfig.PER_MERCHANT: base = (int)(base * 1.3f); break;
            case GameConfig.PER_LAZY: base = (int)(base * 0.7f); break;
            case GameConfig.PER_ORPHAN: base = (int)(base * 0.6f); break;
            case GameConfig.PER_PROUD: base = (int)(base * 1.4f); break;
        }
        base += talentGrade * 10;
        return Math.max(1, base);
    }

    public void dailyTick() {
        age++;
        energy = Math.min(maxEnergy, energy + ENERGY_REGEN);
        stress = Math.max(0, stress - STRESS_RELIEF);
        mood = GameConfig.clamp(mood + (loyalty > 50 ? MOOD_LOYALTY_BONUS : MOOD_LOYALTY_PENALTY), 0, 100);

        if (currentTask == GameConfig.TASK_CULTIVATION) {
            int gain = 10 + intel / 10 + wis / 10 + getTalentBonus() / 5;
            if (fairy != null) gain += fairy.cultivationBonus;
            realmExp += gain;
            if (realmExp >= GameConfig.REALM_EXP_CAP) {
                realmExp = 0;
                if (realm < GameConfig.REALM_MAX - 1) realm++;
            }
        }

        if (currentTask != GameConfig.TASK_NONE) {
            energy -= ENERGY_WORK_COST;
            if (energy < 0) energy = 0;
            stress += STRESS_WORK_GAIN;
            if (stress > 100) stress = 100;
        }

        // Status effects tick
        for (int i = statusEffects.size() - 1; i >= 0; i--) {
            StatusEffect se = statusEffects.get(i);
            se.duration--;
            if (se.duration <= 0) statusEffects.remove(i);
        }

        updateEfficiency();
        recalcCombat();
    }
    
    public void moveTo(float tx, float ty, float dtMultiplier) {
        targetPos.set(tx, ty);
        float dx = tx - position.x;
        float dy = ty - position.y;
        float distSq = dx * dx + dy * dy;
        if (distSq > 0.01f) {
            float dist = (float)Math.sqrt(distSq);
            facing = dx > 0f ? 1 : -1;
            position.x += (dx / dist) * moveSpeed * dtMultiplier;
            position.y += (dy / dist) * moveSpeed * dtMultiplier;
            isMoving = true;
        } else {
            isMoving = false;
        }
    }

    public void moveTo(float tx, float ty) {
        moveTo(tx, ty, 1.0f);
    }

    public boolean isAlive() { return age < lifespan && hp > 0; }
    public boolean canWork() { return energy > 20 && stress < 90 && isAlive(); }
    public boolean canFight() { return isAlive() && energy > 10; }

    public void teleportTo(float tx, float ty) {
        position.set(tx, ty);
        targetPos.set(tx, ty);
    }

    public void addExp(int exp) {
        jobExp += exp;
        int needed = jobLevel * 100;
        if (jobExp >= needed) {
            jobExp -= needed;
            jobLevel++;
            skillPoints += 2;
        }
    }

    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    public void restoreMp(int amount) {
        mp = Math.min(maxMp, mp + amount);
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    public void addStatusEffect(StatusEffect effect) {
        if (effect != null && statusEffects.size() < 20) {
            statusEffects.add(effect);
        }
    }

    public void removeStatusEffect(int type) {
        for (int i = statusEffects.size() - 1; i >= 0; i--) {
            if (statusEffects.get(i).type == type) {
                statusEffects.remove(i);
                return;
            }
        }
    }

    public int getTotalStats() {
        return str + agi + intel + lck + vit + wis + cha;
    }

    public float getPowerRating() {
        return (atk + def + spd) * 0.5f + maxHp * 0.1f + realm * 100f;
    }
}
