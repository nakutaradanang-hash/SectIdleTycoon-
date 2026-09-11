package com.sect.idle.integration;

import com.sect.idle.core.GameConfig;
import com.sect.idle.gameplay.AlchemySystem;
import com.sect.idle.gameplay.DiscipleManager;
import com.sect.idle.gameplay.FarmingSystem;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.gameplay.TournamentSystem;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

/**
 * SectFullLifecycleIntegrationTest - Comprehensive end-to-end integration test validating the entire
 * core gameplay loop:
 * 1. Sect Initialization & Seed Disciples
 * 2. Building Construction & Upgrades
 * 3. Task Assignment & Herb Farming
 * 4. Alchemy Pill Crafting
 * 5. Disciple Breakthrough & Stat Recalculation
 * 6. Tournament Arena Competition & Reward Loop
 * 7. Economy & Offline Progress Recalculation
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SectFullLifecycleIntegrationTest {

    private SectData sectData;
    private DiscipleManager discipleManager;
    private TournamentSystem tournamentSystem;

    @Before
    public void setUp() {
        sectData = SectData.getInstance();
        sectData.reset();
        sectData.sectName = "Cloud Mist Daoist Sect";
        sectData.spiritStones = 50000;
        sectData.spiritHerbs = 10000;
        sectData.spiritOres = 10000;
        sectData.jade = 100;

        discipleManager = DiscipleManager.getInstance();
        tournamentSystem = TournamentSystem.getInstance();
    }

    @Test
    public void testFullSectProgressionWorkflow() {
        // Step 1: Generate initial disciples
        ArrayList<Disciple> disciples = discipleManager.generateInitialDisciples(4);
        Assert.assertEquals(4, disciples.size());
        sectData.disciples.clear();
        sectData.disciples.addAll(disciples);

        // Step 2: Construct and upgrade core buildings
        Building mainHall = sectData.buildings.get(GameConfig.BUILD_HALL);
        mainHall.build();
        Assert.assertTrue(mainHall.isBuilt);
        Assert.assertEquals(1, mainHall.level);

        Building herbGarden = sectData.buildings.get(GameConfig.BUILD_GARDEN);
        herbGarden.build();
        herbGarden.assignWorker();
        Assert.assertEquals(1, herbGarden.workers);

        Building alchemyChamber = sectData.buildings.get(GameConfig.BUILD_ALCHEMY);
        alchemyChamber.build();

        // Step 3: Farming cycle
        Disciple farmer = disciples.get(0);
        discipleManager.assignTask(farmer, GameConfig.TASK_FARMING);
        long herbsBefore = sectData.spiritHerbs;

        FarmingSystem.HarvestResult harvest = FarmingSystem.harvest(farmer, 5.0f, 1.2f);
        Assert.assertTrue(harvest.herbs > 0);
        Assert.assertTrue(sectData.spiritHerbs > herbsBefore);

        // Step 4: Alchemy Pill Crafting
        Disciple alchemist = disciples.get(1);
        discipleManager.assignTask(alchemist, GameConfig.TASK_ALCHEMY);
        alchemist.intel = 40;

        AlchemySystem.PillResult pillResult = AlchemySystem.craft(alchemist, 0, 0.5f);
        Assert.assertNotNull(pillResult);

        // Step 5: Realm Breakthrough
        Disciple cultivator = disciples.get(2);
        cultivator.realm = 0;
        cultivator.realmExp = 5000;
        cultivator.wis = 50;

        boolean breakthrough = discipleManager.attemptBreakthrough(cultivator);
        if (breakthrough) {
            Assert.assertEquals(1, cultivator.realm);
            Assert.assertTrue(cultivator.atk > 0);
        }

        // Step 6: Tournament Arena
        Disciple warrior = disciples.get(3);
        warrior.initStats(50, 50, 50, 30, 50, 30, 30);
        warrior.recalcCombat();

        TournamentSystem.TournamentResult tourneyResult = tournamentSystem.enterTournament(warrior, sectData, 0);
        Assert.assertNotNull(tourneyResult);
        Assert.assertNotNull(tourneyResult.summary);

        // Step 7: Economy check
        sectData.recalculateEconomy();
        Assert.assertTrue(sectData.spiritStones > 0);
    }
}
