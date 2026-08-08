package com.niks0n.mypetsinfo.data

class PetRepository(private val dao: CareDao) {
    suspend fun getTasks(): List<CareTaskEntity> = dao.getTasks()

    suspend fun saveTask(task: CareTaskEntity): CareTaskEntity {
        return if (task.id == 0L) {
            task.copy(id = dao.insertTask(task))
        } else {
            dao.updateTask(task)
            task
        }
    }

    suspend fun deleteTask(taskId: Long) {
        dao.deleteTaskCompletions(taskId)
        dao.deleteTask(taskId)
    }

    suspend fun getCompletions(fromDate: String, toDate: String): Set<TaskCompletionEntity> =
        dao.getCompletions(fromDate, toDate).toSet()

    suspend fun setCompleted(taskId: Long, date: String, completed: Boolean) {
        if (completed) {
            dao.markCompleted(TaskCompletionEntity(taskId, date))
        } else {
            dao.clearCompletion(taskId, date)
        }
    }

    suspend fun getPet(): PetEntity = dao.getPet() ?: PetEntity(
        name = "Мой питомец",
        species = "",
    ).also { dao.savePet(it) }

    suspend fun savePet(pet: PetEntity) = dao.savePet(pet)
}
