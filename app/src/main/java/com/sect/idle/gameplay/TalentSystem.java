package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import com.sect.idle.models.Talent;
import com.sect.idle.systems.RNG;

/**
 * TalentSystem - Procedural Generation System for Disciple Innate Talents.
 * Generates unique Xianxia talents (e.g. 'Fire Affinity', 'Fast Learner', 'Phoenix Bloodline',
 * 'Sword Heart', 'Alchemical Prodigy') and provides statistical bonuses during training
 * (meditation EXP, breakthrough chance, stone discount, task efficiency) and battle scenarios
 * (ATK/DEF/SPD multipliers, Crit Rate/Dmg, Dodge, Damage Mitigation, Life Steal, HP Regen).
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 * Zero-allocation in runtime hot paths, null-safe, and crash-proof.
 */
public final class TalentSystem {

    private TalentSystem() {}

    // ========== PROCEDURAL NAME DICTIONARIES ==========
    private static final String[] PREFIX_MORTAL = { "Ordinary", "Latent", "Budding", "Basic", "Faint" };
    private static final String[] PREFIX_EARTH = { "Pure", "Sturdy", "Solid", "Keen", "Refined" };
    private static final String[] PREFIX_HEAVEN = { "Flawless", "Ancient", "Radiant", "Vibrant", "Resonant" };
    private static final String[] PREFIX_IMMORTAL = { "Supreme", "True", "Sacred", "Ethereal", "Transcendent" };
    private static final String[] PREFIX_DIVINE = { "Heavenly", "Divine", "Celestial", "Imperial", "Sovereign" };
    private static final String[] PREFIX_PRIMORDIAL = { "Primordial", "Chaos", "Genesis", "Eternal", "Godbound" };

    // ========== ARCHETYPE DEFINITIONS ==========
    private static final int ARCH_ELEMENTAL_FIRE = 0;
    private static final int ARCH_ELEMENTAL_WATER = 1;
    private static final int ARCH_ELEMENTAL_WOOD = 2;
    private static final int ARCH_ELEMENTAL_METAL = 3;
    private static final int ARCH_ELEMENTAL_EARTH = 4;
    private static final int ARCH_ELEMENTAL_LIGHTNING = 5;
    private static final int ARCH_ELEMENTAL_ICE = 6;
    private static final int ARCH_ELEMENTAL_WIND = 7;
    private static final int ARCH_ELEMENTAL_LIGHT = 8;
    private static final int ARCH_ELEMENTAL_SHADOW = 9;

    private static final int ARCH_FAST_LEARNER = 10;
    private static final int ARCH_DAO_HEART = 11;
    private static final int ARCH_ANCIENT_SOUL = 12;
    private static final int ARCH_SEVEN_STARS = 13;

    private static final int ARCH_SWORD_HEART = 14;
    private static final int ARCH_BLADE_SOVEREIGN = 15;
    private static final int ARCH_BATTLE_TRANCE = 16;
    private static final int ARCH_SHADOW_WALKER = 17;
    private static final int ARCH_WISDOM_EYE = 18;

    private static final int ARCH_IRON_BONES = 19;
    private static final int ARCH_PHOENIX_BLOODLINE = 20;
    private static final int ARCH_DRAGON_VEIN = 21;
    private static final int ARCH_TITAN_CONSTITUTION = 22;

    private static final int ARCH_ALCHEMICAL_PRODIGY = 23;
    private static final int ARCH_MASTER_ARTIFICER = 24;
    private static final int ARCH_DIVINE_FARMER = 25;

    private static final int ARCH_HEAVENS_CHOSEN = 26;
    private static final int ARCH_LUCKY_STAR = 27;
    private static final int ARCH_PRIMORDIAL_ROOT = 28;

    private static final int TOTAL_ARCHETYPES = 29;

    /**
     * Procedurally generates a new Talent object using weighted probability and archetype selection.
     */
    public static Talent generateRandomTalent() {
        // Determine Grade via Xianxia rarity distribution
        int roll = RNG.nextInt(100);
        int grade;
        if (roll < 45) {
            grade = Talent.GRADE_MORTAL;      // 45% Mortal
        } else if (roll < 75) {
            grade = Talent.GRADE_EARTH;       // 30% Earth
        } else if (roll < 90) {
            grade = Talent.GRADE_HEAVEN;      // 15% Heaven
        } else if (roll < 97) {
            grade = Talent.GRADE_IMMORTAL;    // 7% Immortal
        } else if (roll < 99) {
            grade = Talent.GRADE_DIVINE;      // 2% Divine
        } else {
            grade = Talent.GRADE_PRIMORDIAL;  // 1% Primordial
        }

        int archetype = RNG.nextInt(TOTAL_ARCHETYPES);
        return buildTalent(archetype, grade);
    }

    /**
     * Generates a talent specifically tailored for a disciple, matching their innate element or stats.
     */
    public static Talent generateTalent(Disciple d) {
        if (d == null) return null;

        // Roll grade
        int roll = RNG.nextInt(100);
        int grade;
        if (roll < 45) grade = Talent.GRADE_MORTAL;
        else if (roll < 75) grade = Talent.GRADE_EARTH;
        else if (roll < 90) grade = Talent.GRADE_HEAVEN;
        else if (roll < 97) grade = Talent.GRADE_IMMORTAL;
        else if (roll < 99) grade = Talent.GRADE_DIVINE;
        else grade = Talent.GRADE_PRIMORDIAL;

        // Choose archetype based on disciple's dominant attribute or element
        int archetype;
        if (RNG.chance(35)) {
            // Elemental affinity matching disciple's element
            switch (d.element) {
                case GameConfig.ELEM_FIRE: archetype = ARCH_ELEMENTAL_FIRE; break;
                case GameConfig.ELEM_WATER: archetype = ARCH_ELEMENTAL_WATER; break;
                case GameConfig.ELEM_WOOD: archetype = ARCH_ELEMENTAL_WOOD; break;
                case GameConfig.ELEM_METAL: archetype = ARCH_ELEMENTAL_METAL; break;
                case GameConfig.ELEM_EARTH: archetype = ARCH_ELEMENTAL_EARTH; break;
                case GameConfig.ELEM_LIGHTNING: archetype = ARCH_ELEMENTAL_LIGHTNING; break;
                case GameConfig.ELEM_ICE: archetype = ARCH_ELEMENTAL_ICE; break;
                case GameConfig.ELEM_WIND: archetype = ARCH_ELEMENTAL_WIND; break;
                case GameConfig.ELEM_LIGHT: archetype = ARCH_ELEMENTAL_LIGHT; break;
                case GameConfig.ELEM_SHADOW: archetype = ARCH_ELEMENTAL_SHADOW; break;
                default: archetype = RNG.nextInt(TOTAL_ARCHETYPES); break;
            }
        } else {
            // Stat-oriented archetype selection
            int maxStat = Math.max(d.str, Math.max(d.intel, Math.max(d.vit, Math.max(d.agi, d.wis))));
            if (maxStat == d.intel && RNG.chance(60)) {
                archetype = RNG.chance(50) ? ARCH_FAST_LEARNER : ARCH_ALCHEMICAL_PRODIGY;
            } else if (maxStat == d.str && RNG.chance(60)) {
                archetype = RNG.chance(50) ? ARCH_SWORD_HEART : ARCH_BATTLE_TRANCE;
            } else if (maxStat == d.vit && RNG.chance(60)) {
                archetype = RNG.chance(50) ? ARCH_IRON_BONES : ARCH_TITAN_CONSTITUTION;
            } else if (maxStat == d.agi && RNG.chance(60)) {
                archetype = RNG.chance(50) ? ARCH_SHADOW_WALKER : ARCH_ELEMENTAL_WIND;
            } else if (maxStat == d.wis && RNG.chance(60)) {
                archetype = RNG.chance(50) ? ARCH_DAO_HEART : ARCH_WISDOM_EYE;
            } else {
                archetype = RNG.nextInt(TOTAL_ARCHETYPES);
            }
        }

        Talent t = buildTalent(archetype, grade);
        d.talent = t;
        d.talentType = mapCategoryToLegacyType(t.category);
        d.talentGrade = t.grade;
        d.talentName = t.name;

        // Apply initial attribute boost from talent
        applyInnateAttributeSurge(d, t);
        d.recalcCombat();
        d.updateEfficiency();
        return t;
    }

    /**
     * Builds the complete procedural Talent with full modifiers and description.
     */
    private static Talent buildTalent(int archetype, int grade) {
        Talent t = new Talent();
        t.id = "T_" + archetype + "_" + grade + "_" + RNG.nextInt(10000);
        t.grade = grade;

        String prefix = getPrefixForGrade(grade);
        float gradeScale = 0.8f + (grade * 0.4f); // Scale multiplier: 1.2x at Earth, 3.2x at Primordial

        switch (archetype) {
            // ----- ELEMENTAL AFFINITIES -----
            case ARCH_ELEMENTAL_FIRE:
                t.category = Talent.CATEGORY_ELEMENTAL;
                t.element = GameConfig.ELEM_FIRE;
                t.color = 0xFFFF5722;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Fire Affinity";
                t.title = "Flame Emperor Descendant";
                t.description = "Innately harmonizes with heavenly flame, supercharging fire combat and alchemy.";
                t.expMultiplier = 1.05f + (0.04f * grade);
                t.elementalDmgBonus = 0.15f + (0.07f * grade);
                t.atkMultiplier = 1.05f + (0.03f * grade);
                t.critRateBonus = 3 + (2 * grade);
                t.taskEfficiencyBonus = 5 + (3 * grade);
                t.preferredTask = GameConfig.TASK_ALCHEMY;
                break;

            case ARCH_ELEMENTAL_WATER:
                t.category = Talent.CATEGORY_ELEMENTAL;
                t.element = GameConfig.ELEM_WATER;
                t.color = 0xFF2196F3;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Water Affinity";
                t.title = "Abyssal Ocean Sage";
                t.description = "Flows like endless tides, granting superior mana endurance and steady Qi recovery.";
                t.expMultiplier = 1.08f + (0.04f * grade);
                t.elementalDmgBonus = 0.15f + (0.06f * grade);
                t.defMultiplier = 1.05f + (0.03f * grade);
                t.hpRegenPct = 0.015f * grade;
                t.taskEfficiencyBonus = 5 + (3 * grade);
                t.preferredTask = GameConfig.TASK_ALCHEMY;
                break;

            case ARCH_ELEMENTAL_WOOD:
                t.category = Talent.CATEGORY_ELEMENTAL;
                t.element = GameConfig.ELEM_WOOD;
                t.color = 0xFF4CAF50;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Wood Affinity";
                t.title = "Life Vitality Sovereign";
                t.description = "Resonates with celestial flora, drastically enhancing farming yield and natural regeneration.";
                t.expMultiplier = 1.06f + (0.04f * grade);
                t.elementalDmgBonus = 0.15f + (0.05f * grade);
                t.hpRegenPct = 0.02f * grade;
                t.defMultiplier = 1.04f + (0.02f * grade);
                t.taskEfficiencyBonus = 8 + (4 * grade);
                t.preferredTask = GameConfig.TASK_FARMING;
                break;

            case ARCH_ELEMENTAL_METAL:
                t.category = Talent.CATEGORY_ELEMENTAL;
                t.element = GameConfig.ELEM_METAL;
                t.color = 0xFF78909C;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Metal Affinity";
                t.title = "White Tiger Blade";
                t.description = "Possesses piercing metallic Qi, boosting weapon forging and armor penetration.";
                t.elementalDmgBonus = 0.15f + (0.06f * grade);
                t.atkMultiplier = 1.08f + (0.04f * grade);
                t.critDmgBonus = 10 + (6 * grade);
                t.taskEfficiencyBonus = 8 + (4 * grade);
                t.preferredTask = GameConfig.TASK_CRAFTING;
                break;

            case ARCH_ELEMENTAL_EARTH:
                t.category = Talent.CATEGORY_ELEMENTAL;
                t.element = GameConfig.ELEM_EARTH;
                t.color = 0xFF8D6E63;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Earth Affinity";
                t.title = "Immovable Mountain Lord";
                t.description = "Grounded in terrestrial veins, granting immense damage mitigation and mining mastery.";
                t.elementalDmgBonus = 0.12f + (0.05f * grade);
                t.defMultiplier = 1.10f + (0.05f * grade);
                t.damageReductionPct = 0.04f * grade;
                t.taskEfficiencyBonus = 8 + (4 * grade);
                t.preferredTask = GameConfig.TASK_MINING;
                break;

            case ARCH_ELEMENTAL_LIGHTNING:
                t.category = Talent.CATEGORY_ELEMENTAL;
                t.element = GameConfig.ELEM_LIGHTNING;
                t.color = 0xFFFFEB3B;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Divine Lightning Stride";
                t.title = "Heavenly Tribulation Bearer";
                t.description = "Wields raw tribulation lightning, vastly accelerating attack speed and critical strikes.";
                t.elementalDmgBonus = 0.20f + (0.08f * grade);
                t.spdMultiplier = 1.08f + (0.04f * grade);
                t.critRateBonus = 5 + (3 * grade);
                t.critDmgBonus = 15 + (8 * grade);
                t.breakthroughRateBonus = 3 + (2 * grade);
                break;

            case ARCH_ELEMENTAL_ICE:
                t.category = Talent.CATEGORY_ELEMENTAL;
                t.element = GameConfig.ELEM_ICE;
                t.color = 0xFF00E5FF;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Glacial Ice Heart";
                t.title = "Frost Absolute";
                t.description = "Calm and unyielding as perpetual frost, reducing enemy agility and protecting vital Qi.";
                t.elementalDmgBonus = 0.15f + (0.06f * grade);
                t.defMultiplier = 1.06f + (0.04f * grade);
                t.damageReductionPct = 0.03f * grade;
                t.expMultiplier = 1.06f + (0.03f * grade);
                break;

            case ARCH_ELEMENTAL_WIND:
                t.category = Talent.CATEGORY_ELEMENTAL;
                t.element = GameConfig.ELEM_WIND;
                t.color = 0xFF80CBC4;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Gale Wind Phantom";
                t.title = "Windborne Wanderer";
                t.description = "Moves like untamed celestial gales, boasting peerless dodge and swift exploration speed.";
                t.elementalDmgBonus = 0.14f + (0.05f * grade);
                t.spdMultiplier = 1.10f + (0.05f * grade);
                t.dodgeBonus = 4 + (3 * grade);
                t.taskEfficiencyBonus = 6 + (3 * grade);
                t.preferredTask = GameConfig.TASK_EXPLORING;
                break;

            case ARCH_ELEMENTAL_LIGHT:
                t.category = Talent.CATEGORY_ELEMENTAL;
                t.element = GameConfig.ELEM_LIGHT;
                t.color = 0xFFFFF59D;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Solar Radiance";
                t.title = "Sun Deity Envoy";
                t.description = "Bathed in righteous solar light, dispelling evil and bolstering ally morale and healing.";
                t.elementalDmgBonus = 0.18f + (0.06f * grade);
                t.expMultiplier = 1.08f + (0.04f * grade);
                t.hpRegenPct = 0.015f * grade;
                t.breakthroughRateBonus = 4 + (2 * grade);
                break;

            case ARCH_ELEMENTAL_SHADOW:
                t.category = Talent.CATEGORY_ELEMENTAL;
                t.element = GameConfig.ELEM_SHADOW;
                t.color = 0xFF7E57C2;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Shadow Void Sovereign";
                t.title = "Abyssal Assassin";
                t.description = "Merges with eternal darkness, excelling in lethal critical strikes and vampiric siphon.";
                t.elementalDmgBonus = 0.18f + (0.07f * grade);
                t.critRateBonus = 6 + (3 * grade);
                t.critDmgBonus = 18 + (9 * grade);
                t.lifeStealPct = 0.04f + (0.025f * grade);
                t.dodgeBonus = 4 + (2 * grade);
                break;

            // ----- CULTIVATION & DAO ARCHETYPES -----
            case ARCH_FAST_LEARNER:
                t.category = Talent.CATEGORY_CULTIVATION;
                t.color = 0xFF29B6F6;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Fast Learner";
                t.title = "Prodigious Scholar";
                t.description = "Comprehends ancient mantras at lightning speed, drastically increasing Realm EXP gains.";
                t.expMultiplier = 1.15f + (0.08f * grade);
                t.breakthroughRateBonus = 4 + (3 * grade);
                t.stoneDiscountMultiplier = 1.0f - (0.05f * grade);
                t.sparringStatBonus = 1 + (grade / 2);
                t.taskEfficiencyBonus = 8 + (3 * grade);
                t.preferredTask = GameConfig.TASK_CULTIVATION;
                break;

            case ARCH_DAO_HEART:
                t.category = Talent.CATEGORY_CULTIVATION;
                t.color = 0xFFAB47BC;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Dao Heart Resonance";
                t.title = "Enlightened Sage";
                t.description = "Possesses an unshakeable Dao Heart that effortlessly overcomes heavenly tribulation bottlenecks.";
                t.expMultiplier = 1.12f + (0.06f * grade);
                t.breakthroughRateBonus = 8 + (4 * grade);
                t.stoneDiscountMultiplier = 1.0f - (0.06f * grade);
                t.damageReductionPct = 0.03f * grade;
                t.preferredTask = GameConfig.TASK_RESEARCH;
                break;

            case ARCH_ANCIENT_SOUL:
                t.category = Talent.CATEGORY_CULTIVATION;
                t.color = 0xFF8E24AA;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Ancient Reincarnated Soul";
                t.title = "Old Monster Reborn";
                t.description = "Retains boundless memories from a previous immortal life, excelling in all training and breakthrough.";
                t.expMultiplier = 1.18f + (0.09f * grade);
                t.breakthroughRateBonus = 10 + (4 * grade);
                t.stoneDiscountMultiplier = 1.0f - (0.07f * grade);
                t.sparringStatBonus = 2 + (grade / 2);
                t.atkMultiplier = 1.06f + (0.03f * grade);
                t.defMultiplier = 1.06f + (0.03f * grade);
                break;

            case ARCH_SEVEN_STARS:
                t.category = Talent.CATEGORY_CULTIVATION;
                t.color = 0xFF5C6BC0;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Seven Stars Comprehension";
                t.title = "Astral Diviner";
                t.description = "Aligns meridian channels with the Big Dipper, drawing continuous astral Qi for breakthroughs.";
                t.expMultiplier = 1.14f + (0.07f * grade);
                t.breakthroughRateBonus = 7 + (3 * grade);
                t.stoneDiscountMultiplier = 1.0f - (0.05f * grade);
                t.accuracyBonus = 8 + (3 * grade);
                break;

            // ----- COMBAT & MARTIAL ARCHETYPES -----
            case ARCH_SWORD_HEART:
                t.category = Talent.CATEGORY_COMBAT;
                t.color = 0xFFE53935;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Sword Heart";
                t.title = "Peerless Sword Immortal";
                t.description = "Unifies soul and blade, dealing devastating critical hits and unblockable piercing damage.";
                t.atkMultiplier = 1.12f + (0.06f * grade);
                t.critRateBonus = 8 + (3 * grade);
                t.critDmgBonus = 25 + (10 * grade);
                t.accuracyBonus = 6 + (2 * grade);
                t.sparringStatBonus = 1 + (grade / 3);
                t.preferredTask = GameConfig.TASK_GUARD;
                break;

            case ARCH_BLADE_SOVEREIGN:
                t.category = Talent.CATEGORY_COMBAT;
                t.color = 0xFFFF7043;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Blade Sovereign";
                t.title = "Unrivaled Duelist";
                t.description = "Dominates close-quarters combat with savage striking power and lightning-fast parries.";
                t.atkMultiplier = 1.14f + (0.06f * grade);
                t.spdMultiplier = 1.06f + (0.03f * grade);
                t.critRateBonus = 6 + (3 * grade);
                t.sparringStatBonus = 1 + (grade / 3);
                break;

            case ARCH_BATTLE_TRANCE:
                t.category = Talent.CATEGORY_COMBAT;
                t.color = 0xFFD81B60;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Asura Battle Trance";
                t.title = "Berserker of the Blood Realm";
                t.description = "Enters a ferocious combat focus, converting damage dealt into vitality while boosting attack power.";
                t.atkMultiplier = 1.15f + (0.07f * grade);
                t.lifeStealPct = 0.05f + (0.03f * grade);
                t.spdMultiplier = 1.08f + (0.03f * grade);
                t.damageReductionPct = 0.03f * grade;
                break;

            case ARCH_SHADOW_WALKER:
                t.category = Talent.CATEGORY_COMBAT;
                t.color = 0xFF3949AB;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Shadow Walker";
                t.title = "Ghostly Phantom";
                t.description = "Steps between spatial rifts, easily dodging enemy lethal blows and striking from blind spots.";
                t.dodgeBonus = 8 + (4 * grade);
                t.spdMultiplier = 1.10f + (0.04f * grade);
                t.critRateBonus = 6 + (2 * grade);
                t.preferredTask = GameConfig.TASK_EXPLORING;
                break;

            case ARCH_WISDOM_EYE:
                t.category = Talent.CATEGORY_COMBAT;
                t.color = 0xFF00ACC1;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Wisdom Eye";
                t.title = "All-Seeing Mind";
                t.description = "Perceives the subtle flaws in all techniques, guaranteeing flawless accuracy and research breakthroughs.";
                t.accuracyBonus = 12 + (5 * grade);
                t.critRateBonus = 5 + (2 * grade);
                t.expMultiplier = 1.08f + (0.04f * grade);
                t.preferredTask = GameConfig.TASK_RESEARCH;
                break;

            // ----- BODY & VITALITY ARCHETYPES -----
            case ARCH_IRON_BONES:
                t.category = Talent.CATEGORY_BODY;
                t.color = 0xFF6D4C41;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Iron Bones";
                t.title = "Vajra Indestructible";
                t.description = "Tempers physical bones into sacred vajra steel, massively increasing defense and damage mitigation.";
                t.defMultiplier = 1.16f + (0.08f * grade);
                t.damageReductionPct = 0.06f * grade;
                t.sparringStatBonus = 1 + (grade / 2);
                t.taskEfficiencyBonus = 8 + (3 * grade);
                t.preferredTask = GameConfig.TASK_GUARD;
                break;

            case ARCH_PHOENIX_BLOODLINE:
                t.category = Talent.CATEGORY_BODY;
                t.element = GameConfig.ELEM_FIRE;
                t.color = 0xFFFF3D00;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Phoenix Bloodline";
                t.title = "Nirvana Reborn";
                t.description = "Inherits the mythical Phoenix lineage, regenerating large amounts of HP in battle and surviving fatal odds.";
                t.hpRegenPct = 0.025f + (0.015f * grade);
                t.defMultiplier = 1.08f + (0.04f * grade);
                t.atkMultiplier = 1.08f + (0.04f * grade);
                t.elementalDmgBonus = 0.15f + (0.05f * grade);
                t.breakthroughRateBonus = 5 + (2 * grade);
                break;

            case ARCH_DRAGON_VEIN:
                t.category = Talent.CATEGORY_BODY;
                t.color = 0xFFFFB300;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Ancient Dragon Vein";
                t.title = "True Dragon Sovereign";
                t.description = "Boasts draconian meridians, providing titanic offensive and defensive attribute increases.";
                t.atkMultiplier = 1.12f + (0.05f * grade);
                t.defMultiplier = 1.12f + (0.05f * grade);
                t.hpRegenPct = 0.015f * grade;
                t.sparringStatBonus = 2 + (grade / 2);
                t.expMultiplier = 1.08f + (0.04f * grade);
                break;

            case ARCH_TITAN_CONSTITUTION:
                t.category = Talent.CATEGORY_BODY;
                t.color = 0xFF546E7A;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Titan Constitution";
                t.title = "Colossus of the Earth";
                t.description = "Possesses an colossal physical build that absorbs devastating blows with ease.";
                t.defMultiplier = 1.18f + (0.08f * grade);
                t.damageReductionPct = 0.05f * grade;
                t.taskEfficiencyBonus = 10 + (4 * grade);
                t.preferredTask = GameConfig.TASK_MINING;
                break;

            // ----- OCCUPATION & CRAFTING ARCHETYPES -----
            case ARCH_ALCHEMICAL_PRODIGY:
                t.category = Talent.CATEGORY_OCCUPATION;
                t.element = GameConfig.ELEM_FIRE;
                t.color = 0xFF00E676;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Alchemical Prodigy";
                t.title = "Grand Alchemist";
                t.description = "Commands flame temperature flawlessly, vastly increasing pill crafting rate and ingredient preservation.";
                t.taskEfficiencyBonus = 15 + (8 * grade);
                t.preferredTask = GameConfig.TASK_ALCHEMY;
                t.expMultiplier = 1.06f + (0.03f * grade);
                t.stoneDiscountMultiplier = 1.0f - (0.04f * grade);
                break;

            case ARCH_MASTER_ARTIFICER:
                t.category = Talent.CATEGORY_OCCUPATION;
                t.element = GameConfig.ELEM_METAL;
                t.color = 0xFFFF9800;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Master Artificer";
                t.title = "Divine Blacksmith";
                t.description = "Extracts pure spiritual essence from spirit ores, doubling equipment forging efficiency.";
                t.taskEfficiencyBonus = 15 + (8 * grade);
                t.preferredTask = GameConfig.TASK_CRAFTING;
                t.atkMultiplier = 1.05f + (0.02f * grade);
                t.defMultiplier = 1.05f + (0.02f * grade);
                break;

            case ARCH_DIVINE_FARMER:
                t.category = Talent.CATEGORY_OCCUPATION;
                t.element = GameConfig.ELEM_WOOD;
                t.color = 0xFF8BC34A;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Divine Farmer";
                t.title = "Spirit Garden Steward";
                t.description = "Blesses spirit herb soils, greatly accelerating herb harvests and sect medicinal output.";
                t.taskEfficiencyBonus = 18 + (9 * grade);
                t.preferredTask = GameConfig.TASK_FARMING;
                t.hpRegenPct = 0.015f * grade;
                break;

            // ----- SPECIAL & DESTINY ARCHETYPES -----
            case ARCH_HEAVENS_CHOSEN:
                t.category = Talent.CATEGORY_SPECIAL;
                t.color = 0xFFFFD700;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Heaven's Chosen";
                t.title = "Child of Destiny";
                t.description = "Blessed by the Heavenly Dao itself, enjoying superior bonuses in cultivation, wealth, and battle.";
                t.expMultiplier = 1.16f + (0.08f * grade);
                t.breakthroughRateBonus = 10 + (5 * grade);
                t.stoneDiscountMultiplier = 1.0f - (0.08f * grade);
                t.atkMultiplier = 1.10f + (0.05f * grade);
                t.defMultiplier = 1.10f + (0.05f * grade);
                t.critRateBonus = 6 + (3 * grade);
                t.critDmgBonus = 20 + (8 * grade);
                t.dodgeBonus = 5 + (2 * grade);
                break;

            case ARCH_LUCKY_STAR:
                t.category = Talent.CATEGORY_SPECIAL;
                t.color = 0xFFFFC107;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Lucky Star";
                t.title = "Fortune Sovereign";
                t.description = "Attracts auspicious encounters, dodging danger and discovering rare spirit treasures effortlessly.";
                t.critRateBonus = 10 + (4 * grade);
                t.dodgeBonus = 8 + (3 * grade);
                t.breakthroughRateBonus = 8 + (4 * grade);
                t.stoneDiscountMultiplier = 1.0f - (0.06f * grade);
                t.taskEfficiencyBonus = 10 + (4 * grade);
                t.preferredTask = GameConfig.TASK_TRADING;
                break;

            case ARCH_PRIMORDIAL_ROOT:
            default:
                t.category = Talent.CATEGORY_SPECIAL;
                t.color = 0xFFFF3D71;
                t.name = (grade >= Talent.GRADE_HEAVEN ? prefix + " " : "") + "Primordial Chaos Root";
                t.title = "All-Origin Dao Ancestor";
                t.description = "Contains the root of cosmic creation, effortlessly mastering all elements, training, and warfare.";
                t.expMultiplier = 1.20f + (0.10f * grade);
                t.breakthroughRateBonus = 12 + (5 * grade);
                t.stoneDiscountMultiplier = 1.0f - (0.10f * grade);
                t.atkMultiplier = 1.15f + (0.07f * grade);
                t.defMultiplier = 1.15f + (0.07f * grade);
                t.spdMultiplier = 1.10f + (0.05f * grade);
                t.critRateBonus = 8 + (4 * grade);
                t.critDmgBonus = 25 + (10 * grade);
                t.hpRegenPct = 0.03f * grade;
                t.damageReductionPct = 0.05f * grade;
                t.elementalDmgBonus = 0.20f + (0.08f * grade);
                t.sparringStatBonus = 2 + (grade / 2);
                break;
        }

        return t;
    }

    private static String getPrefixForGrade(int grade) {
        switch (grade) {
            case Talent.GRADE_MORTAL:     return RNG.pick(PREFIX_MORTAL);
            case Talent.GRADE_EARTH:      return RNG.pick(PREFIX_EARTH);
            case Talent.GRADE_HEAVEN:     return RNG.pick(PREFIX_HEAVEN);
            case Talent.GRADE_IMMORTAL:   return RNG.pick(PREFIX_IMMORTAL);
            case Talent.GRADE_DIVINE:     return RNG.pick(PREFIX_DIVINE);
            case Talent.GRADE_PRIMORDIAL: return RNG.pick(PREFIX_PRIMORDIAL);
            default:                      return "Peerless";
        }
    }

    private static int mapCategoryToLegacyType(int category) {
        switch (category) {
            case Talent.CATEGORY_BODY:        return GameConfig.TAL_BODY;
            case Talent.CATEGORY_CULTIVATION: return GameConfig.TAL_MIND;
            case Talent.CATEGORY_ELEMENTAL:   return GameConfig.TAL_SPIRIT;
            case Talent.CATEGORY_SPECIAL:     return GameConfig.TAL_HEAVEN;
            case Talent.CATEGORY_COMBAT:      return GameConfig.TAL_DUAL;
            case Talent.CATEGORY_OCCUPATION:  return GameConfig.TAL_MIND;
            default:                          return GameConfig.TAL_NONE;
        }
    }

    private static void applyInnateAttributeSurge(Disciple d, Talent t) {
        if (d == null || t == null) return;
        int mult = t.grade;
        switch (t.category) {
            case Talent.CATEGORY_BODY:
                d.vit += 4 * mult;
                d.str += 3 * mult;
                break;
            case Talent.CATEGORY_CULTIVATION:
                d.wis += 4 * mult;
                d.intel += 3 * mult;
                break;
            case Talent.CATEGORY_COMBAT:
                d.str += 4 * mult;
                d.agi += 3 * mult;
                break;
            case Talent.CATEGORY_ELEMENTAL:
                d.intel += 3 * mult;
                d.wis += 2 * mult;
                d.maxMp += 25 * mult;
                break;
            case Talent.CATEGORY_OCCUPATION:
                d.intel += 4 * mult;
                d.cha += 2 * mult;
                break;
            case Talent.CATEGORY_SPECIAL:
                d.lck += 5 * mult;
                d.str += 2 * mult;
                d.vit += 2 * mult;
                d.intel += 2 * mult;
                d.agi += 2 * mult;
                d.wis += 2 * mult;
                break;
        }
    }

    public static void applyGrowth(Disciple d) {
        if (d == null || !d.isAlive()) return;
        int growth = d.talent != null ? d.talent.grade : (d.talentGrade > 0 ? d.talentGrade : 1);
        float multiplier = getTrainingExpMultiplier(d);
        d.realmExp += (int)(growth * 6 * multiplier);

        if (d.realmExp >= GameConfig.REALM_EXP_CAP) {
            d.realmExp = 0;
            if (d.realm < GameConfig.REALM_MAX - 1) {
                d.realm++;
                d.str += 2;
                d.agi += 2;
                d.intel += 2;
                d.vit += 2;
                d.recalcCombat();
            }
        }
    }

    // =========================================================================
    // TRAINING & BREAKTHROUGH MODIFIER GETTERS (HOT PATH OPTIMIZED / NO ALLOCATION)
    // =========================================================================

    public static float getTrainingExpMultiplier(Disciple d) {
        if (d == null || d.talent == null) return 1.0f;
        return d.talent.expMultiplier;
    }

    public static int getBreakthroughRateBonus(Disciple d) {
        if (d == null || d.talent == null) return 0;
        return d.talent.breakthroughRateBonus;
    }

    public static float getBreakthroughStoneDiscount(Disciple d) {
        if (d == null || d.talent == null) return 1.0f;
        return d.talent.stoneDiscountMultiplier;
    }

    public static int getSparringStatBonus(Disciple d) {
        if (d == null || d.talent == null) return 0;
        return d.talent.sparringStatBonus;
    }

    public static int getTaskEfficiencyBonus(Disciple d, int taskType) {
        if (d == null || d.talent == null) return 0;
        int bonus = d.talent.taskEfficiencyBonus;
        if (d.talent.preferredTask != GameConfig.TASK_NONE && d.talent.preferredTask == taskType) {
            bonus += 15; // Additional bonus when working on preferred specialty task
        }
        return bonus;
    }

    // =========================================================================
    // BATTLE & COMBAT SCENARIOS GETTERS (HOT PATH OPTIMIZED / NO ALLOCATION)
    // =========================================================================

    public static float getBattleAtkMultiplier(Disciple d) {
        if (d == null || d.talent == null) return 1.0f;
        return d.talent.atkMultiplier;
    }

    public static float getBattleDefMultiplier(Disciple d) {
        if (d == null || d.talent == null) return 1.0f;
        return d.talent.defMultiplier;
    }

    public static float getBattleSpdMultiplier(Disciple d) {
        if (d == null || d.talent == null) return 1.0f;
        return d.talent.spdMultiplier;
    }

    public static int getBattleCritRateBonus(Disciple d) {
        if (d == null || d.talent == null) return 0;
        return d.talent.critRateBonus;
    }

    public static int getBattleCritDmgBonus(Disciple d) {
        if (d == null || d.talent == null) return 0;
        return d.talent.critDmgBonus;
    }

    public static int getBattleDodgeBonus(Disciple d) {
        if (d == null || d.talent == null) return 0;
        return d.talent.dodgeBonus;
    }

    public static int getBattleAccuracyBonus(Disciple d) {
        if (d == null || d.talent == null) return 0;
        return d.talent.accuracyBonus;
    }

    public static float getBattleHpRegenPct(Disciple d) {
        if (d == null || d.talent == null) return 0f;
        return d.talent.hpRegenPct;
    }

    public static float getBattleDamageReductionPct(Disciple d) {
        if (d == null || d.talent == null) return 0f;
        return d.talent.damageReductionPct;
    }

    public static float getBattleLifeStealPct(Disciple d) {
        if (d == null || d.talent == null) return 0f;
        return d.talent.lifeStealPct;
    }

    public static float getElementalDamageBonus(Disciple d, int attackElement) {
        if (d == null || d.talent == null) return 0f;
        if (d.talent.element != GameConfig.ELEM_NONE && d.talent.element == attackElement) {
            return d.talent.elementalDmgBonus;
        }
        return 0f;
    }
}
