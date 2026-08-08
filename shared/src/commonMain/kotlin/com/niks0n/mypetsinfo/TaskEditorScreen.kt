package com.niks0n.mypetsinfo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.niks0n.mypetsinfo.data.ALL_DAYS_MASK
import com.niks0n.mypetsinfo.data.CareTaskEntity
import mypetsinfo.shared.generated.resources.Res
import mypetsinfo.shared.generated.resources.day_friday_short
import mypetsinfo.shared.generated.resources.day_monday_short
import mypetsinfo.shared.generated.resources.day_saturday_short
import mypetsinfo.shared.generated.resources.day_sunday_short
import mypetsinfo.shared.generated.resources.day_thursday_short
import mypetsinfo.shared.generated.resources.day_tuesday_short
import mypetsinfo.shared.generated.resources.day_wednesday_short
import org.jetbrains.compose.resources.stringResource

private val weekDayShortNameResources = listOf(
    Res.string.day_monday_short,
    Res.string.day_tuesday_short,
    Res.string.day_wednesday_short,
    Res.string.day_thursday_short,
    Res.string.day_friday_short,
    Res.string.day_saturday_short,
    Res.string.day_sunday_short,
)

@Composable
internal fun TaskEditorScreen(
    task: CareTaskEntity?,
    isSaving: Boolean,
    onSave: (CareTaskEntity) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var title by remember(task?.id) { mutableStateOf(task?.title.orEmpty()) }
    var description by remember(task?.id) { mutableStateOf(task?.description.orEmpty()) }
    var time by remember(task?.id) {
        mutableStateOf(task?.let { formatTime(it.hour, it.minute) } ?: "09:00")
    }
    var daysMask by remember(task?.id) { mutableStateOf(task?.daysMask ?: ALL_DAYS_MASK) }
    var enabled by remember(task?.id) { mutableStateOf(task?.isEnabled ?: true) }
    var validationMessage by remember(task?.id) { mutableStateOf<String?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                placeholder = { Text("Покормить") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание — необязательно") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = time,
                onValueChange = { if (it.length <= 5) time = it },
                label = { Text("Время") },
                supportingText = { Text("Формат 09:00") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Text(
                text = "Повторять",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                weekDayShortNameResources.forEachIndexed { index, nameResource ->
                    val bit = 1 shl index
                    FilterChip(
                        selected = daysMask and bit != 0,
                        onClick = {
                            daysMask = if (daysMask and bit != 0) {
                                daysMask and bit.inv()
                            } else {
                                daysMask or bit
                            }
                        },
                        label = { Text(stringResource(nameResource)) },
                    )
                }
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Напоминание", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = if (enabled) "Уведомить в выбранное время" else "Уведомление выключено",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
        }
        validationMessage?.let { message ->
            item {
                Text(message, color = MaterialTheme.colorScheme.error)
            }
        }
        item {
            Button(
                enabled = !isSaving,
                onClick = {
                    val parsedTime = parseTime(time)
                    validationMessage = when {
                        title.isBlank() -> "Введите название дела"
                        parsedTime == null -> "Введите корректное время в формате 09:00"
                        daysMask == 0 -> "Выберите хотя бы один день"
                        else -> null
                    }
                    if (validationMessage == null && parsedTime != null) {
                        onSave(
                            CareTaskEntity(
                                id = task?.id ?: 0,
                                title = title.trim(),
                                description = description.trim(),
                                hour = parsedTime.first,
                                minute = parsedTime.second,
                                daysMask = daysMask,
                                isEnabled = enabled,
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isSaving) "Сохраняем…" else "Сохранить")
            }
        }
        if (onDelete != null) {
            item {
                OutlinedButton(
                    enabled = !isSaving,
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Удалить дело", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun parseTime(value: String): Pair<Int, Int>? {
    val parts = value.trim().split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour to minute
}
