package com.niks0n.mypetsinfo.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.niks0n.mypetsinfo.data.PetRepository
import com.niks0n.mypetsinfo.data.buildDatabase
import com.niks0n.mypetsinfo.data.getDatabaseBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val database = buildDatabase(getDatabaseBuilder(context))
                val tasks = PetRepository(database.careDao()).getTasks()
                AndroidReminderScheduler(context).reschedule(tasks)
                database.close()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
