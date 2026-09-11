package com.sect.idle.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sect.idle.data.db.entities.MapProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * MapProgressDao - Data Access Object for World Map region statuses and expeditions.
 */
@Dao
interface MapProgressDao {

    @Query("SELECT * FROM map_progress")
    fun getAllMapProgressFlow(): Flow<List<MapProgressEntity>>

    @Query("SELECT * FROM map_progress")
    suspend fun getAllMapProgress(): List<MapProgressEntity>

    @Query("SELECT * FROM map_progress WHERE regionId = :regionId LIMIT 1")
    suspend fun getRegionProgress(regionId: String): MapProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRegion(progress: MapProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRegions(list: List<MapProgressEntity>)

    @Query("DELETE FROM map_progress")
    suspend fun deleteAll()
}
