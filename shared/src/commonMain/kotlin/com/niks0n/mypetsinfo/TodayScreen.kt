package com.niks0n.mypetsinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niks0n.mypetsinfo.data.CareTaskEntity
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme
import kotlinx.datetime.LocalDate

@Composable
internal fun TodayScreen(
    state: AppState,
    onAddTask: () -> Unit,
    onEditTask: (CareTaskEntity) -> Unit,
) {
    val today = state.today
    val tasks = state.tasksFor(today)
    TodayScreenContent(
        today = today,
        tasks = tasks,
        completedTaskIds = tasks
            .filter { task -> state.isCompleted(task.id, today) }
            .map { task -> task.id }
            .toSet(),
        isLoading = state.isLoading,
        onCompletedChange = { task, completed ->
            state.setCompleted(task.id, today, completed)
        },
        onAddTask = onAddTask,
        onEditTask = onEditTask,
    )
}

@Composable
private fun TodayScreenContent(
    today: LocalDate,
    tasks: List<CareTaskEntity>,
    completedTaskIds: Set<Long>,
    isLoading: Boolean,
    onCompletedChange: (task: CareTaskEntity, completed: Boolean) -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (CareTaskEntity) -> Unit,
) {
    val completedCount = tasks.count { it.id in completedTaskIds }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text(
                text = formatFullDate(today),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (tasks.isNotEmpty()) {
            item {
                ProgressCard(completed = completedCount, total = tasks.size)
            }
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    completed = task.id in completedTaskIds,
                    onCompletedChange = { onCompletedChange(task, it) },
                    onEdit = { onEditTask(task) },
                )
            }
        } else if (!isLoading) {
            item {
                EmptyTodayCard(onAddTask)
            }
        }
    }
}

@Preview(
    name = "Сегодня — дела",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    locale = "ru",
)
@Composable
private fun TodayScreenPreview() {
    val tasks = listOf(
        CareTaskEntity(
            id = 1,
            title = "Покормить",
            description = "Влажный корм, половина пачки",
            hour = 9,
            minute = 0,
        ),
        CareTaskEntity(
            id = 2,
            title = "Поменять воду",
            hour = 12,
            minute = 30,
        ),
        CareTaskEntity(
            id = 3,
            title = "Поиграть",
            description = "Не меньше 15 минут",
            hour = 19,
            minute = 0,
        ),
    )

    MyPetsInfoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            TodayScreenContent(
                today = LocalDate(2026, 8, 8),
                tasks = tasks,
                completedTaskIds = setOf(1),
                isLoading = false,
                onCompletedChange = { _, _ -> },
                onAddTask = {},
                onEditTask = {},
            )
        }
    }
}

@Composable
private fun ProgressCard(completed: Int, total: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Забота на сегодня", fontWeight = FontWeight.SemiBold)
                Text("$completed из $total")
            }
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else completed.toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun EmptyTodayCard(onAddTask: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp),
            ) {
                Text("♡", fontSize = 42.sp, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = "Добавьте первое дело",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Например, «Покормить» каждый день в 09:00.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onAddTask) {
                Text("Создать дело")
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: CareTaskEntity,
    completed: Boolean,
    onCompletedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (completed) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            Checkbox(
                checked = completed,
                onCheckedChange = onCompletedChange,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Text(
                text = formatTime(task.hour, task.minute),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}
