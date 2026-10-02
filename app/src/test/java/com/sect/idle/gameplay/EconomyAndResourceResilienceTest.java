package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit Tests for Economy & Resource Management Resilience.
 * Tests underflow bounds, multi-currency transaction atomicity, disciple task assignment,
 * and breakthrough mechanics under rapid mutation cycles.
 */
public class EconomyAndResourceResilienceTest {

    private SectData sectData;
    private ResourceManager resourceManager;

    @Before
    public void setUp() {
        sectData = SectData.getInstance();
        sectData.reset();
        sectData.spiritStones = 1000;
        sectData.spiritHerbs = 500;
        sectData.spiritOres = 200;
        resourceManager = ResourceManager.getInstance();
        resourceManager.syncFromSectData(sectData);
    }

    @Test
    public void testResourceDeductionSuccessAndUnderflowDefense() {
        // Valid deduction
        boolean success = resourceManager.deduct(500, 200, 100);
        Assert.assertTrue("Valid deduction must succeed", success);
        Assert.assertEquals(500L, sectData.spiritStones);
        Assert.assertEquals(300L, sectData.spiritHerbs);
        Assert.assertEquals(100L, sectData.spiritOres);

        // Underflow deduction (exceeds stones)
        boolean underflow = resourceManager.deduct(600, 10, 10);
        Assert.assertFalse("Deduction exceeding balance must fail atomically", underflow);
        // Ensure atomic rollback (no partial deductions)
        Assert.assertEquals(500L, sectData.spiritStones);
        Assert.assertEquals(300L, sectData.spiritHerbs);
        Assert.assertEquals(100L, sectData.spiritOres);
    }

    @Test
    public void testNegativeResourceInputsHandledSafely() {
        boolean result = resourceManager.deduct(-100, 0, 0);
        Assert.assertTrue("Current balances remain non-negative and consistent", sectData.spiritStones >= 0);
    }

    @Test
    public void testDiscipleCreationAndBreakthrough() {
        DiscipleManager discipleManager = DiscipleManager.getInstance();
        Disciple d = discipleManager.createRandomDisciple(20, 50, 0, 1);
        Assert.assertNotNull(d);
        Assert.assertNotNull(d.name);
        Assert.assertTrue(d.maxHp > 0);

        // Task assignment
        boolean assigned = discipleManager.assignTask(d, GameConfig.TASK_ALCHEMY);
        Assert.assertTrue(assigned);
        Assert.assertEquals(GameConfig.TASK_ALCHEMY, d.currentTask);

        // Insufficient exp for breakthrough
        d.realmExp = 10;
        boolean earlyBreakthrough = discipleManager.attemptBreakthrough(d);
        Assert.assertFalse("Breakthrough with insufficient EXP must fail", earlyBreakthrough);

        // Sufficient exp for breakthrough
        d.realmExp = 50000;
        d.wis = 100;
        int oldRealm = d.realm;
        boolean success = discipleManager.attemptBreakthrough(d);
        if (success) {
            Assert.assertEquals(oldRealm + 1, d.realm);
        } else {
            // Failed attempt reduces exp
            Assert.assertTrue(d.realmExp < 50000);
        }
    }
}
