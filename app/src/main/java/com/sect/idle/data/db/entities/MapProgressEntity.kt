package com.sect.idle.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MapProgressEntity - Persists world map region unlock statuses, expeditions, and loot states.
 */
@Entity(tableName = "map_progress")
data class MapProgressEntity(
    @PrimaryKey
    val regionId: String,
    val isUnlocked: Boolean = false,
    val isExpeditionActive: Boolean = false,
    val expeditionTimeRemainingSec: Int = 0,
    val expeditionTotalTimeSec: Int = 60,
    val hasLootToClaim: Boolean = false,
    val lootSummary: String = "",
    val assignedDiscipleIds: String = "" // Comma-separated disciple IDs
)
