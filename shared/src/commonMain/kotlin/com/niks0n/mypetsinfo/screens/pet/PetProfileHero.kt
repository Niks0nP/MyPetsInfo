package com.niks0n.mypetsinfo.screens.pet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niks0n.mypetsinfo.rememberStoredPhotoBitmap
import mypetsinfo.shared.generated.resources.Res
import mypetsinfo.shared.generated.resources.camera_icon
import mypetsinfo.shared.generated.resources.pet_avatar_description
import mypetsinfo.shared.generated.resources.pet_cover_description
import mypetsinfo.shared.generated.resources.pet_edit_cover_photo
import mypetsinfo.shared.generated.resources.pet_edit_photo
import mypetsinfo.shared.generated.resources.pet_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileHero(
    name: String,
    breed: String,
    sex: String,
    sexIcon: Painter,
    age: String?,
    avatarAssetId: String?,
    coverAssetId: String?,
    photoEditingEnabled: Boolean,
    onAvatarPhotoClick: () -> Unit,
    onCoverPhotoClick: () -> Unit,
) {
    val coverDescription = stringResource(Res.string.pet_cover_description)
    val avatarDescription = stringResource(Res.string.pet_avatar_description)
    val coverBitmap = rememberStoredPhotoBitmap(coverAssetId)
    val avatarBitmap = rememberStoredPhotoBitmap(avatarAssetId)
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp + statusBarPadding),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(194.dp + statusBarPadding)
                .semantics { contentDescription = coverDescription }
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF87A454), Color(0xFF07513E), Color(0xFF043C2E)),
                    ),
                ),
        ) {
            if (coverBitmap != null) {
                Image(
                    bitmap = coverBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarPadding + 16.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.38f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            if (photoEditingEnabled) {
                PhotoActionButton(
                    contentDescription = stringResource(Res.string.pet_edit_cover_photo),
                    onClick = onCoverPhotoClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = statusBarPadding + 14.dp, end = 14.dp),
                )
            }
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
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 5.dp,
            border = BorderStroke(4.dp, MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 132.dp + statusBarPadding)
                .size(100.dp)
                .semantics { contentDescription = avatarDescription },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.inversePrimary,
                        ),
                    ),
                ),
            ) {
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.pet_icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp),
                    )
                }
            }
        }

        if (photoEditingEnabled) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                shadowElevation = 3.dp,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 39.dp, y = 205.dp + statusBarPadding)
                    .size(30.dp)
                    .clickable(onClick = onAvatarPhotoClick),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.camera_icon),
                    contentDescription = stringResource(Res.string.pet_edit_photo),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(7.dp),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(start = 16.dp, top = 240.dp + statusBarPadding, end = 16.dp),
        ) {
            Text(
                text = name,
                fontSize = 34.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = breed,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
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
                    tint = MaterialTheme.colorScheme.primary,
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
private fun PhotoActionButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shape = CircleShape,
        shadowElevation = 3.dp,
        modifier = modifier
            .size(38.dp)
            .clickable(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(Res.drawable.camera_icon),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(9.dp),
        )
    }
}
