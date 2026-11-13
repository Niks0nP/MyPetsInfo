package com.niks0n.mypetsinfo.screens.news

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme

@Preview(
    name = "Новости",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    locale = "ru",
)
@Composable
private fun NewsScreenPreview() {
    MyPetsInfoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NewsScreen()
        }
    }
}
