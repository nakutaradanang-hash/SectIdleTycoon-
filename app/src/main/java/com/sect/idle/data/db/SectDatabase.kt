package com.sect.idle.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sect.idle.data.db.daos.BuildingDao
import com.sect.idle.data.db.daos.DiscipleDao
import com.sect.idle.data.db.daos.DiscipleLifecycleDao
import com.sect.idle.data.db.daos.MapProgressDao
import com.sect.idle.data.db.daos.SectDao
import com.sect.idle.data.db.entities.BuildingEntity
import com.sect.idle.data.db.entities.DiscipleEntity
import com.sect.idle.data.db.entities.DiscipleLifecycleEntity
import com.sect.idle.data.db.entities.MapProgressEntity
import com.sect.idle.data.db.entities.SectEntity

/**
 * SectDatabase - Central Room Database for persisting the Sect's state,
 * current resources, unlocked disciples, facilities, and map progression.
 */
@Database(
    entities = [
        SectEntity::class,
        DiscipleEntity::class,
        DiscipleLifecycleEntity::class,
        MapProgressEntity::class,
        BuildingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SectDatabase : RoomDatabase() {

    abstract fun sectDao(): SectDao
    abstract fun discipleDao(): DiscipleDao
    abstract fun discipleLifecycleDao(): DiscipleLifecycleDao
    abstract fun mapProgressDao(): MapProgressDao
    abstract fun buildingDao(): BuildingDao

    companion object {
        private const val DB_NAME = "sect_idle_cultivation.db"

        @Volatile
        private var INSTANCE: SectDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): SectDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SectDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
