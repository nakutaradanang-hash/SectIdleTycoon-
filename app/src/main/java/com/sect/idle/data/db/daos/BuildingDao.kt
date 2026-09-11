package com.sect.idle.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sect.idle.data.db.entities.BuildingEntity
import kotlinx.coroutines.flow.Flow

/**
 * BuildingDao - Data Access Object for Sect buildings and facilities.
 */
@Dao
interface BuildingDao {

    @Query("SELECT * FROM buildings")
    fun getAllBuildingsFlow(): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM buildings")
    suspend fun getAllBuildings(): List<BuildingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildings(buildings: List<BuildingEntity>)

    @Query("DELETE FROM buildings")
    suspend fun deleteAll()
}
