package com.niks0n.mypetsinfo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

const val ALL_DAYS_MASK = 0b1111111

@Entity(tableName = "care_tasks")
data class CareTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
)
data class TaskCompletionEntity(
    val taskId: Long,
    val date: String,
)

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey val id: Long = 1,
    val name: String,
    val species: String,
)
