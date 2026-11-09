package com.niks0n.mypetsinfo.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CareDaoTests {
    private lateinit var database: AppDatabase
    private lateinit var dao: CareDao

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()

        dao = database.careDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `inserting a task and returning the inserted Id`() = runTest {
        val petId = dao.insertPet(PetEntity(name = "Мило"))

        val careTaskEntity = CareTaskEntity(
            id = 0,
            petId = petId,
            title = "test task",
            description = "",
            hour = 10,
            minute = 5,
        )

        val resultInserting = dao.insertTask(careTaskEntity)
        val getTask = dao.getTasks(petId)

        assertTrue(resultInserting > 0)
        assertEquals(1, getTask.size)
        assertEquals(careTaskEntity.title, getTask[0].title)
    }

    @Test
    fun `delete not existing entity`() = runTest {
        val petId = dao.insertPet(PetEntity(name = "Мило"))
        val notExistingEntity = CareTaskEntity(
            petId = petId,
            title = "title",
            hour = 10,
            minute = 5
        )

        val taskId = dao.insertTask(notExistingEntity)
        val savedTask = notExistingEntity.copy(id = taskId)

        dao.deleteTask(petId, Long.MAX_VALUE)

        assertEquals(listOf(savedTask), dao.getTasks(petId))
    }

    @Test
    fun `pet scoped queries keep two pets isolated`() = runTest {
        val firstPetId = dao.insertPet(PetEntity(name = "Мило"))
        val secondPetId = dao.insertPet(PetEntity(name = "Луна"))

        val firstTaskId = dao.insertTask(
            CareTaskEntity(
                petId = firstPetId,
                title = "Прогулка",
                hour = 9,
                minute = 0,
            ),
        )
        dao.insertTask(
            CareTaskEntity(
                petId = secondPetId,
                title = "Кормление",
                hour = 10,
                minute = 0,
            ),
        )
        dao.markCompleted(TaskCompletionEntity(firstTaskId, "2026-11-09"))

        assertEquals(listOf("Прогулка"), dao.getTasks(firstPetId).map { it.title })
        assertEquals(listOf("Кормление"), dao.getTasks(secondPetId).map { it.title })
        assertEquals(
            1,
            dao.getCompletions(firstPetId, "2026-11-09", "2026-11-09").size,
        )
        assertTrue(
            dao.getCompletions(secondPetId, "2026-11-09", "2026-11-09").isEmpty(),
        )
    }

    @Test
    fun `updating pet preserves owned tasks`() = runTest {
        val petId = dao.insertPet(PetEntity(name = "Мило"))
        dao.insertTask(
            CareTaskEntity(
                petId = petId,
                title = "Прогулка",
                hour = 9,
                minute = 0,
            ),
        )

        dao.updatePet(
            requireNotNull(dao.getPet(petId)).copy(
                breed = "Золотистый ретривер",
                description = "Любит долгие прогулки",
            ),
        )

        assertEquals(1, dao.getTasks(petId).size)
        assertEquals("Золотистый ретривер", dao.getPet(petId)?.breed)
    }
}
