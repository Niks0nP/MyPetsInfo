package com.niks0n.mypetsinfo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CareDao {
    @Query("SELECT * FROM care_tasks ORDER BY hour, minute, title")
    suspend fun getTasks(): List<CareTaskEntity>

    @Insert
    suspend fun insertTask(task: CareTaskEntity): Long

    @Update
    suspend fun updateTask(task: CareTaskEntity)

    @Query("DELETE FROM care_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    @Query("SELECT * FROM task_completions WHERE date BETWEEN :fromDate AND :toDate")
    suspend fun getCompletions(fromDate: String, toDate: String): List<TaskCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markCompleted(completion: TaskCompletionEntity)

    @Query("DELETE FROM task_completions WHERE taskId = :taskId AND date = :date")
    suspend fun clearCompletion(taskId: Long, date: String)

    @Query("DELETE FROM task_completions WHERE taskId = :taskId")
    suspend fun deleteTaskCompletions(taskId: Long)

    @Query("SELECT * FROM pets WHERE id = 1 LIMIT 1")
    suspend fun getPet(): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePet(pet: PetEntity)
}
