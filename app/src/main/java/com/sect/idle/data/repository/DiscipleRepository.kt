package com.sect.idle.data.repository

import android.content.Context
import com.sect.idle.data.db.SectDatabase
import com.sect.idle.data.db.daos.DiscipleDao
import com.sect.idle.data.db.entities.DiscipleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * DiscipleRepository - Dedicated Repository pattern implementation for managing Disciples in Room Database.
 * Provides reactive Flow observers and suspend functions for CRUD operations, cultivation advancement, and energy updates.
 */
class DiscipleRepository(private val discipleDao: DiscipleDao) {

    val allDisciplesFlow: Flow<List<DiscipleEntity>> = discipleDao.getAllDisciplesFlow()

    suspend fun getAllDisciples(): List<DiscipleEntity> = withContext(Dispatchers.IO) {
        discipleDao.getAllDisciples()
    }

    suspend fun getDiscipleById(id: String): DiscipleEntity? = withContext(Dispatchers.IO) {
        discipleDao.getDiscipleById(id)
    }

    suspend fun insertDisciple(disciple: DiscipleEntity) = withContext(Dispatchers.IO) {
        discipleDao.insertDisciple(disciple)
    }

    suspend fun insertDisciples(disciples: List<DiscipleEntity>) = withContext(Dispatchers.IO) {
        discipleDao.insertDisciples(disciples)
    }

    suspend fun deleteDiscipleById(id: String) = withContext(Dispatchers.IO) {
        discipleDao.deleteDiscipleById(id)
    }

    suspend fun deleteAllDisciples() = withContext(Dispatchers.IO) {
        discipleDao.deleteAllDisciples()
    }

    companion object {
        @Volatile
        private var instance: DiscipleRepository? = null

        @JvmStatic
        fun get(context: Context): DiscipleRepository {
            return instance ?: synchronized(this) {
                instance ?: DiscipleRepository(SectDatabase.getInstance(context).discipleDao()).also { instance = it }
            }
        }
    }
}
