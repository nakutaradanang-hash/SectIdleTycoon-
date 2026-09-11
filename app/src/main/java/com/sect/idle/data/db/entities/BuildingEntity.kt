package com.sect.idle.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * BuildingEntity - Persists sect buildings and facilities.
 */
@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey
    val type: Int,
    val name: String,
    val level: Int = 1,
    val isBuilt: Boolean = true,
    val posX: Int = 0,
    val posY: Int = 0,
    val workerCount: Int = 0
)
