package com.niks0n.mypetsinfo.screens.weight

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.niks0n.mypetsinfo.data.WeightMeasurementEntity
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme
import kotlinx.datetime.LocalDate

@Preview(
    name = "История веса",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    locale = "ru",
)
@Composable
private fun WeightHistoryScreenPreview() {
    MyPetsInfoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            WeightHistoryScreen(
                measurements = listOf(
                    WeightMeasurementEntity(
                        id = 2,
                        petId = 1,
                        measuredOn = "2026-11-10",
                        weightGrams = 28_400,
                        note = "После прогулки",
                    ),
                    WeightMeasurementEntity(
                        id = 1,
                        petId = 1,
                        measuredOn = "2026-10-10",
                        weightGrams = 28_750,
                    ),
                ),
                onAdd = {},
                onEdit = {},
            )
        }
    }
}

@Preview(
    name = "Редактор веса",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    locale = "ru",
)
@Composable
private fun WeightEditorScreenPreview() {
    MyPetsInfoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            WeightEditorScreen(
                petId = 1,
                measurement = WeightMeasurementEntity(
                    id = 2,
                    petId = 1,
                    measuredOn = "2026-11-10",
                    weightGrams = 28_400,
                    note = "После прогулки",
                ),
                today = LocalDate(2026, 11, 13),
                isSaving = false,
                onSave = {},
                onDelete = {},
                onCancel = {},
            )
        }
    }
}
