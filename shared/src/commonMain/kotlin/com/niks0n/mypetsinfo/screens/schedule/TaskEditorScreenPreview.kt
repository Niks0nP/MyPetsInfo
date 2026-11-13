package com.niks0n.mypetsinfo.screens.schedule

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.niks0n.mypetsinfo.data.CareTaskEntity
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme

@Preview(
    name = "Редактор дела",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    locale = "ru",
)
@Composable
private fun TaskEditorScreenPreview() {
    MyPetsInfoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            TaskEditorScreen(
                task = CareTaskEntity(
                    id = 1,
                    petId = 1,
                    title = "Покормить",
                    description = "Влажный корм, половина пачки",
                    hour = 9,
                    minute = 0,
                ),
                petId = 1,
                isSaving = false,
                onSave = {},
                onDelete = {},
            )
        }
    }
}
