package com.niks0n.mypetsinfo.screens.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.niks0n.mypetsinfo.data.WeightMeasurementEntity
import com.niks0n.mypetsinfo.ui.theme.AppSpacing
import kotlinx.datetime.LocalDate
import mypetsinfo.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToLong

@Composable
internal fun WeightHistoryScreen(
    measurements: List<WeightMeasurementEntity>,
    onAdd: () -> Unit,
    onEdit: (WeightMeasurementEntity) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.weight_add_action))
            }
        }

        if (measurements.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        modifier = Modifier.padding(AppSpacing.lg),
                    ) {
                        Text(
                            text = stringResource(Res.string.weight_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(Res.string.weight_empty_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(
                items = measurements,
                key = { it.id },
            ) { measurement ->
                Card(
                    onClick = { onEdit(measurement) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(AppSpacing.md),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(
                                    Res.string.pet_weight_kg,
                                    formatWeightKilograms(measurement.weightGrams),
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(
                                    Res.string.weight_saved_on,
                                    measurement.measuredOn,
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = measurement.note?.takeIf { it.isNotBlank() }
                                ?: stringResource(Res.string.weight_note_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun WeightEditorScreen(
    petId: Long,
    measurement: WeightMeasurementEntity?,
    today: LocalDate,
    isSaving: Boolean,
    onSave: (WeightMeasurementEntity) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit,
) {
    val initialDate = measurement?.measuredOn ?: today.toString()
    val initialWeight = measurement?.let { formatWeightKilograms(it.weightGrams) }.orEmpty()
    val initialNote = measurement?.note.orEmpty()

    var measuredOn by remember(measurement?.id) { mutableStateOf(initialDate) }
    var weight by remember(measurement?.id) { mutableStateOf(initialWeight) }
    var note by remember(measurement?.id) { mutableStateOf(initialNote) }
    var validationMessage by remember(measurement?.id) { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember(measurement?.id) { mutableStateOf(false) }

    val requiredError = stringResource(Res.string.weight_required_error)
    val invalidWeightError = stringResource(Res.string.weight_invalid_error)
    val invalidDateError = stringResource(Res.string.pet_birth_date_invalid)
    val futureDateError = stringResource(Res.string.pet_birth_date_future)
    val hasChanges = measuredOn != initialDate || weight != initialWeight || note != initialNote

    fun requestCancel() {
        if (hasChanges) showDiscardDialog = true else onCancel()
    }

    fun saveDraft() {
        val parsedDate = runCatching { LocalDate.parse(measuredOn.trim()) }.getOrNull()
        val kilograms = weight.trim().replace(',', '.').toDoubleOrNull()
        validationMessage = when {
            weight.isBlank() -> requiredError
            kilograms == null || kilograms !in 0.01..1000.0 -> invalidWeightError
            parsedDate == null -> invalidDateError
            parsedDate > today -> futureDateError
            else -> null
        }
        if (validationMessage == null && kilograms != null) {
            onSave(
                WeightMeasurementEntity(
                    id = measurement?.id ?: 0,
                    petId = petId,
                    measuredOn = measuredOn.trim(),
                    weightGrams = (kilograms * 1000).roundToLong(),
                    note = note.trim().ifEmpty { null },
                ),
            )
        }
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
                OutlinedTextField(
                    value = measuredOn,
                    onValueChange = { if (measurement == null && it.length <= 10) measuredOn = it },
                    label = { Text(stringResource(Res.string.weight_measurement_date)) },
                    placeholder = { Text(stringResource(Res.string.pet_birth_date_hint)) },
                    supportingText = {
                        Text(
                            stringResource(
                                if (measurement == null) {
                                    Res.string.weight_same_day_hint
                                } else {
                                    Res.string.weight_date_locked_hint
                                },
                            ),
                        )
                    },
                    enabled = measurement == null,
                    singleLine = true,
                    isError = validationMessage == invalidDateError || validationMessage == futureDateError,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { value ->
                        if (value.length <= 8 && value.all { it.isDigit() || it == '.' || it == ',' }) {
                            weight = value
                        }
                    },
                    label = { Text(stringResource(Res.string.weight_value_label)) },
                    placeholder = { Text(stringResource(Res.string.weight_value_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = validationMessage == requiredError || validationMessage == invalidWeightError,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 200) note = it },
                    label = { Text(stringResource(Res.string.weight_note_label)) },
                    supportingText = { Text("${note.length}/200") },
                    minLines = 3,
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
            if (onDelete != null) {
                item {
                    OutlinedButton(
                        enabled = !isSaving,
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(Res.string.weight_delete_action),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
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

internal fun formatWeightKilograms(grams: Long): String {
    val whole = grams / 1000
    val remainder = grams % 1000
    if (remainder == 0L) return whole.toString()
    return "$whole.${remainder.toString().padStart(3, '0').trimEnd('0')}"
}
