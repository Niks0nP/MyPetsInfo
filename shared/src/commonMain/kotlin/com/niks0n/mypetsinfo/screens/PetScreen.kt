package com.niks0n.mypetsinfo.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niks0n.mypetsinfo.AppState
import com.niks0n.mypetsinfo.data.PetEntity
import com.niks0n.mypetsinfo.data.PetSex
import com.niks0n.mypetsinfo.data.WeightMeasurementEntity
import com.niks0n.mypetsinfo.ui.theme.MyPetsInfoTheme
import kotlinx.datetime.LocalDate
import mypetsinfo.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val Forest = Color(0xFF07513E)
private val ForestDark = Color(0xFF043C2E)
private val Cream = Color(0xFFF7F8F3)
private val SoftGreen = Color(0xFFF0F5EC)
private val Hairline = Color(0xFFDDE5DC)

@Composable
internal fun PetScreen(
    state: AppState,
    onEdit: () -> Unit,
    onWeightClick: () -> Unit,
) {
    PetScreenContent(
        pet = state.pet,
        today = state.today,
        latestWeight = state.latestWeight,
        healthRecordCount = state.healthRecordCount,
        onEdit = onEdit,
        onWeightClick = onWeightClick,
    )
}

@Composable
private fun PetScreenContent(
    pet: PetEntity,
    today: LocalDate,
    latestWeight: WeightMeasurementEntity?,
    healthRecordCount: Int,
    onEdit: () -> Unit,
    onWeightClick: () -> Unit,
) {
    val unknown = stringResource(Res.string.pet_unknown_value)
    val age = pet.birthDate?.let { calculateAge(it, today) }
    val breed = pet.breed?.takeIf { it.isNotBlank() }
        ?: pet.species.takeIf { it.isNotBlank() }
        ?: unknown
    val sex = when (pet.sexCode) {
        PetSex.MALE -> stringResource(Res.string.pet_sex_male)
        PetSex.FEMALE -> stringResource(Res.string.pet_sex_female)
        else -> stringResource(Res.string.pet_sex_unknown)
    }
    val sexIcon = painterResource(
        when (pet.sexCode) {
            PetSex.MALE -> Res.drawable.gender_male_icon
            PetSex.FEMALE -> Res.drawable.gender_female_icon
            else -> Res.drawable.gender_unknown_icon
        },
    )
    val ageText = age?.let { localizedAge(it) }
    val healthValue = if (healthRecordCount == 0) {
        stringResource(Res.string.pet_health_status_empty)
    } else {
        stringResource(Res.string.pet_health_count_short, healthRecordCount)
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        item {
            ProfileHero(
                name = pet.name.ifBlank { stringResource(Res.string.pet_default_name) },
                breed = breed,
                sex = sex,
                sexIcon = sexIcon,
                age = ageText,
                onEdit = onEdit,
            )
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                ProfileFactCard(
                    icon = painterResource(Res.drawable.calendar_icon),
                    value = ageText ?: unknown,
                    label = stringResource(Res.string.pet_age_label),
                    modifier = Modifier.weight(1f),
                )
                ProfileFactCard(
                    icon = painterResource(Res.drawable.scale_icon),
                    value = latestWeight?.let {
                        stringResource(Res.string.pet_weight_kg, formatKilograms(it.weightGrams))
                    } ?: unknown,
                    label = stringResource(Res.string.pet_weight_label_short),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onWeightClick),
                )
                ProfileFactCard(
                    icon = painterResource(Res.drawable.palette_icon),
                    value = pet.color?.takeIf { it.isNotBlank() } ?: unknown,
                    label = stringResource(Res.string.pet_color_label),
                    modifier = Modifier.weight(1f),
                )
                ProfileFactCard(
                    icon = painterResource(Res.drawable.health_icon),
                    value = healthValue,
                    label = stringResource(Res.string.pet_health_label_short),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                contentPadding = PaddingValues(horizontal = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 62.dp)
                    .height(44.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.edit_icon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.pet_edit_action),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        item {
            AboutCard(
                body = pet.description?.takeIf { it.isNotBlank() }
                    ?: stringResource(Res.string.pet_about_empty),
            )
        }
        item {
            HealthSummaryStrip(
                healthValue = healthValue,
                weightValue = latestWeight?.let {
                    stringResource(Res.string.pet_weight_kg, formatKilograms(it.weightGrams))
                } ?: unknown,
                profileValue = if (pet.birthDate == null) {
                    stringResource(Res.string.pet_profile_incomplete)
                } else {
                    stringResource(Res.string.pet_profile_complete)
                },
                onWeightClick = onWeightClick,
            )
        }
        item { PhotoGalleryCard() }
    }
}

@Composable
private fun ProfileHero(
    name: String,
    breed: String,
    sex: String,
    sexIcon: Painter,
    age: String?,
    onEdit: () -> Unit,
) {
    val coverDescription = stringResource(Res.string.pet_cover_description)
    val avatarDescription = stringResource(Res.string.pet_avatar_description)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(194.dp)
                .semantics { contentDescription = coverDescription }
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF87A454), Forest, ForestDark),
                    ),
                ),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-28).dp, y = 8.dp)
                    .size(150.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 28.dp, y = (-20).dp)
                    .size(132.dp)
                    .background(Color(0xFFE6C45D).copy(alpha = 0.18f), CircleShape),
            )
            Icon(
                painter = painterResource(Res.drawable.pet_icon),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.22f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 18.dp)
                    .size(92.dp),
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
                .align(Alignment.BottomCenter),
        ) {}

        Surface(
            shape = CircleShape,
            color = Color(0xFFEAF1E5),
            shadowElevation = 5.dp,
            border = BorderStroke(4.dp, MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 132.dp)
                .size(100.dp)
                .semantics { contentDescription = avatarDescription },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(
                    Brush.radialGradient(listOf(Color(0xFFF1DEAA), Color(0xFF8BA36D))),
                ),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.pet_icon),
                    contentDescription = null,
                    tint = Forest,
                    modifier = Modifier.size(54.dp),
                )
            }
        }

        Surface(
            color = Forest,
            shape = CircleShape,
            shadowElevation = 3.dp,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 39.dp, y = 205.dp)
                .size(30.dp)
                .clickable(onClick = onEdit),
        ) {
            Icon(
                painter = painterResource(Res.drawable.camera_icon),
                contentDescription = stringResource(Res.string.pet_edit_photo),
                tint = Color.White,
                modifier = Modifier.padding(7.dp),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(start = 16.dp, top = 240.dp, end = 16.dp),
        ) {
            Text(
                text = name,
                fontSize = 34.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                color = ForestDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = breed,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Forest,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = sexIcon,
                    contentDescription = null,
                    tint = Forest,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = sex,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (age != null) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 9.dp),
                    )
                    Text(
                        text = age,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileFactCard(
    icon: Painter,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Cream),
        border = BorderStroke(1.dp, Hairline),
        modifier = modifier.height(78.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 3.dp, vertical = 6.dp),
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Forest,
                modifier = Modifier.size(19.dp),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AboutCard(body: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Hairline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = SoftGreen,
                    shape = CircleShape,
                    modifier = Modifier.size(30.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.pet_icon),
                        contentDescription = null,
                        tint = Forest,
                        modifier = Modifier.padding(7.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.pet_about_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(Res.string.pet_read_more),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Forest,
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                modifier = Modifier.padding(start = 38.dp, top = 5.dp),
            )
        }
    }
}

@Composable
private fun HealthSummaryStrip(
    healthValue: String,
    weightValue: String,
    profileValue: String,
    onWeightClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SoftGreen),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
        ) {
            SummaryItem(
                icon = painterResource(Res.drawable.health_icon),
                title = stringResource(Res.string.pet_health_label_short),
                value = healthValue,
                modifier = Modifier.weight(1f),
            )
            VerticalHairline()
            SummaryItem(
                icon = painterResource(Res.drawable.scale_icon),
                title = stringResource(Res.string.pet_weight_label_short),
                value = weightValue,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onWeightClick),
            )
            VerticalHairline()
            SummaryItem(
                icon = painterResource(Res.drawable.shield_icon),
                title = stringResource(Res.string.pet_profile_label),
                value = profileValue,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryItem(
    icon: Painter,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 7.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = Forest,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(5.dp))
        Column {
            Text(text = title, fontSize = 10.sp, lineHeight = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = value,
                fontSize = 8.sp,
                lineHeight = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun VerticalHairline() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp)
            .background(Hairline),
    )
}

@Composable
private fun PhotoGalleryCard() {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(Res.drawable.photo_icon),
                contentDescription = null,
                tint = Forest,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = stringResource(Res.string.pet_gallery_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(Res.string.pet_view_all),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Forest,
            )
        }
        Spacer(Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf(
                listOf(Color(0xFF9CAF72), Color(0xFFE1C377)),
                listOf(Color(0xFFD5B06A), Color(0xFF6F895E)),
                listOf(Color(0xFF829D7A), Color(0xFFB9D3D7)),
            ).forEach { colors ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.2f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(colors)),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.pet_icon),
                        contentDescription = stringResource(Res.string.pet_gallery_placeholder),
                        tint = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
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

@Composable
private fun localizedAge(age: Int): String {
    return pluralStringResource(Res.plurals.pet_age, age, age)
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
    MyPetsInfoTheme(darkTheme = false) {
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
                today = LocalDate(2026, 11, 10),
                latestWeight = WeightMeasurementEntity(
                    petId = 1,
                    measuredOn = "2026-11-01",
                    weightGrams = 28_400,
                ),
                healthRecordCount = 0,
                onEdit = {},
                onWeightClick = {},
            )
        }
    }
}
