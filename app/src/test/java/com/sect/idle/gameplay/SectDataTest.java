package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SectDataTest - Unit tests for Sect data persistence, disciples, and building management.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public class SectDataTest {

    private SectData sectData;

    @Before
    public void setUp() {
        sectData = SectData.getInstance();
        sectData.reset();
    }

    @Test
    public void testInitialDefaultValues() {
        Assert.assertEquals("Cloud Mist Sect", sectData.sectName);
        Assert.assertEquals(1000L, sectData.spiritStones);
        Assert.assertEquals(100L, sectData.spiritHerbs);
        Assert.assertEquals(10L, sectData.spiritPills);
        Assert.assertEquals(50L, sectData.spiritOres);
        Assert.assertEquals(50L, sectData.jade);
        Assert.assertEquals(20L, sectData.essence);
        Assert.assertTrue("Default buildings should be initialized", sectData.buildings.size() > 0);
    }

    @Test
    public void testAddAndRemoveDisciple() {
        int initialCount = sectData.disciples.size();
        Disciple disciple = new Disciple("Lin Feng");

        sectData.addDisciple(disciple);
        Assert.assertEquals(initialCount + 1, sectData.disciples.size());

        sectData.removeDisciple(disciple);
        Assert.assertEquals(initialCount, sectData.disciples.size());
    }

    @Test
    public void testResetFunctionality() {
        sectData.spiritStones = 999999;
        sectData.sectRank = 5;
        Disciple disciple = new Disciple("Xiao Yan");
        sectData.addDisciple(disciple);

        sectData.reset();

        Assert.assertEquals(1000L, sectData.spiritStones);
        Assert.assertEquals(1, sectData.sectRank);
        Assert.assertEquals(0, sectData.disciples.size());
        Assert.assertTrue("Buildings must be re-initialized after reset", sectData.buildings.size() > 0);
    }
}
