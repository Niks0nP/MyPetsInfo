package com.niks0n.mypetsinfo.data

class PetRepository(private val dao: CareDao) {
    suspend fun getTasks(petId: Long): List<CareTaskEntity> = dao.getTasks(petId)

    suspend fun getAllEnabledTasks(): List<CareTaskEntity> = dao.getAllEnabledTasks()

    suspend fun saveTask(petId: Long, task: CareTaskEntity): CareTaskEntity {
        require(task.petId == petId) { "Task owner does not match the active pet" }
        return if (task.id == 0L) {
            task.copy(id = dao.insertTask(task))
        } else {
            require(dao.isTaskOwnedByPet(petId, task.id)) { "Task does not belong to the active pet" }
            dao.updateTask(task)
            task
        }
    }

    suspend fun deleteTask(petId: Long, taskId: Long) = dao.deleteTask(petId, taskId)

    suspend fun getCompletions(
        petId: Long,
        fromDate: String,
        toDate: String,
    ): Set<TaskCompletionEntity> = dao.getCompletions(petId, fromDate, toDate).toSet()

    suspend fun setCompleted(petId: Long, taskId: Long, date: String, completed: Boolean) {
        require(dao.isTaskOwnedByPet(petId, taskId)) { "Task does not belong to the active pet" }
        if (completed) {
            dao.markCompleted(TaskCompletionEntity(taskId, date))
        } else {
            dao.clearCompletion(taskId, date)
        }
    }

    suspend fun getOrCreateActivePet(): PetEntity {
        dao.getPet(LEGACY_PET_ID)?.let { return it }
        dao.getFirstPet()?.let { return it }

        val draft = PetEntity(name = "")
        return draft.copy(id = dao.insertPet(draft))
    }

    suspend fun updatePet(pet: PetEntity) {
        require(pet.id != 0L) { "A saved pet must keep its identity" }
        require(pet.sexCode in PetSex.values) { "Unknown sex code" }
        dao.updatePet(pet)
    }

    suspend fun getLatestWeight(petId: Long): WeightMeasurementEntity? =
        dao.getLatestWeight(petId)

    suspend fun getHealthRecordCount(petId: Long): Int = dao.getHealthRecordCount(petId)
}
