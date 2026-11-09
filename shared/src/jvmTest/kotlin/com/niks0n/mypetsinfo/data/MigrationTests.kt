package com.niks0n.mypetsinfo.data

import androidx.room.Room
import androidx.sqlite.execSQL
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class MigrationTests {
    @Test
    fun `migration 1 to 2 preserves legacy profile tasks and completions`() = runTest {
        val databaseFile = File("build/test-databases/profile-migration.db")
        databaseFile.parentFile.mkdirs()
        databaseFile.delete()

        val connection = BundledSQLiteDriver().open(databaseFile.absolutePath)
        connection.execSQL(
            """
            CREATE TABLE `care_tasks` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `hour` INTEGER NOT NULL,
                `minute` INTEGER NOT NULL,
                `daysMask` INTEGER NOT NULL,
                `isEnabled` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "CREATE TABLE `task_completions` (`taskId` INTEGER NOT NULL, `date` TEXT NOT NULL, " +
                "PRIMARY KEY(`taskId`, `date`))",
        )
        connection.execSQL(
            "CREATE TABLE `pets` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, " +
                "`species` TEXT NOT NULL, PRIMARY KEY(`id`))",
        )
        connection.execSQL(
            "CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)",
        )
        connection.execSQL(
            "INSERT INTO room_master_table (id, identity_hash) " +
                "VALUES (42, 'beacf2fbc800f49990ce6e597009b5d1')",
        )
        connection.execSQL("INSERT INTO pets (id, name, species) VALUES (1, 'Барсик', 'Кот')")
        connection.execSQL(
            "INSERT INTO care_tasks (id, title, description, hour, minute, daysMask, isEnabled) " +
                "VALUES (7, 'Дать лекарство', 'После еды', 9, 30, 127, 1)",
        )
        connection.execSQL(
            "INSERT INTO task_completions (taskId, date) VALUES (7, '2026-11-09')",
        )
        connection.execSQL("PRAGMA user_version = 1")
        connection.close()

        val database = buildDatabase(
            Room.databaseBuilder<AppDatabase>(name = databaseFile.absolutePath),
        )
        try {
            val dao = database.careDao()
            assertEquals("Барсик", dao.getPet(LEGACY_PET_ID)?.name)
            assertEquals(listOf("Дать лекарство"), dao.getTasks(LEGACY_PET_ID).map { it.title })
            assertEquals(
                listOf(TaskCompletionEntity(7, "2026-11-09")),
                dao.getCompletions(LEGACY_PET_ID, "2026-11-09", "2026-11-09"),
            )
            assertNull(dao.getLatestWeight(LEGACY_PET_ID))
            assertEquals(0, dao.getHealthRecordCount(LEGACY_PET_ID))
        } finally {
            database.close()
            databaseFile.delete()
        }
    }

    @Test
    fun `migration creates legacy pet when tasks exist without profile`() = runTest {
        val databaseFile = File("build/test-databases/profile-migration-missing-pet.db")
        databaseFile.parentFile.mkdirs()
        databaseFile.delete()

        val connection = BundledSQLiteDriver().open(databaseFile.absolutePath)
        connection.execSQL(
            "CREATE TABLE `care_tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, `description` TEXT NOT NULL, `hour` INTEGER NOT NULL, " +
                "`minute` INTEGER NOT NULL, `daysMask` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL)",
        )
        connection.execSQL(
            "CREATE TABLE `task_completions` (`taskId` INTEGER NOT NULL, `date` TEXT NOT NULL, " +
                "PRIMARY KEY(`taskId`, `date`))",
        )
        connection.execSQL(
            "CREATE TABLE `pets` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, " +
                "`species` TEXT NOT NULL, PRIMARY KEY(`id`))",
        )
        connection.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
        connection.execSQL(
            "INSERT INTO room_master_table (id, identity_hash) " +
                "VALUES (42, 'beacf2fbc800f49990ce6e597009b5d1')",
        )
        connection.execSQL(
            "INSERT INTO care_tasks (id, title, description, hour, minute, daysMask, isEnabled) " +
                "VALUES (3, 'Кормление', '', 8, 0, 127, 1)",
        )
        connection.execSQL("PRAGMA user_version = 1")
        connection.close()

        val database = Room.databaseBuilder<AppDatabase>(name = databaseFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .addMigrations(MIGRATION_1_2)
            .build()
        try {
            assertEquals(LEGACY_PET_ID, database.careDao().getPet(LEGACY_PET_ID)?.id)
            assertEquals(LEGACY_PET_ID, database.careDao().getTasks(LEGACY_PET_ID).single().petId)
        } finally {
            database.close()
            databaseFile.delete()
        }
    }
}
