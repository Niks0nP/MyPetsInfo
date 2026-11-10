package com.niks0n.mypetsinfo.screens.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mypetsinfo.shared.generated.resources.Res
import mypetsinfo.shared.generated.resources.pet_gallery_placeholder
import mypetsinfo.shared.generated.resources.pet_gallery_title
import mypetsinfo.shared.generated.resources.pet_icon
import mypetsinfo.shared.generated.resources.pet_view_all
import mypetsinfo.shared.generated.resources.photo_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val galleryGradients = listOf(
    listOf(Color(0xFF9CAF72), Color(0xFFE1C377)),
    listOf(Color(0xFFD5B06A), Color(0xFF6F895E)),
    listOf(Color(0xFF829D7A), Color(0xFFB9D3D7)),
)

@Composable
internal fun PhotoGalleryCard() {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(Res.drawable.photo_icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = stringResource(Res.string.pet_gallery_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(Res.string.pet_view_all),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            galleryGradients.forEach { colors ->
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
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f),
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
        }
    }
}
