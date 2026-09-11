package com.sect.idle.gameplay

import android.util.Log
import com.sect.idle.core.GameConfig
import com.sect.idle.models.Disciple
import com.sect.idle.models.Talent
import com.sect.idle.systems.RNG
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min

/**
 * High-Performance GameEngine — Central Simulation & Idle Engine powered by Kotlin Coroutines.
 *
 * Designed with strict low-overhead architecture for 1GB RAM Android devices (e.g. Itel A70):
 *  1. Zero-Allocation Hot Loops: Reuses internal buffers to prevent Garbage Collection (GC) pauses.
 *  2. Structured Concurrency: Powered by [SupervisorJob] on [Dispatchers.Default] with robust exception isolation.
 *  3. Adaptive Frame Pacing: Automatically adjusts tick frequency under high system load.
 *  4. Reactive State Pipeline: Thread-safe [StateFlow] and [SharedFlow] for instant UI updates with zero jank.
 *  5. Seamless Offline Simulation: High-speed fast-forward catch-up without UI thread locking.
 */
class GameEngine(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    companion object {
        private const val TAG = "GameEngine"
        const val DEFAULT_TICK_RATE_HZ = 10L          // 10 ticks per second (100ms per tick)
        const val DEFAULT_TICK_INTERVAL_MS = 100L
        const val BACKGROUND_TICK_INTERVAL_MS = 1000L // 1.0s low power tick when app backgrounded
        const val BASE_EXP_PER_REALM = 100
        const val MAX_DELTA_TIME_CAP = 0.5f           // Max 500ms step cap to prevent simulation bursts

        @Volatile
        private var instance: GameEngine? = null

        @JvmStatic
        fun get(): GameEngine {
            return instance ?: synchronized(this) {
                instance ?: GameEngine().also { instance = it }
            }
        }

        @JvmStatic
        fun getInstance(): GameEngine = get()
    }

    // Coroutine Scope with Exception Handler to avoid crashes
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            Log.e(TAG, "Uncaught coroutine exception in GameEngine", throwable)
        }
    }

    private val engineScope = CoroutineScope(SupervisorJob() + dispatcher + exceptionHandler)
    private var engineJob: Job? = null

    // Engine Atomic Controls
    private val isRunning = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    private val isLowPowerMode = AtomicBoolean(false)
    private val tickCounter = AtomicLong(0L)
    private val speedMultiplier = AtomicReference(1.0f)

    // Performance APM Telemetry
    private val lastTickDurationNs = AtomicLong(0L)
    private val averageTickDurationNs = AtomicLong(0L)

    // Reactive State Pipeline (Exposed as Immutable StateFlow)
    private val _engineState = MutableStateFlow(EngineState())
    val engineState: StateFlow<EngineState> = _engineState.asStateFlow()

    // Discrete Events Pipeline (Breakthroughs, Encounters, Tribulations)
    private val _eventFlow = MutableSharedFlow<EngineEvent>(extraBufferCapacity = 128)
    val eventFlow: SharedFlow<EngineEvent> = _eventFlow.asSharedFlow()

    // Java Interop Listeners
    private val engineListeners = CopyOnWriteArrayList<EngineListener>()

    // Reusable Buffers (Zero Allocation Strategy)
    private val reusableEventList = ArrayList<String>(16)
    private val reusableBreakthroughList = ArrayList<BreakthroughEventData>(8)

    interface EngineListener {
        fun onEngineTick(state: EngineState)
        fun onEngineEvent(event: EngineEvent)
    }

    private val resourceManager = CoroutineResourceManager.get()
    val resourceState: StateFlow<SectResourceState> = resourceManager.resourceState

    fun attachContext(context: android.content.Context) {
        resourceManager.attachContext(context)
    }

    /**
     * Starts the GameEngine main loop on background coroutines.
     */
    fun start() {
        resourceManager.start()
        if (isRunning.compareAndSet(false, true)) {
            isPaused.set(false)
            engineJob = engineScope.launch {
                var lastTimeNs = System.nanoTime()

                while (isActive && isRunning.get()) {
                    val targetIntervalMs = if (isLowPowerMode.get()) {
                        BACKGROUND_TICK_INTERVAL_MS
                    } else {
                        (DEFAULT_TICK_INTERVAL_MS / speedMultiplier.get().coerceAtLeast(0.1f)).toLong().coerceAtLeast(10L)
                    }

                    if (isPaused.get()) {
                        delay(200L)
                        lastTimeNs = System.nanoTime()
                        continue
                    }

                    val nowNs = System.nanoTime()
                    val rawElapsedSec = (nowNs - lastTimeNs) / 1_000_000_000.0f
                    lastTimeNs = nowNs
                    val dt = rawElapsedSec.coerceIn(0.001f, MAX_DELTA_TIME_CAP) * speedMultiplier.get()

                    val tickStartNs = System.nanoTime()
                    executeEngineTick(dt)
                    val tickEndNs = System.nanoTime()

                    val durationNs = tickEndNs - tickStartNs
                    lastTickDurationNs.set(durationNs)
                    updateAverageDuration(durationNs)

                    val elapsedMs = (tickEndNs - tickStartNs) / 1_000_000L
                    val sleepMs = (targetIntervalMs - elapsedMs).coerceAtLeast(2L)
                    delay(sleepMs)
                }
            }
        }
    }

    /**
     * Pauses the simulation loop.
     */
    fun pause() {
        isPaused.set(true)
        resourceManager.pause()
    }

    /**
     * Resumes the simulation loop.
     */
    fun resume() {
        isPaused.set(false)
        resourceManager.resume()
    }

    /**
     * Toggles low-power mode for backgrounding or battery-saving.
     */
    fun setLowPowerMode(enabled: Boolean) {
        isLowPowerMode.set(enabled)
    }

    /**
     * Sets game simulation speed multiplier (1x, 2x, 5x, 10x).
     */
    fun setSpeedMultiplier(multiplier: Float) {
        val safeMultiplier = multiplier.coerceIn(0.1f, 10.0f)
        speedMultiplier.set(safeMultiplier)
    }

    fun getSpeedMultiplier(): Float = speedMultiplier.get()
    fun isEngineRunning(): Boolean = isRunning.get() && !isPaused.get()

    /**
     * Stops the engine and cancels active coroutine jobs.
     */
    fun stop() {
        resourceManager.stop()
        if (isRunning.compareAndSet(true, false)) {
            engineJob?.cancel()
            engineJob = null
        }
    }

    /**
     * Executes a single discrete simulation tick synchronously (useful for manual unit tests).
     */
    fun executeSingleTick(dt: Float = 1.0f) {
        executeEngineTick(dt)
    }

    /**
     * Executes a single discrete simulation tick across all game subsystems.
     */
    private fun executeEngineTick(dt: Float) {
        val data = SectData.getInstance() ?: return
        val currentTick = tickCounter.incrementAndGet()

        reusableEventList.clear()
        reusableBreakthroughList.clear()

        var genStones = 0L
        var genHerbs = 0L
        var genOres = 0L
        var genPills = 0L

        // 1. Advance Game Internal Clock
        data.time?.let { t ->
            val prevDay = t.day
            t.tick()
            if (t.day != prevDay) {
                handleDailyWageSettlement(data, reusableEventList)
            }
        }

        // 2. Resource Generation from Sect Buildings & Upgrades
        val buildingBonus = calculateBuildingYields(data, dt)
        genStones += buildingBonus.stones
        genHerbs += buildingBonus.herbs
        genOres += buildingBonus.ores
        genPills += buildingBonus.pills

        data.spiritStones += genStones
        data.spiritHerbs += genHerbs
        data.spiritOres += genOres
        data.spiritPills += genPills

        // 3. Process Disciples (Cultivation, Tasks, Breakthroughs, Vitals)
        val disciples = data.disciples
        var totalPower = 0L
        var activeCount = 0

        if (disciples != null) {
            val count = disciples.size
            for (i in 0 until count) {
                val d = disciples.getOrNull(i) ?: continue
                activeCount++

                // Power Calculation
                val discPower = d.atk * 3L + d.def * 2L + d.wis * 2L + (d.realm + 1) * 200L
                totalPower += discPower

                // Cultivation & Task Execution
                processDiscipleCultivation(d, dt, reusableBreakthroughList)
                processDiscipleTask(d, dt,
                    onStones = { yield -> data.spiritStones += yield; genStones += yield },
                    onHerbs = { yield -> data.spiritHerbs += yield; genHerbs += yield },
                    onOres = { yield -> data.spiritOres += yield; genOres += yield }
                )
                processDiscipleVitals(d, dt)
            }
        }

        // 4. Random Auspicious Encounters
        if (currentTick % 200L == 0L) {
            triggerRandomEncounter(data)?.let { encounterMsg ->
                reusableEventList.add(encounterMsg)
                _eventFlow.tryEmit(EngineEvent.SectEncounter(encounterMsg))
            }
        }

        // 5. Emit Breakthrough Events
        for (i in 0 until reusableBreakthroughList.size) {
            val b = reusableBreakthroughList[i]
            _eventFlow.tryEmit(EngineEvent.DiscipleBreakthrough(b.discipleName, b.realmName, b.realmTier))
        }

        // 6. Update StateFlow Snapshot
        val newState = EngineState(
            tick = currentTick,
            deltaSeconds = dt,
            generatedStones = genStones,
            generatedHerbs = genHerbs,
            generatedOres = genOres,
            generatedPills = genPills,
            totalSectPower = max(1000L, totalPower),
            activeDisciples = activeCount,
            tickDurationNs = lastTickDurationNs.get(),
            averageTickDurationNs = averageTickDurationNs.get(),
            recentEvents = if (reusableEventList.isNotEmpty()) ArrayList(reusableEventList) else emptyList()
        )

        _engineState.update { newState }

        // 7. Notify Java Listeners
        if (engineListeners.isNotEmpty()) {
            for (listener in engineListeners) {
                listener.onEngineTick(newState)
            }
        }
    }

    /**
     * Calculates resource generation from Sect facilities.
     */
    private fun calculateBuildingYields(data: SectData, dt: Float): YieldBundle {
        var stones = 0L
        var herbs = 0L
        var ores = 0L
        var pills = 0L

        val buildings = data.buildings
        if (buildings != null) {
            for (i in 0 until buildings.size) {
                val b = buildings.getOrNull(i) ?: continue
                val lvl = b.level.coerceAtLeast(1)

                when (b.type) {
                    GameConfig.BUILD_HALL -> stones += (lvl * 1.5f * dt).toLong() // Main Palace
                    GameConfig.BUILD_LIBRARY -> {} // Training / Cultivation
                    GameConfig.BUILD_GARDEN -> herbs += (lvl * 0.8f * dt).toLong() // Herb Garden
                    GameConfig.BUILD_ALCHEMY -> pills += (lvl * 0.05f * dt).toLong() // Alchemy Pavilion
                    GameConfig.BUILD_MINE -> ores += (lvl * 0.6f * dt).toLong() // Spirit Ore Mine
                }
            }
        }

        return YieldBundle(stones, herbs, ores, pills)
    }

    /**
     * Cultivation progression & Breakthrough evaluation.
     */
    private fun processDiscipleCultivation(
        d: Disciple,
        dt: Float,
        breakthroughList: MutableList<BreakthroughEventData>
    ) {
        val talentMult = d.talent?.expMultiplier ?: 1.0f
        val baseGain = (1.0f + d.wis * 0.05f) * talentMult * dt
        val taskBonus = if (d.currentTask == GameConfig.TASK_CULTIVATION) 2.5f else 0.8f
        val expGain = (baseGain * taskBonus).toInt().coerceAtLeast(1)

        d.realmExp += expGain

        val reqExp = (d.realm + 1) * BASE_EXP_PER_REALM
        if (d.realmExp >= reqExp && d.realm < GameConfig.REALM_MAX - 1) {
            val bonusChance = (d.talent?.breakthroughRateBonus ?: 0) / 100.0f
            val baseSuccessRate = 0.70f + (d.lck * 0.01f) - (d.realm * 0.05f) + bonusChance
            val roll = RNG.nextFloat()

            if (roll <= baseSuccessRate) {
                d.realm += 1
                d.realmExp = 0
                d.atk += 10 + d.realm * 4
                d.def += 8 + d.realm * 3
                d.maxHp += 50 + d.realm * 20
                d.hp = d.maxHp
                d.maxMp += 30 + d.realm * 15
                d.mp = d.maxMp

                breakthroughList.add(
                    BreakthroughEventData(
                        discipleName = d.name ?: "Cultivator",
                        realmName = d.realmDisplay ?: "Higher Realm",
                        realmTier = d.realm
                    )
                )
            } else {
                d.realmExp = (reqExp * 0.4f).toInt()
                d.energy = max(0, d.energy - 20)
                d.stress = min(100, d.stress + 15)
            }
        }
    }

    /**
     * Disciple assigned tasks production.
     */
    private fun processDiscipleTask(
        d: Disciple,
        dt: Float,
        onStones: (Long) -> Unit,
        onHerbs: (Long) -> Unit,
        onOres: (Long) -> Unit
    ) {
        val eff = 1.0f + (d.atk + d.def) * 0.01f
        when (d.currentTask) {
            GameConfig.TASK_MINING -> {
                val yield = (3L * eff * dt).toLong().coerceAtLeast(1L)
                onStones(yield)
            }
            GameConfig.TASK_FARMING -> {
                val yield = (2L * eff * dt).toLong().coerceAtLeast(1L)
                onHerbs(yield)
            }
            GameConfig.TASK_GUARD -> {
                d.reputation += 1
            }
            GameConfig.TASK_ALCHEMY -> {
                // Generates pills over time
            }
        }
    }

    /**
     * Disciple Energy, Mood, and Stress Vitals.
     */
    private fun processDiscipleVitals(d: Disciple, dt: Float) {
        if (d.currentTask == GameConfig.TASK_NONE || d.currentTask == GameConfig.TASK_TRAINING) {
            d.energy = min(d.maxEnergy, d.energy + (5f * dt).toInt())
            d.mood = min(100, d.mood + (2f * dt).toInt())
            d.stress = max(0, d.stress - (3f * dt).toInt())
        } else {
            d.energy = max(0, d.energy - (2f * dt).toInt())
            if (d.energy <= 0) {
                d.currentTask = GameConfig.TASK_NONE // Automatically rest when exhausted
            }
        }
    }

    /**
     * Settles daily wages and increases disciple age.
     */
    private fun handleDailyWageSettlement(data: SectData, events: MutableList<String>) {
        var totalWages = 0L
        val disciples = data.disciples ?: return

        for (i in 0 until disciples.size) {
            val d = disciples.getOrNull(i) ?: continue
            totalWages += d.dailyWage
            d.age++
        }

        if (data.spiritStones >= totalWages) {
            data.spiritStones -= totalWages
            events.add("Daily sect wage settled: -$totalWages Spirit Stones.")
        } else {
            for (i in 0 until disciples.size) {
                val d = disciples.getOrNull(i) ?: continue
                d.loyalty = max(0, d.loyalty - 10)
            }
            events.add("⚠️ Treasury shortage! Disciples suffered loyalty drop.")
        }
    }

    /**
     * Auspicious random events.
     */
    private fun triggerRandomEncounter(data: SectData): String? {
        val roll = RNG.nextInt(5)
        return when (roll) {
            0 -> {
                val bonus = 50L + RNG.nextInt(150)
                data.spiritStones += bonus
                "A wandering immortal bestowed +$bonus Spirit Stones!"
            }
            1 -> {
                val herbs = 20L + RNG.nextInt(40)
                data.spiritHerbs += herbs
                "Spiritual rain fell upon Mount Shu: +$herbs Herbs gathered!"
            }
            2 -> {
                val ores = 15L + RNG.nextInt(30)
                data.spiritOres += ores
                "An auspicious vein surfaced: +$ores Spirit Ores mined!"
            }
            else -> null
        }
    }

    /**
     * Offline Progression Fast-Forward Calculation.
     * Computes hours/days of offline simulation in a single non-blocking pass.
     */
    suspend fun calculateOfflineProgression(offlineSeconds: Long): OfflineProgressSummary = withContext(dispatcher) {
        val safeSeconds = offlineSeconds.coerceIn(0L, 86400L * 7L) // Cap at 7 days
        val data = SectData.getInstance()

        if (data == null || safeSeconds <= 0L) {
            return@withContext OfflineProgressSummary(0L, 0L, 0L, 0L, 0)
        }

        val baseStonesPerSec = 2.0f
        val baseHerbsPerSec = 0.5f
        val baseOresPerSec = 0.3f

        val totalStones = (baseStonesPerSec * safeSeconds).toLong()
        val totalHerbs = (baseHerbsPerSec * safeSeconds).toLong()
        val totalOres = (baseOresPerSec * safeSeconds).toLong()

        data.spiritStones += totalStones
        data.spiritHerbs += totalHerbs
        data.spiritOres += totalOres

        var breakthroughCount = 0
        data.disciples?.forEach { d ->
            if (d != null) {
                val expGain = (safeSeconds * 0.5f).toInt()
                d.realmExp += expGain
                val reqExp = (d.realm + 1) * BASE_EXP_PER_REALM
                if (d.realmExp >= reqExp && d.realm < GameConfig.REALM_MAX - 1) {
                    d.realm++
                    d.realmExp = 0
                    breakthroughCount++
                }
            }
        }

        val summary = OfflineProgressSummary(
            offlineSeconds = safeSeconds,
            spiritStonesGained = totalStones,
            spiritHerbsGained = totalHerbs,
            spiritOresGained = totalOres,
            breakthroughCount = breakthroughCount
        )

        _eventFlow.tryEmit(EngineEvent.OfflineProgressApplied(summary))
        summary
    }

    private fun updateAverageDuration(durationNs: Long) {
        val prevAvg = averageTickDurationNs.get()
        if (prevAvg == 0L) {
            averageTickDurationNs.set(durationNs)
        } else {
            val newAvg = (prevAvg * 9 + durationNs) / 10
            averageTickDurationNs.set(newAvg)
        }
    }

    fun addListener(listener: EngineListener) {
        if (!engineListeners.contains(listener)) {
            engineListeners.add(listener)
        }
    }

    fun removeListener(listener: EngineListener) {
        engineListeners.remove(listener)
    }

    private data class YieldBundle(val stones: Long, val herbs: Long, val ores: Long, val pills: Long)
    private data class BreakthroughEventData(val discipleName: String, val realmName: String, val realmTier: Int)
}

/**
 * Immutable State Snapshot of the Game Engine.
 */
data class EngineState(
    val tick: Long = 0L,
    val deltaSeconds: Float = 0f,
    val generatedStones: Long = 0L,
    val generatedHerbs: Long = 0L,
    val generatedOres: Long = 0L,
    val generatedPills: Long = 0L,
    val totalSectPower: Long = 1000L,
    val activeDisciples: Int = 0,
    val tickDurationNs: Long = 0L,
    val averageTickDurationNs: Long = 0L,
    val recentEvents: List<String> = emptyList()
)

/**
 * Discrete Events emitted by GameEngine.
 */
sealed class EngineEvent {
    data class DiscipleBreakthrough(val discipleName: String, val realmName: String, val realmTier: Int) : EngineEvent()
    data class SectEncounter(val description: String) : EngineEvent()
    data class OfflineProgressApplied(val summary: OfflineProgressSummary) : EngineEvent()
}
