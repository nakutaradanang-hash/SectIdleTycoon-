package com.sect.idle.gameplay

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MilestoneSystemTest {

    private lateinit var milestoneSystem: MilestoneSystem

    @Before
    fun setUp() {
        milestoneSystem = MilestoneSystem(null)
    }

    @Test
    fun testMilestoneEvaluation() {
        val evaluated = milestoneSystem.evaluateMilestones(
            discipleCount = 6,
            maxDiscipleRealm = 2,
            currentSpiritStones = 12000L,
            currentSpiritPills = 60L,
            unlockedRegionsCount = 4
        )

        Assert.assertEquals(MilestoneSystem.ALL_MILESTONES.size, evaluated.size)

        val milestone3Disc = evaluated.first { it.id == "disciple_count_3" }
        Assert.assertTrue(milestone3Disc.isCompleted)
        Assert.assertEquals(3L, milestone3Disc.targetValue)
        Assert.assertEquals(6L, milestone3Disc.currentValue)

        val milestone6Disc = evaluated.first { it.id == "disciple_count_6" }
        Assert.assertTrue(milestone6Disc.isCompleted)

        val milestone12Disc = evaluated.first { it.id == "disciple_count_12" }
        Assert.assertFalse(milestone12Disc.isCompleted)
        Assert.assertEquals(0.5f, milestone12Disc.progress, 0.001f)

        val coreFormationMilestone = evaluated.first { it.id == "realm_core_formation_1" }
        Assert.assertTrue(coreFormationMilestone.isCompleted)

        val pillsMilestone = evaluated.first { it.id == "treasury_pills_50" }
        Assert.assertTrue(pillsMilestone.isCompleted)
    }

    @Test
    fun testMilestoneRewardsConfigured() {
        MilestoneSystem.ALL_MILESTONES.forEach { milestone ->
            Assert.assertNotNull(milestone.id)
            Assert.assertNotNull(milestone.title)
            Assert.assertNotNull(milestone.description)
            Assert.assertTrue(milestone.targetValue > 0)
            Assert.assertTrue(milestone.rewardStones > 0 || milestone.rewardJade > 0 || milestone.rewardPills > 0)
        }
    }
}
