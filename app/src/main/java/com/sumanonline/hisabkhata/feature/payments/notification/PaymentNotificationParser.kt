package com.sumanonline.hisabkhata.feature.payments.notification

import com.sumanonline.hisabkhata.feature.payments.model.DetectedPaymentInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentNotificationParser @Inject constructor() {

    private val parsers: List<UpiNotificationParser> = listOf(
        GooglePayNotificationParser(),
        PhonePeNotificationParser(),
        PaytmNotificationParser(),
        BharatPeNotificationParser(),
        BhimNotificationParser(),
        GenericUpiNotificationParser()
    )

    fun parse(
        packageName: String,
        title: String,
        text: String,
        subText: String?,
        timestamp: Long
    ): DetectedPaymentInfo? {

        for (parser in parsers) {
            if (parser.canParse(packageName)) {
                val result = parser.parse(packageName, title, text, subText, timestamp)
                if (result != null) return result
            }
        }

        val generic = parsers.lastOrNull { it is GenericUpiNotificationParser }
        return generic?.parse(packageName, title, text, subText, timestamp)
    }
}
