package com.niks0n.mypetsinfo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

const val ALL_DAYS_MASK = 0b1111111
const val LEGACY_PET_ID = 1L

object PetSex {
    const val UNKNOWN = "unknown"
    const val MALE = "male"
    const val FEMALE = "female"

    val values = setOf(UNKNOWN, MALE, FEMALE)
}

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val species: String = "",
    val breed: String? = null,
    val sexCode: String = PetSex.UNKNOWN,
    val birthDate: String? = null,
    val color: String? = null,
    val description: String? = null,
    val avatarAssetId: String? = null,
    val coverAssetId: String? = null,
)

@Entity(
    tableName = "care_tasks",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("petId")],
)
data class CareTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val title: String,
    val description: String = "",
    val hour: Int,
    val minute: Int,
    val daysMask: Int = ALL_DAYS_MASK,
    val isEnabled: Boolean = true,
)

@Entity(
    tableName = "task_completions",
    primaryKeys = ["taskId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = CareTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TaskCompletionEntity(
    val taskId: Long,
    val date: String,
)

@Entity(
    tableName = "weight_measurements",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index(value = ["petId", "measuredOn"], unique = true)],
)
data class WeightMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val measuredOn: String,
    val weightGrams: Long,
    val note: String? = null,
)

@Entity(
    tableName = "health_records",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index(value = ["petId", "recordedOn"])],
)
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val categoryCode: String,
    val recordedOn: String,
    val title: String,
    val notes: String? = null,
)
