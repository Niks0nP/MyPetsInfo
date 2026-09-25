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

        val careTaskEntity = CareTaskEntity(
            id = 0,
            title = "test task",
            description = "",
            hour = 10,
            minute = 5,
        )

        val resultInserting = dao.insertTask(careTaskEntity)
        val getTask = dao.getTasks()

        assertTrue(resultInserting > 0)
        assertEquals(1, getTask.size)
        assertEquals(careTaskEntity.title, getTask[0].title)
    }

    @Test
    fun `delete not existing entity`() = runTest {
        val notExistingEntity = CareTaskEntity(
            title = "title",
            hour = 10,
            minute = 5
        )

        val taskId = dao.insertTask(notExistingEntity)
        val savedTask = notExistingEntity.copy(id = taskId)

        dao.deleteTask(Long.MAX_VALUE)

        assertEquals(listOf(savedTask), dao.getTasks())
    }
}