package com.niks0n.mypetsinfo.screens.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.niks0n.mypetsinfo.AppState
import com.niks0n.mypetsinfo.data.PetEntity
import com.niks0n.mypetsinfo.data.PetSex
import com.niks0n.mypetsinfo.data.WeightMeasurementEntity
import kotlinx.datetime.LocalDate
import mypetsinfo.shared.generated.resources.Res
import mypetsinfo.shared.generated.resources.calendar_icon
import mypetsinfo.shared.generated.resources.edit_icon
import mypetsinfo.shared.generated.resources.gender_female_icon
import mypetsinfo.shared.generated.resources.gender_male_icon
import mypetsinfo.shared.generated.resources.gender_unknown_icon
import mypetsinfo.shared.generated.resources.health_icon
import mypetsinfo.shared.generated.resources.palette_icon
import mypetsinfo.shared.generated.resources.pet_about_empty
import mypetsinfo.shared.generated.resources.pet_age_label
import mypetsinfo.shared.generated.resources.pet_color_label
import mypetsinfo.shared.generated.resources.pet_default_name
import mypetsinfo.shared.generated.resources.pet_edit_action
import mypetsinfo.shared.generated.resources.pet_health_count_short
import mypetsinfo.shared.generated.resources.pet_health_label_short
import mypetsinfo.shared.generated.resources.pet_health_status_empty
import mypetsinfo.shared.generated.resources.pet_profile_complete
import mypetsinfo.shared.generated.resources.pet_profile_incomplete
import mypetsinfo.shared.generated.resources.pet_sex_female
import mypetsinfo.shared.generated.resources.pet_sex_male
import mypetsinfo.shared.generated.resources.pet_sex_unknown
import mypetsinfo.shared.generated.resources.pet_unknown_value
import mypetsinfo.shared.generated.resources.pet_weight_kg
import mypetsinfo.shared.generated.resources.pet_weight_label_short
import mypetsinfo.shared.generated.resources.scale_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PetScreen(
    state: AppState,
    onEdit: () -> Unit,
    onWeightClick: () -> Unit,
    photoEditingEnabled: Boolean,
    onAvatarPhotoClick: () -> Unit,
    onCoverPhotoClick: () -> Unit,
) {
    PetScreenContent(
        pet = state.pet,
        today = state.today,
        latestWeight = state.latestWeight,
        healthRecordCount = state.healthRecordCount,
        onEdit = onEdit,
        onWeightClick = onWeightClick,
        photoEditingEnabled = photoEditingEnabled,
        onAvatarPhotoClick = onAvatarPhotoClick,
        onCoverPhotoClick = onCoverPhotoClick,
    )
}

@Composable
internal fun PetScreenContent(
    pet: PetEntity,
    today: LocalDate,
    latestWeight: WeightMeasurementEntity?,
    healthRecordCount: Int,
    onEdit: () -> Unit,
    onWeightClick: () -> Unit,
    photoEditingEnabled: Boolean,
    onAvatarPhotoClick: () -> Unit,
    onCoverPhotoClick: () -> Unit,
) {
    val unknown = stringResource(Res.string.pet_unknown_value)
    val age = pet.birthDate?.let { calculateAge(it, today) }
    val ageText = age?.let { localizedAge(it) }
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
    val weightValue = latestWeight?.let {
        stringResource(Res.string.pet_weight_kg, formatKilograms(it.weightGrams))
    } ?: unknown
    val healthValue = if (healthRecordCount == 0) {
        stringResource(Res.string.pet_health_status_empty)
    } else {
        stringResource(Res.string.pet_health_count_short, healthRecordCount)
    }
    val profileValue = if (pet.birthDate == null) {
        stringResource(Res.string.pet_profile_incomplete)
    } else {
        stringResource(Res.string.pet_profile_complete)
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        item(key = "profile_hero", contentType = "profile_hero") {
            ProfileHero(
                name = pet.name.ifBlank { stringResource(Res.string.pet_default_name) },
                breed = breed,
                sex = sex,
                sexIcon = sexIcon,
                age = ageText,
                avatarAssetId = pet.avatarAssetId,
                coverAssetId = pet.coverAssetId,
                photoEditingEnabled = photoEditingEnabled,
                onAvatarPhotoClick = onAvatarPhotoClick,
                onCoverPhotoClick = onCoverPhotoClick,
            )
        }
        item(key = "profile_facts", contentType = "profile_facts") {
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
                    value = weightValue,
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
        item(key = "edit_profile", contentType = "primary_action") {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
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
        item(key = "about", contentType = "profile_card") {
            AboutCard(
                body = pet.description?.takeIf { it.isNotBlank() }
                    ?: stringResource(Res.string.pet_about_empty),
            )
        }
        item(key = "health_summary", contentType = "profile_card") {
            HealthSummaryStrip(
                healthValue = healthValue,
                weightValue = weightValue,
                profileValue = profileValue,
                onWeightClick = onWeightClick,
            )
        }
        item(key = "photo_gallery", contentType = "photo_gallery") {
            PhotoGalleryCard()
        }
    }
}
