package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TrainingSystemTest - Unit tests for disciple cultivation experience and realm breakthroughs.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public class TrainingSystemTest {

    private TrainingSystem trainingSystem;

    @Before
    public void setUp() {
        trainingSystem = TrainingSystem.getInstance();
    }

    @Test
    public void testBreakthroughRequirements() {
        int expNeededRealm0 = trainingSystem.getRequiredExp(0);
        int expNeededRealm1 = trainingSystem.getRequiredExp(1);
        long stoneCostRealm0 = trainingSystem.getRequiredSpiritStones(0);

        Assert.assertTrue("Higher realms should require more realm exp", expNeededRealm1 > expNeededRealm0);
        Assert.assertTrue("Breakthrough stone cost should be positive", stoneCostRealm0 > 0);
    }

    @Test
    public void testRealmNames() {
        String realm0Name = GameConfig.getRealmName(0);
        String realm1Name = GameConfig.getRealmName(1);

        Assert.assertNotNull(realm0Name);
        Assert.assertNotNull(realm1Name);
        Assert.assertTrue("Realm name should not be empty", realm0Name.length() > 0);
    }

    @Test
    public void testCalculateBreakthroughChance() {
        Disciple disciple = new Disciple("Han Li");
        SectData sectData = SectData.getInstance();
        int chance = trainingSystem.getBreakthroughSuccessRate(disciple, sectData);

        Assert.assertTrue("Chance should be between 0 and 100", chance >= 0 && chance <= 100);
    }
}
