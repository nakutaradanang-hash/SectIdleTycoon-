package com.sect.idle.integration

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.sect.idle.gameplay.CoroutineGameLoop
import com.sect.idle.gameplay.SectData
import com.sect.idle.ui.ActiveGameScene
import com.sect.idle.ui.ActiveModalType
import com.sect.idle.ui.GameViewModel
import com.sect.idle.ui.NavigationTab
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * UIStateGameLoopIntegrationTest - Validates the integration between
 * GameViewModel, CoroutineGameLoop, SectData, and UI Navigation/Modal states.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UIStateGameLoopIntegrationTest {

    private lateinit var application: Application
    private lateinit var viewModel: GameViewModel
    private lateinit var sectData: SectData

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        sectData = SectData.getInstance()
        sectData.reset()
        sectData.spiritStones = 5000L
        sectData.spiritHerbs = 2000L
        sectData.spiritOres = 1000L

        viewModel = GameViewModel(application)
    }

    @Test
    fun testNavigationTabTransitionsAndSceneMapping() {
        // Default state
        Assert.assertEquals(NavigationTab.HOME, viewModel.uiState.value.currentTab)
        Assert.assertEquals(ActiveGameScene.SECT_ISOMETRIC_WORLD, viewModel.uiState.value.activeScene)

        // Select Battle Tab
        viewModel.selectTab(NavigationTab.BATTLE)
        Assert.assertEquals(NavigationTab.BATTLE, viewModel.uiState.value.currentTab)
        Assert.assertEquals(ActiveGameScene.BATTLE_ARENA, viewModel.uiState.value.activeScene)

        // Select Sect Management Tab
        viewModel.selectTab(NavigationTab.SECT)
        Assert.assertEquals(NavigationTab.SECT, viewModel.uiState.value.currentTab)
        Assert.assertEquals(ActiveGameScene.SECT_HUB_MANAGEMENT, viewModel.uiState.value.activeScene)

        // Select Bag Tab (Inventory modal)
        viewModel.selectTab(NavigationTab.BAG)
        Assert.assertEquals(NavigationTab.BAG, viewModel.uiState.value.currentTab)
        Assert.assertEquals(ActiveModalType.INVENTORY, viewModel.uiState.value.activeModal)

        // Select Profile Tab (Stats modal)
        viewModel.selectTab(NavigationTab.PROFILE)
        Assert.assertEquals(NavigationTab.PROFILE, viewModel.uiState.value.currentTab)
        Assert.assertEquals(ActiveModalType.STATS, viewModel.uiState.value.activeModal)
    }

    @Test
    fun testModalInspectionAndDismissWorkflow() {
        // Open Quests modal
        viewModel.showModal(ActiveModalType.QUESTS)
        Assert.assertEquals(ActiveModalType.QUESTS, viewModel.uiState.value.activeModal)

        // Dismiss Modal
        viewModel.dismissModal()
        Assert.assertNull(viewModel.uiState.value.activeModal)
        Assert.assertNull(viewModel.uiState.value.inspectedIslandId)

        // Inspect Island
        viewModel.inspectIsland(2)
        Assert.assertEquals(2, viewModel.uiState.value.inspectedIslandId)

        viewModel.dismissModal()
        Assert.assertNull(viewModel.uiState.value.inspectedIslandId)
    }

    @Test
    fun testSpeedMultiplierCycling() {
        Assert.assertEquals(1.0f, viewModel.uiState.value.speedMultiplier, 0.01f)

        viewModel.toggleSpeedMultiplier()
        Assert.assertEquals(2.0f, viewModel.uiState.value.speedMultiplier, 0.01f)

        viewModel.toggleSpeedMultiplier()
        Assert.assertEquals(5.0f, viewModel.uiState.value.speedMultiplier, 0.01f)

        viewModel.toggleSpeedMultiplier()
        Assert.assertEquals(1.0f, viewModel.uiState.value.speedMultiplier, 0.01f)
    }

    @Test
    fun testCoroutineGameLoopAutonomousProgression() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val loop = CoroutineGameLoop(dispatcher = testDispatcher)

        loop.start()
        Assert.assertTrue(loop.isRunning())

        // Set high speed multiplier
        loop.setSpeedMultiplier(2.0f)
        Assert.assertEquals(2.0f, loop.getSpeedMultiplier(), 0.01f)

        // Advance simulated time
        testScheduler.advanceTimeBy(3000L)
        testScheduler.runCurrent()

        Assert.assertTrue(loop.getTickCount() > 0)

        // Pause loop
        loop.pause()
        Assert.assertTrue(loop.isPaused())

        // Resume loop
        loop.resume()
        Assert.assertFalse(loop.isPaused())

        loop.stop()
        Assert.assertFalse(loop.isRunning())
    }
}
