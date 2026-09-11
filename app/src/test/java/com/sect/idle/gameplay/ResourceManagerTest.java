package com.sect.idle.gameplay;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ResourceManagerTest - Unit tests for resource balance, transaction safety, and listeners.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public class ResourceManagerTest {

    private ResourceManager resourceManager;

    @Before
    public void setUp() {
        resourceManager = ResourceManager.getInstance();
        SectData data = SectData.getInstance();
        data.reset();
        resourceManager.syncFromSectData(data);
    }

    @Test
    public void testResourceEarningAndSpending() {
        long initialStones = resourceManager.getSpiritStones();
        resourceManager.addSpiritStones(500);
        Assert.assertEquals(initialStones + 500, resourceManager.getSpiritStones());

        boolean canSpend = resourceManager.spendSpiritStones(300);
        Assert.assertTrue(canSpend);
        Assert.assertEquals(initialStones + 200, resourceManager.getSpiritStones());

        boolean cannotOverspend = resourceManager.spendSpiritStones(99999999);
        Assert.assertFalse(cannotOverspend);
    }

    @Test
    public void testHerbsAndPillsTransaction() {
        resourceManager.addSpiritHerbs(50);
        Assert.assertTrue(resourceManager.spendSpiritHerbs(30));
        Assert.assertFalse(resourceManager.spendSpiritHerbs(999999));

        resourceManager.addSpiritPills(5);
        Assert.assertTrue(resourceManager.spendSpiritPills(3));
        Assert.assertFalse(resourceManager.spendSpiritPills(1000));
    }
}
