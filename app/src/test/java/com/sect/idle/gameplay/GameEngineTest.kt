package com.sect.idle.gameplay

import com.sect.idle.core.GameConfig
import com.sect.idle.models.Disciple
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameEngineTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var engine: GameEngine
    private lateinit var sectData: SectData

    @Before
    fun setUp() {
        engine = GameEngine(testDispatcher)
        sectData = SectData.getInstance()
        sectData.spiritStones = 1000L
        sectData.spiritHerbs = 500L
        sectData.spiritOres = 200L
        sectData.spiritPills = 50L
        sectData.disciples.clear()
    }

    @Test
    fun testEngineInitialization() {
        assertNotNull(engine)
        val state = engine.engineState.value
        assertEquals(0L, state.tick)
        assertTrue(state.totalSectPower >= 1000L)
    }

    @Test
    fun testSpeedMultiplier() {
        engine.setSpeedMultiplier(3.0f)
        assertEquals(3.0f, engine.getSpeedMultiplier(), 0.001f)

        // Test clamping
        engine.setSpeedMultiplier(50.0f)
        assertEquals(10.0f, engine.getSpeedMultiplier(), 0.001f)

        engine.setSpeedMultiplier(0.01f)
        assertEquals(0.1f, engine.getSpeedMultiplier(), 0.001f)
    }

    @Test
    fun testOfflineProgressionCalculation() = testScope.runTest {
        val disciple = Disciple("Test Cultivator").apply {
            realm = 0
            realmExp = 0
            wis = 25
            currentTask = GameConfig.TASK_CULTIVATION
        }
        sectData.addDisciple(disciple)

        val initialStones = sectData.spiritStones
        val initialHerbs = sectData.spiritHerbs
        val initialOres = sectData.spiritOres

        val offlineSec = 3600L // 1 hour
        val summary = engine.calculateOfflineProgression(offlineSec)

        assertEquals(offlineSec, summary.offlineSeconds)
        assertTrue(summary.spiritStonesGained > 0)
        assertTrue(summary.spiritHerbsGained > 0)
        assertTrue(summary.spiritOresGained > 0)

        assertEquals(initialStones + summary.spiritStonesGained, sectData.spiritStones)
        assertEquals(initialHerbs + summary.spiritHerbsGained, sectData.spiritHerbs)
        assertEquals(initialOres + summary.spiritOresGained, sectData.spiritOres)
        assertTrue(disciple.realmExp > 0 || disciple.realm > 0)
    }

    @Test
    fun testSingleTickResourceGeneration() {
        val initialStones = sectData.spiritStones
        val initialHerbs = sectData.spiritHerbs
        val initialOres = sectData.spiritOres

        engine.executeSingleTick(1.0f)

        val state = engine.engineState.value
        assertEquals(1L, state.tick)
        assertTrue(sectData.spiritStones >= initialStones)
        assertTrue(sectData.spiritHerbs >= initialHerbs)
        assertTrue(sectData.spiritOres >= initialOres)
    }

    @Test
    fun testEngineLifecycle() {
        engine.start()
        assertTrue(engine.isEngineRunning())
        engine.pause()
        org.junit.Assert.assertFalse(engine.isEngineRunning())
        engine.resume()
        assertTrue(engine.isEngineRunning())
        engine.setLowPowerMode(true)
        engine.stop()
        org.junit.Assert.assertFalse(engine.isEngineRunning())
    }
}
