package com.sumanonline.hisabkhata.core.common

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

object CertificateUtils {

    fun getSigningSha1(context: Context): String {
        return getFingerprint(context, "SHA-1")
    }

    fun getSigningSha256(context: Context): String {
        return getFingerprint(context, "SHA-256")
    }

    private fun getFingerprint(context: Context, algorithm: String): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            val cert = signatures?.firstOrNull()?.toByteArray() ?: return "Unavailable"
            val md = MessageDigest.getInstance(algorithm)
            val digest = md.digest(cert)
            digest.joinToString(":") { String.format("%02X", it) }
        } catch (e: Exception) {
            "Unavailable"
        }
    }
}
