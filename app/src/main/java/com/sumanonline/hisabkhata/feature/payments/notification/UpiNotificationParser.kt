package com.sumanonline.hisabkhata.feature.payments.notification

import com.sumanonline.hisabkhata.feature.payments.model.DetectedPaymentInfo
import java.util.regex.Pattern

interface UpiNotificationParser {
    fun canParse(packageName: String): Boolean
    fun parse(
        packageName: String,
        title: String,
        text: String,
        subText: String?,
        timestamp: Long
    ): DetectedPaymentInfo?
}

abstract class BaseUpiNotificationParser : UpiNotificationParser {

    companion object {

        private val AMOUNT_PATTERN = Pattern.compile(
            """(?:₹|Rs\.?|INR)\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
            Pattern.CASE_INSENSITIVE
        )

        private val UTR_PATTERN = Pattern.compile(
            """(?:UTR|UPI Ref|Ref No|Reference No|Ref|Txn ID|Transaction ID)[:\s#-]+([A-Za-z0-9]{8,22})""",
            Pattern.CASE_INSENSITIVE
        )

        private val UTR_STANDALONE_PATTERN = Pattern.compile(
            """\b([0-9]{12})\b"""
        )

        private val SENDER_FROM_PATTERN = Pattern.compile(
            """(?:from|by)\s+([A-Za-z0-9\s.]+?)(?:\s+(?:via|into|to|using|on|Ref|UTR|UPI|\.)|$)""",
            Pattern.CASE_INSENSITIVE
        )

        private val SENDER_SENT_PATTERN = Pattern.compile(
            """^([A-Za-z0-9\s.]+?)\s+(?:sent you|has paid you|paid you)""",
            Pattern.CASE_INSENSITIVE
        )

        private val OUTGOING_KEYWORDS = listOf(
            "debited",
            "paid to",
            "sent to",
            "you paid",
            "transferred to",
            "withdrawn",
            "failed",
            "declined",
            "unsuccessful",
            "reversed",
            "otp",
            "verification code",
            "request to pay",
            "has requested"
        )

        private val INCOMING_KEYWORDS = listOf(
            "received",
            "credited",
            "paid you",
            "sent you",
            "payment received",
            "added to your",
            "has paid"
        )

        private val KNOWN_BANKS = listOf(
            "State Bank of India",
            "State Bank Of India",
            "SBI",
            "Paytm Payments Bank",
            "Airtel Payments Bank",
            "HDFC Bank",
            "ICICI Bank",
            "Axis Bank",
            "Kotak Mahindra Bank",
            "Punjab National Bank",
            "PNB",
            "Bank of Baroda",
            "Canara Bank",
            "Union Bank of India",
            "Indian Bank",
            "Bank of India",
            "Federal Bank",
            "IndusInd Bank",
            "IDFC First Bank",
            "Yes Bank",
            "Central Bank of India",
            "UCO Bank"
        )
    }

    protected fun isDebitOrIrrelevant(fullText: String): Boolean {
        val lower = fullText.lowercase()

        for (kw in OUTGOING_KEYWORDS) {
            if (lower.contains(kw)) {
                return true
            }
        }
        return false
    }

    protected fun isIncomingPayment(fullText: String): Boolean {
        val lower = fullText.lowercase()
        for (kw in INCOMING_KEYWORDS) {
            if (lower.contains(kw)) return true
        }
        return false
    }

    protected fun extractAmount(fullText: String): Double? {
        val matcher = AMOUNT_PATTERN.matcher(fullText)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")?.trim()
            return amountStr?.toDoubleOrNull()
        }
        return null
    }

    protected fun extractUtr(fullText: String): String? {
        val matcher = UTR_PATTERN.matcher(fullText)
        if (matcher.find()) {
            return matcher.group(1)?.trim()
        }

        val numMatcher = UTR_STANDALONE_PATTERN.matcher(fullText)
        while (numMatcher.find()) {
            val candidate = numMatcher.group(1)

            if (candidate != null && candidate.length == 12) {
                return candidate
            }
        }
        return null
    }

    protected fun extractSender(title: String, text: String): String? {
        val combined = "$title $text"
        val fromMatcher = SENDER_FROM_PATTERN.matcher(combined)
        if (fromMatcher.find()) {
            val sender = fromMatcher.group(1)?.trim()?.trimEnd('.', ',', '!')
            if (!sender.isNullOrBlank() && sender.length in 2..50 && !sender.contains("account", ignoreCase = true)) {
                return sender
            }
        }

        val sentMatcher = SENDER_SENT_PATTERN.matcher(title)
        if (sentMatcher.find()) {
            val sender = sentMatcher.group(1)?.trim()?.trimEnd('.', ',', '!')
            if (!sender.isNullOrBlank() && sender.length in 2..50) {
                return sender
            }
        }

        return null
    }

    protected fun extractBankName(fullText: String): String? {
        val bankPattern = Pattern.compile(
            """(?:in|to|into)\s+your\s+([A-Za-z\s]+?)\s+(?:account|ac|a/c)""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = bankPattern.matcher(fullText)
        if (matcher.find()) {
            val raw = matcher.group(1)?.trim()
            if (!raw.isNullOrBlank() && raw.length in 3..40) {
                return cleanBankName(raw)
            }
        }

        val bankNamePattern = Pattern.compile(
            """(?:in|to|into)\s+([A-Za-z0-9\s]+?\s+Bank)""",
            Pattern.CASE_INSENSITIVE
        )
        val nameMatcher = bankNamePattern.matcher(fullText)
        if (nameMatcher.find()) {
            val raw = nameMatcher.group(1)?.trim()
            if (!raw.isNullOrBlank() && raw.length in 3..40) {
                return cleanBankName(raw)
            }
        }

        for (bank in KNOWN_BANKS) {
            if (fullText.contains(bank, ignoreCase = true)) {
                return bank
            }
        }
        return null
    }

    private fun cleanBankName(raw: String): String {
        val words = raw.trim().split("\\s+".toRegex())
        return words.joinToString(" ") { w ->
            when {
                w.equals("of", ignoreCase = true) -> "of"
                w.length <= 3 -> w.uppercase()
                else -> w.lowercase().replaceFirstChar { it.uppercase() }
            }
        }
    }

    protected fun extractAccountNumber(fullText: String): String? {
        val accPattern = Pattern.compile(
            """(?:account|ac|a/c|acc|a\/c)[\s:]*[\(]?([X\d*]{3,18})[\)]?""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = accPattern.matcher(fullText)
        if (matcher.find()) {
            val candidate = matcher.group(1)?.trim()
            if (!candidate.isNullOrBlank()) {
                return if (candidate.all { it.isDigit() } && candidate.length == 4) {
                    "XX$candidate"
                } else {
                    candidate
                }
            }
        }

        val endingPattern = Pattern.compile(
            """(?:ending\s+(?:with|in)\s+)([0-9X*]{4,10})""",
            Pattern.CASE_INSENSITIVE
        )
        val endingMatcher = endingPattern.matcher(fullText)
        if (endingMatcher.find()) {
            val candidate = endingMatcher.group(1)?.trim()
            if (!candidate.isNullOrBlank()) {
                return if (candidate.all { it.isDigit() } && candidate.length == 4) {
                    "XX$candidate"
                } else {
                    candidate
                }
            }
        }
        return null
    }

    protected fun extractUpiId(fullText: String): String? {
        val upiPattern = Pattern.compile("""\b([a-zA-Z0-9.\-_]{2,64}@[a-zA-Z]{2,32})\b""")
        val matcher = upiPattern.matcher(fullText)
        if (matcher.find()) {
            return matcher.group(1)?.trim()
        }
        return null
    }
}

class GooglePayNotificationParser : BaseUpiNotificationParser() {
    companion object {
        val PACKAGES = setOf(
            "com.google.android.apps.nbu.paisa.user",
            "com.google.android.apps.nbu.paisa.merchant"
        )
    }

    override fun canParse(packageName: String): Boolean = PACKAGES.contains(packageName)

    override fun parse(
        packageName: String,
        title: String,
        text: String,
        subText: String?,
        timestamp: Long
    ): DetectedPaymentInfo? {
        val fullContent = "$title $text ${subText.orEmpty()}"
        if (isDebitOrIrrelevant(fullContent)) return null
        if (!isIncomingPayment(fullContent)) return null

        val amount = extractAmount(fullContent) ?: return null
        if (amount <= 0.0) return null

        val utr = extractUtr(fullContent)
        val sender = extractSender(title, text)

        return DetectedPaymentInfo(
            amount = amount,
            senderName = sender,
            transactionRef = utr,
            sourceApp = "Google Pay",
            sourcePackage = packageName,
            bankName = extractBankName(fullContent),
            accountNumber = extractAccountNumber(fullContent),
            upiId = extractUpiId(fullContent),
            rawTitle = title,
            rawText = text,
            timestamp = timestamp
        )
    }
}

class PhonePeNotificationParser : BaseUpiNotificationParser() {
    companion object {
        val PACKAGES = setOf(
            "com.phonepe.app",
            "com.phonepe.merchant",
            "com.phonepe.business"
        )
    }

    override fun canParse(packageName: String): Boolean = PACKAGES.contains(packageName)

    override fun parse(
        packageName: String,
        title: String,
        text: String,
        subText: String?,
        timestamp: Long
    ): DetectedPaymentInfo? {
        val fullContent = "$title $text ${subText.orEmpty()}"
        if (isDebitOrIrrelevant(fullContent)) return null
        if (!isIncomingPayment(fullContent)) return null

        val amount = extractAmount(fullContent) ?: return null
        if (amount <= 0.0) return null

        val utr = extractUtr(fullContent)
        val sender = extractSender(title, text)

        return DetectedPaymentInfo(
            amount = amount,
            senderName = sender,
            transactionRef = utr,
            sourceApp = "PhonePe",
            sourcePackage = packageName,
            bankName = extractBankName(fullContent),
            accountNumber = extractAccountNumber(fullContent),
            upiId = extractUpiId(fullContent),
            rawTitle = title,
            rawText = text,
            timestamp = timestamp
        )
    }
}

class PaytmNotificationParser : BaseUpiNotificationParser() {
    companion object {
        val PACKAGES = setOf(
            "net.one97.paytm",
            "com.paytm.business"
        )
    }

    override fun canParse(packageName: String): Boolean = PACKAGES.contains(packageName)

    override fun parse(
        packageName: String,
        title: String,
        text: String,
        subText: String?,
        timestamp: Long
    ): DetectedPaymentInfo? {
        val fullContent = "$title $text ${subText.orEmpty()}"
        if (isDebitOrIrrelevant(fullContent)) return null
        if (!isIncomingPayment(fullContent)) return null

        val amount = extractAmount(fullContent) ?: return null
        if (amount <= 0.0) return null

        val utr = extractUtr(fullContent)
        val sender = extractSender(title, text)

        return DetectedPaymentInfo(
            amount = amount,
            senderName = sender,
            transactionRef = utr,
            sourceApp = "Paytm",
            sourcePackage = packageName,
            bankName = extractBankName(fullContent) ?: "Paytm Payments Bank",
            accountNumber = extractAccountNumber(fullContent),
            upiId = extractUpiId(fullContent),
            rawTitle = title,
            rawText = text,
            timestamp = timestamp
        )
    }
}

class BharatPeNotificationParser : BaseUpiNotificationParser() {
    companion object {
        val PACKAGES = setOf(
            "com.bharatpe.app",
            "com.bharatpe.merchant"
        )
    }

    override fun canParse(packageName: String): Boolean = PACKAGES.contains(packageName)

    override fun parse(
        packageName: String,
        title: String,
        text: String,
        subText: String?,
        timestamp: Long
    ): DetectedPaymentInfo? {
        val fullContent = "$title $text ${subText.orEmpty()}"
        if (isDebitOrIrrelevant(fullContent)) return null
        if (!isIncomingPayment(fullContent)) return null

        val amount = extractAmount(fullContent) ?: return null
        if (amount <= 0.0) return null

        val utr = extractUtr(fullContent)
        val sender = extractSender(title, text)

        return DetectedPaymentInfo(
            amount = amount,
            senderName = sender,
            transactionRef = utr,
            sourceApp = "BharatPe",
            sourcePackage = packageName,
            bankName = extractBankName(fullContent),
            accountNumber = extractAccountNumber(fullContent),
            upiId = extractUpiId(fullContent),
            rawTitle = title,
            rawText = text,
            timestamp = timestamp
        )
    }
}

class BhimNotificationParser : BaseUpiNotificationParser() {
    companion object {
        const val PACKAGE = "in.org.npci.upiapp"
    }

    override fun canParse(packageName: String): Boolean = packageName == PACKAGE

    override fun parse(
        packageName: String,
        title: String,
        text: String,
        subText: String?,
        timestamp: Long
    ): DetectedPaymentInfo? {
        val fullContent = "$title $text ${subText.orEmpty()}"
        if (isDebitOrIrrelevant(fullContent)) return null
        if (!isIncomingPayment(fullContent)) return null

        val amount = extractAmount(fullContent) ?: return null
        if (amount <= 0.0) return null

        val utr = extractUtr(fullContent)
        val sender = extractSender(title, text)

        return DetectedPaymentInfo(
            amount = amount,
            senderName = sender,
            transactionRef = utr,
            sourceApp = "BHIM UPI",
            sourcePackage = packageName,
            bankName = extractBankName(fullContent),
            accountNumber = extractAccountNumber(fullContent),
            upiId = extractUpiId(fullContent),
            rawTitle = title,
            rawText = text,
            timestamp = timestamp
        )
    }
}

class GenericUpiNotificationParser : BaseUpiNotificationParser() {
    companion object {
        val KNOWN_UPI_PACKAGES = setOf(
            "com.dreamplug.androidapp",
            "in.amazon.mShop.android.shopping",
            "com.sbi.upi",
            "com.mgs.hsbclogin",
            "com.icicibank.imobile",
            "com.axis.mobile",
            "com.msf.kbank.mobile",
            "com.bankofbaroda.upi",
            "com.enstage.wibmo.hdfc"
        )
    }

    override fun canParse(packageName: String): Boolean {
        return KNOWN_UPI_PACKAGES.contains(packageName) ||
                packageName.contains("upi", ignoreCase = true) ||
                packageName.contains("bank", ignoreCase = true) ||
                packageName.contains("pay", ignoreCase = true)
    }

    override fun parse(
        packageName: String,
        title: String,
        text: String,
        subText: String?,
        timestamp: Long
    ): DetectedPaymentInfo? {
        val fullContent = "$title $text ${subText.orEmpty()}"
        if (isDebitOrIrrelevant(fullContent)) return null
        if (!isIncomingPayment(fullContent)) return null

        val amount = extractAmount(fullContent) ?: return null
        if (amount <= 0.0) return null

        val utr = extractUtr(fullContent)
        val sender = extractSender(title, text)
        val detectedBank = extractBankName(fullContent)

        val appName = when {
            packageName.contains("dreamplug") -> "CRED"
            packageName.contains("amazon") -> "Amazon Pay"
            packageName.contains("npci") || fullContent.contains("bhim", ignoreCase = true) -> "BHIM UPI"
            packageName.contains("sbi") -> "State Bank of India"
            packageName.contains("icici") -> "iMobile Pay"
            packageName.contains("axis") -> "Axis UPI"
            packageName.contains("hdfc") -> "HDFC Bank"
            detectedBank != null -> detectedBank
            else -> "UPI App"
        }

        return DetectedPaymentInfo(
            amount = amount,
            senderName = sender,
            transactionRef = utr,
            sourceApp = appName,
            sourcePackage = packageName,
            bankName = detectedBank,
            accountNumber = extractAccountNumber(fullContent),
            upiId = extractUpiId(fullContent),
            rawTitle = title,
            rawText = text,
            timestamp = timestamp
        )
    }
}
