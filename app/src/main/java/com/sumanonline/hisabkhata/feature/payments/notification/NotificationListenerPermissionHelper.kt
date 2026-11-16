package com.sumanonline.hisabkhata.feature.payments.notification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat

object NotificationListenerPermissionHelper {

    fun isPermissionGranted(context: Context): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        if (enabledPackages.contains(context.packageName)) {
            return true
        }

        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return !flat.isNullOrEmpty() && flat.contains(context.packageName)
    }

    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun requestServiceRebind(context: Context) {
        try {
            val component = ComponentName(context, UpiPaymentNotificationListenerService::class.java)
            NotificationListenerService.requestRebind(component)
        } catch (_: Exception) {

        }
    }
}
