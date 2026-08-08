package com.niks0n.mypetsinfo

import com.niks0n.mypetsinfo.data.CareTaskEntity

interface ReminderScheduler {
    fun schedule(task: CareTaskEntity)
    fun cancel(taskId: Long)
    fun reschedule(tasks: List<CareTaskEntity>)
}

object NoOpReminderScheduler : ReminderScheduler {
    override fun schedule(task: CareTaskEntity) = Unit
    override fun cancel(taskId: Long) = Unit
    override fun reschedule(tasks: List<CareTaskEntity>) = Unit
}
