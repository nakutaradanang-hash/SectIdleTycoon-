package com.sect.idle.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SectEntity - Persists the top-level Sect state and resources.
 */
@Entity(tableName = "sect_state")
data class SectEntity(
    @PrimaryKey
    val id: Int = 1,
    val sectName: String = "Cloud Mist Sect",
    val spiritStones: Long = 1000L,
    val spiritHerbs: Long = 100L,
    val spiritPills: Long = 10L,
    val spiritOres: Long = 50L,
    val jade: Long = 50L,
    val essence: Long = 20L,
    val sectQi: Long = 100L,
    val sectPower: Long = 1000L,
    val sectExp: Long = 0L,
    val sectRealm: Int = 0,
    val sectRealmExp: Int = 0,
    val sectRank: Int = 1,
    val maxDisciples: Int = 10,
    val mapProgression: Int = 1,
    val lastSaveTimestamp: Long = System.currentTimeMillis()
)
