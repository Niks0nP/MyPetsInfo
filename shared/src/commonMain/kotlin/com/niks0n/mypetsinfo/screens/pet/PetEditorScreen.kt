package com.niks0n.mypetsinfo.screens.pet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.niks0n.mypetsinfo.data.PetEntity
import com.niks0n.mypetsinfo.data.PetSex
import com.niks0n.mypetsinfo.ui.theme.AppSpacing
import kotlinx.datetime.LocalDate
import mypetsinfo.shared.generated.resources.Res
import mypetsinfo.shared.generated.resources.pet_birth_date_future
import mypetsinfo.shared.generated.resources.pet_birth_date_hint
import mypetsinfo.shared.generated.resources.pet_birth_date_invalid
import mypetsinfo.shared.generated.resources.pet_birth_date_label
import mypetsinfo.shared.generated.resources.pet_breed_label
import mypetsinfo.shared.generated.resources.pet_cancel_action
import mypetsinfo.shared.generated.resources.pet_color_label
import mypetsinfo.shared.generated.resources.pet_description_label
import mypetsinfo.shared.generated.resources.pet_discard_action
import mypetsinfo.shared.generated.resources.pet_discard_body
import mypetsinfo.shared.generated.resources.pet_discard_title
import mypetsinfo.shared.generated.resources.pet_keep_editing_action
import mypetsinfo.shared.generated.resources.pet_name_label
import mypetsinfo.shared.generated.resources.pet_name_required
import mypetsinfo.shared.generated.resources.pet_optional_hint
import mypetsinfo.shared.generated.resources.pet_profile_details_title
import mypetsinfo.shared.generated.resources.pet_save_action
import mypetsinfo.shared.generated.resources.pet_saving
import mypetsinfo.shared.generated.resources.pet_sex_female
import mypetsinfo.shared.generated.resources.pet_sex_label
import mypetsinfo.shared.generated.resources.pet_sex_male
import mypetsinfo.shared.generated.resources.pet_sex_unknown
import mypetsinfo.shared.generated.resources.pet_species_hint
import mypetsinfo.shared.generated.resources.pet_species_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PetEditorScreen(
    pet: PetEntity,
    today: LocalDate,
    isSaving: Boolean,
    onSave: (PetEntity) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember(pet.id) { mutableStateOf(pet.name) }
    var species by remember(pet.id) { mutableStateOf(pet.species) }
    var breed by remember(pet.id) { mutableStateOf(pet.breed.orEmpty()) }
    var sexCode by remember(pet.id) { mutableStateOf(pet.sexCode) }
    var birthDate by remember(pet.id) { mutableStateOf(pet.birthDate.orEmpty()) }
    var color by remember(pet.id) { mutableStateOf(pet.color.orEmpty()) }
    var description by remember(pet.id) { mutableStateOf(pet.description.orEmpty()) }
    var validationMessage by remember(pet.id) { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember(pet.id) { mutableStateOf(false) }

    val nameRequiredMessage = stringResource(Res.string.pet_name_required)
    val invalidDateMessage = stringResource(Res.string.pet_birth_date_invalid)
    val futureDateMessage = stringResource(Res.string.pet_birth_date_future)

    fun draft(): PetEntity = pet.copy(
        name = name.trim(),
        species = species.trim(),
        breed = breed.trim().ifEmpty { null },
        sexCode = sexCode,
        birthDate = birthDate.trim().ifEmpty { null },
        color = color.trim().ifEmpty { null },
        description = description.trim().ifEmpty { null },
    )

    fun requestCancel() {
        if (draft() == pet) onCancel() else showDiscardDialog = true
    }

    fun saveDraft() {
        val parsedDate = birthDate.trim()
            .takeIf { it.isNotEmpty() }
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        validationMessage = when {
            name.isBlank() -> nameRequiredMessage
            birthDate.isNotBlank() && parsedDate == null -> invalidDateMessage
            parsedDate != null && parsedDate > today -> futureDateMessage
            else -> null
        }
        if (validationMessage == null) onSave(draft())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = AppSpacing.md,
                top = AppSpacing.md,
                end = AppSpacing.md,
                bottom = 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            modifier = Modifier.fillMaxSize(),
        ) {
        item {
            Text(
                text = stringResource(Res.string.pet_profile_details_title),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 60) name = it },
                label = { Text(stringResource(Res.string.pet_name_label)) },
                singleLine = true,
                isError = validationMessage == nameRequiredMessage,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = species,
                onValueChange = { if (it.length <= 60) species = it },
                label = { Text(stringResource(Res.string.pet_species_label)) },
                placeholder = { Text(stringResource(Res.string.pet_species_hint)) },
                supportingText = { Text(stringResource(Res.string.pet_optional_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = breed,
                onValueChange = { if (it.length <= 80) breed = it },
                label = { Text(stringResource(Res.string.pet_breed_label)) },
                supportingText = { Text(stringResource(Res.string.pet_optional_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Text(
                text = stringResource(Res.string.pet_sex_label),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                SexChip(
                    selected = sexCode == PetSex.UNKNOWN,
                    label = stringResource(Res.string.pet_sex_unknown),
                    onClick = { sexCode = PetSex.UNKNOWN },
                )
                SexChip(
                    selected = sexCode == PetSex.MALE,
                    label = stringResource(Res.string.pet_sex_male),
                    onClick = { sexCode = PetSex.MALE },
                )
                SexChip(
                    selected = sexCode == PetSex.FEMALE,
                    label = stringResource(Res.string.pet_sex_female),
                    onClick = { sexCode = PetSex.FEMALE },
                )
            }
        }
        item {
            OutlinedTextField(
                value = birthDate,
                onValueChange = { if (it.length <= 10) birthDate = it },
                label = { Text(stringResource(Res.string.pet_birth_date_label)) },
                placeholder = { Text(stringResource(Res.string.pet_birth_date_hint)) },
                supportingText = { Text(stringResource(Res.string.pet_optional_hint)) },
                singleLine = true,
                isError = validationMessage == invalidDateMessage || validationMessage == futureDateMessage,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = color,
                onValueChange = { if (it.length <= 80) color = it },
                label = { Text(stringResource(Res.string.pet_color_label)) },
                supportingText = { Text(stringResource(Res.string.pet_optional_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 500) description = it },
                label = { Text(stringResource(Res.string.pet_description_label)) },
                supportingText = { Text("${description.length}/500") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        validationMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        }

        Surface(
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                modifier = Modifier.padding(AppSpacing.md),
            ) {
                OutlinedButton(
                    enabled = !isSaving,
                    onClick = ::requestCancel,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(Res.string.pet_cancel_action))
                }
                Button(
                    enabled = !isSaving,
                    onClick = ::saveDraft,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        stringResource(
                            if (isSaving) Res.string.pet_saving else Res.string.pet_save_action,
                        ),
                    )
                }
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(Res.string.pet_discard_title)) },
            text = { Text(stringResource(Res.string.pet_discard_body)) },
            confirmButton = {
                TextButton(onClick = onCancel) {
                    Text(stringResource(Res.string.pet_discard_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(Res.string.pet_keep_editing_action))
                }
            },
        )
    }
}

@Composable
private fun SexChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}
