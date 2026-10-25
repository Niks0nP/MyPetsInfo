package com.niks0n.mypetsinfo.di

import androidx.room.RoomDatabase
import com.niks0n.mypetsinfo.ReminderScheduler
import com.niks0n.mypetsinfo.data.AppDatabase
import com.niks0n.mypetsinfo.data.getDatabaseBuilder
import com.niks0n.mypetsinfo.notifications.AndroidReminderScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder(androidContext())
    }

    single<ReminderScheduler> {
        AndroidReminderScheduler(androidContext())
    }
}