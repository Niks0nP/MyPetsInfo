package com.niks0n.mypetsinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.niks0n.mypetsinfo.data.PetEntity
import com.niks0n.mypetsinfo.ui.theme.AppSpacing
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme

@Composable
internal fun PetScreen(state: AppState) {
    PetScreenContent(
        pet = state.pet,
        isSaving = state.isSaving,
        onSave = state::savePet,
    )
}

@Composable
private fun PetScreenContent(
    pet: PetEntity,
    isSaving: Boolean,
    onSave: (name: String, species: String) -> Unit,
) {
    var name by remember(pet) { mutableStateOf(pet.name) }
    var species by remember(pet) { mutableStateOf(pet.species) }

    LazyColumn(
        contentPadding = PaddingValues(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(72.dp),
                    ) {
                        Text("♡", fontSize = 48.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = name.ifBlank { "Мой питомец" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = species.ifBlank { "Укажите вид питомца" },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Вид") },
                placeholder = { Text("Кошка, собака…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Button(
                enabled = name.isNotBlank() && !isSaving,
                onClick = { onSave(name.trim(), species.trim()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Сохранить профиль")
            }
        }
    }
}

@Preview(
    name = "Питомец",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    locale = "ru",
)
@Composable
private fun PetScreenPreview() {
    MyPetsInfoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PetScreenContent(
                pet = PetEntity(name = "Барсик", species = "Кот"),
                isSaving = false,
                onSave = { _, _ -> },
            )
        }
    }
}
