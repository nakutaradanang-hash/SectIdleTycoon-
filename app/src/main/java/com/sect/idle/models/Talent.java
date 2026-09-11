package com.sect.idle.models;

import com.sect.idle.core.GameConfig;

/**
 * Talent - Data model representing a procedurally generated disciple innate talent.
 * Encapsulates unique statistical modifiers for both training and combat/battle scenarios.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 * Zero-allocation in runtime hot paths, null-safe, and robust against NPE.
 */
public final class Talent {

    // ========== TALENT CATEGORIES ==========
    public static final int CATEGORY_ELEMENTAL = 0;
    public static final int CATEGORY_CULTIVATION = 1;
    public static final int CATEGORY_COMBAT = 2;
    public static final int CATEGORY_BODY = 3;
    public static final int CATEGORY_OCCUPATION = 4;
    public static final int CATEGORY_SPECIAL = 5;
    public static final int CATEGORY_COUNT = 6;

    // ========== TALENT GRADES / RARITY ==========
    public static final int GRADE_MORTAL = 1;     // Common (Gray)
    public static final int GRADE_EARTH = 2;      // Uncommon (Green)
    public static final int GRADE_HEAVEN = 3;     // Rare (Blue)
    public static final int GRADE_IMMORTAL = 4;   // Epic (Purple)
    public static final int GRADE_DIVINE = 5;     // Legendary (Gold)
    public static final int GRADE_PRIMORDIAL = 6; // Mythic (Crimson)

    // ========== FIELDS ==========
    public String id;
    public String name;
    public String title;
    public String description;
    public int category;
    public int grade;
    public int element;
    public int color;

    // ----- Training & Cultivation Statistical Modifiers -----
    public float expMultiplier;          // e.g. 1.25f = +25% Realm EXP in meditation/cultivation
    public int breakthroughRateBonus;     // e.g. +5 to +25 percentage points to breakthrough chance
    public float stoneDiscountMultiplier; // e.g. 0.85f = 15% discount on breakthrough Spirit Stones
    public int sparringStatBonus;         // Extra stat points earned during sparring/tempering (+1 to +3)
    public int taskEfficiencyBonus;       // General task efficiency bonus (+10 to +40)
    public int preferredTask;             // Preferred task matching this talent root (e.g. TASK_ALCHEMY)

    // ----- Battle & Combat Scenario Modifiers -----
    public float atkMultiplier;           // e.g. 1.15f = +15% Attack
    public float defMultiplier;           // e.g. 1.20f = +20% Defense
    public float spdMultiplier;           // e.g. 1.15f = +15% Action speed / ATB fill
    public int critRateBonus;             // e.g. +5% to +25% Critical Strike chance
    public int critDmgBonus;              // e.g. +15% to +60% Critical Damage bonus
    public int dodgeBonus;                // e.g. +5% to +20% Dodge rate
    public int accuracyBonus;             // e.g. +10% to +25% Accuracy
    public float hpRegenPct;              // e.g. 0.03f = 3% max HP regeneration per battle round
    public float damageReductionPct;      // e.g. 0.10f = 10% flat incoming damage mitigation
    public float elementalDmgBonus;       // e.g. 0.25f = +25% bonus damage when using matching element
    public float lifeStealPct;            // e.g. 0.08f = 8% life steal on dealing attack damage

    public Talent() {
        this.id = "T0";
        this.name = "Mortal Root";
        this.title = "Standard Cultivator";
        this.description = "A standard mortal aptitude with no specialized bonuses.";
        this.category = CATEGORY_CULTIVATION;
        this.grade = GRADE_MORTAL;
        this.element = GameConfig.ELEM_NONE;
        this.color = 0xFFAAAAAA;

        this.expMultiplier = 1.0f;
        this.breakthroughRateBonus = 0;
        this.stoneDiscountMultiplier = 1.0f;
        this.sparringStatBonus = 0;
        this.taskEfficiencyBonus = 0;
        this.preferredTask = GameConfig.TASK_NONE;

        this.atkMultiplier = 1.0f;
        this.defMultiplier = 1.0f;
        this.spdMultiplier = 1.0f;
        this.critRateBonus = 0;
        this.critDmgBonus = 0;
        this.dodgeBonus = 0;
        this.accuracyBonus = 0;
        this.hpRegenPct = 0f;
        this.damageReductionPct = 0f;
        this.elementalDmgBonus = 0f;
        this.lifeStealPct = 0f;
    }

    public Talent(String id, String name, String title, String description,
                  int category, int grade, int element, int color) {
        this();
        this.id = id != null ? id : "T0";
        this.name = name != null ? name : "Innate Talent";
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.category = category;
        this.grade = grade;
        this.element = element;
        this.color = color;
    }

    /**
     * Returns the user-friendly grade title (e.g. "Immortal Grade (Epic)").
     */
    public String getGradeLabel() {
        switch (grade) {
            case GRADE_MORTAL:     return "Mortal Grade [Common]";
            case GRADE_EARTH:      return "Earth Grade [Uncommon]";
            case GRADE_HEAVEN:     return "Heaven Grade [Rare]";
            case GRADE_IMMORTAL:   return "Immortal Grade [Epic]";
            case GRADE_DIVINE:     return "Divine Grade [Legend]";
            case GRADE_PRIMORDIAL: return "Primordial Grade [Mythic]";
            default:               return "Grade " + grade;
        }
    }

    /**
     * Returns a concise multi-line summary of training and battle bonuses.
     */
    public String getFormattedSummary() {
        StringBuilder sb = new StringBuilder(128);
        sb.append(name).append(" (").append(getGradeLabel()).append(")\n");
        if (description != null && description.length() > 0) {
            sb.append("• ").append(description).append("\n");
        }

        // Training effects
        if (expMultiplier > 1.0f) {
            sb.append("• Cultivation EXP: +").append((int)((expMultiplier - 1.0f) * 100)).append("%\n");
        }
        if (breakthroughRateBonus > 0) {
            sb.append("• Breakthrough Chance: +").append(breakthroughRateBonus).append("%\n");
        }
        if (stoneDiscountMultiplier < 1.0f) {
            sb.append("• Breakthrough Stone Cost: -").append((int)((1.0f - stoneDiscountMultiplier) * 100)).append("%\n");
        }
        if (sparringStatBonus > 0) {
            sb.append("• Training Stat Surge: +").append(sparringStatBonus).append(" bonus pts\n");
        }
        if (taskEfficiencyBonus > 0) {
            sb.append("• Task Efficiency: +").append(taskEfficiencyBonus).append("%\n");
        }

        // Battle effects
        if (atkMultiplier > 1.0f) {
            sb.append("• Combat ATK: +").append((int)((atkMultiplier - 1.0f) * 100)).append("%\n");
        }
        if (defMultiplier > 1.0f) {
            sb.append("• Combat DEF: +").append((int)((defMultiplier - 1.0f) * 100)).append("%\n");
        }
        if (spdMultiplier > 1.0f) {
            sb.append("• Action Speed: +").append((int)((spdMultiplier - 1.0f) * 100)).append("%\n");
        }
        if (critRateBonus > 0) {
            sb.append("• Critical Chance: +").append(critRateBonus).append("%\n");
        }
        if (critDmgBonus > 0) {
            sb.append("• Critical Damage: +").append(critDmgBonus).append("%\n");
        }
        if (dodgeBonus > 0) {
            sb.append("• Dodge Rate: +").append(dodgeBonus).append("%\n");
        }
        if (hpRegenPct > 0f) {
            sb.append("• Combat HP Regen: ").append((int)(hpRegenPct * 100)).append("% / round\n");
        }
        if (damageReductionPct > 0f) {
            sb.append("• Damage Mitigation: +").append((int)(damageReductionPct * 100)).append("%\n");
        }
        if (elementalDmgBonus > 0f) {
            sb.append("• ").append(GameConfig.getElementName(element)).append(" Affinity Damage: +")
              .append((int)(elementalDmgBonus * 100)).append("%\n");
        }
        if (lifeStealPct > 0f) {
            sb.append("• Vampiric Life Steal: ").append((int)(lifeStealPct * 100)).append("%\n");
        }

        return sb.toString().trim();
    }
}
