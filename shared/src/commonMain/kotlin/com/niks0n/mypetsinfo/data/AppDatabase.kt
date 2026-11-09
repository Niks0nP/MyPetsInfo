package com.niks0n.mypetsinfo.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        PetEntity::class,
        CareTaskEntity::class,
        TaskCompletionEntity::class,
        WeightMeasurementEntity::class,
        HealthRecordEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun careDao(): CareDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pets_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `species` TEXT NOT NULL,
                `breed` TEXT,
                `sexCode` TEXT NOT NULL,
                `birthDate` TEXT,
                `color` TEXT,
                `description` TEXT,
                `avatarAssetId` TEXT,
                `coverAssetId` TEXT
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            INSERT INTO `pets_new` (
                `id`, `name`, `species`, `breed`, `sexCode`, `birthDate`, `color`,
                `description`, `avatarAssetId`, `coverAssetId`
            )
            SELECT `id`, `name`, `species`, NULL, 'unknown', NULL, NULL, NULL, NULL, NULL
            FROM `pets`
            """.trimIndent(),
        )
        connection.execSQL(
            """
            INSERT OR IGNORE INTO `pets_new` (
                `id`, `name`, `species`, `breed`, `sexCode`, `birthDate`, `color`,
                `description`, `avatarAssetId`, `coverAssetId`
            )
            SELECT 1, '', '', NULL, 'unknown', NULL, NULL, NULL, NULL, NULL
            WHERE EXISTS (SELECT 1 FROM `care_tasks`)
            """.trimIndent(),
        )
        connection.execSQL("DROP TABLE `pets`")
        connection.execSQL("ALTER TABLE `pets_new` RENAME TO `pets`")

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `care_tasks_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `petId` INTEGER NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `hour` INTEGER NOT NULL,
                `minute` INTEGER NOT NULL,
                `daysMask` INTEGER NOT NULL,
                `isEnabled` INTEGER NOT NULL,
                FOREIGN KEY(`petId`) REFERENCES `pets`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            INSERT INTO `care_tasks_new` (
                `id`, `petId`, `title`, `description`, `hour`, `minute`, `daysMask`, `isEnabled`
            )
            SELECT `id`, 1, `title`, `description`, `hour`, `minute`, `daysMask`, `isEnabled`
            FROM `care_tasks`
            """.trimIndent(),
        )
        connection.execSQL("DROP TABLE `care_tasks`")
        connection.execSQL("ALTER TABLE `care_tasks_new` RENAME TO `care_tasks`")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_care_tasks_petId` ON `care_tasks` (`petId`)")

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `task_completions_new` (
                `taskId` INTEGER NOT NULL,
                `date` TEXT NOT NULL,
                PRIMARY KEY(`taskId`, `date`),
                FOREIGN KEY(`taskId`) REFERENCES `care_tasks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            INSERT INTO `task_completions_new` (`taskId`, `date`)
            SELECT `taskId`, `date` FROM `task_completions`
            """.trimIndent(),
        )
        connection.execSQL("DROP TABLE `task_completions`")
        connection.execSQL("ALTER TABLE `task_completions_new` RENAME TO `task_completions`")

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `weight_measurements` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `petId` INTEGER NOT NULL,
                `measuredOn` TEXT NOT NULL,
                `weightGrams` INTEGER NOT NULL,
                `note` TEXT,
                FOREIGN KEY(`petId`) REFERENCES `pets`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_weight_measurements_petId_measuredOn` " +
                "ON `weight_measurements` (`petId`, `measuredOn`)",
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `health_records` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `petId` INTEGER NOT NULL,
                `categoryCode` TEXT NOT NULL,
                `recordedOn` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `notes` TEXT,
                FOREIGN KEY(`petId`) REFERENCES `pets`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_health_records_petId_recordedOn` " +
                "ON `health_records` (`petId`, `recordedOn`)",
        )
        connection.prepare("PRAGMA foreign_key_check").use { statement ->
            check(!statement.step()) { "Foreign key check failed after migration 1 to 2" }
        }
    }
}

fun buildDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .addMigrations(MIGRATION_1_2)
        .build()
