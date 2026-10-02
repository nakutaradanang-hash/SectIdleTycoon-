package com.sect.idle.gameplay

import java.util.Locale
import kotlin.math.*

/**
 * CultivationMathEngine - Professional Numerical, Mathematical & Algorithmic Progression Engine.
 *
 * Implements balanced, non-linear mathematical formulas for:
 * 1. Exponential Qi scaling & minor realm stage thresholds.
 * 2. Dynamic Heavenly Tribulation breakthrough success, miracle epiphanies & heart-demon perils.
 * 3. Wuxing 5-Element Generating & Overcoming resonance matrices.
 * 4. Sect Dragon Vein & Leyline compounding resource compounding.
 * 5. Multi-attribute Combat Power (CP) tensor calculus.
 * 6. High-precision large number formatting (Xianxia spiritual units & short metric notation).
 */
object CultivationMathEngine {

    const val ELEMENT_WOOD = 0
    const val ELEMENT_FIRE = 1
    const val ELEMENT_EARTH = 2
    const val ELEMENT_METAL = 3
    const val ELEMENT_WATER = 4

    val ELEMENT_NAMES = arrayOf("Wood", "Fire", "Earth", "Metal", "Water")
    val ELEMENT_EMOJIS = arrayOf("🌿", "🔥", "⛰️", "⚔️", "💧")

    val REALM_NAMES = arrayOf(
        "Qi Condensation",
        "Foundation Establishment",
        "Core Formation",
        "Nascent Soul",
        "Soul Formation",
        "Void Refining",
        "Body Integration",
        "Great Ascension",
        "True Immortal",
        "Golden Immortal",
        "Primordial Chaos"
    )

    private val NUMBER_SUFFIXES = arrayOf(
        "", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc",
        "Spirit Dao", "Immortal Core", "Heavenly Origin", "Chaos Primordium"
    )

    // =========================================================================
    // 1. QI & REALM PROGRESSION MATHEMATICS
    // =========================================================================

    /**
     * Calculates the required Qi experience for a given major realm and minor stage (0-9).
     * Formula: Base * (1 + realm)^2.85 * (1.18)^minorStage
     */
    fun calculateRequiredQi(realm: Int, minorStage: Int = 0): Long {
        val safeRealm = realm.coerceAtLeast(0)
        val safeStage = minorStage.coerceIn(0, 9)
        val base = 100.0
        val realmMultiplier = (1.0 + safeRealm).pow(2.85)
        val stageMultiplier = 1.18.pow(safeStage.toDouble())
        return (base * realmMultiplier * stageMultiplier).roundToLong().coerceAtLeast(100L)
    }

    /**
     * Calculates dynamic breakthrough success probability (in percent 0-100).
     */
    fun calculateBreakthroughRate(
        realm: Int,
        daoHeart: Int,
        pillBonusPercent: Int,
        arrayLevel: Int,
        hasElementalHarmony: Boolean
    ): BreakthroughChanceInfo {
        val baseRate = (85.0 - (realm * 6.5)).coerceIn(20.0, 85.0)
        val daoHeartBoost = (daoHeart.coerceIn(0, 100) / 100.0) * 15.0
        val pillBoost = pillBonusPercent.coerceIn(0, 50).toDouble()
        val arrayBonus = (arrayLevel.coerceIn(1, 10) - 1) * 2.5
        val harmonyBonus = if (hasElementalHarmony) 8.0 else 0.0

        val totalChance = (baseRate + daoHeartBoost + pillBoost + arrayBonus + harmonyBonus).coerceIn(5.0, 99.0)
        val miracleChance = (2.0 + (daoHeart / 30.0) + (if (hasElementalHarmony) 3.0 else 0.0)).coerceIn(1.0, 15.0)
        val heartDemonRisk = (100.0 - totalChance) * 0.4

        return BreakthroughChanceInfo(
            successChancePercent = totalChance.roundToInt(),
            miracleEpiphanyChancePercent = miracleChance.roundToInt(),
            heartDemonRiskPercent = heartDemonRisk.roundToInt(),
            estimatedDamageOnFailure = calculateTribulationDamage(realm)
        )
    }

    /**
     * Calculates potential HP damage sustained during a failed Tribulation.
     */
    fun calculateTribulationDamage(realm: Int): Int {
        val baseDamage = 30 + (realm * 25)
        val variance = (baseDamage * 0.2).toInt()
        return (baseDamage + (-variance..variance).random()).coerceAtLeast(10)
    }

    // =========================================================================
    // 2. WUXING 5-ELEMENT RESONANCE & GENERATING HARMONY
    // =========================================================================

    /**
     * Checks if element A generates element B (Wood -> Fire -> Earth -> Metal -> Water -> Wood).
     * Yields +25% Qi gathering efficiency.
     */
    fun isElementGenerating(sourceElement: Int, targetElement: Int): Boolean {
        val s = (sourceElement % 5 + 5) % 5
        val t = (targetElement % 5 + 5) % 5
        return (s + 1) % 5 == t
    }

    /**
     * Checks if element A overcomes element B (Wood -> Earth -> Water -> Fire -> Metal -> Wood).
     * Yields +35% combat damage.
     */
    fun isElementOvercoming(attackerElement: Int, defenderElement: Int): Boolean {
        val a = (attackerElement % 5 + 5) % 5
        val d = (defenderElement % 5 + 5) % 5
        return (a + 2) % 5 == d
    }

    /**
     * Computes the elemental affinity multiplier for cultivation array resonance.
     */
    fun getArrayResonanceMultiplier(discipleElement: Int, arrayElement: Int): Float {
        return when {
            discipleElement == arrayElement -> 1.35f // Perfect affinity
            isElementGenerating(arrayElement, discipleElement) -> 1.20f // Harmonic nourishment
            isElementOvercoming(arrayElement, discipleElement) -> 0.85f // Suppressive clash
            else -> 1.0f
        }
    }

    // =========================================================================
    // 3. COMBAT POWER (CP) TENSOR FORMULA
    // =========================================================================

    /**
     * Multi-attribute combat power tensor formula with non-linear scaling.
     */
    fun calculateCombatPower(
        str: Int,
        agi: Int,
        intel: Int,
        vit: Int,
        wis: Int,
        lck: Int,
        realm: Int,
        talentGrade: Int
    ): Long {
        val basePower = (str * 2.5) + (agi * 2.0) + (intel * 2.2) + (vit * 3.0) + (wis * 1.5) + (lck * 1.2)
        val realmMultiplier = (1.0 + realm.coerceAtLeast(0)).pow(1.75)
        val talentBonus = 1.0 + (talentGrade.coerceIn(1, 5) * 0.12)
        return (basePower * realmMultiplier * talentBonus).roundToLong().coerceAtLeast(10L)
    }

    // =========================================================================
    // 4. SECT DRAGON VEIN PROSPERITY & COMPOUNDING
    // =========================================================================

    /**
     * Computes the daily yield rate based on active disciples, dragon vein level, and array bonuses.
     */
    fun calculateDragonVeinYield(
        activeWorkers: Int,
        baseRatePerWorker: Float,
        dragonVeinLevel: Int,
        sectRank: Int
    ): Long {
        val workerBase = activeWorkers.coerceAtLeast(0) * baseRatePerWorker.toDouble()
        val veinMultiplier = (1.0 + (dragonVeinLevel.coerceIn(1, 20) * 0.15)).pow(1.12)
        val rankMultiplier = 1.0 + (sectRank.coerceIn(1, 10) * 0.10)
        return (workerBase * veinMultiplier * rankMultiplier).roundToLong()
    }

    // =========================================================================
    // 5. LARGE NUMBER FORMATTING (XIANXIA SPIRITUAL & SHORT METRIC)
    // =========================================================================

    /**
     * Formats large numeric amounts into elegant, intuitive strings (e.g. 1.25K, 4.50M, 12.80B).
     */
    fun formatNumber(value: Long): String {
        if (value < 1000L && value > -1000L) return value.toString()
        val absVal = abs(value.toDouble())
        val exponent = (log10(absVal) / 3.0).toInt().coerceIn(0, NUMBER_SUFFIXES.size - 1)
        val scaled = value.toDouble() / 10.0.pow(exponent * 3.0)
        val suffix = NUMBER_SUFFIXES[exponent]

        return if (scaled == scaled.roundToLong().toDouble()) {
            String.format(Locale.US, "%.0f%s", scaled, suffix)
        } else {
            String.format(Locale.US, "%.2f%s", scaled, suffix)
        }
    }

    /**
     * Formats with explicit spiritual unit labeling.
     */
    fun formatSpiritualResource(value: Long, resourceName: String): String {
        return "${formatNumber(value)} $resourceName"
    }

    data class BreakthroughChanceInfo(
        val successChancePercent: Int,
        val miracleEpiphanyChancePercent: Int,
        val heartDemonRiskPercent: Int,
        val estimatedDamageOnFailure: Int
    )
}
