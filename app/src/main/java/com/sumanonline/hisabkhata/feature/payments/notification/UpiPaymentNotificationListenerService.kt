package com.sumanonline.hisabkhata.feature.payments.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.sumanonline.hisabkhata.feature.payments.reconciliation.PaymentReconciliationEngine
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PaymentServiceEntryPoint {
    fun reconciliationEngine(): PaymentReconciliationEngine
    fun parserOrchestrator(): PaymentNotificationParser
}

@AndroidEntryPoint
class UpiPaymentNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var reconciliationEngine: PaymentReconciliationEngine

    @Inject
    lateinit var parserOrchestrator: PaymentNotificationParser

    companion object {
        private const val TAG = "UpiPaymentNotification"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected successfully")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Notification listener disconnected; requesting rebind")
        NotificationListenerPermissionHelper.requestServiceRebind(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        try {
            ensureDependenciesInjected()

            val packageName = sbn.packageName ?: return
            val extras = sbn.notification?.extras ?: return

            val title = extras.getCharSequence("android.title")?.toString().orEmpty()
            val text = extras.getCharSequence("android.text")?.toString().orEmpty()
            val subText = extras.getCharSequence("android.subText")?.toString()
            val bigText = extras.getCharSequence("android.bigText")?.toString()

            val combinedText = if (!bigText.isNullOrBlank() && bigText.length > text.length) bigText else text
            val timestamp = sbn.postTime

            val detectedPayment = parserOrchestrator.parse(
                packageName = packageName,
                title = title,
                text = combinedText,
                subText = subText,
                timestamp = timestamp
            )

            if (detectedPayment != null) {
                Log.i(TAG, "Valid UPI payment detected! Amount: ₹${detectedPayment.amount} from ${detectedPayment.sourceApp}")
                reconciliationEngine.onPaymentDetected(detectedPayment)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing incoming notification", e)
        }
    }

    private fun ensureDependenciesInjected() {
        if (!::reconciliationEngine.isInitialized || !::parserOrchestrator.isInitialized) {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    applicationContext,
                    PaymentServiceEntryPoint::class.java
                )
                reconciliationEngine = entryPoint.reconciliationEngine()
                parserOrchestrator = entryPoint.parserOrchestrator()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to resolve EntryPoint dependencies", e)
            }
        }
    }
}
