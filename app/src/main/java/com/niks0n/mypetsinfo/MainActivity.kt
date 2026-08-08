package com.niks0n.mypetsinfo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.niks0n.mypetsinfo.data.getDatabaseBuilder
import com.niks0n.mypetsinfo.notifications.AndroidReminderScheduler

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val reminderScheduler = remember { AndroidReminderScheduler(applicationContext) }
            val databaseBuilder = remember { getDatabaseBuilder(applicationContext) }
            App(
                databaseBuilder = databaseBuilder,
                reminderScheduler = reminderScheduler,
                requestNotificationPermission = ::requestNotificationPermission,
            )
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
