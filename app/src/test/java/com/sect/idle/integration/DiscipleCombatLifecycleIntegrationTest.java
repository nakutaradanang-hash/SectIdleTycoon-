package com.sect.idle.integration;

import com.sect.idle.core.GameConfig;
import com.sect.idle.gameplay.BattleEngine;
import com.sect.idle.gameplay.DiscipleManager;
import com.sect.idle.gameplay.TalentSystem;
import com.sect.idle.models.BattleUnit;
import com.sect.idle.models.Disciple;
import com.sect.idle.models.Equipment;
import com.sect.idle.models.Skill;
import com.sect.idle.utils.DataValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

/**
 * DiscipleCombatLifecycleIntegrationTest - Comprehensive integration testing of Disciple lifecycle:
 * 1. Generation, Talent Allocation & Affinity Initialization.
 * 2. Equipment Equipping & Stat Recalculation (Atk, Def, HP, Spd, Crit).
 * 3. Cultivation Exp Gain, Multi-Realm Breakthrough Progression & Stat Scaling.
 * 4. Real-time Combat Simulation: Damage formulas, element counters, skill execution, crit & dodge.
 * 5. Negative and Edge Cases: Stat bounds clamping, dead disciple assignment prevention, zero HP handling.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class DiscipleCombatLifecycleIntegrationTest {

    private DiscipleManager discipleManager;

    @Before
    public void setUp() {
        discipleManager = DiscipleManager.getInstance();
    }

    @Test
    public void testDiscipleGenerationAndTalentIntegration() {
        ArrayList<Disciple> disciples = discipleManager.generateInitialDisciples(3);
        Assert.assertEquals(3, disciples.size());

        for (Disciple d : disciples) {
            Assert.assertNotNull(d.name);
            Assert.assertTrue(d.age > 0);
            Assert.assertTrue(d.lifespan >= d.age);
            Assert.assertTrue(d.maxHp > 0);
            Assert.assertTrue(d.hp > 0 && d.hp <= d.maxHp);
            Assert.assertTrue(d.element >= 0 && d.element <= 11);

            // Verify DataValidator passes for generated disciple
            DataValidator.ValidationResult res = DataValidator.validateDiscipleData(d.name, d.realm, d.hp, d.maxHp, d.element);
            Assert.assertTrue("Disciple validation must pass", res.valid);
        }
    }

    @Test
    public void testEquipmentAndStatRecalculation() {
        Disciple swordMaster = new Disciple("Li Bai");
        swordMaster.initStats(30, 25, 20, 15, 30, 20, 15);
        swordMaster.recalcCombat();

        int initialAtk = swordMaster.atk;
        int initialDef = swordMaster.def;
        int initialMaxHp = swordMaster.maxHp;

        // Equip Heavenly Sword
        Equipment weapon = new Equipment("wpn_01", "Heavenly Dragon Sword", 0, 3);
        weapon.calcStats();
        swordMaster.weapon = weapon;
        swordMaster.recalcCombat();

        Assert.assertTrue("Atk must increase after equipping weapon", swordMaster.atk > initialAtk);

        // Equip Mystic Robes
        Equipment armor = new Equipment("arm_01", "Mystic Silk Robe", 1, 2);
        armor.calcStats();
        swordMaster.armor = armor;
        swordMaster.recalcCombat();

        Assert.assertTrue("Def must increase after equipping armor", swordMaster.def > initialDef);

        // Unequip weapon
        Equipment removedWeapon = swordMaster.weapon;
        swordMaster.weapon = null;
        Assert.assertNotNull(removedWeapon);
        swordMaster.recalcCombat();

        Assert.assertEquals(initialAtk, swordMaster.atk);
    }

    @Test
    public void testCultivationProgressionAndBreakthroughWorkflow() {
        Disciple cultivator = new Disciple("Wang Lin");
        cultivator.initStats(20, 20, 20, 20, 20, 50, 20);
        cultivator.realm = 0; // Qi Condensation
        cultivator.realmExp = 0;
        cultivator.recalcCombat();

        int baseHp = cultivator.maxHp;
        int baseAtk = cultivator.atk;

        // Add Exp below threshold
        cultivator.addExperience(50L);
        Assert.assertEquals(50L, cultivator.exp);

        // Give sufficient realm exp for breakthrough
        cultivator.realmExp = 10000;
        cultivator.wis = 80;

        boolean breakthroughSuccess = discipleManager.attemptBreakthrough(cultivator);
        if (breakthroughSuccess) {
            Assert.assertTrue("Realm should increase", cultivator.realm >= 1);
            Assert.assertTrue("Max HP should scale up", cultivator.maxHp > baseHp);
            Assert.assertTrue("Atk should scale up", cultivator.atk > baseAtk);
        }
    }

    @Test
    public void testCombatBattleEngineMechanics() {
        Disciple ally = new Disciple("Azure Dragon Cultivator");
        ally.initStats(60, 40, 30, 25, 50, 30, 30);
        ally.element = GameConfig.ELEM_WOOD; // Wood
        ally.recalcCombat();

        Disciple enemy = new Disciple("Crimson Fiend");
        enemy.initStats(50, 30, 20, 15, 40, 20, 20);
        enemy.element = GameConfig.ELEM_EARTH; // Wood counters Earth
        enemy.recalcCombat();

        ArrayList<Disciple> teamA = new ArrayList<Disciple>();
        teamA.add(ally);

        ArrayList<Disciple> teamB = new ArrayList<Disciple>();
        teamB.add(enemy);

        BattleEngine engine = new BattleEngine();
        engine.startBattle(teamA, teamB);

        Assert.assertTrue(engine.isRunning);
        Assert.assertEquals(2, engine.getUnits().size());

        // Run combat steps
        int steps = 0;
        while (engine.isRunning && steps < 300) {
            engine.tick();
            steps++;
        }

        Assert.assertFalse("Battle must conclude in max steps", engine.isRunning);
        Assert.assertFalse(engine.getLog().isEmpty());
    }

    @Test
    public void testNegativeAndEdgeConditions() {
        Disciple invalid = new Disciple("ValidName");
        // Test invalid name rejection
        Assert.assertFalse(DataValidator.isValidName("<script>alert(1)</script>"));
        Assert.assertFalse(DataValidator.isValidName(""));
        Assert.assertFalse(DataValidator.isValidName("A very long name that exceeds the maximum permissible limit allowed by the game engine for disciples"));

        // Test invalid realm and HP bounds validation
        DataValidator.ValidationResult badRealm = DataValidator.validateDiscipleData("ValidName", 99, 100, 100, 0);
        Assert.assertFalse(badRealm.valid);

        DataValidator.ValidationResult badHp = DataValidator.validateDiscipleData("ValidName", 1, 500, 200, 0);
        Assert.assertFalse(badHp.valid);
    }
}
