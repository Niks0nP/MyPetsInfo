package com.niks0n.mypetsinfo.di

import org.koin.core.context.startKoin

fun initKoinIos() {
    startKoin {
        modules(
            commonModule,
            iosModule
        )
    }
}