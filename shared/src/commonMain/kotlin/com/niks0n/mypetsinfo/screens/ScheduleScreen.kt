package com.niks0n.mypetsinfo.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.niks0n.mypetsinfo.AppState
import com.niks0n.mypetsinfo.data.CareTaskEntity
import com.niks0n.mypetsinfo.formatFullDate
import com.niks0n.mypetsinfo.formatTime
import com.niks0n.mypetsinfo.monthNameResources
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleScreen(
    state: AppState,
    onEditTask: (CareTaskEntity) -> Unit,
) {
    ScheduleScreenContent(
        today = state.today,
        tasksForDate = state::tasksFor,
        isCompleted = state::isCompleted,
        onEditTask = onEditTask,
    )
}

@Composable
private fun ScheduleScreenContent(
    today: LocalDate,
    tasksForDate: (LocalDate) -> List<CareTaskEntity>,
    isCompleted: (taskId: Long, date: LocalDate) -> Boolean,
    onEditTask: (CareTaskEntity) -> Unit,
) {
    val dates = remember(today) {
        (0..6).map { offset -> today.plus(offset, DateTimeUnit.DAY) }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(dates, key = { it.toString() }) { date ->
            val tasks = tasksForDate(date)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (date == today) {
                        "Сегодня, ${date.day} ${stringResource(monthNameResources[date.month.ordinal])}"
                    } else {
                        formatFullDate(date)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (tasks.isEmpty()) {
                    Text(
                        text = "Дел нет",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    tasks.forEach { task ->
                        CompactScheduleTask(
                            task = task,
                            completed = isCompleted(task.id, date),
                            onClick = { onEditTask(task) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Расписание",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    locale = "ru",
)
@Composable
private fun ScheduleScreenPreview() {
    val tasks = listOf(
        CareTaskEntity(
            id = 1,
            title = "Покормить",
            hour = 9,
            minute = 0,
        ),
        CareTaskEntity(
            id = 2,
            title = "Расчесать шерсть",
            hour = 18,
            minute = 30,
            daysMask = 0b0101010,
        ),
        CareTaskEntity(
            id = 3,
            title = "Почистить лоток",
            hour = 20,
            minute = 0,
            daysMask = 0b1100000,
        ),
    )
    val today = LocalDate(2026, 8, 8)

    MyPetsInfoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ScheduleScreenContent(
                today = today,
                tasksForDate = { date ->
                    tasks.filter { task ->
                        task.daysMask and (1 shl date.dayOfWeek.ordinal) != 0
                    }
                },
                isCompleted = { taskId, date -> taskId == 1L && date == today },
                onEditTask = {},
            )
        }
    }
}

@Composable
private fun CompactScheduleTask(
    task: CareTaskEntity,
    completed: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = formatTime(task.hour, task.minute),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(64.dp),
        )
        Text(
            text = task.title,
            textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f),
        )
        if (completed) {
            Text("Готово", color = MaterialTheme.colorScheme.primary)
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
