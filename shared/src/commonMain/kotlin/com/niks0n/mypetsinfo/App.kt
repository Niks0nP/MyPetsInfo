package com.niks0n.mypetsinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.RoomDatabase
import com.niks0n.mypetsinfo.data.ALL_DAYS_MASK
import com.niks0n.mypetsinfo.data.AppDatabase
import com.niks0n.mypetsinfo.data.CareTaskEntity
import com.niks0n.mypetsinfo.data.PetEntity
import com.niks0n.mypetsinfo.data.PetRepository
import com.niks0n.mypetsinfo.data.TaskCompletionEntity
import com.niks0n.mypetsinfo.data.buildDatabase
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import mypetsinfo.shared.generated.resources.Res
import mypetsinfo.shared.generated.resources.calendar_icon
import mypetsinfo.shared.generated.resources.change_case
import mypetsinfo.shared.generated.resources.day_friday
import mypetsinfo.shared.generated.resources.day_friday_short
import mypetsinfo.shared.generated.resources.day_monday
import mypetsinfo.shared.generated.resources.day_monday_short
import mypetsinfo.shared.generated.resources.day_saturday
import mypetsinfo.shared.generated.resources.day_saturday_short
import mypetsinfo.shared.generated.resources.day_sunday
import mypetsinfo.shared.generated.resources.day_sunday_short
import mypetsinfo.shared.generated.resources.day_thursday
import mypetsinfo.shared.generated.resources.day_thursday_short
import mypetsinfo.shared.generated.resources.day_tuesday
import mypetsinfo.shared.generated.resources.day_tuesday_short
import mypetsinfo.shared.generated.resources.day_wednesday
import mypetsinfo.shared.generated.resources.day_wednesday_short
import mypetsinfo.shared.generated.resources.house_icon
import mypetsinfo.shared.generated.resources.month_april
import mypetsinfo.shared.generated.resources.month_august
import mypetsinfo.shared.generated.resources.month_december
import mypetsinfo.shared.generated.resources.month_february
import mypetsinfo.shared.generated.resources.month_january
import mypetsinfo.shared.generated.resources.month_july
import mypetsinfo.shared.generated.resources.month_june
import mypetsinfo.shared.generated.resources.month_march
import mypetsinfo.shared.generated.resources.month_may
import mypetsinfo.shared.generated.resources.month_november
import mypetsinfo.shared.generated.resources.month_october
import mypetsinfo.shared.generated.resources.month_september
import mypetsinfo.shared.generated.resources.new_case
import mypetsinfo.shared.generated.resources.pet
import mypetsinfo.shared.generated.resources.pet_icon
import mypetsinfo.shared.generated.resources.schedule
import mypetsinfo.shared.generated.resources.today
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private enum class Screen {
    Today,
    Schedule,
    Pet,
    Editor,
}

private val weekDayShortNameResources = listOf(
    Res.string.day_monday_short,
    Res.string.day_tuesday_short,
    Res.string.day_wednesday_short,
    Res.string.day_thursday_short,
    Res.string.day_friday_short,
    Res.string.day_saturday_short,
    Res.string.day_sunday_short,
)
private val weekDayNameResources = listOf(
    Res.string.day_monday,
    Res.string.day_tuesday,
    Res.string.day_wednesday,
    Res.string.day_thursday,
    Res.string.day_friday,
    Res.string.day_saturday,
    Res.string.day_sunday,
)
private val monthNameResources = listOf(
    Res.string.month_january,
    Res.string.month_february,
    Res.string.month_march,
    Res.string.month_april,
    Res.string.month_may,
    Res.string.month_june,
    Res.string.month_july,
    Res.string.month_august,
    Res.string.month_september,
    Res.string.month_october,
    Res.string.month_november,
    Res.string.month_december,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    databaseBuilder: RoomDatabase.Builder<AppDatabase>,
    reminderScheduler: ReminderScheduler = NoOpReminderScheduler,
    requestNotificationPermission: () -> Unit = {},
) {

    val database = remember(databaseBuilder) { buildDatabase(databaseBuilder) }
    val repository = remember(database) { PetRepository(database.careDao()) }
    val scope = rememberCoroutineScope()
    val state = remember(repository, reminderScheduler) {
        AppState(repository, reminderScheduler, scope)
    }

    var screen by remember { mutableStateOf(Screen.Today) }
    var editedTask by remember { mutableStateOf<CareTaskEntity?>(null) }

    LaunchedEffect(state) {
        state.load()
    }

    MyPetsInfoTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (screen) {
                                Screen.Today -> stringResource(Res.string.today)
                                Screen.Schedule -> stringResource(Res.string.schedule)
                                Screen.Pet -> stringResource(Res.string.pet)
                                Screen.Editor -> stringResource(if (editedTask == null) Res.string.new_case else Res.string.change_case)
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        if (screen == Screen.Editor) {
                            TextButton(onClick = { screen = Screen.Today }) {
                                Text("Назад")
                            }
                        }
                    },
                )
            },
            bottomBar = {
                if (screen != Screen.Editor) {
                    AppNavigationBar(
                        selected = screen,
                        onSelect = { screen = it },
                    )
                }
            },
            floatingActionButton = {
                if (screen == Screen.Today || screen == Screen.Schedule) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            editedTask = null
                            screen = Screen.Editor
                        },
                        text = { Text("Добавить дело") },
                        icon = { Text("+", fontSize = 24.sp) },
                    )
                }
            },
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                when (screen) {
                    Screen.Today -> TodayScreen(
                        state = state,
                        onAddTask = {
                            editedTask = null
                            screen = Screen.Editor
                        },
                        onEditTask = {
                            editedTask = it
                            screen = Screen.Editor
                        },
                    )

                    Screen.Schedule -> ScheduleScreen(
                        state = state,
                        onEditTask = {
                            editedTask = it
                            screen = Screen.Editor
                        },
                    )

                    Screen.Pet -> PetScreen(state)

                    Screen.Editor -> TaskEditorScreen(
                        task = editedTask,
                        isSaving = state.isSaving,
                        onSave = { task ->
                            state.saveTask(task) {
                                requestNotificationPermission()
                                screen = Screen.Today
                            }
                        },
                        onDelete = editedTask?.let { task ->
                            {
                                state.deleteTask(task) {
                                    screen = Screen.Today
                                }
                            }
                        },
                    )
                }

                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                state.errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = state::clearError) {
                                Text("Закрыть")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppNavigationBar(
    selected: Screen,
    onSelect: (Screen) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == Screen.Today,
            onClick = { onSelect(Screen.Today) },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.house_icon),
                    contentDescription = stringResource(Res.string.today),
                )
            },
            label = { Text(stringResource(Res.string.today)) },
        )
        NavigationBarItem(
            selected = selected == Screen.Schedule,
            onClick = { onSelect(Screen.Schedule) },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.calendar_icon),
                    contentDescription = stringResource(Res.string.schedule)
                )
            },
            label = { Text(stringResource(Res.string.schedule)) },
        )
        NavigationBarItem(
            selected = selected == Screen.Pet,
            onClick = { onSelect(Screen.Pet) },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.pet_icon),
                    contentDescription = stringResource(Res.string.pet)
                )
            },
            label = { Text(stringResource(Res.string.pet)) },
        )
    }
}

@Composable
private fun TodayScreen(
    state: AppState,
    onAddTask: () -> Unit,
    onEditTask: (CareTaskEntity) -> Unit,
) {
    val today = state.today
    val tasks = state.tasksFor(today)
    val completedCount = tasks.count { state.isCompleted(it.id, today) }

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
                    completed = state.isCompleted(task.id, today),
                    onCompletedChange = { state.setCompleted(task.id, today, it) },
                    onEdit = { onEditTask(task) },
                )
            }
        } else if (!state.isLoading) {
            item {
                EmptyTodayCard(onAddTask)
            }
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
                modifier = Modifier
                    .size(64.dp),
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

@Composable
private fun ScheduleScreen(
    state: AppState,
    onEditTask: (CareTaskEntity) -> Unit,
) {
    val dates = remember(state.today) {
        (0..6).map { offset -> state.today.plus(offset, DateTimeUnit.DAY) }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(dates, key = { it.toString() }) { date ->
            val tasks = state.tasksFor(date)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (date == state.today) {
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
                            completed = state.isCompleted(task.id, date),
                            onClick = { onEditTask(task) },
                        )
                    }
                }
            }
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

@Composable
private fun TaskEditorScreen(
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

@Composable
private fun PetScreen(state: AppState) {
    var name by remember(state.pet) { mutableStateOf(state.pet.name) }
    var species by remember(state.pet) { mutableStateOf(state.pet.species) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                enabled = name.isNotBlank() && !state.isSaving,
                onClick = { state.savePet(name.trim(), species.trim()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Сохранить профиль")
            }
        }
    }
}

private class AppState(
    private val repository: PetRepository,
    private val reminderScheduler: ReminderScheduler,
    private val scope: CoroutineScope,
) {
    var tasks by mutableStateOf<List<CareTaskEntity>>(emptyList())
        private set
    var completions by mutableStateOf<Set<TaskCompletionEntity>>(emptySet())
        private set
    var pet by mutableStateOf(PetEntity(name = "Мой питомец", species = ""))
        private set
    var isLoading by mutableStateOf(true)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val today: LocalDate
        get() = currentDate()

    suspend fun load() {
        isLoading = true
        runCatching {
            refresh()
            reminderScheduler.reschedule(tasks)
        }.onFailure(::showError)
        isLoading = false
    }

    fun tasksFor(date: LocalDate): List<CareTaskEntity> = tasks.filter { task ->
        task.isEnabled && task.daysMask and (1 shl date.dayOfWeek.ordinal) != 0
    }

    fun isCompleted(taskId: Long, date: LocalDate): Boolean =
        TaskCompletionEntity(taskId, date.toString()) in completions

    fun setCompleted(taskId: Long, date: LocalDate, completed: Boolean) {
        scope.launch {
            runCatching {
                repository.setCompleted(taskId, date.toString(), completed)
                refreshCompletions()
            }.onFailure(::showError)
        }
    }

    fun saveTask(task: CareTaskEntity, onSaved: () -> Unit) {
        scope.launch {
            isSaving = true
            runCatching {
                val saved = repository.saveTask(task)
                if (saved.isEnabled) reminderScheduler.schedule(saved) else reminderScheduler.cancel(
                    saved.id
                )
                refresh()
            }.onSuccess {
                onSaved()
            }.onFailure(::showError)
            isSaving = false
        }
    }

    fun deleteTask(task: CareTaskEntity, onDeleted: () -> Unit) {
        scope.launch {
            isSaving = true
            runCatching {
                repository.deleteTask(task.id)
                reminderScheduler.cancel(task.id)
                refresh()
            }.onSuccess {
                onDeleted()
            }.onFailure(::showError)
            isSaving = false
        }
    }

    fun savePet(name: String, species: String) {
        scope.launch {
            isSaving = true
            runCatching {
                val updated = PetEntity(name = name, species = species)
                repository.savePet(updated)
                pet = updated
            }.onFailure(::showError)
            isSaving = false
        }
    }

    fun clearError() {
        errorMessage = null
    }

    private suspend fun refresh() {
        tasks = repository.getTasks()
        pet = repository.getPet()
        refreshCompletions()
    }

    private suspend fun refreshCompletions() {
        val endDate = today.plus(6, DateTimeUnit.DAY)
        completions = repository.getCompletions(today.toString(), endDate.toString())
    }

    private fun showError(throwable: Throwable) {
        errorMessage = throwable.message ?: "Не удалось выполнить действие"
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

private fun formatTime(hour: Int, minute: Int): String =
    hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')

@Composable
private fun formatFullDate(date: LocalDate): String =
    "${stringResource(weekDayNameResources[date.dayOfWeek.ordinal])}, " +
            "${date.day} ${stringResource(monthNameResources[date.month.ordinal])}"

@OptIn(ExperimentalTime::class)
private fun currentDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
