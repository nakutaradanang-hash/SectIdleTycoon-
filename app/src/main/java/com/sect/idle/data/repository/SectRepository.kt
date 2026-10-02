package com.sect.idle.data.repository

import android.content.Context
import com.sect.idle.data.db.SectDatabase
import com.sect.idle.data.db.entities.BuildingEntity
import com.sect.idle.data.db.entities.DiscipleEntity
import com.sect.idle.data.db.entities.MapProgressEntity
import com.sect.idle.data.db.entities.SectEntity
import com.sect.idle.gameplay.SectData
import com.sect.idle.hub.MapRegion
import com.sect.idle.models.Building
import com.sect.idle.models.Disciple
import com.sect.idle.models.Equipment
import com.sect.idle.models.Talent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * SectRepository - Clean repository layer abstracting Room persistence operations.
 * Handles bidirectional state mapping between the runtime SectData model and SQLite Room tables.
 */
class SectRepository(private val database: SectDatabase) {

    val sectFlow: Flow<SectEntity?> = database.sectDao().getSectFlow()
    val disciplesFlow: Flow<List<DiscipleEntity>> = database.discipleDao().getAllDisciplesFlow()
    val mapProgressFlow: Flow<List<MapProgressEntity>> = database.mapProgressDao().getAllMapProgressFlow()
    val buildingsFlow: Flow<List<BuildingEntity>> = database.buildingDao().getAllBuildingsFlow()

    companion object {
        @Volatile
        private var instance: SectRepository? = null

        @JvmStatic
        fun get(context: Context): SectRepository {
            return instance ?: synchronized(this) {
                instance ?: SectRepository(SectDatabase.getInstance(context)).also { instance = it }
            }
        }
    }

    /**
     * Persists the entire game state into Room Database asynchronously.
     */
    suspend fun saveCurrentGameState(data: SectData) = withContext(Dispatchers.IO) {
        val sectEntity = SectEntity(
            id = 1,
            sectName = data.sectName ?: "Cloud Mist Sect",
            spiritStones = data.spiritStones,
            spiritHerbs = data.spiritHerbs,
            spiritPills = data.spiritPills,
            spiritOres = data.spiritOres,
            jade = data.jade,
            essence = data.essence,
            sectQi = data.sectPower, // Or sectQi if tracked
            sectPower = data.sectPower,
            sectExp = data.sectExp,
            sectRealm = data.sectRealm,
            sectRealmExp = data.sectRealmExp,
            sectRank = data.sectRank,
            maxDisciples = data.maxDisciples,
            lastSaveTimestamp = System.currentTimeMillis()
        )
        database.sectDao().insertOrUpdateSect(sectEntity)

        // Save Disciples
        val discipleEntities = ArrayList<DiscipleEntity>()
        data.disciples?.let { list ->
            for (i in 0 until list.size) {
                val d = list.getOrNull(i) ?: continue
                discipleEntities.add(
                    DiscipleEntity(
                        id = d.id ?: "disc_${i}_${d.name}",
                        name = d.name ?: "Unknown Cultivator",
                        cultivationLevel = d.realm,
                        spiritEnergy = d.energy,
                        maxSpiritEnergy = d.maxEnergy,
                        title = d.title ?: "",
                        isMale = d.isMale,
                        age = d.age,
                        lifespan = d.lifespan,
                        realm = d.realm,
                        realmExp = d.realmExp,
                        element = d.element,
                        talentName = d.talent?.name ?: "Common Roots",
                        talentGrade = d.talent?.grade ?: 1,
                        currentTask = d.currentTask,
                        atk = d.atk,
                        def = d.def,
                        spd = d.spd,
                        crit = d.critRate,
                        maxHp = d.maxHp,
                        hp = d.hp,
                        maxMp = d.maxMp,
                        mp = d.mp,
                        energy = d.energy,
                        maxEnergy = d.maxEnergy,
                        mood = d.mood,
                        stress = d.stress,
                        loyalty = d.loyalty,
                        dailyWage = d.dailyWage,
                        alchemySkill = d.alchemySkill,
                        bodyRefiningStage = d.bodyRefiningStage,
                        str = d.str,
                        agi = d.agi,
                        intel = d.intel,
                        lck = d.lck,
                        vit = d.vit,
                        wis = d.wis,
                        cha = d.cha,
                        equippedWeaponName = d.weapon?.name ?: "",
                        equippedArmorName = d.armor?.name ?: "",
                        equippedRelicName = d.accessory?.name ?: ""
                    )
                )
            }
        }
        if (discipleEntities.isNotEmpty()) {
            database.discipleDao().insertDisciples(discipleEntities)
        }

        // Save Buildings
        val buildingEntities = ArrayList<BuildingEntity>()
        data.buildings?.let { list ->
            for (i in 0 until list.size) {
                val b = list.getOrNull(i) ?: continue
                buildingEntities.add(
                    BuildingEntity(
                        type = b.type,
                        name = b.name ?: "Facility",
                        level = b.level,
                        isBuilt = b.isBuilt,
                        posX = b.posX,
                        posY = b.posY,
                        workerCount = b.workers
                    )
                )
            }
        }
        if (buildingEntities.isNotEmpty()) {
            database.buildingDao().insertBuildings(buildingEntities)
        }
    }

    /**
     * Loads saved state from Room into the SectData singleton.
     * Returns true if saved data was successfully restored.
     */
    suspend fun loadGameStateIntoSectData(data: SectData): Boolean = withContext(Dispatchers.IO) {
        val sectEntity = database.sectDao().getSect() ?: return@withContext false

        data.sectName = sectEntity.sectName
        data.spiritStones = sectEntity.spiritStones
        data.spiritHerbs = sectEntity.spiritHerbs
        data.spiritPills = sectEntity.spiritPills
        data.spiritOres = sectEntity.spiritOres
        data.jade = sectEntity.jade
        data.essence = sectEntity.essence
        data.sectPower = sectEntity.sectPower
        data.sectExp = sectEntity.sectExp
        data.sectRealm = sectEntity.sectRealm
        data.sectRealmExp = sectEntity.sectRealmExp
        data.sectRank = sectEntity.sectRank
        data.maxDisciples = sectEntity.maxDisciples

        // Restore disciples
        val discipleEntities = database.discipleDao().getAllDisciples()
        if (discipleEntities.isNotEmpty()) {
            data.disciples.clear()
            for (entity in discipleEntities) {
                val d = Disciple(entity.name)
                d.id = entity.id
                d.title = entity.title
                d.isMale = entity.isMale
                d.age = entity.age
                d.lifespan = entity.lifespan
                d.realm = entity.realm
                d.realmExp = entity.realmExp
                d.element = entity.element
                d.talent = Talent().apply {
                    id = "talent_${entity.id}"
                    name = entity.talentName
                    grade = entity.talentGrade
                }
                d.currentTask = entity.currentTask
                d.alchemySkill = entity.alchemySkill
                d.bodyRefiningStage = entity.bodyRefiningStage
                d.initStats(entity.str, entity.agi, entity.intel, entity.lck, entity.vit, entity.wis, entity.cha)
                d.atk = entity.atk
                d.def = entity.def
                d.spd = entity.spd
                d.critRate = entity.crit
                d.maxHp = entity.maxHp
                d.hp = entity.hp
                d.maxMp = entity.maxMp
                d.mp = entity.mp
                d.energy = entity.energy
                d.maxEnergy = entity.maxEnergy
                d.mood = entity.mood
                d.stress = entity.stress
                d.loyalty = entity.loyalty
                d.dailyWage = entity.dailyWage

                if (entity.equippedWeaponName.isNotEmpty()) {
                    d.weapon = Equipment("wpn_${entity.id}", entity.equippedWeaponName, 0, 1)
                }
                if (entity.equippedArmorName.isNotEmpty()) {
                    d.armor = Equipment("arm_${entity.id}", entity.equippedArmorName, 1, 1)
                }

                data.disciples.add(d)
            }
        }

        // Restore buildings
        val buildingEntities = database.buildingDao().getAllBuildings()
        if (buildingEntities.isNotEmpty()) {
            data.buildings.clear()
            for (bEntity in buildingEntities) {
                val b = Building(bEntity.type, bEntity.name, 10, 200L, 5)
                b.level = bEntity.level
                b.isBuilt = bEntity.isBuilt
                b.posX = bEntity.posX
                b.posY = bEntity.posY
                b.workers = bEntity.workerCount
                data.buildings.add(b)
            }
        }

        data.recalculateEconomy()
        true
    }

    /**
     * Persists map progression state.
     */
    suspend fun saveMapRegions(regions: List<MapRegion>) = withContext(Dispatchers.IO) {
        val entities = regions.map { r ->
            MapProgressEntity(
                regionId = r.id,
                isUnlocked = r.isUnlocked,
                isExpeditionActive = r.isExpeditionActive,
                expeditionTimeRemainingSec = r.expeditionTimeRemainingSec,
                expeditionTotalTimeSec = r.expeditionTotalTimeSec,
                hasLootToClaim = r.hasLootToClaim,
                lootSummary = r.lootSummary,
                assignedDiscipleIds = r.assignedDiscipleNames.joinToString(",")
            )
        }
        database.mapProgressDao().insertAllRegions(entities)
    }

    /**
     * Incremental resource updates without full serializations.
     */
    suspend fun updateResources(stones: Long, herbs: Long, ores: Long, pills: Long, qi: Long) = withContext(Dispatchers.IO) {
        database.sectDao().updateResources(stones, herbs, ores, pills, qi, System.currentTimeMillis())
    }
}
