package com.sect.idle.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sect.idle.data.db.entities.DiscipleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DiscipleDao - Data Access Object for Disciples and their cultivation attributes.
 */
@Dao
interface DiscipleDao {

    @Query("SELECT * FROM disciples ORDER BY realm DESC, atk DESC")
    fun getAllDisciplesFlow(): Flow<List<DiscipleEntity>>

    @Query("SELECT * FROM disciples ORDER BY realm DESC, atk DESC")
    suspend fun getAllDisciples(): List<DiscipleEntity>

    @Query("SELECT * FROM disciples WHERE id = :id LIMIT 1")
    suspend fun getDiscipleById(id: String): DiscipleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisciples(disciples: List<DiscipleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisciple(disciple: DiscipleEntity)

    @Query("DELETE FROM disciples WHERE id = :id")
    suspend fun deleteDiscipleById(id: String)

    @Query("DELETE FROM disciples")
    suspend fun deleteAllDisciples()
}
