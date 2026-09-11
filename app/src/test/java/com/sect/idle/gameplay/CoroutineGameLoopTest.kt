package com.sect.idle.gameplay

import com.sect.idle.core.GameConfig
import com.sect.idle.models.Building
import com.sect.idle.models.Disciple
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineGameLoopTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var gameLoop: CoroutineGameLoop
    private lateinit var sectData: SectData

    @Before
    fun setUp() {
        sectData = SectData.getInstance()
        sectData.reset()
        gameLoop = CoroutineGameLoop(testDispatcher)
    }

    @Test
    fun testInitialState() {
        val state = gameLoop.loopState.value
        assertNotNull(state)
        assertEquals(0L, state.tick)
        assertFalse(gameLoop.isLoopRunning())
    }

    @Test
    fun testSingleTickResourceGeneration() {
        val initialStones = sectData.spiritStones
        val initialHerbs = sectData.spiritHerbs
        val initialOres = sectData.spiritOres

        val d = Disciple("Elder Han").apply {
            currentTask = GameConfig.TASK_MINING
            taskEfficiency = 80
            efficiency = 1.0f
        }
        sectData.addDisciple(d)

        val state = gameLoop.stepOnce(1.0f)

        assertTrue(state.tick > 0)
        assertTrue(sectData.spiritStones >= initialStones)
        assertTrue(sectData.spiritOres >= initialOres)
    }

    @Test
    fun testDiscipleCultivationAndExpAccumulation() {
        val disciple = Disciple("Lin Fan").apply {
            realm = 0
            realmExp = 0
            wis = 20
            currentTask = GameConfig.TASK_CULTIVATION
        }
        sectData.addDisciple(disciple)

        val state = gameLoop.stepOnce(5.0f)

        assertTrue(disciple.realmExp > 0)
        assertTrue(state.totalSectPower > 0)
    }

    @Test
    fun testSpeedMultiplierConfiguration() {
        gameLoop.setSpeedMultiplier(2.5f)
        assertEquals(2.5f, gameLoop.getSpeedMultiplier(), 0.001f)

        gameLoop.setSpeedMultiplier(150.0f) // Clamped to 100.0f
        assertEquals(100.0f, gameLoop.getSpeedMultiplier(), 0.001f)
    }

    @Test
    fun testOfflineProgressionCalculation() = runTest(testDispatcher) {
        val disciple = Disciple("Gu Yue").apply {
            realm = 1
            realmExp = 10
            wis = 15
            currentTask = GameConfig.TASK_FARMING
        }
        sectData.addDisciple(disciple)

        val summary = gameLoop.calculateOfflineProgression(3600L) // 1 hour offline

        assertNotNull(summary)
        assertEquals(3600L, summary.offlineSeconds)
        assertTrue(summary.spiritHerbsGained > 0)
    }

    @Test
    fun testDiscipleVitalsRecoveryWhenResting() {
        val disciple = Disciple("Xiao Yan").apply {
            energy = 30
            mood = 40
            stress = 60
            currentTask = GameConfig.TASK_NONE
        }
        sectData.addDisciple(disciple)

        gameLoop.stepOnce(2.0f)

        assertTrue(disciple.energy > 30)
        assertTrue(disciple.mood > 40)
        assertTrue(disciple.stress < 60)
    }
}
