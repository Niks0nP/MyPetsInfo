package com.niks0n.mypetsinfo.screens.pet

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import mypetsinfo.shared.generated.resources.Res
import mypetsinfo.shared.generated.resources.pet_age
import org.jetbrains.compose.resources.pluralStringResource

internal fun calculateAge(birthDate: String, today: LocalDate): Int? {
    val birth = runCatching { LocalDate.parse(birthDate) }.getOrNull() ?: return null
    if (birth > today) return null

    var years = today.year - birth.year
    if (today.month < birth.month ||
        (today.month == birth.month && today.day < birth.day)
    ) {
        years -= 1
    }
    return years
}

internal fun formatKilograms(grams: Long): String {
    val whole = grams / 1000
    val tenths = (grams % 1000) / 100
    return if (tenths == 0L) whole.toString() else "$whole.$tenths"
}

@Composable
internal fun localizedAge(age: Int): String {
    return pluralStringResource(Res.plurals.pet_age, age, age)
}
