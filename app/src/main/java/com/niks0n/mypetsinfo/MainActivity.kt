package com.niks0n.mypetsinfo

import android.Manifest
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity(), PetPhotoPicker {
    private var pendingPhotoTarget: PetPhotoTarget? = null
    private var pendingPhotoResult: ((String) -> Unit)? = null
    private var pendingPhotoError: ((Throwable) -> Unit)? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    private val petPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val target = pendingPhotoTarget
        val onResult = pendingPhotoResult
        val onError = pendingPhotoError
        clearPendingPhotoRequest()

        if (uri != null && target != null && onResult != null && onError != null) {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching { persistPetPhoto(uri, target) }
                }
                result.onSuccess(onResult).onFailure(onError)
            }
        }
    }

    override val isAvailable: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            App(
                requestNotificationPermission = ::requestNotificationPermission,
                petPhotoPicker = this,
                onProfileStatusBarChanged = ::setProfileStatusBar,
            )
        }
    }

    override fun pick(
        target: PetPhotoTarget,
        onPicked: (assetId: String) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        if (pendingPhotoTarget != null) return
        pendingPhotoTarget = target
        pendingPhotoResult = onPicked
        pendingPhotoError = onError
        petPhotoLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    override fun delete(assetId: String) {
        val photoDirectory = File(filesDir, PET_PHOTO_DIRECTORY).canonicalFile
        val photoFile = File(assetId).canonicalFile
        if (photoFile.parentFile == photoDirectory && photoFile.isFile) {
            photoFile.delete()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setProfileStatusBar(isProfile: Boolean) {
        val isDarkTheme = (
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        ) == Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            !isProfile && !isDarkTheme
    }

    private fun persistPetPhoto(uri: Uri, target: PetPhotoTarget): String {
        val maxDimension = when (target) {
            PetPhotoTarget.Avatar -> 1_024
            PetPhotoTarget.Cover -> 1_920
        }
        val source = ImageDecoder.createSource(contentResolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val sourceWidth = info.size.width
            val sourceHeight = info.size.height
            val largestDimension = maxOf(sourceWidth, sourceHeight)
            if (largestDimension > maxDimension) {
                val scale = maxDimension.toDouble() / largestDimension.toDouble()
                decoder.setTargetSize(
                    maxOf(1, (sourceWidth * scale).toInt()),
                    maxOf(1, (sourceHeight * scale).toInt()),
                )
            }
        }

        val directory = File(filesDir, PET_PHOTO_DIRECTORY).apply { mkdirs() }
        check(directory.isDirectory) { "Не удалось создать папку для фотографий" }
        val prefix = when (target) {
            PetPhotoTarget.Avatar -> "avatar"
            PetPhotoTarget.Cover -> "cover"
        }
        val output = File(directory, "$prefix-${UUID.randomUUID()}.jpg")

        return try {
            FileOutputStream(output).use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)) {
                    "Не удалось сохранить фотографию"
                }
            }
            output.canonicalPath
        } catch (throwable: Throwable) {
            output.delete()
            throw throwable
        }
    }

    private fun clearPendingPhotoRequest() {
        pendingPhotoTarget = null
        pendingPhotoResult = null
        pendingPhotoError = null
    }

    private companion object {
        const val PET_PHOTO_DIRECTORY = "pet_photos"
    }
}
