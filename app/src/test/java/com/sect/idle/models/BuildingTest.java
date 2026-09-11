package com.sect.idle.models;

import com.sect.idle.core.GameConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * BuildingTest - Unit tests for building construction, level upgrades,
 * worker allocation, production income, and storage capacity.
 */
public class BuildingTest {

    @Test
    public void testConstructionAndInitialState() {
        Building b = new Building(GameConfig.BUILD_HALL, "Grand Main Hall", 10, 500L, 5);

        Assert.assertEquals("Grand Main Hall", b.name);
        Assert.assertEquals(0, b.level);
        Assert.assertFalse(b.isBuilt);
        Assert.assertEquals(0L, b.getDailyIncome());

        b.build();
        Assert.assertTrue(b.isBuilt);
        Assert.assertEquals(1, b.level);
        Assert.assertTrue(b.getDailyIncome() > 0L);
    }

    @Test
    public void testUpgradeProgression() {
        Building b = new Building(GameConfig.BUILD_ALCHEMY, "Alchemy Chamber", 5, 200L, 4);
        b.build();

        long cost1 = b.getUpgradeCost();
        Assert.assertTrue(b.canUpgrade(cost1 + 100));
        Assert.assertFalse(b.canUpgrade(cost1 - 50));

        b.upgrade();
        Assert.assertEquals(2, b.level);
        Assert.assertTrue(b.getUpgradeCost() > cost1);
        Assert.assertTrue(b.maxWorkers > 4);
        Assert.assertTrue(b.getStorageCapacity() > 0);
    }

    @Test
    public void testWorkerAssignment() {
        Building b = new Building(GameConfig.BUILD_GARDEN, "Herb Garden", 5, 100L, 2);
        b.build();

        Assert.assertEquals(0, b.workers);
        Assert.assertTrue(b.assignWorker());
        Assert.assertEquals(1, b.workers);
        Assert.assertTrue(b.assignWorker());
        Assert.assertEquals(2, b.workers);

        // Cannot exceed max workers
        Assert.assertFalse(b.assignWorker());
        Assert.assertEquals(2, b.workers);

        // Remove worker
        Assert.assertTrue(b.removeWorker());
        Assert.assertEquals(1, b.workers);
        Assert.assertTrue(b.removeWorker());
        Assert.assertEquals(0, b.workers);
        Assert.assertFalse(b.removeWorker());
    }
}
