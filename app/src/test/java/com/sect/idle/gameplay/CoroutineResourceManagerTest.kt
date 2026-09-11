package com.sect.idle.gameplay

import com.sect.idle.core.GameConfig
import com.sect.idle.models.Building
import com.sect.idle.models.Disciple
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * CoroutineResourceManagerTest - Validates the background Coroutine resource accumulation,
 * gather rate formulas for buildings & disciples, StateFlow reactivity, and edge cases.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CoroutineResourceManagerTest {

    private lateinit var sectData: SectData

    @Before
    fun setUp() {
        sectData = SectData.getInstance()
        sectData.reset()
        sectData.sectQi = 100L
        sectData.spiritStones = 1000L
        sectData.spiritHerbs = 100L
        sectData.spiritOres = 50L
        sectData.spiritPills = 10L
        sectData.sectPower = 1000L
        sectData.disciples.clear()
        sectData.buildings.clear()
    }

    @Test
    fun testBaseAccumulationWithoutBuildingsOrDisciples() {
        val manager = CoroutineResourceManager()

        val initialQi = sectData.sectQi
        val initialStones = sectData.spiritStones

        manager.accumulateResourcesOneSecond()

        assertTrue("Sect Qi should have increased", sectData.sectQi > initialQi)
        assertTrue("Spirit Stones should have increased", sectData.spiritStones > initialStones)

        val state = manager.resourceState.value
        assertEquals(sectData.sectQi, state.sectQi)
        assertEquals(sectData.spiritStones, state.spiritStones)
    }

    @Test
    fun testBuildingContributionsToResourceGatherRates() {
        val manager = CoroutineResourceManager()

        // Add level 3 Herb Garden and level 2 Ore Mine
        val garden = Building(GameConfig.BUILD_GARDEN, "Herb Garden", 10, 200L, 5).apply {
            level = 3
            isBuilt = true
        }
        val mine = Building(GameConfig.BUILD_MINE, "Spirit Ore Mine", 10, 200L, 5).apply {
            level = 2
            isBuilt = true
        }
        sectData.buildings.add(garden)
        sectData.buildings.add(mine)

        val initialHerbs = sectData.spiritHerbs
        val initialOres = sectData.spiritOres

        manager.accumulateResourcesOneSecond()

        val state = manager.resourceState.value
        // Base herbs (1.0) + level 3 garden (3 * 1.2 = 3.6) = 4.6 herbs/s
        assertTrue("Herbs rate should account for garden level", state.herbsRatePerSec >= 4.0f)
        // Base ores (0.8) + level 2 mine (2 * 1.0 = 2.0) = 2.8 ores/s
        assertTrue("Ores rate should account for mine level", state.oresRatePerSec >= 2.5f)

        assertTrue(sectData.spiritHerbs > initialHerbs)
        assertTrue(sectData.spiritOres > initialOres)
    }

    @Test
    fun testDiscipleTaskAssignmentContributions() {
        val manager = CoroutineResourceManager()

        val farmingDisciple = Disciple("Farmer Zhang").apply {
            currentTask = GameConfig.TASK_FARMING
            taskEfficiency = 100 // 2.0x efficiency
            atk = 50
            def = 40
            wis = 30
            realm = 2
        }
        val miningDisciple = Disciple("Miner Li").apply {
            currentTask = GameConfig.TASK_MINING
            taskEfficiency = 80 // 1.6x efficiency
            atk = 45
            def = 35
            wis = 25
            realm = 1
        }
        sectData.disciples.add(farmingDisciple)
        sectData.disciples.add(miningDisciple)

        manager.accumulateResourcesOneSecond()

        val state = manager.resourceState.value
        assertTrue("Herbs rate should increase with assigned farming disciple", state.herbsRatePerSec > 1.0f)
        assertTrue("Ores rate should increase with assigned mining disciple", state.oresRatePerSec > 0.8f)
        assertTrue("Sect total power should be recalculated", state.sectPower > 1000L)
    }

    @Test
    fun testStartPauseResumeLifecycleWithCoroutines() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val manager = CoroutineResourceManager(dispatcher = testDispatcher)

        manager.start()

        // Advance 2 seconds
        testScheduler.advanceTimeBy(2500L)
        testScheduler.runCurrent()

        val qiAfter2s = manager.resourceState.value.sectQi
        assertTrue(qiAfter2s > 100L)

        // Pause
        manager.pause()
        testScheduler.advanceTimeBy(3000L)
        testScheduler.runCurrent()

        val qiAfterPause = manager.resourceState.value.sectQi
        assertEquals("Qi should not increase while paused", qiAfter2s, qiAfterPause)

        // Resume
        manager.resume()
        testScheduler.advanceTimeBy(2000L)
        testScheduler.runCurrent()

        val qiAfterResume = manager.resourceState.value.sectQi
        assertTrue("Qi should resume accumulation", qiAfterResume > qiAfterPause)

        manager.stop()
    }
}
