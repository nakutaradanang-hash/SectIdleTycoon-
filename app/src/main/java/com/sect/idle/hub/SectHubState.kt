package com.sect.idle.hub

import com.sect.idle.core.GameConfig
import com.sect.idle.models.Disciple

enum class SectHubTab(val title: String, val iconLabel: String) {
    SECT_REALM("3D Realm", "🌌"),
    OVERVIEW("Sect Hub", "🏛️"),
    DISCIPLES("Disciple List", "👥"),
    CULTIVATION("Cultivation Chamber", "🧘"),
    WORLD_MAP("World Map", "🗺️")
}

data class DiscipleUiModel(
    val id: String,
    val name: String,
    val title: String,
    val isMale: Boolean,
    val age: Int,
    val realm: Int,
    val realmName: String,
    val realmExp: Int,
    val realmMaxExp: Int,
    val element: Int,
    val elementName: String,
    val elementColor: Long,
    val talentName: String,
    val talentGrade: Int,
    val currentTask: Int,
    val taskName: String,
    val taskEfficiency: Int,
    val combatPower: Int,
    val hp: Int,
    val maxHp: Int,
    val mp: Int,
    val maxMp: Int,
    val energy: Int,
    val mood: Int,
    val loyalty: Int,
    val dailyWage: Int,
    val alchemySkill: Int,
    val bodyRefiningStage: Int,
    val str: Int,
    val agi: Int,
    val intel: Int,
    val lck: Int,
    val vit: Int,
    val wis: Int,
    val cha: Int,
    val isReadyForBreakthrough: Boolean
)

data class MeditationSlot(
    val slotIndex: Int,
    val isUnlocked: Boolean = false,
    val unlockCost: Long = 0L,
    val assignedDiscipleId: String? = null,
    val assignedDiscipleName: String? = null,
    val assignedDiscipleRealm: String? = null,
    val qiGainMultiplier: Float = 1.0f,
    val meditationProgress: Float = 0f
)

data class CultivationChamberState(
    val arrayLevel: Int = 1,
    val arrayUpgradeCost: Long = 800L,
    val qiDensityBonusPercent: Int = 20,
    val ambientQiGatherRate: Float = 5.0f,
    val meditationSlots: List<MeditationSlot> = emptyList(),
    val breakthroughDiscipleId: String? = null,
    val baseBreakthroughChance: Int = 65,
    val pillBonusChance: Int = 20,
    val pillCount: Long = 10L,
    val pillBoostActive: Boolean = false,
    val isBreakthroughInProgress: Boolean = false,
    val lastBreakthroughLog: String = "No recent breakthrough attempt."
)

data class MapRegion(
    val id: String,
    val name: String,
    val chineseName: String,
    val description: String,
    val dangerLevel: Int, // 1 to 5 stars
    val minSectRealmRequired: Int,
    val recommendedPower: Int = 100,
    val primaryResourceName: String = "Spirit Stones",
    val resourceYieldDescription: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val isExpeditionActive: Boolean = false,
    val expeditionTimeRemainingSec: Int = 0,
    val expeditionTotalTimeSec: Int = 60,
    val assignedDiscipleNames: List<String> = emptyList(),
    val hasLootToClaim: Boolean = false,
    val lootSummary: String = ""
)

data class WorldMapState(
    val regions: List<MapRegion> = emptyList(),
    val selectedRegionId: String? = null,
    val activeExpeditionCount: Int = 0,
    val maxExpeditions: Int = 3,
    val recentExplorationLogs: List<String> = emptyList()
)

data class SectEconomyState(
    val dailyIncomeSS: Long = 0L,
    val dailyExpenseSS: Long = 0L,
    val netDailySS: Long = 0L,
    val activeFarmers: Int = 0,
    val activeAlchemists: Int = 0,
    val activeMiners: Int = 0,
    val activeGuards: Int = 0,
    val activeCultivators: Int = 0
)

data class SectHubState(
    val sectName: String = "Cloud Mist Sect",
    val sectRealm: Int = 0,
    val sectRealmName: String = "Qi Condensation",
    val sectRealmExp: Int = 0,
    val sectRealmMaxExp: Int = 100,
    val sectRank: Int = 1,
    val sectPower: Long = 1000L,
    val spiritStones: Long = 1000L,
    val spiritHerbs: Long = 100L,
    val spiritPills: Long = 10L,
    val spiritOres: Long = 50L,
    val jade: Long = 50L,
    val essence: Long = 20L,
    val currentTab: SectHubTab = SectHubTab.OVERVIEW,
    val disciples: List<DiscipleUiModel> = emptyList(),
    val selectedDiscipleId: String? = null,
    val searchQuery: String = "",
    val taskFilter: Int? = null, // null = All
    val cultivationChamber: CultivationChamberState = CultivationChamberState(),
    val worldMap: WorldMapState = WorldMapState(),
    val economy: SectEconomyState = SectEconomyState(),
    val activeNotification: String? = null,
    val isRecruiting: Boolean = false
)
