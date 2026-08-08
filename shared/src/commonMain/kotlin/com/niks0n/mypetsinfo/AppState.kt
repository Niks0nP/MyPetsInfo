package com.niks0n.mypetsinfo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.niks0n.mypetsinfo.data.CareTaskEntity
import com.niks0n.mypetsinfo.data.PetEntity
import com.niks0n.mypetsinfo.data.PetRepository
import com.niks0n.mypetsinfo.data.TaskCompletionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class AppState(
    private val repository: PetRepository,
    private val reminderScheduler: ReminderScheduler,
    private val scope: CoroutineScope,
) {
    var tasks by mutableStateOf<List<CareTaskEntity>>(emptyList())
        private set
    var completions by mutableStateOf<Set<TaskCompletionEntity>>(emptySet())
        private set
    var pet by mutableStateOf(PetEntity(name = "Мой питомец", species = ""))
        private set
    var isLoading by mutableStateOf(true)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val today: LocalDate
        get() = currentDate()

    suspend fun load() {
        isLoading = true
        runCatching {
            refresh()
            reminderScheduler.reschedule(tasks)
        }.onFailure(::showError)
        isLoading = false
    }

    fun tasksFor(date: LocalDate): List<CareTaskEntity> = tasks.filter { task ->
        task.isEnabled && task.daysMask and (1 shl date.dayOfWeek.ordinal) != 0
    }

    fun isCompleted(taskId: Long, date: LocalDate): Boolean =
        TaskCompletionEntity(taskId, date.toString()) in completions

    fun setCompleted(taskId: Long, date: LocalDate, completed: Boolean) {
        scope.launch {
            runCatching {
                repository.setCompleted(taskId, date.toString(), completed)
                refreshCompletions()
            }.onFailure(::showError)
        }
    }

    fun saveTask(task: CareTaskEntity, onSaved: () -> Unit) {
        scope.launch {
            isSaving = true
            runCatching {
                val saved = repository.saveTask(task)
                if (saved.isEnabled) {
                    reminderScheduler.schedule(saved)
                } else {
                    reminderScheduler.cancel(saved.id)
                }
                refresh()
            }.onSuccess {
                onSaved()
            }.onFailure(::showError)
            isSaving = false
        }
    }

    fun deleteTask(task: CareTaskEntity, onDeleted: () -> Unit) {
        scope.launch {
            isSaving = true
            runCatching {
                repository.deleteTask(task.id)
                reminderScheduler.cancel(task.id)
                refresh()
            }.onSuccess {
                onDeleted()
            }.onFailure(::showError)
            isSaving = false
        }
    }

    fun savePet(name: String, species: String) {
        scope.launch {
            isSaving = true
            runCatching {
                val updated = PetEntity(name = name, species = species)
                repository.savePet(updated)
                pet = updated
            }.onFailure(::showError)
            isSaving = false
        }
    }

    fun clearError() {
        errorMessage = null
    }

    private suspend fun refresh() {
        tasks = repository.getTasks()
        pet = repository.getPet()
        refreshCompletions()
    }

    private suspend fun refreshCompletions() {
        val endDate = today.plus(6, DateTimeUnit.DAY)
        completions = repository.getCompletions(today.toString(), endDate.toString())
    }

    private fun showError(throwable: Throwable) {
        errorMessage = throwable.message ?: "Не удалось выполнить действие"
    }
}

@OptIn(ExperimentalTime::class)
private fun currentDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
