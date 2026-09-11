package com.sect.idle.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sect.idle.data.repository.SectRepository
import com.sect.idle.gameplay.CoroutineGameLoop
import com.sect.idle.gameplay.CoroutineResourceManager
import com.sect.idle.gameplay.GameEngine
import com.sect.idle.gameplay.SectData
import com.sect.idle.systems.AudioManager
import com.sect.idle.systems.HapticManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Navigation destination tabs for the 5-Tab Xianxia System.
 */
enum class NavigationTab(val index: Int, val title: String) {
    HOME(0, "Home"),
    BATTLE(1, "Battle"),
    SECT(2, "Sect"),
    BAG(3, "Bag"),
    PROFILE(4, "Profile");

    companion object {
        fun fromIndex(index: Int): NavigationTab {
            return entries.firstOrNull { it.index == index } ?: HOME
        }
    }
}

/**
 * Active high-level game scenes handled by the master UI coordinator.
 */
enum class ActiveGameScene {
    SECT_ISOMETRIC_WORLD,
    BATTLE_ARENA,
    SECT_HUB_MANAGEMENT,
    DISCIPLE_RECRUITMENT,
    TOURNAMENT_GROUNDS,
    WAR_CONQUEST
}

/**
 * Global game state holding active scene, navigation tab, and modal states.
 */
data class GameUiState(
    val currentTab: NavigationTab = NavigationTab.HOME,
    val activeScene: ActiveGameScene = ActiveGameScene.SECT_ISOMETRIC_WORLD,
    val activeModal: ActiveModalType? = null,
    val inspectedIslandId: Int? = null,
    val qiToastMessage: String? = null,
    val speedMultiplier: Float = 1.0f,
    val sectQi: Long = 100L,
    val spiritStones: Long = 0L,
    val spiritHerbs: Long = 0L,
    val spiritOres: Long = 0L,
    val spiritPills: Long = 0L,
    val sectPower: Long = 1000L
)

/**
 * Master GameViewModel managing navigation state, active game scenes,
 * audio & haptic triggers, and streamlined coordination across Xianxia modules.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val audioManager = AudioManager.get(application)
    private val hapticManager = HapticManager.get(application)
    private val repository = SectRepository.get(application)
    private val gameLoop = CoroutineGameLoop.get()
    private val resourceManager = CoroutineResourceManager.get()
    private val gameEngine = GameEngine.get()

    init {
        resourceManager.attachContext(application)
        gameEngine.attachContext(application)

        // 1. Restore state from Room Database on startup
        viewModelScope.launch {
            val data = SectData.getInstance()
            if (data != null) {
                val restored = repository.loadGameStateIntoSectData(data)
                if (restored) {
                    syncWithSectData()
                }
            }
        }

        // 2. Observe reactive resource accumulation from CoroutineResourceManager
        viewModelScope.launch {
            resourceManager.resourceState.collect { resState ->
                _uiState.update { current ->
                    current.copy(
                        sectQi = resState.sectQi,
                        spiritStones = resState.spiritStones,
                        spiritHerbs = resState.spiritHerbs,
                        spiritOres = resState.spiritOres,
                        spiritPills = resState.spiritPills,
                        sectPower = resState.sectPower
                    )
                }
            }
        }

        syncWithSectData()
    }

    /**
     * Updates active navigation tab and switches the displayed scene appropriately.
     */
    fun selectTab(tab: NavigationTab) {
        audioManager.playSfx(AudioManager.SFX_CLICK)
        hapticManager.tap()
        _uiState.update { current ->
            val targetScene = when (tab) {
                NavigationTab.HOME -> ActiveGameScene.SECT_ISOMETRIC_WORLD
                NavigationTab.BATTLE -> ActiveGameScene.BATTLE_ARENA
                NavigationTab.SECT -> ActiveGameScene.SECT_HUB_MANAGEMENT
                NavigationTab.BAG -> current.activeScene
                NavigationTab.PROFILE -> current.activeScene
            }
            val targetModal = when (tab) {
                NavigationTab.BAG -> ActiveModalType.INVENTORY
                NavigationTab.PROFILE -> ActiveModalType.STATS
                else -> if (tab == NavigationTab.HOME) null else current.activeModal
            }
            current.copy(
                currentTab = tab,
                activeScene = targetScene,
                activeModal = targetModal
            )
        }
    }

    fun selectTabByIndex(index: Int) {
        selectTab(NavigationTab.fromIndex(index))
    }

    /**
     * Explicitly transitions the active game scene.
     */
    fun setScene(scene: ActiveGameScene) {
        audioManager.playSfx(AudioManager.SFX_CLICK)
        hapticManager.tap()
        _uiState.update { it.copy(activeScene = scene) }
    }

    /**
     * Modal dialog management.
     */
    fun showModal(modal: ActiveModalType?) {
        if (modal != null) {
            audioManager.playSfx(AudioManager.SFX_CLICK)
            hapticManager.click()
        }
        _uiState.update { it.copy(activeModal = modal) }
    }

    fun dismissModal() {
        hapticManager.tap()
        _uiState.update { it.copy(activeModal = null, inspectedIslandId = null) }
    }

    fun inspectIsland(islandId: Int?) {
        if (islandId != null) {
            audioManager.playSfx(AudioManager.SFX_IMMORTAL_BELL)
            hapticManager.canvasTouch()
        }
        _uiState.update { it.copy(inspectedIslandId = islandId) }
    }

    /**
     * Cycles speed multiplier (1x -> 2x -> 5x).
     */
    fun toggleSpeedMultiplier() {
        val nextSpeed = when (_uiState.value.speedMultiplier) {
            1.0f -> 2.0f
            2.0f -> 5.0f
            else -> 1.0f
        }
        gameLoop.setSpeedMultiplier(nextSpeed)
        gameEngine.setSpeedMultiplier(nextSpeed)
        audioManager.playSfx(AudioManager.SFX_SPIRIT_BURST)
        hapticManager.impact()
        _uiState.update {
            it.copy(
                speedMultiplier = nextSpeed,
                qiToastMessage = "⚡ Time Dilated to ${nextSpeed}x Speed!"
            )
        }
    }

    /**
     * Displays a temporary Qi notification banner.
     */
    fun showToast(message: String) {
        _uiState.update { it.copy(qiToastMessage = message) }
    }

    fun clearToast() {
        _uiState.update { it.copy(qiToastMessage = null) }
    }

    /**
     * Persists current game state to Room Database.
     */
    fun saveGameToDatabase() {
        viewModelScope.launch {
            val data = SectData.getInstance() ?: return@launch
            repository.saveCurrentGameState(data)
        }
    }

    /**
     * Refreshes economic snapshot.
     */
    fun syncWithSectData() {
        val data = SectData.getInstance() ?: return
        _uiState.update {
            it.copy(
                sectQi = data.sectQi,
                spiritStones = data.spiritStones,
                spiritHerbs = data.spiritHerbs,
                spiritOres = data.spiritOres,
                spiritPills = data.spiritPills,
                sectPower = data.sectPower
            )
        }
    }

    fun playSfx(sfxKey: String) {
        audioManager.playSfx(sfxKey)
    }

    fun playBreakthrough() {
        audioManager.playSfx(AudioManager.SFX_BREAKTHROUGH)
        hapticManager.breakthroughSuccess()
    }
}
