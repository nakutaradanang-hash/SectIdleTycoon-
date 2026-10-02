package com.sect.idle.gameplay

import android.content.Context
import android.content.SharedPreferences

enum class MilestoneCategory(val label: String, val iconEmoji: String) {
    ALL("All", "🌟"),
    DISCIPLES("Disciples", "👥"),
    CULTIVATION("Cultivation", "🧘"),
    TREASURY("Treasury", "🪙"),
    EXPEDITIONS("Expeditions", "🗺️")
}

data class AchievementMilestone(
    val id: String,
    val title: String,
    val description: String,
    val category: MilestoneCategory,
    val targetValue: Long,
    val currentValue: Long = 0L,
    val isClaimed: Boolean = false,
    val rewardStones: Long = 0L,
    val rewardJade: Long = 0L,
    val rewardPills: Long = 0L,
    val rewardItemName: String? = null,
    val iconEmoji: String = "🏆"
) {
    val isCompleted: Boolean
        get() = currentValue >= targetValue

    val progress: Float
        get() = if (targetValue <= 0) 1f else (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
}

class MilestoneSystem(private val context: Context?) {

    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("IdleSect_Milestones", Context.MODE_PRIVATE)

    companion object {
        val ALL_MILESTONES = listOf(
            // Disciples Milestones
            AchievementMilestone(
                id = "disciple_count_3",
                title = "Modest Gathering",
                description = "Recruit at least 3 disciples into your sect.",
                category = MilestoneCategory.DISCIPLES,
                targetValue = 3L,
                rewardStones = 500L,
                rewardJade = 10L,
                rewardPills = 5L,
                iconEmoji = "👥"
            ),
            AchievementMilestone(
                id = "disciple_count_6",
                title = "Flourishing Sect",
                description = "Grow your sect to 6 active disciples.",
                category = MilestoneCategory.DISCIPLES,
                targetValue = 6L,
                rewardStones = 1500L,
                rewardJade = 25L,
                rewardPills = 10L,
                rewardItemName = "Immortal Recruitment Banner",
                iconEmoji = "🏮"
            ),
            AchievementMilestone(
                id = "disciple_count_12",
                title = "Grand Congregation",
                description = "Reach 12 disciples studying the Dao under your lineage.",
                category = MilestoneCategory.DISCIPLES,
                targetValue = 12L,
                rewardStones = 5000L,
                rewardJade = 80L,
                rewardPills = 25L,
                rewardItemName = "Celestial Spirit Whistle",
                iconEmoji = "🏛️"
            ),

            // Cultivation Realm Milestones
            AchievementMilestone(
                id = "realm_foundation_1",
                title = "Foundation Established",
                description = "Have at least 1 disciple break through to Foundation Establishment realm.",
                category = MilestoneCategory.CULTIVATION,
                targetValue = 1L,
                rewardStones = 1000L,
                rewardJade = 20L,
                rewardPills = 8L,
                rewardItemName = "Foundation Pill Formula",
                iconEmoji = "🌱"
            ),
            AchievementMilestone(
                id = "realm_core_formation_1",
                title = "Golden Core Luminescence",
                description = "Advance a disciple to Core Formation realm.",
                category = MilestoneCategory.CULTIVATION,
                targetValue = 2L,
                rewardStones = 3000L,
                rewardJade = 50L,
                rewardPills = 15L,
                rewardItemName = "Golden Core Nectar",
                iconEmoji = "✨"
            ),
            AchievementMilestone(
                id = "realm_nascent_soul_1",
                title = "Nascent Soul Rebirth",
                description = "Cultivate a disciple to the transcendent Nascent Soul realm.",
                category = MilestoneCategory.CULTIVATION,
                targetValue = 3L,
                rewardStones = 10000L,
                rewardJade = 150L,
                rewardPills = 30L,
                rewardItemName = "Heavenly Tribulation Shield",
                iconEmoji = "🌌"
            ),

            // Treasury Milestones
            AchievementMilestone(
                id = "treasury_stones_5k",
                title = "First Pot of Spirit Gold",
                description = "Accumulate 5,000 Spirit Stones in the treasury.",
                category = MilestoneCategory.TREASURY,
                targetValue = 5000L,
                rewardStones = 1000L,
                rewardJade = 15L,
                rewardPills = 5L,
                iconEmoji = "🪙"
            ),
            AchievementMilestone(
                id = "treasury_stones_25k",
                title = "Heavenly Treasury Abundance",
                description = "Accumulate 25,000 Spirit Stones in the treasury.",
                category = MilestoneCategory.TREASURY,
                targetValue = 25000L,
                rewardStones = 5000L,
                rewardJade = 50L,
                rewardPills = 15L,
                rewardItemName = "Dragon Treasure Bag",
                iconEmoji = "💰"
            ),
            AchievementMilestone(
                id = "treasury_pills_50",
                title = "Apothecary Hoard",
                description = "Store at least 50 Spirit Pills in sect reserve.",
                category = MilestoneCategory.TREASURY,
                targetValue = 50L,
                rewardStones = 2000L,
                rewardJade = 30L,
                rewardPills = 20L,
                rewardItemName = "Nine Revolutions Cauldron",
                iconEmoji = "💊"
            ),

            // Expeditions & Exploration
            AchievementMilestone(
                id = "world_regions_unlocked_3",
                title = "Territory Pioneer",
                description = "Unlock and map out at least 3 foreign territory regions.",
                category = MilestoneCategory.EXPEDITIONS,
                targetValue = 3L,
                rewardStones = 2000L,
                rewardJade = 35L,
                rewardPills = 10L,
                iconEmoji = "🗺️"
            ),
            AchievementMilestone(
                id = "world_regions_unlocked_5",
                title = "Mortal Realm Hegemon",
                description = "Conquer and survey 5 grand territory regions across the world.",
                category = MilestoneCategory.EXPEDITIONS,
                targetValue = 5L,
                rewardStones = 8000L,
                rewardJade = 120L,
                rewardPills = 25L,
                rewardItemName = "All-Seeing Celestial Compass",
                iconEmoji = "🧭"
            )
        )
    }

    /**
     * Evaluates current progress for all milestones against sect state.
     */
    fun evaluateMilestones(
        discipleCount: Int,
        maxDiscipleRealm: Int,
        currentSpiritStones: Long,
        currentSpiritPills: Long,
        unlockedRegionsCount: Int
    ): List<AchievementMilestone> {
        val claimedIds = getClaimedMilestoneIds()

        return ALL_MILESTONES.map { milestone ->
            val currentValue = when (milestone.id) {
                "disciple_count_3", "disciple_count_6", "disciple_count_12" -> discipleCount.toLong()
                "realm_foundation_1", "realm_core_formation_1", "realm_nascent_soul_1" -> maxDiscipleRealm.toLong()
                "treasury_stones_5k", "treasury_stones_25k" -> currentSpiritStones
                "treasury_pills_50" -> currentSpiritPills
                "world_regions_unlocked_3", "world_regions_unlocked_5" -> unlockedRegionsCount.toLong()
                else -> 0L
            }

            milestone.copy(
                currentValue = currentValue,
                isClaimed = claimedIds.contains(milestone.id)
            )
        }
    }

    fun isMilestoneClaimed(id: String): Boolean {
        return getClaimedMilestoneIds().contains(id)
    }

    fun markClaimed(id: String) {
        val claimed = getClaimedMilestoneIds().toMutableSet()
        claimed.add(id)
        prefs?.edit()?.putStringSet("claimed_ids", claimed)?.apply()
    }

    private fun getClaimedMilestoneIds(): Set<String> {
        return prefs?.getStringSet("claimed_ids", emptySet()) ?: emptySet()
    }
}
