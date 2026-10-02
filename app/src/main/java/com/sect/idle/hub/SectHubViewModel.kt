package com.sect.idle.hub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sect.idle.core.GameConfig
import com.sect.idle.data.repository.SectRepository
import com.sect.idle.gameplay.CoroutineResourceManager
import com.sect.idle.gameplay.SaveManager
import com.sect.idle.gameplay.SectData
import com.sect.idle.models.Disciple
import com.sect.idle.systems.AudioManager
import com.sect.idle.systems.HapticManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import java.util.UUID

class SectHubViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SectHubState())
    val uiState: StateFlow<SectHubState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private val saveManager = SaveManager(application)
    private val repository = SectRepository.get(application)
    private val hapticManager = HapticManager.get(application)
    private val resourceManager = CoroutineResourceManager.get()
    private val milestoneSystem = com.sect.idle.gameplay.MilestoneSystem(application)
    private val random = Random()

    init {
        initDefaultHubState()
        syncFromSectData()
        evaluateMilestones()
        startPeriodicTicker()

        viewModelScope.launch {
            resourceManager.resourceState.collect { res ->
                _uiState.update { current ->
                    current.copy(
                        spiritStones = res.spiritStones,
                        spiritHerbs = res.spiritHerbs,
                        spiritOres = res.spiritOres,
                        spiritPills = res.spiritPills,
                        jade = res.jade,
                        essence = res.essence,
                        sectPower = res.sectPower
                    )
                }
            }
        }
    }

    private fun getRealmMaxExp(realm: Int): Int {
        return (realm + 1) * 200
    }

    private fun initDefaultHubState() {
        val defaultRegions = listOf(
            MapRegion(
                id = "emerald_bamboo_forest",
                name = "Emerald Bamboo Forest",
                chineseName = "青竹林",
                description = "Dense mist and spirit bamboo stalks where low-level spiritual herbs and essence thrive.",
                dangerLevel = 1,
                minSectRealmRequired = 0,
                isUnlocked = true,
                resourceYieldDescription = "🌱 50-80 Herbs, 🪙 150 Spirit Stones",
                iconEmoji = "🎋"
            ),
            MapRegion(
                id = "dragon_vein_mountains",
                name = "Dragon Vein Mountains",
                chineseName = "龙脉群山",
                description = "Craggy peaks rich in spirit ores, ancient meteor metals, and raw spirit stones.",
                dangerLevel = 2,
                minSectRealmRequired = 0,
                isUnlocked = true,
                resourceYieldDescription = "⛏️ 40-70 Ores, 🪙 300 Spirit Stones",
                iconEmoji = "⛰️"
            ),
            MapRegion(
                id = "heavenly_lotus_valley",
                name = "Heavenly Lotus Valley",
                chineseName = "天莲幽谷",
                description = "Sacred springs sheltered by ancient formations. High chance of discovering Spirit Jade & Pills.",
                dangerLevel = 3,
                minSectRealmRequired = 1,
                isUnlocked = true,
                resourceYieldDescription = "💎 5-15 Jade, 💊 3 Spirit Pills",
                iconEmoji = "🪷"
            ),
            MapRegion(
                id = "demon_subduing_abyss",
                name = "Demon Subduing Abyss",
                chineseName = "镇魔深渊",
                description = "Ancient battlefield sealing remnant demonic spirits. Tremendous yields but perilous tribulations.",
                dangerLevel = 4,
                minSectRealmRequired = 2,
                isUnlocked = true,
                resourceYieldDescription = "✨ 20 Essence, 🪙 800 Spirit Stones, Rare Artifacts",
                iconEmoji = "🌋"
            ),
            MapRegion(
                id = "nine_heavens_astral_peak",
                name = "Nine Heavens Astral Peak",
                chineseName = "九天星辰峰",
                description = "Touches the boundless starry sky. Gathers celestial dao fragments and primordial immortal stones.",
                dangerLevel = 5,
                minSectRealmRequired = 3,
                isUnlocked = false,
                resourceYieldDescription = "🌌 Celestial Dao Treasures, 💎 30 Jade",
                iconEmoji = "✨"
            )
        )

        val defaultSlots = listOf(
            MeditationSlot(slotIndex = 0, isUnlocked = true, qiGainMultiplier = 1.0f),
            MeditationSlot(slotIndex = 1, isUnlocked = true, qiGainMultiplier = 1.2f),
            MeditationSlot(slotIndex = 2, isUnlocked = false, unlockCost = 500, qiGainMultiplier = 1.5f),
            MeditationSlot(slotIndex = 3, isUnlocked = false, unlockCost = 1500, qiGainMultiplier = 2.0f)
        )

        _uiState.update { state ->
            state.copy(
                worldMap = state.worldMap.copy(regions = defaultRegions),
                cultivationChamber = state.cultivationChamber.copy(meditationSlots = defaultSlots)
            )
        }
    }

    private fun startPeriodicTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            var tickCounter = 0
            while (isActive) {
                delay(1000L)
                tickCounter++
                val data = SectData.getInstance() ?: continue

                // Coroutine-based Timer: Automatically increments Sect resources every 2 seconds based on disciple count
                if (tickCounter % 2 == 0) {
                    val activeDiscipleCount = data.disciples.size.coerceAtLeast(1)
                    val basePassiveStones = activeDiscipleCount * 3L
                    data.spiritStones += basePassiveStones

                    val farmers = data.getActiveTaskCount(GameConfig.TASK_FARMING)
                    val miners = data.getActiveTaskCount(GameConfig.TASK_MINING)
                    val alchemists = data.getActiveTaskCount(GameConfig.TASK_ALCHEMY)

                    if (farmers > 0) data.spiritHerbs += (farmers * 2L)
                    if (miners > 0) data.spiritOres += (miners * 2L)
                    if (alchemists > 0 && data.spiritHerbs >= alchemists * 2) {
                        data.spiritHerbs -= alchemists * 2
                        data.spiritPills += alchemists
                    }

                    data.recalculateEconomy()
                }

                // Update active expeditions
                _uiState.update { state ->
                    val updatedRegions = state.worldMap.regions.map { region ->
                        if (region.isExpeditionActive) {
                            val newRemaining = (region.expeditionTimeRemainingSec - 1).coerceAtLeast(0)
                            if (newRemaining == 0) {
                                region.copy(
                                    isExpeditionActive = false,
                                    expeditionTimeRemainingSec = 0,
                                    hasLootToClaim = true,
                                    lootSummary = "Disciples returned victorious with abundant spiritual treasures!"
                                )
                            } else {
                                region.copy(expeditionTimeRemainingSec = newRemaining)
                            }
                        } else {
                            region
                        }
                    }

                    // Update meditation slots progress
                    val updatedSlots = state.cultivationChamber.meditationSlots.map { slot ->
                        if (slot.isUnlocked && slot.assignedDiscipleId != null) {
                            val targetDisciple = data.disciples.firstOrNull { it.id == slot.assignedDiscipleId }
                            if (targetDisciple != null) {
                                val expGain = (3 * slot.qiGainMultiplier * (1f + state.cultivationChamber.qiDensityBonusPercent / 100f)).toInt()
                                targetDisciple.realmExp += expGain
                                val maxExp = getRealmMaxExp(targetDisciple.realm)
                                val progress = (targetDisciple.realmExp.toFloat() / maxExp.coerceAtLeast(1)).coerceIn(0f, 1f)
                                slot.copy(meditationProgress = progress)
                            } else {
                                slot.copy(assignedDiscipleId = null, assignedDiscipleName = null)
                            }
                        } else {
                            slot
                        }
                    }

                    state.copy(
                        worldMap = state.worldMap.copy(regions = updatedRegions),
                        cultivationChamber = state.cultivationChamber.copy(meditationSlots = updatedSlots)
                    )
                }

                // Periodically evaluate milestones
                if (tickCounter % 4 == 0) {
                    evaluateMilestones()
                }

                // Occasionally trigger random encounters if none is currently active
                if (tickCounter % 30 == 0 && _uiState.value.activeEncounter == null) {
                    if (random.nextInt(100) < 35) {
                        triggerRandomEncounter()
                    }
                }

                syncFromSectData()
            }
        }
    }

    fun syncFromSectData() {
        val data = SectData.getInstance() ?: return
        val mappedDisciples = data.disciples.map { d ->
            val maxExp = getRealmMaxExp(d.realm)
            DiscipleUiModel(
                id = d.id ?: UUID.randomUUID().toString().also { d.id = it },
                name = d.name ?: "Cultivator",
                title = d.title ?: "Disciple",
                isMale = d.isMale,
                age = d.age,
                realm = d.realm,
                realmName = if (d.realm >= 0 && d.realm < GameConfig.REALMS.size) GameConfig.REALMS[d.realm] else "Mortal",
                realmExp = d.realmExp,
                realmMaxExp = maxExp,
                element = d.element,
                elementName = if (d.element >= 0 && d.element < GameConfig.ELEMENT_NAMES.size) GameConfig.ELEMENT_NAMES[d.element] else "Standard",
                elementColor = getElementColorHex(d.element),
                talentName = d.talentName ?: "Standard Roots",
                talentGrade = d.talentGrade,
                currentTask = d.currentTask,
                taskName = if (d.currentTask >= 0 && d.currentTask < GameConfig.TASK_NAMES.size) GameConfig.TASK_NAMES[d.currentTask] else "Idle",
                taskEfficiency = d.taskEfficiency,
                combatPower = d.getPowerRating().toInt(),
                hp = d.hp,
                maxHp = d.maxHp.coerceAtLeast(1),
                mp = d.mp,
                maxMp = d.maxMp.coerceAtLeast(1),
                energy = d.energy,
                mood = d.mood,
                loyalty = d.loyalty,
                dailyWage = d.dailyWage,
                alchemySkill = d.alchemySkill,
                bodyRefiningStage = d.bodyRefiningStage,
                str = d.str,
                agi = d.agi,
                intel = d.intel,
                lck = d.lck,
                vit = d.vit,
                wis = d.wis,
                cha = d.cha,
                isReadyForBreakthrough = d.realmExp >= maxExp
            )
        }

        val economy = SectEconomyState(
            dailyIncomeSS = data.dailyIncomeSS,
            dailyExpenseSS = data.dailyExpenseSS,
            netDailySS = data.netDailySS,
            activeFarmers = data.getActiveTaskCount(GameConfig.TASK_FARMING),
            activeAlchemists = data.getActiveTaskCount(GameConfig.TASK_ALCHEMY),
            activeMiners = data.getActiveTaskCount(GameConfig.TASK_MINING),
            activeGuards = data.getActiveTaskCount(GameConfig.TASK_GUARD),
            activeCultivators = data.getActiveTaskCount(GameConfig.TASK_CULTIVATION)
        )

        _uiState.update { current ->
            current.copy(
                sectName = data.sectName,
                sectRealm = data.sectRealm,
                sectRealmName = if (data.sectRealm >= 0 && data.sectRealm < GameConfig.REALMS.size) GameConfig.REALMS[data.sectRealm] else "Mortal",
                sectRealmExp = data.sectRealmExp,
                sectRealmMaxExp = (data.sectRealm + 1) * 100,
                sectRank = data.sectRank,
                sectPower = data.sectPower,
                spiritStones = data.spiritStones,
                spiritHerbs = data.spiritHerbs,
                spiritPills = data.spiritPills,
                spiritOres = data.spiritOres,
                jade = data.jade,
                essence = data.essence,
                disciples = mappedDisciples,
                economy = economy,
                cultivationChamber = current.cultivationChamber.copy(
                    pillCount = data.spiritPills
                )
            )
        }
    }

    private fun getElementColorHex(element: Int): Long {
        return when (element) {
            GameConfig.ELEM_FIRE -> 0xFFFF5722
            GameConfig.ELEM_WATER -> 0xFF29B6F6
            GameConfig.ELEM_WOOD -> 0xFF66BB6A
            GameConfig.ELEM_EARTH -> 0xFFFFA726
            GameConfig.ELEM_METAL -> 0xFFFFD54F
            GameConfig.ELEM_LIGHTNING -> 0xFFAB47BC
            else -> 0xFF00E5FF
        }
    }

    fun selectTab(tab: SectHubTab) {
        _uiState.update { it.copy(currentTab = tab) }
        playClickSfx()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setTaskFilter(task: Int?) {
        _uiState.update { it.copy(taskFilter = task) }
        playClickSfx()
    }

    fun assignDiscipleTask(discipleId: String, newTask: Int) {
        val data = SectData.getInstance() ?: return
        val disciple = data.disciples.firstOrNull { it.id == discipleId } ?: return
        disciple.currentTask = newTask
        disciple.updateEfficiency()
        data.recalculateEconomy()
        syncFromSectData()

        val taskName = if (newTask >= 0 && newTask < GameConfig.TASK_NAMES.size) GameConfig.TASK_NAMES[newTask] else "Task"
        showNotification("Assigned ${disciple.name} to $taskName duty.")
        playSfx(AudioManager.SFX_ASSIGN_TASK)
        saveState()
    }

    fun upgradeCultivationArray() {
        val state = _uiState.value
        val cost = state.cultivationChamber.arrayUpgradeCost
        val data = SectData.getInstance() ?: return

        if (data.spiritStones < cost) {
            showNotification("Insufficient Spirit Stones (Requires $cost stones).")
            return
        }

        data.spiritStones -= cost
        val newLevel = state.cultivationChamber.arrayLevel + 1
        val newBonus = newLevel * 25
        val nextCost = (cost * 1.8f).toLong()

        _uiState.update { current ->
            current.copy(
                cultivationChamber = current.cultivationChamber.copy(
                    arrayLevel = newLevel,
                    qiDensityBonusPercent = newBonus,
                    arrayUpgradeCost = nextCost
                )
            )
        }

        data.recalculateEconomy()
        syncFromSectData()
        showNotification("Nine Dragons Qi Array upgraded to Tier $newLevel! Qi density bonus: +$newBonus%")
        playSfx(AudioManager.SFX_UPGRADE)
        saveState()
    }

    fun unlockMeditationSlot(slotIndex: Int) {
        val slot = _uiState.value.cultivationChamber.meditationSlots.firstOrNull { it.slotIndex == slotIndex } ?: return
        if (slot.isUnlocked) return

        val data = SectData.getInstance() ?: return
        if (data.spiritStones < slot.unlockCost) {
            showNotification("Insufficient Spirit Stones (Requires ${slot.unlockCost} stones).")
            return
        }

        data.spiritStones -= slot.unlockCost
        _uiState.update { state ->
            val updated = state.cultivationChamber.meditationSlots.map {
                if (it.slotIndex == slotIndex) it.copy(isUnlocked = true) else it
            }
            state.copy(cultivationChamber = state.cultivationChamber.copy(meditationSlots = updated))
        }

        data.recalculateEconomy()
        syncFromSectData()
        showNotification("Unlocked Meditation Cushion #${slotIndex + 1}!")
        playSfx(AudioManager.SFX_UPGRADE)
        saveState()
    }

    fun seatDiscipleInMeditation(slotIndex: Int, discipleId: String) {
        val data = SectData.getInstance() ?: return
        val disciple = data.disciples.firstOrNull { it.id == discipleId } ?: return

        disciple.currentTask = GameConfig.TASK_CULTIVATION
        disciple.updateEfficiency()

        _uiState.update { state ->
            val updated = state.cultivationChamber.meditationSlots.map {
                if (it.slotIndex == slotIndex) {
                    val realmName = if (disciple.realm >= 0 && disciple.realm < GameConfig.REALMS.size) GameConfig.REALMS[disciple.realm] else "Mortal"
                    it.copy(
                        assignedDiscipleId = disciple.id,
                        assignedDiscipleName = disciple.name,
                        assignedDiscipleRealm = realmName
                    )
                } else if (it.assignedDiscipleId == discipleId) {
                    it.copy(assignedDiscipleId = null, assignedDiscipleName = null, assignedDiscipleRealm = null)
                } else {
                    it
                }
            }
            state.copy(cultivationChamber = state.cultivationChamber.copy(meditationSlots = updated))
        }

        data.recalculateEconomy()
        syncFromSectData()
        showNotification("Seated ${disciple.name} on Meditation Cushion #${slotIndex + 1}.")
        playSfx(AudioManager.SFX_CLICK)
        saveState()
    }

    fun vacateMeditationSlot(slotIndex: Int) {
        _uiState.update { state ->
            val updated = state.cultivationChamber.meditationSlots.map {
                if (it.slotIndex == slotIndex) {
                    it.copy(assignedDiscipleId = null, assignedDiscipleName = null, assignedDiscipleRealm = null, meditationProgress = 0f)
                } else it
            }
            state.copy(cultivationChamber = state.cultivationChamber.copy(meditationSlots = updated))
        }
        showNotification("Vacated Meditation Cushion #${slotIndex + 1}.")
        playClickSfx()
    }

    fun togglePillBoost() {
        _uiState.update { state ->
            state.copy(
                cultivationChamber = state.cultivationChamber.copy(
                    pillBoostActive = !state.cultivationChamber.pillBoostActive
                )
            )
        }
        playClickSfx()
    }

    fun attemptChamberBreakthrough(discipleId: String) {
        val data = SectData.getInstance() ?: return
        val disc = data.disciples.firstOrNull { it.id == discipleId } ?: return
        val chamber = _uiState.value.cultivationChamber

        val maxExp = getRealmMaxExp(disc.realm)
        if (disc.realmExp < maxExp) {
            showNotification("${disc.name} has not accumulated enough spiritual Qi for a breakthrough yet.")
            return
        }

        var successChance = chamber.baseBreakthroughChance
        if (chamber.pillBoostActive && data.spiritPills > 0) {
            data.spiritPills--
            successChance += chamber.pillBonusChance
        }

        val roll = random.nextInt(100)
        val success = roll < successChance

        if (success) {
            disc.realm++
            disc.realmExp = 0
            disc.atk += 15 + disc.realm * 5
            disc.def += 10 + disc.realm * 3
            disc.maxHp += 50 + disc.realm * 20
            disc.hp = disc.maxHp
            disc.maxMp += 30 + disc.realm * 10
            disc.mp = disc.maxMp
            disc.loyalty = (disc.loyalty + 15).coerceAtMost(100)
            data.sectExp += 50 * (disc.realm + 1)
            data.sectRealmExp += 10

            val realmTitle = if (disc.realm >= 0 && disc.realm < GameConfig.REALMS.size) GameConfig.REALMS[disc.realm] else "Mortal"
            val log = "✦ TRIUMPH! ${disc.name} broke through the shackles and ascended to $realmTitle!"
            _uiState.update { state ->
                state.copy(
                    cultivationChamber = state.cultivationChamber.copy(lastBreakthroughLog = log)
                )
            }
            showNotification(log)
            playSfx(AudioManager.SFX_BREAKTHROUGH)
        } else {
            disc.realmExp = (disc.realmExp * 0.7f).toInt()
            disc.energy = (disc.energy - 30).coerceAtLeast(10)
            val log = "⚠ Bottleneck Rebound! ${disc.name}'s meridians trembled. Qi was partially dispersed."
            _uiState.update { state ->
                state.copy(
                    cultivationChamber = state.cultivationChamber.copy(lastBreakthroughLog = log)
                )
            }
            showNotification(log)
            playSfx(AudioManager.SFX_BREAKTHROUGH_FAIL)
        }

        data.recalculateEconomy()
        syncFromSectData()
        saveState()
    }

    fun dispatchExpedition(regionId: String, discipleIds: List<String>) {
        val region = _uiState.value.worldMap.regions.firstOrNull { it.id == regionId } ?: return
        if (!region.isUnlocked) {
            val reqRealmName = if (region.minSectRealmRequired >= 0 && region.minSectRealmRequired < GameConfig.REALMS.size) GameConfig.REALMS[region.minSectRealmRequired] else "Mortal"
            showNotification("Region locked! Requires Sect Realm $reqRealmName.")
            return
        }
        if (region.isExpeditionActive) {
            showNotification("An expedition party is already exploring ${region.name}.")
            return
        }

        val data = SectData.getInstance() ?: return
        val selectedDisciples = data.disciples.filter { discipleIds.contains(it.id) }
        val names = selectedDisciples.map { it.name }

        _uiState.update { state ->
            val updated = state.worldMap.regions.map {
                if (it.id == regionId) {
                    it.copy(
                        isExpeditionActive = true,
                        expeditionTotalTimeSec = 30 + it.dangerLevel * 15,
                        expeditionTimeRemainingSec = 30 + it.dangerLevel * 15,
                        assignedDiscipleNames = names,
                        hasLootToClaim = false,
                        lootSummary = ""
                    )
                } else it
            }
            state.copy(worldMap = state.worldMap.copy(regions = updated))
        }

        showNotification("Dispatched ${names.joinToString(", ")} to explore ${region.name}!")
        playClickSfx()
    }

    fun claimExpeditionLoot(regionId: String) {
        val region = _uiState.value.worldMap.regions.firstOrNull { it.id == regionId } ?: return
        if (!region.hasLootToClaim) return

        val data = SectData.getInstance() ?: return
        val danger = region.dangerLevel
        val stonesGain = 150L * danger + random.nextInt(100)
        val herbsGain = if (danger <= 2) 40L * danger else 15L
        val oresGain = if (danger == 2 || danger == 4) 30L * danger else 10L
        val jadeGain = if (danger >= 3) 5L * (danger - 2) else 0L
        val pillsGain = if (danger >= 3) 3L else 0L

        data.spiritStones += stonesGain
        data.spiritHerbs += herbsGain
        data.spiritOres += oresGain
        data.jade += jadeGain
        data.spiritPills += pillsGain
        data.sectRealmExp += danger * 5
        data.recalculateEconomy()

        val logEntry = "Claimed from ${region.name}: +$stonesGain Stones, +$herbsGain Herbs, +$oresGain Ores, +$jadeGain Jade, +$pillsGain Pills."

        _uiState.update { state ->
            val updated = state.worldMap.regions.map {
                if (it.id == regionId) {
                    it.copy(hasLootToClaim = false, lootSummary = "")
                } else it
            }
            val logs = (listOf(logEntry) + state.worldMap.recentExplorationLogs).take(6)
            state.copy(worldMap = state.worldMap.copy(regions = updated, recentExplorationLogs = logs))
        }

        syncFromSectData()
        showNotification("Loot Claimed! $logEntry")
        playSfx(AudioManager.SFX_VICTORY)
        saveState()
    }

    fun gatherAmbientQi() {
        val data = SectData.getInstance() ?: return
        val gain = 25 + (_uiState.value.cultivationChamber.arrayLevel * 10)
        data.spiritStones += gain
        data.spiritHerbs += 2
        data.recalculateEconomy()
        syncFromSectData()
        showNotification("Gathered ambient heaven & earth Qi: +$gain Spirit Stones, +2 Spirit Herbs!")
        playClickSfx()
    }

    fun recruitNewDisciple() {
        val data = SectData.getInstance() ?: return
        if (data.disciples.size >= data.maxDisciples) {
            showNotification("Sect capacity reached (${data.disciples.size}/${data.maxDisciples}). Expand Main Hall to recruit more.")
            return
        }
        val cost = 200L
        if (data.spiritStones < cost) {
            showNotification("Insufficient Spirit Stones (Requires 200 stones to invite cultivators).")
            return
        }

        data.spiritStones -= cost
        val candidate = generateRandomDisciple()
        data.addDisciple(candidate)
        data.recalculateEconomy()
        syncFromSectData()
        val candidateRealmName = if (candidate.realm >= 0 && candidate.realm < GameConfig.REALMS.size) GameConfig.REALMS[candidate.realm] else "Mortal"
        showNotification("Welcome new cultivator ${candidate.name} ($candidateRealmName) to the sect!")
        playSfx(AudioManager.SFX_RECRUIT)
        saveState()
    }

    private fun generateRandomDisciple(): Disciple {
        val d = Disciple()
        val names = arrayOf("Ling Feng", "Mu Chen", "Xiao Yan", "Han Li", "Bai Xiaochun", "Ye Fan", "Su Ping", "Chu Wan", "Lin Dong", "Gu Changge")
        d.id = UUID.randomUUID().toString()
        d.name = names[random.nextInt(names.size)]
        d.title = "Junior Disciple"
        d.isMale = random.nextBoolean()
        d.age = 16 + random.nextInt(12)
        d.realm = 0
        d.realmExp = 0
        d.element = random.nextInt(6)
        d.str = 10 + random.nextInt(15)
        d.agi = 10 + random.nextInt(15)
        d.intel = 10 + random.nextInt(15)
        d.lck = 5 + random.nextInt(20)
        d.vit = 12 + random.nextInt(15)
        d.wis = 10 + random.nextInt(15)
        d.cha = 8 + random.nextInt(12)
        d.hp = 100 + d.vit * 10
        d.maxHp = d.hp
        d.mp = 50 + d.intel * 8
        d.maxMp = d.mp
        d.energy = 100
        d.maxEnergy = 100
        d.mood = 80
        d.loyalty = 75
        d.dailyWage = 5
        d.currentTask = GameConfig.TASK_CULTIVATION
        d.alchemySkill = 1 + random.nextInt(3)
        d.bodyRefiningStage = 1
        d.talentGrade = 1 + random.nextInt(3)
        d.talentName = when (d.talentGrade) {
            1 -> "Mortal Vein"
            2 -> "Spiritual Meridian"
            3 -> "Earth Spirit Root"
            else -> "Heavenly Dao Body"
        }
        d.updateEfficiency()
        return d
    }

    fun showNotification(msg: String) {
        _uiState.update { it.copy(activeNotification = msg) }
    }

    fun dismissNotification() {
        _uiState.update { it.copy(activeNotification = null) }
    }

    private fun playClickSfx() {
        try {
            AudioManager.getInstance(getApplication()).playSfx(AudioManager.SFX_CLICK)
            hapticManager.tap()
        } catch (ignored: Exception) {}
    }

    private fun playSfx(sfxKey: String) {
        try {
            AudioManager.getInstance(getApplication()).playSfx(sfxKey)
            when (sfxKey) {
                AudioManager.SFX_BREAKTHROUGH -> hapticManager.breakthroughSuccess()
                AudioManager.SFX_UPGRADE -> hapticManager.impact()
                AudioManager.SFX_ASSIGN_TASK -> hapticManager.tap()
                AudioManager.SFX_SPIRIT_BURST -> hapticManager.impact()
                else -> hapticManager.tap()
            }
        } catch (ignored: Exception) {}
    }

    private fun saveState() {
        try {
            saveManager.save()
            viewModelScope.launch {
                val data = SectData.getInstance()
                if (data != null) {
                    repository.saveCurrentGameState(data)
                    repository.saveMapRegions(_uiState.value.worldMap.regions)
                }
            }
        } catch (ignored: Exception) {}
    }

    // =========================================================================
    // Milestone & Achievement System
    // =========================================================================

    fun evaluateMilestones() {
        val data = SectData.getInstance()
        val discipleList = data?.disciples ?: emptyList<Disciple>()
        val maxRealm = discipleList.maxOfOrNull { it.realm } ?: 0
        val currentStones = data?.spiritStones ?: _uiState.value.spiritStones
        val currentPills = data?.spiritPills ?: _uiState.value.spiritPills
        val unlockedCount = _uiState.value.worldMap.regions.count { it.isUnlocked }

        val evaluated = milestoneSystem.evaluateMilestones(
            discipleCount = discipleList.size,
            maxDiscipleRealm = maxRealm,
            currentSpiritStones = currentStones,
            currentSpiritPills = currentPills,
            unlockedRegionsCount = unlockedCount
        )

        _uiState.update { it.copy(milestones = evaluated) }
    }

    fun claimMilestone(milestoneId: String) {
        val target = _uiState.value.milestones.find { it.id == milestoneId } ?: return
        if (!target.isCompleted || target.isClaimed) return

        milestoneSystem.markClaimed(milestoneId)
        val data = SectData.getInstance()

        if (target.rewardStones > 0) {
            data?.let { it.spiritStones += target.rewardStones }
        }
        if (target.rewardJade > 0) {
            data?.let { it.jade += target.rewardJade.toInt() }
        }
        if (target.rewardPills > 0) {
            data?.let { it.spiritPills += target.rewardPills }
        }

        evaluateMilestones()
        syncFromSectData()
        playSfx(AudioManager.SFX_BREAKTHROUGH)
        showNotification("🏆 Milestone Claimed: ${target.title}! Rewards added to treasury.")
        saveState()
    }

    fun claimAllMilestones() {
        val claimables = _uiState.value.milestones.filter { it.isCompleted && !it.isClaimed }
        if (claimables.isEmpty()) return

        var totalStones = 0L
        var totalJade = 0L
        var totalPills = 0L

        val data = SectData.getInstance()
        claimables.forEach { m ->
            milestoneSystem.markClaimed(m.id)
            totalStones += m.rewardStones
            totalJade += m.rewardJade
            totalPills += m.rewardPills
        }

        data?.let {
            it.spiritStones += totalStones
            it.jade += totalJade.toInt()
            it.spiritPills += totalPills
        }

        evaluateMilestones()
        syncFromSectData()
        playSfx(AudioManager.SFX_BREAKTHROUGH)
        showNotification("🏆 Claimed ${claimables.size} Milestones! (+${totalStones} Stones, +${totalJade} Jade, +${totalPills} Pills)")
        saveState()
    }

    // =========================================================================
    // Random Encounter & Sect Events
    // =========================================================================

    fun triggerRandomEncounter() {
        val encounter = com.sect.idle.gameplay.SectEncounterSystem.generateRandomEncounter(_uiState.value.sectRealm)
        _uiState.update { it.copy(activeEncounter = encounter) }
        playSfx(AudioManager.SFX_SPIRIT_BURST)
        showNotification("⚡ New Sect Encounter: ${encounter.title} (${encounter.chineseTitle})!")
    }

    fun resolveEncounter(choiceId: String) {
        val encounter = _uiState.value.activeEncounter ?: return
        val result = com.sect.idle.gameplay.SectEncounterSystem.resolveChoice(encounter, choiceId)

        val data = SectData.getInstance()
        data?.let { d ->
            d.spiritStones = (d.spiritStones + result.stonesDelta).coerceAtLeast(0L)
            d.spiritHerbs = (d.spiritHerbs + result.herbsDelta).coerceAtLeast(0L)
            d.spiritOres = (d.spiritOres + result.oresDelta).coerceAtLeast(0L)
            d.spiritPills = (d.spiritPills + result.pillsDelta).coerceAtLeast(0L)
            d.jade = (d.jade + result.jadeDelta.toInt()).coerceAtLeast(0)
            d.essence = (d.essence + result.essenceDelta.toInt()).coerceAtLeast(0)

            if (result.discipleHpDamagePercent > 0) {
                d.disciples.forEach { disc ->
                    val dmg = (disc.maxHp * (result.discipleHpDamagePercent / 100f)).toInt()
                    disc.hp = (disc.hp - dmg).coerceAtLeast(1)
                }
            }

            if (result.recruitNewDisciple) {
                val newDisciple = com.sect.idle.gameplay.DiscipleManager.getInstance()
                    .createRandomDisciple(30, 65, 0, 1)
                d.disciples.add(newDisciple)
            }
        }

        _uiState.update { it.copy(activeEncounter = null) }
        evaluateMilestones()
        syncFromSectData()
        playSfx(AudioManager.SFX_UPGRADE)
        showNotification("${result.title}: ${result.message}")
        saveState()
    }

    fun dismissEncounter() {
        _uiState.update { it.copy(activeEncounter = null) }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
