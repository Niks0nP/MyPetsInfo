package com.niks0n.mypetsinfo

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.Foundation.NSData

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun readStoredPhotoBytes(assetId: String): ByteArray? {
    val data = NSData.dataWithContentsOfFile(assetId) ?: return null
    return data.bytes?.readBytes(data.length.toInt())
}
