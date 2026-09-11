package com.sect.idle.models;

/**
 * BattleSkill - Xianxia Donghua Immortal Cultivation combat technique.
 * Includes elemental affinity, 3D trajectory type, cinematic camera focus, and damage scaling.
 */
public final class BattleSkill {

    public static final int TYPE_MELEE = 0;
    public static final int TYPE_RANGED_PROJECTILE = 1;
    public static final int TYPE_AOE_DOMAIN = 2;
    public static final int TYPE_HEAVENLY_ULTIMATE = 3;
    public static final int TYPE_BUFF_DEFENSE = 4;
    public static final int TYPE_HEAL_REGEN = 5;

    public String id;
    public String name;
    public String chineseName; // e.g. "万剑归宗", "九天玄雷", "太极神印"
    public int skillType;
    public int element; // 0=None, 1=Metal, 2=Wood, 3=Water, 4=Fire, 5=Earth, 6=Thunder, 7=Wind, 8=Yin, 9=Yang
    public float damageMultiplier;
    public int qiCost;
    public float cooldownSec;
    public float currentCooldown;
    public boolean isAoE;
    public int vfxColor;
    public String description;

    public BattleSkill(String id, String name, String chineseName, int skillType, int element,
                       float damageMultiplier, int qiCost, float cooldownSec, boolean isAoE, int vfxColor, String description) {
        this.id = id;
        this.name = name;
        this.chineseName = chineseName;
        this.skillType = skillType;
        this.element = element;
        this.damageMultiplier = damageMultiplier;
        this.qiCost = qiCost;
        this.cooldownSec = cooldownSec;
        this.currentCooldown = 0f;
        this.isAoE = isAoE;
        this.vfxColor = vfxColor;
        this.description = description;
    }

    public boolean isReady(int currentQi) {
        return currentCooldown <= 0.001f && currentQi >= qiCost;
    }

    public void triggerCooldown() {
        this.currentCooldown = cooldownSec;
    }

    public void tick(float dt) {
        if (currentCooldown > 0f) {
            currentCooldown = Math.max(0f, currentCooldown - dt);
        }
    }

    public static BattleSkill createDefaultBasicAttack(int element) {
        return new BattleSkill(
                "skill_basic",
                "Spirit Strike",
                "灵光一击",
                TYPE_MELEE,
                element,
                1.0f,
                0,
                1.2f,
                false,
                0xFF448AFF,
                "Concentrates spiritual Qi onto weapons to deliver a direct strike."
        );
    }

    public static BattleSkill createSwordArrayUltimate() {
        return new BattleSkill(
                "skill_sword_array",
                "10,000 Flying Swords Array",
                "万剑归宗",
                TYPE_HEAVENLY_ULTIMATE,
                1, // Metal / Blade
                3.5f,
                100,
                8.0f,
                true,
                0xFFFFD700,
                "Summons ten thousand celestial flying swords descending in a torrential storm."
        );
    }

    public static BattleSkill createNineHeavensThunder() {
        return new BattleSkill(
                "skill_nine_thunder",
                "Nine Heavens Divine Thunder",
                "九天神雷",
                TYPE_HEAVENLY_ULTIMATE,
                6, // Thunder
                4.2f,
                100,
                9.0f,
                true,
                0xFF9C27B0,
                "Calls down celestial tribulation lightning to obliterate mortal and demonic foes."
        );
    }

    public static BattleSkill createPhoenixInferno() {
        return new BattleSkill(
                "skill_phoenix_inferno",
                "Nirvana Phoenix Domain",
                "凤翼天翔",
                TYPE_HEAVENLY_ULTIMATE,
                4, // Fire
                3.8f,
                100,
                8.5f,
                true,
                0xFFFF3D00,
                "Manifests the immortal phoenix spirit, scorching the battlefield with true fire."
        );
    }

    public static BattleSkill createTaijiShield() {
        return new BattleSkill(
                "skill_taiji_seal",
                "Taiji Eight Trigrams Shield",
                "八卦神盾",
                TYPE_BUFF_DEFENSE,
                5, // Earth
                0.5f,
                50,
                6.0f,
                false,
                0xFF00E676,
                "Erects an indestructible Daoist Yin-Yang barrier, deflecting 50% damage."
        );
    }

    public static BattleSkill createSpiritHealingSpring() {
        return new BattleSkill(
                "skill_healing_spring",
                "Celestial Qi Rejuvenation",
                "青木回春",
                TYPE_HEAL_REGEN,
                2, // Wood
                1.5f,
                60,
                7.0f,
                true,
                0xFF00E5FF,
                "Channels supreme wood vitality to restore HP to all allied cultivators."
        );
    }
}
