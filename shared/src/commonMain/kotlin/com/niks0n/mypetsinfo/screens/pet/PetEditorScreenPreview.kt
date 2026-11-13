package com.niks0n.mypetsinfo.screens.pet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.niks0n.mypetsinfo.data.PetEntity
import com.niks0n.mypetsinfo.data.PetSex
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme
import kotlinx.datetime.LocalDate

@Preview(
    name = "Редактор питомца",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    locale = "ru",
)
@Composable
private fun PetEditorScreenPreview() {
    MyPetsInfoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PetEditorScreen(
                pet = PetEntity(
                    id = 1,
                    name = "Мило",
                    species = "Собака",
                    breed = "Золотистый ретривер",
                    sexCode = PetSex.MALE,
                    birthDate = "2024-08-19",
                    color = "Золотистый",
                    description = "Любит долгие прогулки и игры с мячом.",
                ),
                today = LocalDate(2026, 11, 13),
                isSaving = false,
                onSave = {},
                onCancel = {},
            )
        }
    }
}
