package com.niks0n.mypetsinfo.di

import androidx.room.RoomDatabase
import com.niks0n.mypetsinfo.NoOpReminderScheduler
import com.niks0n.mypetsinfo.ReminderScheduler
import com.niks0n.mypetsinfo.data.AppDatabase
import com.niks0n.mypetsinfo.data.getDatabaseBuilder
import org.koin.dsl.module

val iosModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder()
    }

    single<ReminderScheduler> {
        NoOpReminderScheduler
    }
}