package com.sect.idle.gameplay;

import com.sect.idle.models.Quest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * QuestManagerTest - Unit tests for starter quest generation, progress addition,
 * completion validation, and reward claiming.
 */
public class QuestManagerTest {

    private QuestManager questManager;
    private SectData sectData;

    @Before
    public void setUp() {
        questManager = QuestManager.getInstance();
        sectData = SectData.getInstance();
        sectData.reset();
    }

    @Test
    public void testGenerateStarterQuests() {
        ArrayList<Quest> starterQuests = questManager.generateStarterQuests();
        Assert.assertNotNull(starterQuests);
        Assert.assertTrue(starterQuests.size() >= 3);

        Quest q = starterQuests.get(0);
        Assert.assertNotNull(q.id);
        Assert.assertNotNull(q.title);
        Assert.assertTrue(q.target > 0);
        Assert.assertFalse(q.completed);
        Assert.assertFalse(q.claimed);
        Assert.assertEquals(0f, q.getPercent(), 0.001f);
    }

    @Test
    public void testQuestProgressAndCompletion() {
        Quest q = new Quest("q_test", "Harvest Spirit Herbs", "Gather 100 herbs", 1, 100, 500, 20, 10);

        q.addProgress(40);
        Assert.assertEquals(40, q.progress);
        Assert.assertEquals(40.0f, q.getPercent(), 0.001f);
        Assert.assertFalse(q.completed);

        q.addProgress(60);
        Assert.assertEquals(100, q.progress);
        Assert.assertEquals(100.0f, q.getPercent(), 0.001f);
        Assert.assertTrue(q.completed);

        // Additional progress beyond target is capped
        q.addProgress(50);
        Assert.assertEquals(100, q.progress);
    }

    @Test
    public void testClaimReward() {
        Quest q = new Quest("q_claim", "Cultivation", "Reach Realm 1", 0, 1, 1000, 50, 25);
        long stonesBefore = sectData.spiritStones;
        long jadeBefore = sectData.jade;

        // Uncompleted quest cannot be claimed
        Assert.assertFalse(questManager.claimReward(q, sectData));

        q.addProgress(1);
        Assert.assertTrue(q.completed);

        // Claim reward
        boolean claimed = questManager.claimReward(q, sectData);
        Assert.assertTrue(claimed);
        Assert.assertTrue(q.claimed);
        Assert.assertEquals(stonesBefore + 1000, sectData.spiritStones);
        Assert.assertEquals(jadeBefore + 50, sectData.jade);

        // Cannot claim twice
        Assert.assertFalse(questManager.claimReward(q, sectData));
    }
}
