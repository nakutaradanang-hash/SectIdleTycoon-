package com.sect.idle.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sect.idle.core.GameConfig
import com.sect.idle.data.db.SectDatabase
import com.sect.idle.data.db.entities.BuildingEntity
import com.sect.idle.data.db.entities.DiscipleEntity
import com.sect.idle.data.db.entities.MapProgressEntity
import com.sect.idle.data.db.entities.SectEntity
import com.sect.idle.data.repository.SectRepository
import com.sect.idle.gameplay.SectData
import com.sect.idle.hub.MapRegion
import com.sect.idle.models.Building
import com.sect.idle.models.Disciple
import com.sect.idle.models.Equipment
import com.sect.idle.models.Talent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * RoomDatabaseAndRepositoryIntegrationTest - Validates local Room database integration,
 * DAOs, entity mapping, full save/restore workflows, and edge case error resilience.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomDatabaseAndRepositoryIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: SectDatabase
    private lateinit var repository: SectRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, SectDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SectRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSectDaoCrudOperations() = runBlocking {
        val sectDao = database.sectDao()
        assertNull(sectDao.getSect())

        val sect = SectEntity(
            id = 1,
            sectName = "Ethereal Cloud Sect",
            spiritStones = 50000L,
            spiritHerbs = 3000L,
            spiritOres = 2000L,
            spiritPills = 150L,
            jade = 80L,
            essence = 40L,
            sectQi = 1200L,
            sectPower = 8500L,
            sectExp = 25000L,
            sectRealm = 3,
            sectRealmExp = 450,
            sectRank = 2,
            maxDisciples = 20,
            mapProgression = 4
        )

        sectDao.insertOrUpdateSect(sect)

        val retrieved = sectDao.getSect()
        assertNotNull(retrieved)
        assertEquals("Ethereal Cloud Sect", retrieved?.sectName)
        assertEquals(50000L, retrieved?.spiritStones)
        assertEquals(3, retrieved?.sectRealm)

        // Update resources incrementally
        sectDao.updateResources(
            stones = 60000L,
            herbs = 3500L,
            ores = 2200L,
            pills = 180L,
            qi = 1500L,
            timestamp = System.currentTimeMillis()
        )

        val updated = sectDao.getSect()
        assertNotNull(updated)
        assertEquals(60000L, updated?.spiritStones)
        assertEquals(3500L, updated?.spiritHerbs)
        assertEquals(1500L, updated?.sectQi)
        // Ensure other fields were preserved
        assertEquals("Ethereal Cloud Sect", updated?.sectName)
        assertEquals(3, updated?.sectRealm)

        // Flow verification
        val flowSect = sectDao.getSectFlow().first()
        assertEquals(60000L, flowSect?.spiritStones)

        // Delete all
        sectDao.deleteAll()
        assertNull(sectDao.getSect())
    }

    @Test
    fun testDiscipleDaoCrudAndSorting() = runBlocking {
        val discipleDao = database.discipleDao()
        assertTrue(discipleDao.getAllDisciples().isEmpty())

        val disciple1 = DiscipleEntity(
            id = "d_001",
            name = "Elder Lin",
            realm = 4,
            atk = 250,
            def = 180,
            spd = 90,
            crit = 15,
            talentName = "Heavenly Fire Root",
            talentGrade = 5
        )

        val disciple2 = DiscipleEntity(
            id = "d_002",
            name = "Novice Xiao",
            realm = 1,
            atk = 30,
            def = 20,
            spd = 15,
            crit = 5,
            talentName = "Mortal Root",
            talentGrade = 1
        )

        val disciple3 = DiscipleEntity(
            id = "d_003",
            name = "Core Disciple Gu",
            realm = 4,
            atk = 320,
            def = 210,
            spd = 110,
            crit = 25,
            talentName = "Void Essence Root",
            talentGrade = 6
        )

        discipleDao.insertDisciples(listOf(disciple1, disciple2, disciple3))

        val all = discipleDao.getAllDisciples()
        assertEquals(3, all.size)

        // Sorted by realm DESC, atk DESC -> disciple3 (r4, atk 320), disciple1 (r4, atk 250), disciple2 (r1, atk 30)
        assertEquals("Core Disciple Gu", all[0].name)
        assertEquals("Elder Lin", all[1].name)
        assertEquals("Novice Xiao", all[2].name)

        // Find by ID
        val found = discipleDao.getDiscipleById("d_001")
        assertNotNull(found)
        assertEquals("Elder Lin", found?.name)

        // Delete by ID
        discipleDao.deleteDiscipleById("d_002")
        val remaining = discipleDao.getAllDisciples()
        assertEquals(2, remaining.size)
        assertFalse(remaining.any { it.id == "d_002" })

        // Delete all
        discipleDao.deleteAllDisciples()
        assertTrue(discipleDao.getAllDisciples().isEmpty())
    }

    @Test
    fun testMapProgressDaoOperations() = runBlocking {
        val mapDao = database.mapProgressDao()

        val region1 = MapProgressEntity(
            regionId = "reg_misty_valley",
            isUnlocked = true,
            isExpeditionActive = true,
            expeditionTimeRemainingSec = 45,
            expeditionTotalTimeSec = 120,
            hasLootToClaim = false,
            lootSummary = "",
            assignedDiscipleIds = "d_001,d_003"
        )

        val region2 = MapProgressEntity(
            regionId = "reg_dragon_abyss",
            isUnlocked = false,
            isExpeditionActive = false
        )

        mapDao.insertAllRegions(listOf(region1, region2))

        val all = mapDao.getAllMapProgress()
        assertEquals(2, all.size)

        val misty = mapDao.getRegionProgress("reg_misty_valley")
        assertNotNull(misty)
        assertTrue(misty!!.isUnlocked)
        assertTrue(misty.isExpeditionActive)
        assertEquals(45, misty.expeditionTimeRemainingSec)

        // Complete expedition and grant loot
        val updatedMisty = misty.copy(
            isExpeditionActive = false,
            expeditionTimeRemainingSec = 0,
            hasLootToClaim = true,
            lootSummary = "+500 Spirit Stones, +1 Dragon Lotus"
        )
        mapDao.insertOrUpdateRegion(updatedMisty)

        val afterExpedition = mapDao.getRegionProgress("reg_misty_valley")
        assertNotNull(afterExpedition)
        assertFalse(afterExpedition!!.isExpeditionActive)
        assertTrue(afterExpedition.hasLootToClaim)
        assertEquals("+500 Spirit Stones, +1 Dragon Lotus", afterExpedition.lootSummary)
    }

    @Test
    fun testBuildingDaoOperations() = runBlocking {
        val buildingDao = database.buildingDao()

        val b1 = BuildingEntity(type = GameConfig.BUILD_HALL, name = "Main Hall", level = 3, isBuilt = true, workerCount = 2)
        val b2 = BuildingEntity(type = GameConfig.BUILD_LIBRARY, name = "Scripture Pavilion", level = 2, isBuilt = true, workerCount = 1)
        val b3 = BuildingEntity(type = GameConfig.BUILD_GARDEN, name = "Herb Garden", level = 4, isBuilt = true, workerCount = 3)

        buildingDao.insertBuildings(listOf(b1, b2, b3))

        val buildings = buildingDao.getAllBuildings()
        assertEquals(3, buildings.size)

        val hall = buildings.find { it.type == GameConfig.BUILD_HALL }
        assertNotNull(hall)
        assertEquals(3, hall?.level)
        assertEquals(2, hall?.workerCount)
    }

    @Test
    fun testRepositoryFullSaveAndRestoreLifecycle() = runBlocking {
        val sectData = SectData.getInstance()
        sectData.reset()
        sectData.sectName = "Divine Blade Peak"
        sectData.spiritStones = 77777L
        sectData.spiritHerbs = 8888L
        sectData.spiritOres = 4444L
        sectData.spiritPills = 333L
        sectData.jade = 120L
        sectData.essence = 95L
        sectData.sectQi = 4500L
        sectData.sectPower = 15000L
        sectData.sectRealm = 4
        sectData.maxDisciples = 15

        // Add Disciples
        sectData.disciples.clear()
        val disciple = Disciple("Master Chen").apply {
            id = "chen_01"
            realm = 3
            realmExp = 500
            atk = 180
            def = 140
            spd = 75
            critRate = 12
            talent = Talent().apply {
                name = "Mystic Thunder Root"
                grade = 4
            }
            weapon = Equipment("wpn_thunder", "Thunder Blade", 0, 2)
            armor = Equipment("arm_thunder", "Thunder Robe", 1, 2)
        }
        sectData.disciples.add(disciple)

        // Add Buildings
        sectData.buildings.clear()
        val hall = Building(GameConfig.BUILD_HALL, "Sect Hall", 10, 500L, 5).apply {
            level = 4
            isBuilt = true
            workers = 3
        }
        sectData.buildings.add(hall)

        // 1. Save to Room via repository
        repository.saveCurrentGameState(sectData)

        // 2. Corrupt/Mutate in-memory state
        sectData.reset()
        sectData.sectName = "Corrupted State"
        sectData.spiritStones = 0L
        sectData.disciples.clear()
        sectData.buildings.clear()

        // 3. Restore from Room via repository
        val restored = repository.loadGameStateIntoSectData(sectData)
        assertTrue("Repository should successfully restore state", restored)

        // 4. Assert restored values
        assertEquals("Divine Blade Peak", sectData.sectName)
        assertEquals(77777L, sectData.spiritStones)
        assertEquals(8888L, sectData.spiritHerbs)
        assertEquals(4444L, sectData.spiritOres)
        assertEquals(4, sectData.sectRealm)
        assertEquals(1, sectData.disciples.size)

        val restoredDisciple = sectData.disciples[0]
        assertEquals("Master Chen", restoredDisciple.name)
        assertEquals(3, restoredDisciple.realm)
        assertEquals(180, restoredDisciple.atk)
        assertEquals("Mystic Thunder Root", restoredDisciple.talent.name)
        assertNotNull(restoredDisciple.weapon)
        assertEquals("Thunder Blade", restoredDisciple.weapon.name)

        assertEquals(1, sectData.buildings.size)
        assertEquals(4, sectData.buildings[0].level)
    }

    @Test
    fun testRepositoryRestoreOnEmptyDatabaseReturnsFalseGracefully() = runBlocking {
        val emptyDb = Room.inMemoryDatabaseBuilder(context, SectDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val emptyRepo = SectRepository(emptyDb)
        val sectData = SectData.getInstance()

        val restored = emptyRepo.loadGameStateIntoSectData(sectData)
        assertFalse("Restoring on empty database should return false without error", restored)

        emptyDb.close()
    }

    @Test
    fun testRepositorySaveMapRegions() = runBlocking {
        val regions = listOf(
            MapRegion(
                id = "misty_forest",
                name = "Misty Forest",
                chineseName = "雾林",
                description = "Dense mist filled with low tier beasts",
                dangerLevel = 1,
                minSectRealmRequired = 0,
                resourceYieldDescription = "+100 Spirit Stones",
                iconEmoji = "🌲",
                isUnlocked = true,
                isExpeditionActive = false,
                hasLootToClaim = true,
                lootSummary = "100 Stones",
                assignedDiscipleNames = listOf("Novice A", "Novice B")
            )
        )

        repository.saveMapRegions(regions)

        val progress = database.mapProgressDao().getAllMapProgress()
        assertEquals(1, progress.size)
        assertEquals("misty_forest", progress[0].regionId)
        assertTrue(progress[0].isUnlocked)
        assertTrue(progress[0].hasLootToClaim)
        assertEquals("Novice A,Novice B", progress[0].assignedDiscipleIds)
    }
}
