package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * FarmingSystemTest - Unit tests for herb cultivation, yield calculations,
 * worker efficiency, and sect resource accumulation.
 */
public class FarmingSystemTest {

    private SectData sectData;

    @Before
    public void setUp() {
        sectData = SectData.getInstance();
        sectData.spiritHerbs = 0;
        sectData.spiritStones = 0;
    }

    @Test
    public void testHarvestYieldCalculation() {
        Disciple farmer = new Disciple("Green Thumb");
        farmer.taskEfficiency = 80;
        farmer.intel = 30;

        FarmingSystem.HarvestResult result = FarmingSystem.harvest(farmer, 2.0f, 1.0f);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.herbs > 0);
        Assert.assertTrue(result.expGained > 0);
        Assert.assertTrue(sectData.spiritHerbs >= result.herbs);
    }

    @Test
    public void testHarvestWithNullWorker() {
        FarmingSystem.HarvestResult result = FarmingSystem.harvest(null, 1.0f, 1.0f);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.herbs >= 1);
    }
}
