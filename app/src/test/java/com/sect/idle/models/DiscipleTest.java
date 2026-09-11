package com.sect.idle.models;

import com.sect.idle.core.GameConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * DiscipleTest - Unit tests for disciple attributes, combat stat recalculation,
 * realm progression, tasks & efficiency, experience gain, and lifespan.
 */
public class DiscipleTest {

    @Test
    public void testDefaultInitialization() {
        Disciple d = new Disciple("Lin Feng");
        Assert.assertEquals("Lin Feng", d.name);
        Assert.assertNotNull(d.id);
        Assert.assertTrue(d.age >= 16);
        Assert.assertTrue(d.lifespan > d.age);
        Assert.assertTrue(d.isAlive());
        Assert.assertTrue(d.canWork());
        Assert.assertTrue(d.canFight());
        Assert.assertEquals(100, d.energy);
        Assert.assertEquals(0, d.stress);
        Assert.assertEquals(50, d.mood);
    }

    @Test
    public void testStatInitializationAndCombatRecalculation() {
        Disciple d = new Disciple("Xiao Yan");
        d.initStats(20, 15, 10, 8, 25, 12, 14);

        Assert.assertEquals(20, d.str);
        Assert.assertEquals(15, d.agi);
        Assert.assertEquals(10, d.intel);
        Assert.assertEquals(8, d.lck);
        Assert.assertEquals(25, d.vit);
        Assert.assertEquals(12, d.wis);
        Assert.assertEquals(14, d.cha);
        Assert.assertEquals(104, d.getTotalStats());

        Assert.assertTrue(d.maxHp > 100);
        Assert.assertTrue(d.maxMp > 50);
        Assert.assertTrue(d.atk > 0);
        Assert.assertTrue(d.def > 0);
        Assert.assertTrue(d.spd > 0);
        Assert.assertTrue(d.getPowerRating() > 0f);
    }

    @Test
    public void testExperienceAndLevelProgression() {
        Disciple d = new Disciple("Han Li");
        int initialLevel = d.level;
        int initialStr = d.str;

        boolean leveledUp = d.addExperience(150);
        Assert.assertTrue(leveledUp);
        Assert.assertTrue(d.level > initialLevel);
        Assert.assertTrue(d.str > initialStr);
    }

    @Test
    public void testEquipmentStatApplication() {
        Disciple d = new Disciple("Swordsman");
        d.initStats(10, 10, 10, 10, 10, 10, 10);
        int baseAtk = d.atk;

        Equipment sword = new Equipment("wp_1", "Iron Sword", 0, GameConfig.RARITY_COMMON);
        sword.atk = 50;
        d.weapon = sword;
        d.recalcCombat();

        Assert.assertTrue(d.atk > baseAtk);
    }

    @Test
    public void testTaskEfficiency() {
        Disciple d = new Disciple("Worker");
        d.initStats(30, 20, 20, 10, 30, 10, 10);
        d.currentTask = GameConfig.TASK_FARMING;

        d.updateEfficiency();
        Assert.assertTrue(d.taskEfficiency >= 50);
        Assert.assertTrue(d.getTaskIncome() > 0);
    }

    @Test
    public void testDailyTickProgression() {
        Disciple d = new Disciple("Cultivator");
        d.currentTask = GameConfig.TASK_CULTIVATION;
        int initialAge = d.age;
        int initialExp = d.realmExp;

        d.dailyTick();
        Assert.assertEquals(initialAge + 1, d.age);
        Assert.assertTrue(d.realmExp > initialExp);
    }

    @Test
    public void testDamageAndHealing() {
        Disciple d = new Disciple("Target");
        d.hp = 100;
        d.maxHp = 100;

        d.takeDamage(40);
        Assert.assertEquals(60, d.hp);

        d.heal(30);
        Assert.assertEquals(90, d.hp);

        // Overheal clamped
        d.heal(100);
        Assert.assertEquals(100, d.hp);
    }
}
