package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * DiscipleManagerTest - Unit tests for disciple recruitment, task assignment,
 * breakthrough success/failure mechanics, and stat scaling.
 */
public class DiscipleManagerTest {

    @Test
    public void testRandomDiscipleCreation() {
        DiscipleManager mgr = DiscipleManager.getInstance();
        Disciple d = mgr.createRandomDisciple(20, 50, 0, 2);

        Assert.assertNotNull(d);
        Assert.assertNotNull(d.name);
        Assert.assertTrue(d.str >= 20 && d.str <= 50);
        Assert.assertTrue(d.realm >= 0 && d.realm <= 2);
        Assert.assertTrue(d.isAlive());
    }

    @Test
    public void testGenerateInitialDisciples() {
        DiscipleManager mgr = DiscipleManager.getInstance();
        ArrayList<Disciple> list = mgr.generateInitialDisciples(5);

        Assert.assertEquals(5, list.size());
        Assert.assertEquals(GameConfig.TASK_FARMING, list.get(0).currentTask);
        Assert.assertEquals(GameConfig.TASK_ALCHEMY, list.get(1).currentTask);
        Assert.assertEquals(GameConfig.TASK_GUARD, list.get(2).currentTask);
    }

    @Test
    public void testAssignTask() {
        DiscipleManager mgr = DiscipleManager.getInstance();
        Disciple d = new Disciple("Task Worker");

        Assert.assertTrue(mgr.assignTask(d, GameConfig.TASK_MINING));
        Assert.assertEquals(GameConfig.TASK_MINING, d.currentTask);
        Assert.assertFalse(mgr.assignTask(null, GameConfig.TASK_MINING));
    }

    @Test
    public void testBreakthroughMechanics() {
        DiscipleManager mgr = DiscipleManager.getInstance();
        Disciple d = new Disciple("Breakthrough Candidate");
        d.realm = 0;
        d.realmExp = 5000;
        d.wis = 50;

        int initialRealm = d.realm;
        boolean result = mgr.attemptBreakthrough(d);
        if (result) {
            Assert.assertEquals(initialRealm + 1, d.realm);
            Assert.assertTrue(d.str > 0);
        } else {
            Assert.assertEquals(initialRealm, d.realm);
        }

        // Insufficient exp
        d.realmExp = 10;
        Assert.assertFalse(mgr.attemptBreakthrough(d));
    }
}
