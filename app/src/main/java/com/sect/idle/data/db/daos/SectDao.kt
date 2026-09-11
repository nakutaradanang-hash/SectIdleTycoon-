package com.sect.idle.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sect.idle.data.db.entities.SectEntity
import kotlinx.coroutines.flow.Flow

/**
 * SectDao - Data Access Object for Sect state and resource balances.
 */
@Dao
interface SectDao {

    @Query("SELECT * FROM sect_state WHERE id = 1 LIMIT 1")
    fun getSectFlow(): Flow<SectEntity?>

    @Query("SELECT * FROM sect_state WHERE id = 1 LIMIT 1")
    suspend fun getSect(): SectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSect(sect: SectEntity)

    @Query("UPDATE sect_state SET spiritStones = :stones, spiritHerbs = :herbs, spiritOres = :ores, spiritPills = :pills, sectQi = :qi, lastSaveTimestamp = :timestamp WHERE id = 1")
    suspend fun updateResources(stones: Long, herbs: Long, ores: Long, pills: Long, qi: Long, timestamp: Long)

    @Query("DELETE FROM sect_state")
    suspend fun deleteAll()
}
