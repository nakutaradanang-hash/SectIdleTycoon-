package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Test;

/**
 * FairyJobHobbyAgeTest - Unit tests for Fairy companions, Job designations,
 * Hobby assignments, and Lifespan/Aging progression.
 */
public class FairyJobHobbyAgeTest {

    @Test
    public void testFairySummoning() {
        Disciple d = new Disciple("Lin Dong");
        Assert.assertFalse(d.hasFairy);
        Assert.assertNull(d.fairy);

        FairySystem.summonFairy(d);
        Assert.assertTrue(d.hasFairy);
        Assert.assertNotNull(d.fairy);
        Assert.assertNotNull(d.fairy.name);
        Assert.assertNotNull(d.fairy.element);

        // Cannot summon a second fairy on the same disciple
        String firstFairyName = d.fairy.name;
        FairySystem.summonFairy(d);
        Assert.assertEquals(firstFairyName, d.fairy.name);
    }

    @Test
    public void testJobSystemTitlesAndDescriptions() {
        Disciple d = new Disciple("Xiao Ding");

        d.currentTask = GameConfig.TASK_FARMING;
        Assert.assertEquals("Herbalist", JobSystem.getJobName(d));

        d.currentTask = GameConfig.TASK_ALCHEMY;
        Assert.assertEquals("Alchemist", JobSystem.getJobName(d));

        d.currentTask = GameConfig.TASK_CRAFTING;
        Assert.assertEquals("Blacksmith", JobSystem.getJobName(d));

        String desc = JobSystem.getTaskDescription(GameConfig.TASK_ALCHEMY);
        Assert.assertNotNull(desc);
        Assert.assertTrue(desc.contains("pills") || desc.contains("elixirs"));
    }

    @Test
    public void testHobbySystem() {
        Disciple d = new Disciple("Meng Hao");
        HobbySystem.assignRandomHobby(d);
        Assert.assertTrue(d.hobby >= 0 && d.hobby < HobbySystem.HOBBIES.length);

        String hobbyName = HobbySystem.getHobbyName(d.hobby);
        Assert.assertNotNull(hobbyName);
        Assert.assertNotEquals("None", hobbyName);
    }

    @Test
    public void testAgeSystemAndLifespan() {
        Disciple d = new Disciple("Grand Elder");
        d.age = 80;
        d.lifespan = 100;
        d.hp = 100;

        AgeSystem.ageTick(d);
        Assert.assertEquals(81, d.age);
        Assert.assertTrue(d.isAlive());

        // Extend lifespan by pill or treasure
        AgeSystem.extendLifespan(d, 50);
        Assert.assertEquals(150, d.lifespan);

        // Advance to natural expiration
        d.age = 150;
        AgeSystem.ageTick(d);
        Assert.assertFalse(d.isAlive());
    }
}
