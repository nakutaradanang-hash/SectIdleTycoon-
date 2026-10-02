package com.sect.idle.gameplay

import org.junit.Assert
import org.junit.Test

class CultivationMathEngineTest {

    @Test
    fun testRequiredQiScaling() {
        val qiRealm0 = CultivationMathEngine.calculateRequiredQi(0, 0)
        val qiRealm0Stage5 = CultivationMathEngine.calculateRequiredQi(0, 5)
        val qiRealm1 = CultivationMathEngine.calculateRequiredQi(1, 0)
        val qiRealm3 = CultivationMathEngine.calculateRequiredQi(3, 0)

        Assert.assertEquals(100L, qiRealm0)
        Assert.assertTrue(qiRealm0Stage5 > qiRealm0)
        Assert.assertTrue(qiRealm1 > qiRealm0Stage5)
        Assert.assertTrue(qiRealm3 > qiRealm1)
    }

    @Test
    fun testBreakthroughRateCalculation() {
        val infoLowRealm = CultivationMathEngine.calculateBreakthroughRate(
            realm = 0,
            daoHeart = 80,
            pillBonusPercent = 20,
            arrayLevel = 2,
            hasElementalHarmony = true
        )

        Assert.assertTrue(infoLowRealm.successChancePercent in 70..99)
        Assert.assertTrue(infoLowRealm.miracleEpiphanyChancePercent > 0)
        Assert.assertTrue(infoLowRealm.heartDemonRiskPercent >= 0)

        val infoHighRealm = CultivationMathEngine.calculateBreakthroughRate(
            realm = 7, // Great Ascension
            daoHeart = 20,
            pillBonusPercent = 0,
            arrayLevel = 1,
            hasElementalHarmony = false
        )

        Assert.assertTrue(infoHighRealm.successChancePercent < infoLowRealm.successChancePercent)
        Assert.assertTrue(infoHighRealm.estimatedDamageOnFailure > infoLowRealm.estimatedDamageOnFailure)
    }

    @Test
    fun testWuxingElementalResonance() {
        // Wood -> Fire -> Earth -> Metal -> Water -> Wood
        Assert.assertTrue(CultivationMathEngine.isElementGenerating(CultivationMathEngine.ELEMENT_WOOD, CultivationMathEngine.ELEMENT_FIRE))
        Assert.assertTrue(CultivationMathEngine.isElementGenerating(CultivationMathEngine.ELEMENT_FIRE, CultivationMathEngine.ELEMENT_EARTH))
        Assert.assertTrue(CultivationMathEngine.isElementGenerating(CultivationMathEngine.ELEMENT_WATER, CultivationMathEngine.ELEMENT_WOOD))

        // Wood overcomes Earth, Fire overcomes Metal, Water overcomes Fire
        Assert.assertTrue(CultivationMathEngine.isElementOvercoming(CultivationMathEngine.ELEMENT_WOOD, CultivationMathEngine.ELEMENT_EARTH))
        Assert.assertTrue(CultivationMathEngine.isElementOvercoming(CultivationMathEngine.ELEMENT_WATER, CultivationMathEngine.ELEMENT_FIRE))

        val resonanceMatch = CultivationMathEngine.getArrayResonanceMultiplier(CultivationMathEngine.ELEMENT_WOOD, CultivationMathEngine.ELEMENT_WOOD)
        val resonanceNourish = CultivationMathEngine.getArrayResonanceMultiplier(CultivationMathEngine.ELEMENT_FIRE, CultivationMathEngine.ELEMENT_WOOD)
        val resonanceClash = CultivationMathEngine.getArrayResonanceMultiplier(CultivationMathEngine.ELEMENT_EARTH, CultivationMathEngine.ELEMENT_WOOD)

        Assert.assertEquals(1.35f, resonanceMatch, 0.001f)
        Assert.assertEquals(1.20f, resonanceNourish, 0.001f)
        Assert.assertEquals(0.85f, resonanceClash, 0.001f)
    }

    @Test
    fun testCombatPowerScaling() {
        val cpMortal = CultivationMathEngine.calculateCombatPower(
            str = 10, agi = 10, intel = 10, vit = 10, wis = 10, lck = 10,
            realm = 0, talentGrade = 1
        )
        val cpImmortal = CultivationMathEngine.calculateCombatPower(
            str = 80, agi = 75, intel = 90, vit = 85, wis = 70, lck = 60,
            realm = 4, talentGrade = 5
        )

        Assert.assertTrue(cpMortal > 0)
        Assert.assertTrue(cpImmortal > cpMortal * 10)
    }

    @Test
    fun testLargeNumberFormatting() {
        Assert.assertEquals("500", CultivationMathEngine.formatNumber(500L))
        Assert.assertEquals("1.50K", CultivationMathEngine.formatNumber(1500L))
        Assert.assertEquals("25M", CultivationMathEngine.formatNumber(25000000L))
        Assert.assertEquals("4.20B", CultivationMathEngine.formatNumber(4200000000L))
        Assert.assertEquals("1.25T", CultivationMathEngine.formatNumber(1250000000000L))
    }
}
