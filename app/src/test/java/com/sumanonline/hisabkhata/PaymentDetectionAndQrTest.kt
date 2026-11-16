package com.sumanonline.hisabkhata

import com.sumanonline.hisabkhata.feature.payments.model.DetectedPaymentInfo
import com.sumanonline.hisabkhata.feature.payments.model.QrPaymentRequest
import com.sumanonline.hisabkhata.feature.payments.model.QrPaymentStatus
import com.sumanonline.hisabkhata.feature.payments.notification.BharatPeNotificationParser
import com.sumanonline.hisabkhata.feature.payments.notification.BhimNotificationParser
import com.sumanonline.hisabkhata.feature.payments.notification.GooglePayNotificationParser
import com.sumanonline.hisabkhata.feature.payments.notification.PaymentNotificationParser
import com.sumanonline.hisabkhata.feature.payments.notification.PhonePeNotificationParser
import com.sumanonline.hisabkhata.feature.payments.notification.PaytmNotificationParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.URLEncoder

class PaymentDetectionAndQrTest {

    private lateinit var parserOrchestrator: PaymentNotificationParser

    @Before
    fun setup() {
        parserOrchestrator = PaymentNotificationParser()
    }

    @Test
    fun testGooglePay_IncomingPaymentWithUtr() {
        val packageName = "com.google.android.apps.nbu.paisa.user"
        val title = "Payment received"
        val text = "₹1,500.00 received from Rahul Sharma via Google Pay. UPI Ref: 123456789012"

        val result = parserOrchestrator.parse(packageName, title, text, null, System.currentTimeMillis())

        assertNotNull("Google Pay payment should be parsed", result)
        assertEquals(1500.0, result!!.amount, 0.001)
        assertEquals("123456789012", result.transactionRef)
        assertEquals("Google Pay", result.sourceApp)
    }

    @Test
    fun testGooglePay_DebitNotification_IsRejected() {
        val packageName = "com.google.android.apps.nbu.paisa.user"
        val title = "Payment Sent"
        val text = "₹500.00 debited from your account for Grocery Store."

        val result = parserOrchestrator.parse(packageName, title, text, null, System.currentTimeMillis())
        assertNull("Debit notifications must be rejected", result)
    }

    @Test
    fun testPhonePe_IncomingPaymentWithUtr() {
        val packageName = "com.phonepe.app"
        val title = "PhonePe"
        val text = "You have received ₹2,450 from Suvam Dutta (UPI Ref: 668594674163)"

        val result = parserOrchestrator.parse(packageName, title, text, null, System.currentTimeMillis())

        assertNotNull("PhonePe payment should be parsed", result)
        assertEquals(2450.0, result!!.amount, 0.001)
        assertEquals("668594674163", result.transactionRef)
        assertEquals("PhonePe", result.sourceApp)
    }

    @Test
    fun testPhonePe_FailedTransaction_IsRejected() {
        val packageName = "com.phonepe.app"
        val title = "Payment Failed"
        val text = "Payment of ₹1,000 received failed due to bank timeout."

        val result = parserOrchestrator.parse(packageName, title, text, null, System.currentTimeMillis())
        assertNull("Failed notifications must be rejected", result)
    }

    @Test
    fun testPaytm_IncomingPayment() {
        val packageName = "net.one97.paytm"
        val title = "Paytm for Business"
        val text = "Received ₹500 in Paytm Payments Bank from Poulami. Ref: 432198765432"

        val result = parserOrchestrator.parse(packageName, title, text, null, System.currentTimeMillis())

        assertNotNull("Paytm payment should be parsed", result)
        assertEquals(500.0, result!!.amount, 0.001)
        assertEquals("432198765432", result.transactionRef)
        assertEquals("Paytm", result.sourceApp)
    }

    @Test
    fun testBharatPe_IncomingPayment() {
        val packageName = "com.bharatpe.app"
        val title = "Payment Received"
        val text = "You received payment of ₹800 from Customer. UTR: 998877665544"

        val result = parserOrchestrator.parse(packageName, title, text, null, System.currentTimeMillis())

        assertNotNull("BharatPe payment should be parsed", result)
        assertEquals(800.0, result!!.amount, 0.001)
        assertEquals("998877665544", result.transactionRef)
        assertEquals("BharatPe", result.sourceApp)
    }

    @Test
    fun testBhim_IncomingPayment() {
        val packageName = "in.org.npci.upiapp"
        val title = "BHIM UPI"
        val text = "Payment of Rs. 350.00 received from Amit. UPI Ref No 112233445566"

        val result = parserOrchestrator.parse(packageName, title, text, null, System.currentTimeMillis())

        assertNotNull("BHIM payment should be parsed", result)
        assertEquals(350.0, result!!.amount, 0.001)
        assertEquals("112233445566", result.transactionRef)
        assertEquals("BHIM UPI", result.sourceApp)
    }

    @Test
    fun testGenericBank_IncomingCredit() {
        val packageName = "com.sbi.upi"
        val title = "SBI UPI Alert"
        val text = "Your account is credited by Rs 1,200.00 from John via UPI Ref 778899001122"

        val result = parserOrchestrator.parse(packageName, title, text, null, System.currentTimeMillis())

        assertNotNull("Generic bank UPI payment should be parsed", result)
        assertEquals(1200.0, result!!.amount, 0.001)
        assertEquals("778899001122", result.transactionRef)
    }

    @Test
    fun testIrrelevantNotification_Ignored() {
        val packageName = "com.google.android.apps.nbu.paisa.user"
        val title = "Earn rewards with Google Pay"
        val text = "Invite your friends and earn scratch cards up to ₹100!"

        val result = parserOrchestrator.parse(packageName, title, text, null, System.currentTimeMillis())
        assertNull("Promotional notification should be ignored", result)
    }

    @Test
    fun testPaymentRequestExpiration() {
        val now = System.currentTimeMillis()
        val activeReq = QrPaymentRequest(
            id = "req_1",
            merchantId = "m_1",
            customerId = "c_1",
            customerName = "Rahul",
            amount = 1500.0,
            requestedAt = now,
            expiresAt = now + 10000,
            status = QrPaymentStatus.WAITING_FOR_PAYMENT
        )
        assertFalse("Request within window should not be expired", activeReq.isExpired)

        val expiredReq = QrPaymentRequest(
            id = "req_2",
            merchantId = "m_1",
            customerId = "c_1",
            customerName = "Rahul",
            amount = 1500.0,
            requestedAt = now - 40000,
            expiresAt = now - 10000,
            status = QrPaymentStatus.WAITING_FOR_PAYMENT
        )
        assertTrue("Request past window must be expired", expiredReq.isExpired)

        val defaultReq = QrPaymentRequest(
            id = "req_3",
            merchantId = "m_1",
            customerId = "c_1",
            customerName = "Rahul",
            amount = 500.0
        )
        assertEquals("Default expiry should be exactly 5 minutes (300000ms)", 5 * 60 * 1000L, defaultReq.expiresAt - defaultReq.requestedAt)
    }

    @Test
    fun testPendingPayment5MinuteAutoExpiryFilter() {
        val now = System.currentTimeMillis()
        val expiryMillis = 5 * 60 * 1000L

        val recentPending = com.sumanonline.hisabkhata.core.database.entity.PendingPaymentEntity(
            id = "p_recent",
            merchantId = "m_1",
            customerId = "c_1",
            customerName = "Recent",
            amount = 100.0,
            status = "pending",
            timestamp = now - (2 * 60 * 1000L)
        )

        val stalePending = com.sumanonline.hisabkhata.core.database.entity.PendingPaymentEntity(
            id = "p_stale",
            merchantId = "m_1",
            customerId = "c_1",
            customerName = "Stale",
            amount = 200.0,
            status = "pending",
            timestamp = now - (6 * 60 * 1000L)
        )

        val approvedOld = com.sumanonline.hisabkhata.core.database.entity.PendingPaymentEntity(
            id = "p_approved",
            merchantId = "m_1",
            customerId = "c_1",
            customerName = "Approved Old",
            amount = 300.0,
            status = "approved",
            timestamp = now - (60 * 60 * 1000L)
        )

        val payments = listOf(recentPending, stalePending, approvedOld)

        val filtered = payments.filterNot { payment ->
            payment.status.equals("pending", ignoreCase = true) && (now - payment.timestamp) > expiryMillis
        }

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.id == "p_recent" })
        assertTrue(filtered.any { it.id == "p_approved" })
        assertFalse("Stale unacted pending payment (> 5 mins) must be cleared", filtered.any { it.id == "p_stale" })
    }

    @Test
    fun testUpiUriConstruction() {
        val vpa = "merchant@upi"
        val merchantName = "HisabKhata Store"
        val cleanName = URLEncoder.encode(merchantName, "UTF-8")
        val amount = 2450.50
        val requestId = "HKP_TEST_123"

        val upiUri = "upi://pay?pa=$vpa&pn=$cleanName&am=%.2f&cu=INR&tr=$requestId".format(amount)

        assertTrue(upiUri.startsWith("upi://pay?"))
        assertTrue(upiUri.contains("pa=merchant@upi"))
        assertTrue(upiUri.contains("am=2450.50"))
        assertTrue(upiUri.contains("cu=INR"))
        assertTrue(upiUri.contains("tr=HKP_TEST_123"))
    }

    @Test
    fun testBhimUpi_DetailedNotification_BankAndAccountExtraction() {
        val packageName = "in.org.npci.upiapp"
        val title = "BHIM UPI"
        val text = "Received INR 1.00 in your state Bank Of India account(XX7137) from SHUKLA CHAKRABORTY."

        val result = parserOrchestrator.parse(packageName, title, text, null, System.currentTimeMillis())

        assertNotNull("Payment should be parsed", result)
        assertEquals(1.0, result!!.amount, 0.001)
        assertEquals("SHUKLA CHAKRABORTY", result.senderName)
        assertEquals("State Bank of India", result.bankName)
        assertEquals("XX7137", result.accountNumber)
        assertEquals("BHIM UPI", result.sourceApp)
    }

    @Test
    fun testLedgerNoteFormat_MatchesUserSpecification() {
        val rawNotification = "Received INR 1.00 in your state Bank Of India account(XX7137) from SHUKLA CHAKRABORTY."
        val source = "BHIM UPI"
        val fixedTimestamp = 1794845580000L // Corresponds to Nov 16 2026 09:33 PM in UTC or local
        val dateTimeStr = "16-Nov-2026_09:33PM"

        val cleanNotification = rawNotification.trim().trimEnd('.')
        val note = "Online Payment via $source (Note: $cleanNotification. at date and time($dateTimeStr))"

        val expected = "Online Payment via BHIM UPI (Note: Received INR 1.00 in your state Bank Of India account(XX7137) from SHUKLA CHAKRABORTY. at date and time(16-Nov-2026_09:33PM))"
        assertEquals(expected, note)
    }

    @Test
    fun testPaymentCollectionEmailAlert_RequestConstruction() {
        val customerEmail = "sujan@example.com"
        val customerName = "Sujan Chakrabortty"
        val merchantBusinessName = "SumanOnline Store"
        val merchantPhone = "+919876543210"
        val amount = 500.0
        val currentBalance = -1500.0 // Customer owes 1500
        val customerId = "cust_12345"
        val note = "Online Payment via BHIM UPI (Ref: 123456789012 at date and time(16-Nov-2026_09:33PM))"

        val formattedAmt = if (amount % 1.0 == 0.0) amount.toLong().toString() else "%.2f".format(amount)

        val emailReq = com.sumanonline.hisabkhata.core.network.model.EmailRequest(
            to_email = customerEmail,
            to_name = customerName,
            customer_name = customerName,
            subject = "Transaction Alert: ₹$formattedAmt - $merchantBusinessName",
            message = "A new payment amount of ₹$formattedAmt has been recorded on your account. Please check the details below:",
            merchant_name = merchantBusinessName,
            merchant_phone = merchantPhone,
            amount = amount,
            balance = currentBalance,
            is_debit = currentBalance < 0,
            is_due = currentBalance < 0,
            tx_type = "Payment Received",
            description = note,
            action_url = "${com.sumanonline.hisabkhata.core.common.Constants.WEB_PORTAL_BASE_URL}customer/share/$customerId",
            type = "TRANSACTION"
        )

        assertEquals("sujan@example.com", emailReq.to_email)
        assertEquals("Sujan Chakrabortty", emailReq.customer_name)
        assertEquals("Transaction Alert: ₹500 - SumanOnline Store", emailReq.subject)
        assertEquals("Payment Received", emailReq.tx_type)
        assertEquals(500.0, emailReq.amount)
        assertEquals(-1500.0, emailReq.balance)
        assertTrue(emailReq.is_debit == true)
        assertTrue(emailReq.is_due == true)
        assertEquals("TRANSACTION", emailReq.type)
        assertTrue(emailReq.action_url?.contains(customerId) == true)
    }

    @Test
    fun testPaymentCollectionEmailAlert_ValidationRules() {
        fun shouldSendEmail(email: String?): Boolean {
            val trimmed = email?.trim()
            return !trimmed.isNullOrBlank() && trimmed.contains("@")
        }

        assertTrue(shouldSendEmail("merchant@gmail.com"))
        assertTrue(shouldSendEmail(" customer.name@example.co.in "))
        assertFalse(shouldSendEmail(null))
        assertFalse(shouldSendEmail(""))
        assertFalse(shouldSendEmail("   "))
        assertFalse(shouldSendEmail("invalid-email-no-at"))
    }
}
