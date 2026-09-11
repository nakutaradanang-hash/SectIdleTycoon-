package com.sect.idle.gameplay

import android.content.Context
import android.util.Log
import com.sect.idle.core.GameConfig
import com.sect.idle.models.Disciple
import com.sect.idle.models.Talent
import com.sect.idle.systems.RNG
import kotlinx.coroutines.CoroutineDispatcher
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
 * CoroutineGameLoop - Central background game simulation loop powered by Kotlin Coroutines.
 * Runs autonomously on a background dispatcher, performing asynchronous:
 *  1. Sect resource generation (Spirit Stones, Herbs, Ores, Pills, Qi)
 *  2. Disciple cultivation progression (Exp, Realm breakthroughs, Attributes, Energy/Mood)
 *  3. Dynamic economy balancing & daily wage distribution
 *  4. Random sect encounters & events
 *  5. Seamless offline progression calculation
 */
class CoroutineGameLoop(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    companion object {
        private const val TAG = "CoroutineGameLoop"
        private const val DEFAULT_TICK_INTERVAL_MS = 1000L // 1.0 second per tick
        private const val BASE_EXP_PER_REALM = 100

        @Volatile
        private var instance: CoroutineGameLoop? = null

        @JvmStatic
        fun get(): CoroutineGameLoop {
            return instance ?: synchronized(this) {
                instance ?: CoroutineGameLoop().also { instance = it }
            }
        }

        @JvmStatic
        fun getInstance(): CoroutineGameLoop = get()
    }

    // Coroutine Scope with SupervisorJob to ensure crash resilience
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var loopJob: Job? = null

    // State Tracking
    private val isRunning = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    private val tickCounter = AtomicLong(0L)
    private val speedMultiplier = AtomicReference(1.0f)

    // Reactive StateFlow for UI & ViewModel observation
    private val _loopState = MutableStateFlow(GameLoopTickState())
    val loopState: StateFlow<GameLoopTickState> = _loopState.asStateFlow()

    // SharedFlow for discrete game events (Breakthroughs, Encounters, Disasters)
    private val _eventFlow = MutableSharedFlow<GameLoopEvent>(extraBufferCapacity = 64)
    val eventFlow: SharedFlow<GameLoopEvent> = _eventFlow.asSharedFlow()

    // Event Listeners for Java interoperability
    private val eventListeners = CopyOnWriteArrayList<GameLoopEventListener>()

    interface GameLoopEventListener {
        fun onTick(state: GameLoopTickState)
        fun onEvent(event: GameLoopEvent)
    }

    /**
     * Starts the background Coroutine game loop if not already running.
     */
    fun start() {
        if (isRunning.compareAndSet(false, true)) {
            isPaused.set(false)
            loopJob = scope.launch {
                Log.i(TAG, "Coroutine Game Loop started on background dispatcher.")
                var lastTimeNs = System.nanoTime()

                while (isActive && isRunning.get()) {
                    if (!isPaused.get()) {
                        val nowNs = System.nanoTime()
                        val dt = (nowNs - lastTimeNs) / 1_000_000_000.0f
                        lastTimeNs = nowNs

                        val mult = speedMultiplier.get()
                        val effectiveDt = dt * mult

                        // Process Background Simulation Tick
                        processSimulationTick(effectiveDt)
                    } else {
                        lastTimeNs = System.nanoTime()
                    }

                    // Respect base tick rate interval
                    val delayMs = (DEFAULT_TICK_INTERVAL_MS / max(0.1f, speedMultiplier.get())).toLong()
                    delay(delayMs)
                }
                Log.i(TAG, "Coroutine Game Loop stopped.")
            }
        }
    }

    /**
     * Pauses the loop without terminating the Coroutine.
     */
    fun pause() {
        isPaused.set(true)
    }

    /**
     * Resumes the paused Coroutine loop.
     */
    fun resume() {
        isPaused.set(false)
    }

    /**
     * Terminates the background loop cleanly.
     */
    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            loopJob?.cancel()
            loopJob = null
        }
    }

    /**
     * Sets the simulation speed multiplier (e.g. 1.0x, 2.0x, 5.0x, 10.0x).
     */
    fun setSpeedMultiplier(multiplier: Float) {
        val clamped = multiplier.coerceIn(0.1f, 100.0f)
        speedMultiplier.set(clamped)
    }

    fun getSpeedMultiplier(): Float = speedMultiplier.get()
    fun isLoopRunning(): Boolean = isRunning.get() && !isPaused.get()
    fun isRunning(): Boolean = isRunning.get()
    fun isPaused(): Boolean = isPaused.get()
    fun getTickCount(): Long = tickCounter.get()

    /**
     * Executes a single simulation tick synchronously (useful for manual unit tests or step debugging).
     */
    fun stepOnce(deltaSeconds: Float = 1.0f): GameLoopTickState {
        return processSimulationTick(deltaSeconds)
    }

    /**
     * Calculates fast-forward offline progression when the player returns.
     */
    suspend fun calculateOfflineProgression(offlineSeconds: Long): OfflineProgressSummary = withContext(dispatcher) {
        val safeSeconds = offlineSeconds.coerceIn(0L, 86400L * 7) // Up to 7 days offline
        val ticks = (safeSeconds / 5).coerceIn(1, 5000).toInt() // Aggregate in larger chunk ticks
        val dtPerChunk = safeSeconds.toFloat() / ticks.toFloat()

        var totalStonesEarned = 0L
        var totalHerbsEarned = 0L
        var totalOresEarned = 0L
        var totalBreakthroughs = 0

        for (i in 0 until ticks) {
            val tickResult = processSimulationTick(dtPerChunk)
            totalStonesEarned += tickResult.generatedStones
            totalHerbsEarned += tickResult.generatedHerbs
            totalOresEarned += tickResult.generatedOres
        }

        val summary = OfflineProgressSummary(
            offlineSeconds = safeSeconds,
            spiritStonesGained = totalStonesEarned,
            spiritHerbsGained = totalHerbsEarned,
            spiritOresGained = totalOresEarned,
            breakthroughCount = totalBreakthroughs
        )

        _eventFlow.tryEmit(GameLoopEvent.OfflineProgressApplied(summary))
        summary
    }

    // =========================================================================
    // CORE SIMULATION LOGIC (RESOURCES & DISCIPLES)
    // =========================================================================

    private fun processSimulationTick(dt: Float): GameLoopTickState {
        val currentTick = tickCounter.incrementAndGet()
        val data = SectData.getInstance() ?: return _loopState.value

        var deltaStones = 0L
        var deltaHerbs = 0L
        var deltaOres = 0L
        var deltaPills = 0L
        val recentEvents = mutableListOf<String>()

        synchronized(data) {
            // 1. Advance Internal Game Time
            data.time?.let { t ->
                val prevDay = t.day
                t.tick()
                if (t.day != prevDay) {
                    // Daily Sect Economy & Wage Settlement
                    handleDailySectSettlement(data, recentEvents)
                }
            }

            // 2. Resource Generation from Buildings & Passives
            val buildings = data.buildings
            if (buildings != null) {
                for (i in 0 until buildings.size) {
                    val b = buildings[i] ?: continue
                    if (b.isBuilt) {
                        when (b.type) {
                            GameConfig.BUILD_MINE -> {
                                val rate = (b.level * 2L * dt).toLong().coerceAtLeast(1L)
                                deltaOres += rate
                                deltaStones += (b.level * 1L * dt).toLong().coerceAtLeast(1L)
                            }
                            GameConfig.BUILD_GARDEN -> {
                                val rate = (b.level * 3L * dt).toLong().coerceAtLeast(1L)
                                deltaHerbs += rate
                            }
                            GameConfig.BUILD_ALCHEMY -> {
                                if (data.spiritHerbs >= 5 && RNG.nextFloat() < 0.15f * dt) {
                                    data.spiritHerbs -= 5
                                    deltaPills += 1
                                    recentEvents.add("Alchemy Pavilion auto-crafted 1 Spirit Pill.")
                                }
                            }
                            GameConfig.BUILD_HALL -> {
                                deltaStones += (b.level * 1L * dt).toLong()
                            }
                        }
                    }
                }
            }

            // 3. Disciple Progression Engine
            val disciples = data.disciples
            var totalPower = 0L
            if (disciples != null) {
                for (i in 0 until disciples.size) {
                    val d = disciples[i] ?: continue
                    
                    // A. Update Cultivation Exp & Breakthrough Checks
                    processDiscipleCultivation(d, dt, recentEvents)

                    // B. Task Progress & Labor Yield
                    processDiscipleTask(d, dt, { s -> deltaStones += s }, { h -> deltaHerbs += h }, { o -> deltaOres += o })

                    // C. Energy, Fatigue, and Mood
                    processDiscipleVitals(d, dt)

                    totalPower += (d.atk + d.def + d.hp / 10 + d.realm * 50)
                }
                data.sectPower = totalPower
            }

            // 4. Commit Resource Accumulation
            data.spiritStones = (data.spiritStones + deltaStones).coerceAtLeast(0L)
            data.spiritHerbs = (data.spiritHerbs + deltaHerbs).coerceAtLeast(0L)
            data.spiritOres = (data.spiritOres + deltaOres).coerceAtLeast(0L)
            data.spiritPills = (data.spiritPills + deltaPills).coerceAtLeast(0L)

            // 5. Random Encounter / Sect Events (Probabilistic trigger per minute)
            if (RNG.nextFloat() < (0.02f * dt)) {
                val encounter = triggerRandomSectEncounter(data)
                if (encounter != null) {
                    recentEvents.add(encounter)
                }
            }
        }

        val newState = GameLoopTickState(
            tick = currentTick,
            deltaSeconds = dt,
            generatedStones = deltaStones,
            generatedHerbs = deltaHerbs,
            generatedOres = deltaOres,
            totalSectPower = data.sectPower,
            activeDisciples = data.disciples?.size ?: 0,
            recentEvents = recentEvents
        )

        _loopState.update { newState }

        // Notify Java listeners
        for (listener in eventListeners) {
            try {
                listener.onTick(newState)
            } catch (ignored: Throwable) {}
        }

        return newState
    }

    private fun processDiscipleCultivation(d: Disciple, dt: Float, events: MutableList<String>) {
        val talentMult = d.talent?.expMultiplier ?: 1.0f
        val baseGain = (1.0f + d.wis * 0.05f) * talentMult * dt
        
        // Cultivation speed bonus when meditating in library or cultivation chamber
        val taskBonus = if (d.currentTask == GameConfig.TASK_CULTIVATION) 2.5f else 0.8f
        val expGain = (baseGain * taskBonus).toInt().coerceAtLeast(1)

        d.realmExp += expGain

        // Check Realm Breakthrough Threshold
        val reqExp = (d.realm + 1) * BASE_EXP_PER_REALM
        if (d.realmExp >= reqExp && d.realm < GameConfig.REALM_MAX - 1) {
            // Attempt breakthrough
            val bonusChance = (d.talent?.breakthroughRateBonus ?: 0) / 100.0f
            val baseSuccessRate = 0.70f + (d.lck * 0.01f) - (d.realm * 0.05f) + bonusChance
            val roll = RNG.nextFloat()

            if (roll <= baseSuccessRate) {
                d.realm++
                d.realmExp = 0
                d.recalcCombat()
                val eventMsg = "✦ Breakthrough! ${d.name} ascended to ${d.realmDisplay}!"
                events.add(eventMsg)
                _eventFlow.tryEmit(GameLoopEvent.DiscipleBreakthrough(d.name, d.realmDisplay, d.realm))
            } else {
                // Minor setback on failure, retains 70% exp
                d.realmExp = (reqExp * 0.7f).toInt()
                d.mood = (d.mood - 5).coerceAtLeast(0)
            }
        }
    }

    private fun processDiscipleTask(
        d: Disciple,
        dt: Float,
        onStones: (Long) -> Unit,
        onHerbs: (Long) -> Unit,
        onOres: (Long) -> Unit
    ) {
        if (d.energy <= 10) {
            // Exhausted disciples cannot work effectively
            return
        }

        d.updateEfficiency()
        val eff = d.efficiency * (d.taskEfficiency / 100.0f)

        when (d.currentTask) {
            GameConfig.TASK_MINING -> {
                val yield = (1L * eff * dt).toLong().coerceAtLeast(1L)
                onOres(yield)
                onStones(yield / 2)
            }
            GameConfig.TASK_FARMING -> {
                val yield = (2L * eff * dt).toLong().coerceAtLeast(1L)
                onHerbs(yield)
            }
            GameConfig.TASK_GUARD -> {
                d.reputation += 1
            }
            GameConfig.TASK_ALCHEMY -> {
                d.intel = min(100, d.intel + if (RNG.nextFloat() < 0.05f * dt) 1 else 0)
            }
        }
    }

    private fun processDiscipleVitals(d: Disciple, dt: Float) {
        if (d.currentTask == GameConfig.TASK_NONE || d.currentTask == GameConfig.TASK_TRAINING) {
            // Recover energy and mood while resting / training
            d.energy = min(d.maxEnergy, d.energy + (5f * dt).toInt())
            d.mood = min(100, d.mood + (2f * dt).toInt())
            d.stress = max(0, d.stress - (3f * dt).toInt())
        } else {
            // Working expends energy
            d.energy = max(0, d.energy - (1.5f * dt).toInt())
            d.stress = min(100, d.stress + (0.5f * dt).toInt())
        }

        // Natural HP / MP recovery
        d.hp = min(d.maxHp, d.hp + (10f * dt).toInt())
        d.mp = min(d.maxMp, d.mp + (10f * dt).toInt())
    }

    private fun handleDailySectSettlement(data: SectData, events: MutableList<String>) {
        var totalWages = 0L
        data.disciples?.forEach { d ->
            if (d != null) {
                totalWages += d.dailyWage
                // Age progression
                d.age++
            }
        }

        if (data.spiritStones >= totalWages) {
            data.spiritStones -= totalWages
            events.add("Daily sect wage settled: -${totalWages} Spirit Stones.")
        } else {
            // Wage unrest: reduce disciple loyalty
            data.disciples?.forEach { d ->
                if (d != null) d.loyalty = max(0, d.loyalty - 10)
            }
            events.add("⚠️ Insufficient treasury! Sect disciples suffered loyalty drop.")
        }
    }

    private fun triggerRandomSectEncounter(data: SectData): String? {
        val roll = RNG.nextInt(5)
        return when (roll) {
            0 -> {
                val bonus = 50L + RNG.nextInt(150)
                data.spiritStones += bonus
                "A wandering Daoist bestowed +${bonus} Spirit Stones upon the Sect!"
            }
            1 -> {
                val herbs = 20L + RNG.nextInt(40)
                data.spiritHerbs += herbs
                "Spiritual rain fell upon Mount Shu, granting +${herbs} Spirit Herbs!"
            }
            2 -> {
                "Sect disciples harmonized spiritual Qi, boosting cultivation aura."
            }
            3 -> {
                val ores = 15L + RNG.nextInt(30)
                data.spiritOres += ores
                "An auspicious vein burst in the mines: +${ores} Spirit Ores found!"
            }
            else -> null
        }
    }

    fun addEventListener(listener: GameLoopEventListener) {
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener)
        }
    }

    fun removeEventListener(listener: GameLoopEventListener) {
        eventListeners.remove(listener)
    }
}

/**
 * Immutable tick state for reactive UI consumption.
 */
data class GameLoopTickState(
    val tick: Long = 0L,
    val deltaSeconds: Float = 0f,
    val generatedStones: Long = 0L,
    val generatedHerbs: Long = 0L,
    val generatedOres: Long = 0L,
    val totalSectPower: Long = 1000L,
    val activeDisciples: Int = 0,
    val recentEvents: List<String> = emptyList()
)

/**
 * Discrete one-off events emitted by the game loop.
 */
sealed class GameLoopEvent {
    data class DiscipleBreakthrough(val discipleName: String, val realmName: String, val realmTier: Int) : GameLoopEvent()
    data class SectEncounter(val description: String) : GameLoopEvent()
    data class OfflineProgressApplied(val summary: OfflineProgressSummary) : GameLoopEvent()
}

/**
 * Summary for offline progression calculations.
 */
data class OfflineProgressSummary(
    val offlineSeconds: Long,
    val spiritStonesGained: Long,
    val spiritHerbsGained: Long,
    val spiritOresGained: Long,
    val breakthroughCount: Int
)
