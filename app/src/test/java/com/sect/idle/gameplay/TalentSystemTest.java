package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import com.sect.idle.models.Talent;
import org.junit.Assert;
import org.junit.Test;

/**
 * TalentSystemTest - Unit tests for procedural talent generation and statistical bonuses.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public class TalentSystemTest {

    @Test
    public void testProceduralTalentGenerationNotNull() {
        Disciple d = new Disciple("Xiao Yan");
        d.element = GameConfig.ELEM_FIRE;
        Talent talent = TalentSystem.generateTalent(d);

        Assert.assertNotNull(talent);
        Assert.assertNotNull(talent.name);
        Assert.assertNotNull(talent.description);
        Assert.assertTrue(talent.name.length() > 0);
        Assert.assertTrue(talent.description.length() > 0);
        Assert.assertTrue(talent.grade >= Talent.GRADE_MORTAL && talent.grade <= Talent.GRADE_PRIMORDIAL);
        Assert.assertNotNull(talent.getGradeLabel());
        Assert.assertNotNull(talent.getFormattedSummary());
    }

    @Test
    public void testTalentModifiersPositiveAndClamped() {
        Disciple d = new Disciple("Lin Dong");
        Talent talent = TalentSystem.generateTalent(d);
        d.talent = talent;

        float expMult = TalentSystem.getTrainingExpMultiplier(d);
        Assert.assertTrue("EXP multiplier should be >= 0.8", expMult >= 0.8f);

        int extraStat = TalentSystem.getSparringStatBonus(d);
        Assert.assertTrue("Sparring stat bonus should be >= 0", extraStat >= 0);

        int breakthroughBonus = TalentSystem.getBreakthroughRateBonus(d);
        Assert.assertTrue("Breakthrough rate bonus should be >= 0", breakthroughBonus >= 0);

        float stoneDiscount = TalentSystem.getBreakthroughStoneDiscount(d);
        Assert.assertTrue("Stone cost discount should be between 0.2 and 1.0", stoneDiscount >= 0.2f && stoneDiscount <= 1.0f);
    }

    @Test
    public void testCombatTalentModifiers() {
        Disciple d = new Disciple("Han Li");
        Talent talent = TalentSystem.generateTalent(d);
        d.talent = talent;

        float atkMult = TalentSystem.getBattleAtkMultiplier(d);
        float defMult = TalentSystem.getBattleDefMultiplier(d);
        float spdMult = TalentSystem.getBattleSpdMultiplier(d);

        Assert.assertTrue("ATK multiplier should be >= 1.0", atkMult >= 1.0f);
        Assert.assertTrue("DEF multiplier should be >= 1.0", defMult >= 1.0f);
        Assert.assertTrue("SPD multiplier should be >= 1.0", spdMult >= 1.0f);

        int critRate = TalentSystem.getBattleCritRateBonus(d);
        int dodge = TalentSystem.getBattleDodgeBonus(d);
        Assert.assertTrue("Crit rate bonus should be >= 0", critRate >= 0);
        Assert.assertTrue("Dodge bonus should be >= 0", dodge >= 0);
    }

    @Test
    public void testElementalAffinityBonus() {
        Disciple d = new Disciple("Fire Cultivator");
        d.element = GameConfig.ELEM_FIRE;
        Talent talent = TalentSystem.generateTalent(d);
        d.talent = talent;

        float fireBonus = TalentSystem.getElementalDamageBonus(d, GameConfig.ELEM_FIRE);
        Assert.assertTrue("Fire bonus should be >= 0.0", fireBonus >= 0.0f);
    }

    @Test
    public void testNullSafety() {
        // Defensive zero-NPE verification
        Assert.assertEquals(1.0f, TalentSystem.getTrainingExpMultiplier(null), 0.001f);
        Assert.assertEquals(0, TalentSystem.getSparringStatBonus(null));
        Assert.assertEquals(0, TalentSystem.getBreakthroughRateBonus(null));
        Assert.assertEquals(1.0f, TalentSystem.getBreakthroughStoneDiscount(null), 0.001f);
        Assert.assertEquals(1.0f, TalentSystem.getBattleAtkMultiplier(null), 0.001f);
        Assert.assertEquals(0, TalentSystem.getBattleCritRateBonus(null));
        Assert.assertEquals(0.0f, TalentSystem.getBattleLifeStealPct(null), 0.001f);
    }
}
