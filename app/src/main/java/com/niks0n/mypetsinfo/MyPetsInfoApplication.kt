package com.niks0n.mypetsinfo

import android.app.Application
import com.niks0n.mypetsinfo.di.androidModule
import com.niks0n.mypetsinfo.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyPetsInfoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyPetsInfoApplication)
            modules(
                commonModule,
                androidModule
            )
        }
    }
}