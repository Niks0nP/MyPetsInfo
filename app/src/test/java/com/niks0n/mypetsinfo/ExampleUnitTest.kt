package com.niks0n.mypetsinfo

import com.niks0n.mypetsinfo.notifications.AndroidReminderScheduler
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderScheduleTest {
    @Test
    fun dailyReminderAfterTodaysTimeMovesToTomorrow() {
        val now = ZonedDateTime.of(2026, 8, 5, 10, 0, 0, 0, ZoneId.of("Europe/Moscow"))

        val next = AndroidReminderScheduler.nextTrigger(
            hour = 9,
            minute = 0,
            daysMask = 0b1111111,
            now = now,
        )

        assertEquals(ZonedDateTime.of(2026, 8, 6, 9, 0, 0, 0, now.zone), next)
    }

    @Test
    fun weeklyReminderUsesSelectedDay() {
        val now = ZonedDateTime.of(2026, 8, 5, 8, 0, 0, 0, ZoneId.of("Europe/Moscow"))

        val next = AndroidReminderScheduler.nextTrigger(
            hour = 9,
            minute = 0,
            daysMask = 1 shl 0,
            now = now,
        )

        assertEquals(ZonedDateTime.of(2026, 8, 10, 9, 0, 0, 0, now.zone), next)
    }
}
