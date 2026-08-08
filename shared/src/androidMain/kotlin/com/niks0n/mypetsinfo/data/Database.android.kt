package com.niks0n.mypetsinfo.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val databaseFile = appContext.getDatabasePath("my_pets_info.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = databaseFile.absolutePath,
    )
}
