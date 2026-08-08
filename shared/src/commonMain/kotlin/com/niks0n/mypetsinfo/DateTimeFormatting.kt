package com.niks0n.mypetsinfo

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import mypetsinfo.shared.generated.resources.Res
import mypetsinfo.shared.generated.resources.day_friday
import mypetsinfo.shared.generated.resources.day_monday
import mypetsinfo.shared.generated.resources.day_saturday
import mypetsinfo.shared.generated.resources.day_sunday
import mypetsinfo.shared.generated.resources.day_thursday
import mypetsinfo.shared.generated.resources.day_tuesday
import mypetsinfo.shared.generated.resources.day_wednesday
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
import org.jetbrains.compose.resources.stringResource

private val weekDayNameResources = listOf(
    Res.string.day_monday,
    Res.string.day_tuesday,
    Res.string.day_wednesday,
    Res.string.day_thursday,
    Res.string.day_friday,
    Res.string.day_saturday,
    Res.string.day_sunday,
)

internal val monthNameResources = listOf(
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

internal fun formatTime(hour: Int, minute: Int): String =
    hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')

@Composable
internal fun formatFullDate(date: LocalDate): String =
    "${stringResource(weekDayNameResources[date.dayOfWeek.ordinal])}, " +
        "${date.day} ${stringResource(monthNameResources[date.month.ordinal])}"
