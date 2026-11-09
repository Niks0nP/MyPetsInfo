package com.niks0n.mypetsinfo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CareDao {
    @Query("SELECT * FROM care_tasks WHERE petId = :petId ORDER BY hour, minute, title")
    suspend fun getTasks(petId: Long): List<CareTaskEntity>

    @Query("SELECT * FROM care_tasks WHERE isEnabled = 1 ORDER BY petId, hour, minute, title")
    suspend fun getAllEnabledTasks(): List<CareTaskEntity>

    @Insert
    suspend fun insertTask(task: CareTaskEntity): Long

    @Update
    suspend fun updateTask(task: CareTaskEntity)

    @Query("DELETE FROM care_tasks WHERE id = :taskId AND petId = :petId")
    suspend fun deleteTask(petId: Long, taskId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM care_tasks WHERE id = :taskId AND petId = :petId)")
    suspend fun isTaskOwnedByPet(petId: Long, taskId: Long): Boolean

    @Query(
        """
        SELECT task_completions.* FROM task_completions
        INNER JOIN care_tasks ON care_tasks.id = task_completions.taskId
        WHERE care_tasks.petId = :petId AND date BETWEEN :fromDate AND :toDate
        """,
    )
    suspend fun getCompletions(
        petId: Long,
        fromDate: String,
        toDate: String,
    ): List<TaskCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markCompleted(completion: TaskCompletionEntity)

    @Query("DELETE FROM task_completions WHERE taskId = :taskId AND date = :date")
    suspend fun clearCompletion(taskId: Long, date: String)

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getPet(petId: Long): PetEntity?

    @Query("SELECT * FROM pets ORDER BY id LIMIT 1")
    suspend fun getFirstPet(): PetEntity?

    @Insert
    suspend fun insertPet(pet: PetEntity): Long

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Query(
        """
        SELECT * FROM weight_measurements
        WHERE petId = :petId
        ORDER BY measuredOn DESC, id DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestWeight(petId: Long): WeightMeasurementEntity?

    @Query(
        """
        SELECT * FROM weight_measurements
        WHERE petId = :petId
        ORDER BY measuredOn DESC, id DESC
        LIMIT 100
        """,
    )
    suspend fun getWeightHistory(petId: Long): List<WeightMeasurementEntity>

    @Query(
        """
        SELECT * FROM weight_measurements
        WHERE petId = :petId AND measuredOn = :measuredOn
        LIMIT 1
        """,
    )
    suspend fun getWeightForDate(petId: Long, measuredOn: String): WeightMeasurementEntity?

    @Query("SELECT * FROM weight_measurements WHERE id = :measurementId AND petId = :petId LIMIT 1")
    suspend fun getWeight(petId: Long, measurementId: Long): WeightMeasurementEntity?

    @Insert
    suspend fun insertWeight(measurement: WeightMeasurementEntity): Long

    @Update
    suspend fun updateWeight(measurement: WeightMeasurementEntity)

    @Query("DELETE FROM weight_measurements WHERE id = :measurementId AND petId = :petId")
    suspend fun deleteWeight(petId: Long, measurementId: Long)

    @Query("SELECT COUNT(*) FROM health_records WHERE petId = :petId")
    suspend fun getHealthRecordCount(petId: Long): Int
}
