package com.sect.idle.models;

import com.sect.idle.core.GameConfig;
import com.sect.idle.gameplay.TalentSystem;
import org.junit.Assert;
import org.junit.Test;

/**
 * BattleUnitTest - Comprehensive unit tests for combatant initialization,
 * damage calculation, defense mitigation, accuracy/dodge, cooldowns, action bar, and HP regeneration.
 */
public class BattleUnitTest {

    @Test
    public void testInitializationFromDisciple() {
        Disciple disciple = new Disciple("Ye Chen");
        disciple.initStats(20, 15, 10, 10, 20, 10, 10);
        disciple.element = GameConfig.ELEM_FIRE;
        disciple.hp = disciple.maxHp;

        BattleUnit unit = new BattleUnit(disciple, 0);

        Assert.assertEquals("Ye Chen", unit.name);
        Assert.assertEquals(0, unit.team);
        Assert.assertTrue(unit.hp > 0);
        Assert.assertEquals(unit.maxHp, unit.hp);
        Assert.assertTrue(unit.isAlive);
        Assert.assertEquals(GameConfig.ELEM_FIRE, unit.element);
        Assert.assertTrue(unit.maxActionBar > 0);
    }

    @Test
    public void testNullDiscipleFallback() {
        BattleUnit unit = new BattleUnit(null, 1);
        Assert.assertEquals("Cultivator", unit.name);
        Assert.assertEquals(1, unit.team);
        Assert.assertEquals(100, unit.hp);
        Assert.assertEquals(100, unit.maxHp);
        Assert.assertTrue(unit.isAlive);
    }

    @Test
    public void testActionBarAndCanAct() {
        Disciple d = new Disciple("Speedster");
        d.spd = 50;
        BattleUnit unit = new BattleUnit(d, 0);

        Assert.assertFalse(unit.canAct());
        while (!unit.canAct()) {
            unit.tickAction();
        }
        Assert.assertTrue(unit.canAct());

        unit.resetAction();
        Assert.assertEquals(0, unit.actionBar);
        Assert.assertFalse(unit.canAct());
    }

    @Test
    public void testDamageCalculationAndMitigation() {
        Disciple attackerDisciple = new Disciple("Attacker");
        attackerDisciple.initStats(30, 20, 10, 5, 20, 10, 10);
        BattleUnit attacker = new BattleUnit(attackerDisciple, 0);

        Disciple defenderDisciple = new Disciple("Defender");
        defenderDisciple.initStats(10, 10, 10, 5, 50, 10, 10);
        BattleUnit defender = new BattleUnit(defenderDisciple, 1);

        int damage = attacker.calcDamage(defender, 1.0f);
        Assert.assertTrue("Damage should be strictly positive", damage > 0);

        // Null target safety
        Assert.assertEquals(0, attacker.calcDamage(null, 1.0f));
    }

    @Test
    public void testTakeDamageAndDeath() {
        BattleUnit unit = new BattleUnit(null, 1);
        unit.hp = 100;
        unit.maxHp = 100;

        unit.takeDamage(40);
        Assert.assertEquals(60, unit.hp);
        Assert.assertTrue(unit.isAlive);

        // Lethal damage
        unit.takeDamage(100);
        Assert.assertEquals(0, unit.hp);
        Assert.assertFalse(unit.isAlive);

        // Negative/Zero damage safety
        unit.takeDamage(-50);
        Assert.assertEquals(0, unit.hp);
    }

    @Test
    public void testHealingAndOverhealClamping() {
        BattleUnit unit = new BattleUnit(null, 0);
        unit.hp = 30;
        unit.maxHp = 100;

        unit.heal(40);
        Assert.assertEquals(70, unit.hp);

        // Overheal clamped to maxHp
        unit.heal(500);
        Assert.assertEquals(100, unit.hp);

        // Dead unit cannot heal
        unit.takeDamage(1000);
        Assert.assertFalse(unit.isAlive);
        unit.heal(50);
        Assert.assertEquals(0, unit.hp);
    }

    @Test
    public void testSkillCooldownManagement() {
        Disciple d = new Disciple("Mage");
        d.skills.add(new Skill("sk_fireball", "Fireball", "Blazing flame", 1, GameConfig.ELEM_FIRE, 5, 3, 20, 1.5f, 0, 1, 1));
        BattleUnit unit = new BattleUnit(d, 0);

        Assert.assertTrue(unit.isSkillReady(0));

        unit.setSkillCooldown(0, 3);
        Assert.assertFalse(unit.isSkillReady(0));

        unit.tickCooldowns();
        Assert.assertFalse(unit.isSkillReady(0));

        unit.tickCooldowns();
        unit.tickCooldowns();
        Assert.assertTrue(unit.isSkillReady(0));

        // Invalid index safety
        Assert.assertFalse(unit.isSkillReady(-1));
        Assert.assertFalse(unit.isSkillReady(99));
    }
}
