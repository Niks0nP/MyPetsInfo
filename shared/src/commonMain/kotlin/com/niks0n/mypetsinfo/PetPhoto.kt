package com.niks0n.mypetsinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class PetPhotoTarget {
    Avatar,
    Cover,
}

interface PetPhotoPicker {
    val isAvailable: Boolean

    fun pick(
        target: PetPhotoTarget,
        onPicked: (assetId: String) -> Unit,
        onError: (Throwable) -> Unit,
    )

    fun delete(assetId: String)
}

object NoOpPetPhotoPicker : PetPhotoPicker {
    override val isAvailable: Boolean = false

    override fun pick(
        target: PetPhotoTarget,
        onPicked: (assetId: String) -> Unit,
        onError: (Throwable) -> Unit,
    ) = Unit

    override fun delete(assetId: String) = Unit
}

@Composable
internal fun rememberStoredPhotoBitmap(assetId: String?): ImageBitmap? {
    val image by produceState<ImageBitmap?>(initialValue = null, key1 = assetId) {
        value = assetId?.let { id ->
            withContext(Dispatchers.Default) {
                runCatching {
                    readStoredPhotoBytes(id)?.decodeToImageBitmap()
                }.getOrNull()
            }
        }
    }
    return image
}

internal expect suspend fun readStoredPhotoBytes(assetId: String): ByteArray?
