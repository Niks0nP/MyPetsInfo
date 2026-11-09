package com.niks0n.mypetsinfo.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.niks0n.mypetsinfo.AppState
import com.niks0n.mypetsinfo.data.PetEntity
import com.niks0n.mypetsinfo.data.PetSex
import com.niks0n.mypetsinfo.data.WeightMeasurementEntity
import com.niks0n.mypetsinfo.ui.theme.AppSpacing
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme
import kotlinx.datetime.LocalDate
import mypetsinfo.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PetScreen(
    state: AppState,
    onEdit: () -> Unit,
) {
    PetScreenContent(
        pet = state.pet,
        today = state.today,
        latestWeight = state.latestWeight,
        healthRecordCount = state.healthRecordCount,
        onEdit = onEdit,
    )
}

@Composable
private fun PetScreenContent(
    pet: PetEntity,
    today: LocalDate,
    latestWeight: WeightMeasurementEntity?,
    healthRecordCount: Int,
    onEdit: () -> Unit,
) {
    val unknown = stringResource(Res.string.pet_unknown_value)
    val age = pet.birthDate?.let { calculateAge(it, today) }
    val speciesAndBreed = listOfNotNull(
        pet.species.ifBlank { null },
        pet.breed?.takeIf { it.isNotBlank() },
    ).joinToString(" · ").ifBlank { unknown }
    val sex = when (pet.sexCode) {
        PetSex.MALE -> stringResource(Res.string.pet_sex_male)
        PetSex.FEMALE -> stringResource(Res.string.pet_sex_female)
        else -> stringResource(Res.string.pet_sex_unknown)
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            ProfileHero(
                name = pet.name.ifBlank { stringResource(Res.string.pet_default_name) },
                subtitle = speciesAndBreed,
                sex = sex,
            )
        }
        item {
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md)
                    .height(52.dp),
            ) {
                Text(stringResource(Res.string.pet_edit_action))
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                modifier = Modifier.padding(horizontal = AppSpacing.md),
            ) {
                ProfileFactCard(
                    value = age?.let { stringResource(Res.string.pet_age_years, it) } ?: unknown,
                    label = stringResource(Res.string.pet_age_label),
                    modifier = Modifier.weight(1f),
                )
                ProfileFactCard(
                    value = latestWeight?.let { stringResource(Res.string.pet_weight_kg, formatKilograms(it.weightGrams)) }
                        ?: unknown,
                    label = stringResource(Res.string.pet_weight_label),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                modifier = Modifier.padding(horizontal = AppSpacing.md),
            ) {
                ProfileFactCard(
                    value = pet.color?.takeIf { it.isNotBlank() } ?: unknown,
                    label = stringResource(Res.string.pet_color_label),
                    modifier = Modifier.weight(1f),
                )
                ProfileFactCard(
                    value = if (healthRecordCount == 0) unknown else {
                        stringResource(Res.string.pet_health_count, healthRecordCount)
                    },
                    label = stringResource(Res.string.pet_health_label),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            ProfileSectionCard(
                title = stringResource(Res.string.pet_about_title),
                body = pet.description?.takeIf { it.isNotBlank() }
                    ?: stringResource(Res.string.pet_about_empty),
            )
        }
        item {
            SummaryCard(
                title = stringResource(Res.string.pet_weight_title),
                value = latestWeight?.let {
                    stringResource(Res.string.pet_weight_kg, formatKilograms(it.weightGrams))
                } ?: stringResource(Res.string.pet_weight_empty),
                supportingText = latestWeight?.measuredOn
                    ?: stringResource(Res.string.pet_weight_empty_hint),
            )
        }
        item {
            SummaryCard(
                title = stringResource(Res.string.pet_health_title),
                value = if (healthRecordCount == 0) {
                    stringResource(Res.string.pet_health_empty)
                } else {
                    stringResource(Res.string.pet_health_count, healthRecordCount)
                },
                supportingText = if (healthRecordCount == 0) {
                    stringResource(Res.string.pet_health_empty_hint)
                } else {
                    stringResource(Res.string.pet_health_label)
                },
            )
        }
    }
}

@Composable
private fun ProfileHero(
    name: String,
    subtitle: String,
    sex: String,
) {
    val coverDescription = stringResource(Res.string.pet_cover_description)
    val avatarDescription = stringResource(Res.string.pet_avatar_description)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
                .semantics { contentDescription = coverDescription }
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiaryContainer,
                        ),
                    ),
                ),
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(
                5.dp,
                MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 108.dp)
                .size(96.dp)
                .semantics { contentDescription = avatarDescription },
        ) {
            Icon(
                painter = painterResource(Res.drawable.pet_icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(25.dp),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = AppSpacing.md),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = sex,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileFactCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Spacer(Modifier.height(AppSpacing.xxs))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    body: String,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(AppSpacing.xs))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    supportingText: String,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.sm))
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun calculateAge(birthDate: String, today: LocalDate): Int? {
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

private fun formatKilograms(grams: Long): String {
    val whole = grams / 1000
    val tenths = (grams % 1000) / 100
    return if (tenths == 0L) whole.toString() else "$whole.$tenths"
}

@Preview(
    name = "Pet profile",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    locale = "ru",
)
@Composable
private fun PetScreenPreview() {
    MyPetsInfoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PetScreenContent(
                pet = PetEntity(
                    id = 1,
                    name = "Мило",
                    species = "Собака",
                    breed = "Золотистый ретривер",
                    sexCode = PetSex.MALE,
                    birthDate = "2024-08-19",
                    color = "Золотистый",
                    description = "Дружелюбный и энергичный пёс, который любит прогулки и игры.",
                ),
                today = LocalDate(2026, 11, 9),
                latestWeight = WeightMeasurementEntity(
                    petId = 1,
                    measuredOn = "2026-11-01",
                    weightGrams = 28_400,
                ),
                healthRecordCount = 0,
                onEdit = {},
            )
        }
    }
}
