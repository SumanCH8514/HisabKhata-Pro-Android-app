package com.sumanonline.hisabkhata.core.common

import android.util.Base64

object ImageUtils {

    /**
     * Sanitizes image source strings for Coil.
     * - Decodes base64 data URIs ("data:image/...;base64,...") into ByteArray so Coil can display them directly.
     * - Upgrades cleartext http URLs to https.
     * - Trims surrounding whitespaces.
     */
    fun sanitizeImageSource(raw: String?): Any? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()

        if (trimmed.startsWith("data:image", ignoreCase = true) && trimmed.contains("base64,")) {
            val base64Data = trimmed.substringAfter("base64,").trim()
            return try {
                Base64.decode(base64Data, Base64.DEFAULT)
            } catch (_: Exception) {
                null
            }
        }

        if (trimmed.startsWith("http://", ignoreCase = true)) {
            return trimmed.replaceFirst("http://", "https://", ignoreCase = true)
        }

        return trimmed
    }

    fun isImageAvailable(raw: String?): Boolean {
        return !raw.isNullOrBlank()
    }
}
