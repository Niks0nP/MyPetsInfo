package com.niks0n.mypetsinfo.screens.pet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.niks0n.mypetsinfo.data.PetEntity
import com.niks0n.mypetsinfo.data.PetSex
import com.niks0n.mypetsinfo.data.WeightMeasurementEntity
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme
import kotlinx.datetime.LocalDate

@Preview(
    name = "Pet profile · Light",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    locale = "ru",
)
@Composable
private fun PetScreenPreview() {
    PetScreenPreviewContent(darkTheme = false)
}

@Preview(
    name = "Pet profile · Dark",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    locale = "ru",
)
@Composable
private fun PetScreenDarkPreview() {
    PetScreenPreviewContent(darkTheme = true)
}

@Composable
private fun PetScreenPreviewContent(darkTheme: Boolean) {
    MyPetsInfoTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            PetScreenContent(
                pet = PetEntity(
                    id = 1,
                    name = "Мило",
                    species = "Собака",
                    breed = "Золотистый ретривер",
                    sexCode = PetSex.MALE,
                    birthDate = "2024-08-19",
                    color = "Золотистый",
                    description = "Дружелюбный и энергичный пёс, который любит прогулки и игры.",
                ),
                today = LocalDate(2026, 11, 10),
                latestWeight = WeightMeasurementEntity(
                    petId = 1,
                    measuredOn = "2026-11-01",
                    weightGrams = 28_400,
                ),
                healthRecordCount = 0,
                onEdit = {},
                onWeightClick = {},
                photoEditingEnabled = true,
                onAvatarPhotoClick = {},
                onCoverPhotoClick = {},
            )
        }
    }
}
