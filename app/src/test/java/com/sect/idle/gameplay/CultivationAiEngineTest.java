package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * CultivationAiEngineTest - Unit tests for autonomous task assignment,
 * sect workforce optimization, and contextual cultivation guidance.
 */
public class CultivationAiEngineTest {

    @Test
    public void testAutoAssignOptimalTaskBasedOnStats() {
        // High intel disciple should be assigned Alchemy or Research
        Disciple alchemist = new Disciple("Xiao Yan");
        alchemist.initStats(10, 10, 50, 10, 10, 10, 10);
        CultivationAiEngine.autoAssignOptimalTask(alchemist);
        Assert.assertEquals(GameConfig.TASK_ALCHEMY, alchemist.currentTask);

        // High str disciple should be assigned Crafting
        Disciple blacksmith = new Disciple("Tang San");
        blacksmith.initStats(60, 10, 10, 10, 10, 10, 10);
        CultivationAiEngine.autoAssignOptimalTask(blacksmith);
        Assert.assertEquals(GameConfig.TASK_CRAFTING, blacksmith.currentTask);

        // High vit disciple should be assigned Mining
        Disciple miner = new Disciple("Shi Hao");
        miner.initStats(10, 10, 10, 10, 60, 10, 10);
        CultivationAiEngine.autoAssignOptimalTask(miner);
        Assert.assertEquals(GameConfig.TASK_MINING, miner.currentTask);
    }

    @Test
    public void testOptimizeSectWorkforce() {
        ArrayList<Disciple> list = new ArrayList<Disciple>();
        Disciple d1 = new Disciple("Disciple 1");
        d1.initStats(40, 10, 10, 10, 10, 10, 10);
        d1.currentTask = GameConfig.TASK_NONE;
        list.add(d1);

        CultivationAiEngine.optimizeSectWorkforce(list);
        Assert.assertNotEquals(GameConfig.TASK_NONE, d1.currentTask);
    }

    @Test
    public void testCultivationTips() {
        Disciple d = new Disciple("Han Li");
        d.stress = 80;
        String tipStress = CultivationAiEngine.suggestCultivationTip(d);
        Assert.assertTrue(tipStress.contains("stressed"));

        d.stress = 0;
        d.energy = 10;
        String tipEnergy = CultivationAiEngine.suggestCultivationTip(d);
        Assert.assertTrue(tipEnergy.contains("exhausted") || tipEnergy.contains("Qi"));

        d.energy = 100;
        d.realmExp = (int)(GameConfig.REALM_EXP_CAP * 0.9f);
        String tipBreakthrough = CultivationAiEngine.suggestCultivationTip(d);
        Assert.assertTrue(tipBreakthrough.contains("breakthrough"));
    }
}
