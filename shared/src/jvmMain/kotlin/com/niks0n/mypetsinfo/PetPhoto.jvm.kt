package com.niks0n.mypetsinfo

import java.io.File

internal actual suspend fun readStoredPhotoBytes(assetId: String): ByteArray? =
    File(assetId).takeIf(File::isFile)?.readBytes()
