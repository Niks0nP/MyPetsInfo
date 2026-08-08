package com.niks0n.mypetsinfo

import androidx.compose.ui.window.ComposeUIViewController
import com.niks0n.mypetsinfo.data.getDatabaseBuilder

@Suppress("unused") // Called by the SwiftUI host application.
fun MainViewController() = ComposeUIViewController {
    App(databaseBuilder = getDatabaseBuilder())
}
