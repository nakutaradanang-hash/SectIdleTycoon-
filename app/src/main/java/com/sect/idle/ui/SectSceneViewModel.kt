package com.sect.idle.ui

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sect.idle.R
import com.sect.idle.core.GameConfig
import com.sect.idle.systems.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * High-performance, lifecycle-aware ViewModel managing the state of the 3D Cultivation World.
 * Ensures zero memory leaks by relying on Application context and viewModelScope cancellation.
 */
class SectSceneViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SectSceneState())
    val state: StateFlow<SectSceneState> = _state.asStateFlow()

    private var transitionJob: Job? = null
    private var telemetryJob: Job? = null

    init {
        detectHardwareCapabilities()
        preload3DAssets()
        startTelemetryMonitoring()
    }

    private fun detectHardwareCapabilities() {
        val am = getApplication<Application>().getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val isLowRam = am?.isLowRamDevice ?: false
        val initialQuality = if (isLowRam) RenderQualityTier.LITE_LOW_RAM else RenderQualityTier.HIGH_3D

        _state.update {
            it.copy(
                isLowRamDevice = isLowRam,
                renderQuality = initialQuality
            )
        }
    }

    /**
     * Preloads and decodes 3D drawable asset metadata asynchronously off the main thread.
     * Prevents frame drops during gameplay scene transitions.
     */
    fun preload3DAssets() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    assetLoadState = AssetLoadingProgress(
                        isLoading = true,
                        progressPercent = 0.05f,
                        statusMessage = "Invoking Ancient Formations..."
                    )
                )
            }

            val assetList = listOf(
                AssetDescriptor("Grand Palace Hall 3D", R.drawable.obj_3d_main_hall),
                AssetDescriptor("Alchemy Pavilion 3D", R.drawable.obj_3d_alchemy),
                AssetDescriptor("Divine Forge 3D", R.drawable.obj_3d_forge),
                AssetDescriptor("Spirit Spring Pool 3D", R.drawable.obj_3d_spirit_pool),
                AssetDescriptor("Scripture Tower 3D", R.drawable.obj_3d_scripture),
                AssetDescriptor("Spiritual Herb Garden 3D", R.drawable.obj_3d_herb_garden),
                AssetDescriptor("Celestial Terrain 3D", R.drawable.obj_3d_terrain),
                AssetDescriptor("Sword Immortal Cultivator 3D", R.drawable.img_cultivator_hero),
                AssetDescriptor("Grand Sect Panorama 3D", R.drawable.img_grand_sect_hub),
                AssetDescriptor("Xianxia Open World 3D", R.drawable.img_xianxia_openworld)
            )

            val total = assetList.size
            val loadedMap = mutableMapOf<String, Boolean>()
            val resources = getApplication<Application>().resources

            for ((index, asset) in assetList.withIndex()) {
                if (!isActive) break

                val boundsOpts = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                try {
                    BitmapFactory.decodeResource(resources, asset.drawableRes, boundsOpts)
                    loadedMap[asset.name] = (boundsOpts.outWidth > 0 && boundsOpts.outHeight > 0)
                } catch (e: Throwable) {
                    loadedMap[asset.name] = false
                }

                val currentProgress = (index + 1).toFloat() / total
                _state.update {
                    it.copy(
                        assetLoadState = AssetLoadingProgress(
                            isLoading = true,
                            progressPercent = currentProgress,
                            loadedCount = index + 1,
                            totalCount = total,
                            currentAssetName = asset.name,
                            statusMessage = "Infusing ${asset.name} (${(currentProgress * 100).toInt()}%)",
                            loadedAssets = loadedMap.toMap()
                        )
                    )
                }
                delay(35) // Smooth animated loading progress
            }

            _state.update {
                it.copy(
                    assetLoadState = it.assetLoadState.copy(
                        isLoading = false,
                        progressPercent = 1.0f,
                        statusMessage = "3D Dao Realm Materialized",
                        isComplete = true
                    )
                )
            }
        }
    }

    /**
     * Initiates smooth camera and atmospheric transitions between scenes, syncing soundscapes.
     */
    fun transitionTo(targetMode: SectSceneMode) {
        if (_state.value.currentMode == targetMode) return

        // Switch appropriate BGM soundtrack for the new realm
        val targetTheme = when (targetMode) {
            SectSceneMode.REALM_OVERVIEW -> AudioManager.THEME_SECT_PEACE
            SectSceneMode.DISCIPLE_MEDITATION -> AudioManager.THEME_MEDITATION_ZEN
            SectSceneMode.EXPLORATION_MODE -> AudioManager.THEME_WILDERNESS_EXPLORE
            SectSceneMode.TRIBULATION_ZONE -> AudioManager.THEME_DEMON_TRIBULATION
            SectSceneMode.BUILDING_INSPECT -> AudioManager.THEME_IMMORTAL_HYMN
        }
        playBgmTheme(targetTheme)
        playSfx(AudioManager.SFX_CLICK)

        transitionJob?.cancel()
        transitionJob = viewModelScope.launch {
            val fromMode = _state.value.currentMode
            _state.update {
                it.copy(
                    transition = SceneTransitionState(
                        isTransitioning = true,
                        fromMode = fromMode,
                        toMode = targetMode,
                        progress = 0f
                    )
                )
            }

            val steps = 15
            for (i in 1..steps) {
                delay(16)
                val progress = i.toFloat() / steps
                _state.update {
                    it.copy(
                        transition = it.transition.copy(progress = progress)
                    )
                }
            }

            _state.update {
                it.copy(
                    currentMode = targetMode,
                    transition = SceneTransitionState(
                        isTransitioning = false,
                        fromMode = targetMode,
                        toMode = targetMode,
                        progress = 1f
                    )
                )
            }
        }
    }

    fun playSfx(sfxKey: String) {
        try {
            AudioManager.get(getApplication()).playSfx(sfxKey)
        } catch (ignored: Throwable) {}
    }

    fun playBgmTheme(themeKey: String) {
        try {
            AudioManager.get(getApplication()).playBgm(themeKey)
        } catch (ignored: Throwable) {}
    }

    fun playClick() {
        playSfx(AudioManager.SFX_CLICK)
    }

    fun playBreakthrough() {
        playSfx(AudioManager.SFX_BREAKTHROUGH)
    }

    fun playSpiritBurst() {
        playSfx(AudioManager.SFX_SPIRIT_BURST)
    }

    fun playBell() {
        playSfx(AudioManager.SFX_IMMORTAL_BELL)
    }

    fun playCoin() {
        playSfx(AudioManager.SFX_COLLECT)
    }

    fun selectBuilding(buildingIndex: Int?) {
        if (buildingIndex != null) {
            playSfx(AudioManager.SFX_CLICK)
        }
        _state.update {
            it.copy(
                selectedBuildingIndex = buildingIndex,
                selectedDiscipleId = null
            )
        }
    }

    fun selectDisciple(discipleId: String?, discipleIndex: Int? = null) {
        if (discipleId != null) {
            playSfx(AudioManager.SFX_DISCIPLE_GREETING)
        }
        _state.update {
            it.copy(
                selectedDiscipleId = discipleId,
                selectedDiscipleIndex = discipleIndex,
                selectedBuildingIndex = null
            )
        }
    }

    fun setCameraFocus(x: Float, y: Float, zoom: Float) {
        _state.update {
            it.copy(
                cameraFocusX = x,
                cameraFocusY = y,
                cameraZoom = zoom
            )
        }
    }

    fun setAtmosphere(atmosphere: WeatherAtmosphere) {
        _state.update { it.copy(atmosphere = atmosphere) }
    }

    fun updateDayNight(hour: Float) {
        val safeHour = (hour % 24f + 24f) % 24f
        val isNight = safeHour < 6f || safeHour >= 18f
        _state.update {
            it.copy(
                timeOfDayHour = safeHour,
                isNightTime = isNight
            )
        }
    }

    fun updateTelemetry(fps: Int, particleCount: Int, memoryMb: Float) {
        _state.update {
            it.copy(
                currentFps = fps,
                activeParticles = particleCount,
                heapMemoryMb = memoryMb
            )
        }
    }

    fun setRenderQuality(quality: RenderQualityTier) {
        _state.update { it.copy(renderQuality = quality) }
    }

    private fun startTelemetryMonitoring() {
        telemetryJob = viewModelScope.launch(Dispatchers.Default) {
            val runtime = Runtime.getRuntime()
            while (isActive) {
                val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
                _state.update {
                    it.copy(heapMemoryMb = usedMem)
                }
                delay(2000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        transitionJob?.cancel()
        transitionJob = null
        telemetryJob?.cancel()
        telemetryJob = null
    }
}

// -------------------------------------------------------------
// State Models
// -------------------------------------------------------------

data class SectSceneState(
    val currentMode: SectSceneMode = SectSceneMode.REALM_OVERVIEW,
    val transition: SceneTransitionState = SceneTransitionState(),
    val assetLoadState: AssetLoadingProgress = AssetLoadingProgress(),
    val renderQuality: RenderQualityTier = RenderQualityTier.HIGH_3D,
    val atmosphere: WeatherAtmosphere = WeatherAtmosphere.CLEAR_QI_AURA,
    val timeOfDayHour: Float = 12.0f,
    val isNightTime: Boolean = false,
    val isLowRamDevice: Boolean = false,
    val cameraFocusX: Float = 2000f,
    val cameraFocusY: Float = 2000f,
    val cameraZoom: Float = 1.0f,
    val selectedBuildingIndex: Int? = null,
    val selectedDiscipleIndex: Int? = null,
    val selectedDiscipleId: String? = null,
    val currentFps: Int = 60,
    val activeParticles: Int = 0,
    val heapMemoryMb: Float = 0f
)

enum class SectSceneMode {
    REALM_OVERVIEW,
    BUILDING_INSPECT,
    DISCIPLE_MEDITATION,
    EXPLORATION_MODE,
    TRIBULATION_ZONE
}

enum class RenderQualityTier {
    HIGH_3D,
    MEDIUM_3D,
    LITE_LOW_RAM
}

enum class WeatherAtmosphere {
    CLEAR_QI_AURA,
    CELESTIAL_MIST,
    GOLDEN_DAO_RAIN,
    PURPLE_SPIRIT_STORM
}

data class SceneTransitionState(
    val isTransitioning: Boolean = false,
    val fromMode: SectSceneMode = SectSceneMode.REALM_OVERVIEW,
    val toMode: SectSceneMode = SectSceneMode.REALM_OVERVIEW,
    val progress: Float = 0f
)

data class AssetLoadingProgress(
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val progressPercent: Float = 0f,
    val loadedCount: Int = 0,
    val totalCount: Int = 0,
    val currentAssetName: String = "",
    val statusMessage: String = "Initializing...",
    val loadedAssets: Map<String, Boolean> = emptyMap()
)

data class AssetDescriptor(
    val name: String,
    @DrawableRes val drawableRes: Int
)
