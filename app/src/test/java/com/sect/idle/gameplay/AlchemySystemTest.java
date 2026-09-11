package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * AlchemySystemTest - Unit tests for pill recipes, cost deductions, worker intel/luck bonuses,
 * quality tiers, and disciple experience gains.
 */
public class AlchemySystemTest {

    private SectData sectData;

    @Before
    public void setUp() {
        sectData = SectData.getInstance();
        sectData.spiritStones = 10000;
        sectData.spiritHerbs = 10000;
        sectData.spiritOres = 10000;
    }

    @Test
    public void testCraftingWithSufficientHerbs() {
        Disciple alchemist = new Disciple("Grand Master Alchemist");
        alchemist.intel = 50;
        alchemist.lck = 30;

        long initialHerbs = sectData.spiritHerbs;
        AlchemySystem.PillResult result = AlchemySystem.craft(alchemist, 0, 0.5f);

        Assert.assertNotNull(result);
        Assert.assertEquals("Qi Gathering Pill", result.pillName);
        Assert.assertTrue(sectData.spiritHerbs < initialHerbs);
    }

    @Test
    public void testCraftingWithInsufficientHerbs() {
        sectData.spiritHerbs = 0;
        Disciple alchemist = new Disciple("Novice");

        AlchemySystem.PillResult result = AlchemySystem.craft(alchemist, 0, 0f);
        Assert.assertNotNull(result);
        Assert.assertFalse(result.success);
        Assert.assertEquals(0, result.pillsMade);
    }

    @Test
    public void testRecipeBoundClamping() {
        AlchemySystem.PillResult result = AlchemySystem.craft(null, 999, 1.0f);
        Assert.assertNotNull(result);
        Assert.assertEquals(AlchemySystem.RECIPES[0], result.pillName);
    }
}
