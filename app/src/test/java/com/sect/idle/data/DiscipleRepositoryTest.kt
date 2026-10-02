package com.sect.idle.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sect.idle.data.db.SectDatabase
import com.sect.idle.data.db.entities.DiscipleEntity
import com.sect.idle.data.repository.DiscipleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DiscipleRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: SectDatabase
    private lateinit var repository: DiscipleRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, SectDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DiscipleRepository(database.discipleDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testDiscipleRepositoryCrudAndFlow() = runBlocking {
        val disciple1 = DiscipleEntity(
            id = "d_01",
            name = "Lin Feng",
            cultivationLevel = 3,
            spiritEnergy = 120,
            maxSpiritEnergy = 150,
            atk = 80,
            def = 50
        )

        val disciple2 = DiscipleEntity(
            id = "d_02",
            name = "Xiao Yan",
            cultivationLevel = 5,
            spiritEnergy = 200,
            maxSpiritEnergy = 200,
            atk = 150,
            def = 90
        )

        repository.insertDisciples(listOf(disciple1, disciple2))

        val all = repository.getAllDisciples()
        assertEquals(2, all.size)

        // Observe through reactive Flow
        val flowList = repository.allDisciplesFlow.first()
        assertEquals(2, flowList.size)
        // Ordered by realm/cultivationLevel DESC
        assertEquals("Xiao Yan", flowList[0].name)
        assertEquals(5, flowList[0].cultivationLevel)
        assertEquals(200, flowList[0].spiritEnergy)

        val retrieved = repository.getDiscipleById("d_01")
        assertNotNull(retrieved)
        assertEquals("Lin Feng", retrieved?.name)
        assertEquals(3, retrieved?.cultivationLevel)
        assertEquals(120, retrieved?.spiritEnergy)

        // Delete disciple
        repository.deleteDiscipleById("d_01")
        assertNull(repository.getDiscipleById("d_01"))
        assertEquals(1, repository.getAllDisciples().size)

        // Delete all
        repository.deleteAllDisciples()
        assertTrue(repository.getAllDisciples().isEmpty())
    }
}
