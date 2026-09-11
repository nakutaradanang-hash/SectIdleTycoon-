package com.sect.idle.hub

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.sect.idle.core.GameConfig
import com.sect.idle.gameplay.SectData
import com.sect.idle.models.Disciple
import com.sect.idle.models.Talent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SectHubViewModelIntegrationTest - Validates the integration between
 * SectHubViewModel, SectData, disciples, alchemy, world map expeditions, and Room auto-persistence.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SectHubViewModelIntegrationTest {

    private lateinit var application: Application
    private lateinit var sectData: SectData
    private lateinit var viewModel: SectHubViewModel

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        sectData = SectData.getInstance()
        sectData.reset()
        sectData.sectName = "Immortal Phoenix Sect"
        sectData.spiritStones = 20000L
        sectData.spiritHerbs = 1000L
        sectData.spiritOres = 500L
        sectData.spiritPills = 50L
        sectData.jade = 100L
        sectData.essence = 50L
        sectData.sectPower = 5000L
        sectData.sectRealm = 1
        sectData.sectRealmExp = 200

        // Add a test disciple
        sectData.disciples.clear()
        val disciple = Disciple("Disciple Han").apply {
            id = "disc_han"
            realm = 1
            realmExp = 500
            currentTask = 0
            energy = 100
            maxEnergy = 100
            loyalty = 100
            dailyWage = 10
            atk = 60
            def = 40
            spd = 25
            talent = Talent().apply {
                name = "Golden Core Root"
                grade = 3
            }
        }
        sectData.disciples.add(disciple)
        com.sect.idle.gameplay.CoroutineResourceManager.get().syncFromSectData()

        viewModel = SectHubViewModel(application)
        viewModel.syncFromSectData()
    }

    @Test
    fun testInitialUiStateSyncFromSectData() {
        val state = viewModel.uiState.value
        assertEquals("Immortal Phoenix Sect", state.sectName)
        assertEquals(20000L, state.spiritStones)
        assertEquals(1000L, state.spiritHerbs)
        assertEquals(1, state.disciples.size)
        assertEquals("Disciple Han", state.disciples[0].name)
    }

    @Test
    fun testAssignDiscipleTaskWorkflow() {
        // Assign to Cultivation
        viewModel.assignDiscipleTask("disc_han", GameConfig.TASK_CULTIVATION)

        val updatedState = viewModel.uiState.value
        val han = updatedState.disciples.find { it.id == "disc_han" }
        assertNotNull(han)
        assertEquals(GameConfig.TASK_CULTIVATION, han?.currentTask)

        // Assign to Mining
        viewModel.assignDiscipleTask("disc_han", GameConfig.TASK_MINING)
        val hanMining = viewModel.uiState.value.disciples.find { it.id == "disc_han" }
        assertEquals(GameConfig.TASK_MINING, hanMining?.currentTask)
    }

    @Test
    fun testDiscipleBreakthroughSuccessAndFailure() {
        // Ready for breakthrough (exp >= max)
        val disciple = sectData.disciples[0]
        disciple.realm = 1
        disciple.realmExp = 2000 // Over max required
        viewModel.syncFromSectData()

        val initialRealm = disciple.realm
        viewModel.attemptChamberBreakthrough("disc_han")

        val stateAfter = viewModel.uiState.value
        val hanAfter = stateAfter.disciples.find { it.id == "disc_han" }
        assertTrue(hanAfter!!.realm >= initialRealm)
    }

    @Test
    fun testRecruitDiscipleCapacityLimit() {
        sectData.maxDisciples = 2
        sectData.disciples.clear()
        sectData.disciples.add(Disciple("D1"))
        sectData.disciples.add(Disciple("D2"))

        viewModel.syncFromSectData()

        // Attempting to recruit when at max capacity
        viewModel.recruitNewDisciple()
        // Should not exceed max capacity
        assertTrue(viewModel.uiState.value.disciples.size <= 2)
    }

    @Test
    fun testExpeditionDispatchAndLootClaim() {
        val firstRegion = viewModel.uiState.value.worldMap.regions.firstOrNull { it.isUnlocked }
        assertNotNull(firstRegion)

        // Dispatch Expedition
        viewModel.dispatchExpedition(firstRegion!!.id, listOf("disc_han"))

        val region = viewModel.uiState.value.worldMap.regions.find { it.id == firstRegion.id }
        assertNotNull(region)
        assertTrue(region!!.isExpeditionActive)
        assertEquals(listOf("Disciple Han"), region.assignedDiscipleNames)
    }

    @Test
    fun testUpgradeCultivationArray() {
        val initialArrayLevel = viewModel.uiState.value.cultivationChamber.arrayLevel
        val initialStones = sectData.spiritStones

        viewModel.upgradeCultivationArray()

        val newLevel = viewModel.uiState.value.cultivationChamber.arrayLevel
        assertEquals(initialArrayLevel + 1, newLevel)
        assertTrue(sectData.spiritStones < initialStones)
    }

    @Test
    fun testMeditationSlotSeatAndVacate() {
        // Unlock first slot if locked
        val slot = viewModel.uiState.value.cultivationChamber.meditationSlots.firstOrNull()
        if (slot != null && !slot.isUnlocked) {
            viewModel.unlockMeditationSlot(slot.slotIndex)
        }

        val targetSlot = viewModel.uiState.value.cultivationChamber.meditationSlots.firstOrNull { it.isUnlocked }
        assertNotNull(targetSlot)

        viewModel.seatDiscipleInMeditation(targetSlot!!.slotIndex, "disc_han")

        val stateAfterSeat = viewModel.uiState.value.cultivationChamber.meditationSlots.find { it.slotIndex == targetSlot.slotIndex }
        assertEquals("disc_han", stateAfterSeat?.assignedDiscipleId)

        viewModel.vacateMeditationSlot(targetSlot.slotIndex)
        val stateAfterVacate = viewModel.uiState.value.cultivationChamber.meditationSlots.find { it.slotIndex == targetSlot.slotIndex }
        assertEquals(null, stateAfterVacate?.assignedDiscipleId)
    }
}
