package com.sect.idle.gameplay

import android.content.Context
import android.util.Log
import com.sect.idle.core.GameConfig
import com.sect.idle.data.repository.SectRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

/**
 * Immutable StateFlow snapshot representing the Sect's reactive resource balances.
 */
data class SectResourceState(
    val sectQi: Long = 100L,
    val spiritStones: Long = 1000L,
    val spiritHerbs: Long = 100L,
    val spiritOres: Long = 50L,
    val spiritPills: Long = 10L,
    val jade: Long = 50L,
    val essence: Long = 20L,
    val sectPower: Long = 1000L,
    val qiGatherRatePerSec: Float = 5.0f,
    val stonesRatePerSec: Float = 2.0f,
    val herbsRatePerSec: Float = 1.0f,
    val oresRatePerSec: Float = 0.8f,
    val pillsRatePerSec: Float = 0.1f,
    val lastAccumulateTimestamp: Long = System.currentTimeMillis()
)

/**
 * CoroutineResourceManager - Background Coroutine-based resource manager.
 *
 * Automatically accumulates Sect Qi and cultivation materials every second (1000ms),
 * dynamically calculates gather rates from active disciples and sect buildings,
 * ensures UI updates reactively using StateFlow, and periodically syncs with Room.
 */
class CoroutineResourceManager(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    companion object {
        private const val TAG = "CoroutineResourceMgr"
        const val ACCUMULATION_INTERVAL_MS = 1000L // Accumulate every second

        @Volatile
        private var instance: CoroutineResourceManager? = null

        @JvmStatic
        fun get(): CoroutineResourceManager {
            return instance ?: synchronized(this) {
                instance ?: CoroutineResourceManager().also { instance = it }
            }
        }

        @JvmStatic
        fun getInstance(): CoroutineResourceManager = get()
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            Log.e(TAG, "Uncaught exception in CoroutineResourceManager", throwable)
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + dispatcher + exceptionHandler)
    private var accumulationJob: Job? = null

    private val isRunning = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    private val tickCount = AtomicLong(0L)

    private var repository: SectRepository? = null

    // Reactive StateFlow exposed directly for UI observing
    private val _resourceState = MutableStateFlow(SectResourceState())
    val resourceState: StateFlow<SectResourceState> = _resourceState.asStateFlow()

    /**
     * Initializes the repository for periodic auto-save.
     */
    fun attachContext(context: Context) {
        if (repository == null) {
            repository = SectRepository.get(context)
        }
    }

    /**
     * Starts the 1-second accumulation coroutine loop.
     */
    fun start() {
        if (isRunning.compareAndSet(false, true)) {
            isPaused.set(false)
            accumulationJob = scope.launch {
                Log.d(TAG, "CoroutineResourceManager started background 1s ticker.")
                while (isActive && isRunning.get()) {
                    if (isPaused.get()) {
                        delay(500L)
                        continue
                    }

                    accumulateResourcesOneSecond()
                    delay(ACCUMULATION_INTERVAL_MS)
                }
            }
        }
    }

    /**
     * Pauses accumulation when app is suspended.
     */
    fun pause() {
        isPaused.set(true)
    }

    /**
     * Resumes accumulation.
     */
    fun resume() {
        isPaused.set(false)
    }

    /**
     * Stops the accumulation job.
     */
    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            accumulationJob?.cancel()
            accumulationJob = null
        }
    }

    /**
     * Immediately syncs StateFlow resourceState snapshot with current SectData.
     */
    fun syncFromSectData() {
        val data = SectData.getInstance() ?: return
        _resourceState.update { current ->
            current.copy(
                sectQi = data.sectQi,
                spiritStones = data.spiritStones,
                spiritHerbs = data.spiritHerbs,
                spiritOres = data.spiritOres,
                spiritPills = data.spiritPills,
                jade = data.jade,
                essence = data.essence,
                sectPower = data.sectPower
            )
        }
    }

    /**
     * Single discrete 1-second accumulation calculation.
     */
    fun accumulateResourcesOneSecond() {
        val data = SectData.getInstance() ?: return
        val currentTick = tickCount.incrementAndGet()

        // 1. Calculate Gather Rates from Buildings
        var baseQiPerSec = 5.0f
        var baseStonesPerSec = 2.0f
        var baseHerbsPerSec = 1.0f
        var baseOresPerSec = 0.8f
        var basePillsPerSec = 0.1f

        data.buildings?.let { buildings ->
            for (i in 0 until buildings.size) {
                val b = buildings.getOrNull(i) ?: continue
                if (!b.isBuilt) continue
                val lvl = b.level.coerceAtLeast(1)
                when (b.type) {
                    GameConfig.BUILD_HALL -> {
                        baseStonesPerSec += lvl * 1.5f
                        baseQiPerSec += lvl * 2.0f
                    }
                    GameConfig.BUILD_LIBRARY -> {
                        baseQiPerSec += lvl * 3.5f
                    }
                    GameConfig.BUILD_GARDEN -> {
                        baseHerbsPerSec += lvl * 1.2f
                    }
                    GameConfig.BUILD_MINE -> {
                        baseOresPerSec += lvl * 1.0f
                    }
                    GameConfig.BUILD_ALCHEMY -> {
                        basePillsPerSec += lvl * 0.15f
                    }
                }
            }
        }

        // 2. Add Contributions from Disciples on Tasks
        var totalPower = 0L
        data.disciples?.let { disciples ->
            for (i in 0 until disciples.size) {
                val d = disciples.getOrNull(i) ?: continue
                val power = d.atk * 3L + d.def * 2L + d.wis * 2L + (d.realm + 1) * 200L
                totalPower += power

                val eff = (d.taskEfficiency / 50.0f).coerceIn(0.5f, 3.0f)
                when (d.currentTask) {
                    GameConfig.TASK_CULTIVATION -> baseQiPerSec += 2.0f * eff
                    GameConfig.TASK_MINING -> baseOresPerSec += 1.5f * eff
                    GameConfig.TASK_FARMING -> baseHerbsPerSec += 1.5f * eff
                    GameConfig.TASK_ALCHEMY -> basePillsPerSec += 0.2f * eff
                }
            }
        }

        // 3. Increment SectData resource values
        val gainedQi = baseQiPerSec.toLong().coerceAtLeast(1L)
        val gainedStones = baseStonesPerSec.toLong().coerceAtLeast(1L)
        val gainedHerbs = baseHerbsPerSec.toLong().coerceAtLeast(0L)
        val gainedOres = baseOresPerSec.toLong().coerceAtLeast(0L)
        val gainedPills = if (currentTick % 10L == 0L) basePillsPerSec.toLong().coerceAtLeast(1L) else 0L

        data.sectQi += gainedQi
        data.spiritStones += gainedStones
        data.spiritHerbs += gainedHerbs
        data.spiritOres += gainedOres
        data.spiritPills += gainedPills
        data.sectPower = max(data.sectPower, totalPower)

        // 4. Update StateFlow Snapshot for reactive UI updates
        val updatedState = SectResourceState(
            sectQi = data.sectQi,
            spiritStones = data.spiritStones,
            spiritHerbs = data.spiritHerbs,
            spiritOres = data.spiritOres,
            spiritPills = data.spiritPills,
            jade = data.jade,
            essence = data.essence,
            sectPower = data.sectPower,
            qiGatherRatePerSec = baseQiPerSec,
            stonesRatePerSec = baseStonesPerSec,
            herbsRatePerSec = baseHerbsPerSec,
            oresRatePerSec = baseOresPerSec,
            pillsRatePerSec = basePillsPerSec,
            lastAccumulateTimestamp = System.currentTimeMillis()
        )

        _resourceState.update { updatedState }

        // 5. Periodic Auto-Persistence every 30 seconds
        if (currentTick % 30L == 0L) {
            repository?.let { repo ->
                scope.launch(Dispatchers.IO) {
                    try {
                        repo.updateResources(
                            data.spiritStones,
                            data.spiritHerbs,
                            data.spiritOres,
                            data.spiritPills,
                            data.sectQi
                        )
                    } catch (ignored: Throwable) {}
                }
            }
        }
    }
}
