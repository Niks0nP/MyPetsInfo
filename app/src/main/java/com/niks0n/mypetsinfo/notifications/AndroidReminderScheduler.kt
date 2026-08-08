package com.niks0n.mypetsinfo.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.niks0n.mypetsinfo.MainActivity
import com.niks0n.mypetsinfo.R
import com.niks0n.mypetsinfo.ReminderScheduler
import com.niks0n.mypetsinfo.data.CareTaskEntity
import java.time.ZonedDateTime

class AndroidReminderScheduler(context: Context) : ReminderScheduler {
    private val appContext = context.applicationContext

    init {
        createNotificationChannel(appContext)
    }

    override fun schedule(task: CareTaskEntity) {
        if (!task.isEnabled) {
            cancel(task.id)
            return
        }
        scheduleReminder(
            context = appContext,
            taskId = task.id,
            title = task.title,
            description = task.description,
            hour = task.hour,
            minute = task.minute,
            daysMask = task.daysMask,
        )
    }

    override fun cancel(taskId: Long) {
        val alarmManager = appContext.getSystemService(AlarmManager::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            taskId.toRequestCode(),
            reminderIntent(appContext, taskId),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    override fun reschedule(tasks: List<CareTaskEntity>) {
        tasks.forEach(::schedule)
    }

    companion object {
        const val CHANNEL_ID = "pet_care_reminders"

        internal const val EXTRA_TASK_ID = "task_id"
        internal const val EXTRA_TITLE = "title"
        internal const val EXTRA_DESCRIPTION = "description"
        internal const val EXTRA_HOUR = "hour"
        internal const val EXTRA_MINUTE = "minute"
        internal const val EXTRA_DAYS_MASK = "days_mask"

        internal fun scheduleReminder(
            context: Context,
            taskId: Long,
            title: String,
            description: String,
            hour: Int,
            minute: Int,
            daysMask: Int,
        ) {
            val triggerAtMillis = nextTrigger(hour, minute, daysMask)
                ?.toInstant()
                ?.toEpochMilli()
                ?: return
            val intent = reminderIntent(context, taskId).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DESCRIPTION, description)
                putExtra(EXTRA_HOUR, hour)
                putExtra(EXTRA_MINUTE, minute)
                putExtra(EXTRA_DAYS_MASK, daysMask)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.toRequestCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }

        internal fun nextTrigger(
            hour: Int,
            minute: Int,
            daysMask: Int,
            now: ZonedDateTime = ZonedDateTime.now(),
        ): ZonedDateTime? {
            for (offset in 0..7) {
                val date = now.toLocalDate().plusDays(offset.toLong())
                val dayBit = 1 shl (date.dayOfWeek.value - 1)
                if (daysMask and dayBit == 0) continue
                val candidate = date.atTime(hour, minute).atZone(now.zone)
                if (candidate.isAfter(now)) return candidate
            }
            return null
        }

        private fun reminderIntent(context: Context, taskId: Long) =
            Intent(context, ReminderReceiver::class.java).apply {
                action = "${context.packageName}.REMINDER.$taskId"
                putExtra(EXTRA_TASK_ID, taskId)
            }

        internal fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Забота о питомце",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Напоминания о ежедневных делах питомца"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(AndroidReminderScheduler.EXTRA_TASK_ID, 0L)
        val title = intent.getStringExtra(AndroidReminderScheduler.EXTRA_TITLE).orEmpty()
        val description = intent.getStringExtra(AndroidReminderScheduler.EXTRA_DESCRIPTION).orEmpty()
        val hour = intent.getIntExtra(AndroidReminderScheduler.EXTRA_HOUR, 9)
        val minute = intent.getIntExtra(AndroidReminderScheduler.EXTRA_MINUTE, 0)
        val daysMask = intent.getIntExtra(AndroidReminderScheduler.EXTRA_DAYS_MASK, 0)

        AndroidReminderScheduler.createNotificationChannel(context)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            val openApp = PendingIntent.getActivity(
                context,
                taskId.toRequestCode(),
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = Notification.Builder(context, AndroidReminderScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title.ifBlank { "Пора позаботиться о питомце" })
                .setContentText(description.ifBlank { "Откройте приложение и отметьте дело выполненным" })
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .build()
            context.getSystemService(NotificationManager::class.java)
                .notify(taskId.toNotificationId(), notification)
        }

        AndroidReminderScheduler.scheduleReminder(
            context = context,
            taskId = taskId,
            title = title,
            description = description,
            hour = hour,
            minute = minute,
            daysMask = daysMask,
        )
    }
}

private fun Long.toRequestCode(): Int = (this xor (this ushr 32)).toInt()

private fun Long.toNotificationId(): Int = toRequestCode()
