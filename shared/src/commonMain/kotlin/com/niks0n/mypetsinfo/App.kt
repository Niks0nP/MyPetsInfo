package com.niks0n.mypetsinfo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import com.niks0n.mypetsinfo.data.CareTaskEntity
import com.niks0n.mypetsinfo.data.PetRepository
import com.niks0n.mypetsinfo.screens.NewsScreen
import com.niks0n.mypetsinfo.screens.PetScreen
import com.niks0n.mypetsinfo.screens.ScheduleScreen
import com.niks0n.mypetsinfo.screens.TaskEditorScreen
import com.niks0n.mypetsinfo.screens.TodayScreen
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme
import mypetsinfo.shared.generated.resources.Res
import mypetsinfo.shared.generated.resources.add_case
import mypetsinfo.shared.generated.resources.back
import mypetsinfo.shared.generated.resources.calendar_icon
import mypetsinfo.shared.generated.resources.change_case
import mypetsinfo.shared.generated.resources.house_icon
import mypetsinfo.shared.generated.resources.new_case
import mypetsinfo.shared.generated.resources.news
import mypetsinfo.shared.generated.resources.news_icon
import mypetsinfo.shared.generated.resources.pet
import mypetsinfo.shared.generated.resources.pet_icon
import mypetsinfo.shared.generated.resources.plus_icon
import mypetsinfo.shared.generated.resources.schedule
import mypetsinfo.shared.generated.resources.today
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private enum class Screen {
    Today,
    Schedule,
    News,
    Pet,
    Editor,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    repository: PetRepository = koinInject(),
    reminderScheduler: ReminderScheduler = NoOpReminderScheduler,
    requestNotificationPermission: () -> Unit = {},
) {
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
                                Screen.News -> stringResource(Res.string.news)
                                Screen.Pet -> stringResource(Res.string.pet)
                                Screen.Editor -> stringResource(
                                    if (editedTask == null) Res.string.new_case else Res.string.change_case,
                                )
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        if (screen == Screen.Editor) {
                            TextButton(onClick = { screen = Screen.Today }) {
                                Text(stringResource(Res.string.back))
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
                if (screen == Screen.Today) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            editedTask = null
                            screen = Screen.Editor
                        },
                        text = { Text(stringResource(Res.string.add_case)) },
                        icon = {
                            Icon(
                                painter = painterResource(Res.drawable.plus_icon),
                                contentDescription = null
                            )
                        },
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

                    Screen.News -> NewsScreen()

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
                    contentDescription = stringResource(Res.string.schedule),
                )
            },
            label = { Text(stringResource(Res.string.schedule)) },
        )
        NavigationBarItem(
            selected = selected == Screen.News,
            onClick = { onSelect(Screen.News) },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.news_icon),
                    contentDescription = stringResource(Res.string.news),
                )
            },
            label = { Text(stringResource(Res.string.news)) },
        )
        NavigationBarItem(
            selected = selected == Screen.Pet,
            onClick = { onSelect(Screen.Pet) },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.pet_icon),
                    contentDescription = stringResource(Res.string.pet),
                )
            },
            label = { Text(stringResource(Res.string.pet)) },
        )
    }
}
