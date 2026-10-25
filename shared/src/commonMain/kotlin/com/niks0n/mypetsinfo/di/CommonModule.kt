package com.niks0n.mypetsinfo.di

import com.niks0n.mypetsinfo.data.AppDatabase
import com.niks0n.mypetsinfo.data.PetRepository
import com.niks0n.mypetsinfo.data.buildDatabase
import org.koin.dsl.module

val commonModule = module {
    single<AppDatabase> {
        buildDatabase(get())
    }

    single {
        get<AppDatabase>().careDao()
    }

    single {
        PetRepository(get())
    }
}
