package com.sumanonline.hisabkhata.feature.reminders

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumanonline.hisabkhata.core.common.Constants
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.common.DueStatus
import com.sumanonline.hisabkhata.core.database.dao.CustomerDao
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.network.BackendApiService
import com.sumanonline.hisabkhata.core.network.model.EmailRequest
import com.sumanonline.hisabkhata.core.security.SecureSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val customerDao: CustomerDao,
    private val backendApi: BackendApiService,
    private val sessionManager: SecureSessionManager
) : ViewModel() {

    val session = sessionManager.sessionState

    @OptIn(ExperimentalCoroutinesApi::class)
    val dueCustomers: StateFlow<List<CustomerEntity>> = sessionManager.sessionState
        .flatMapLatest { s ->
            if (s.userId.isNotBlank()) {
                customerDao.observeCustomersByUser(s.userId)
            } else {
                flowOf(emptyList())
            }
        }
        .map { list ->
            list.filter { it.balance < 0.0 }
                .sortedBy {
                    when (val status = DateUtils.getDueDateStatus(it.dueDate)) {
                        is DueStatus.DueToday -> 0
                        is DueStatus.Overdue -> 1
                        is DueStatus.DueSoon -> 2
                        is DueStatus.Upcoming -> 3
                        is DueStatus.None -> 4
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendWhatsAppReminder(context: Context, customer: CustomerEntity) {
        val cleanPhone = customer.phone.replace("+", "").replace(" ", "").replace("-", "").trim()
        val targetPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
        val balanceFormatted = CurrencyFormatter.formatInr(Math.abs(customer.balance))
        val shareLink = "${Constants.WEB_PORTAL_BASE_URL}customer/share/${customer.id}"

        val message = "👋 *Hello ${customer.name},*\n\n📢 *Payment Reminder from HisabKhata Web*\n\nYour outstanding balance is: *$balanceFormatted*\n\n🔗 *View your full digital statement here:*\n$shareLink\n\n🙏 Please verify your transactions and clear dues. Thank you!"

        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmailReminder(context: Context, customer: CustomerEntity) {
        if (customer.email.isBlank()) {
            Toast.makeText(context, "No email address registered for ${customer.name}", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            try {
                val merchantName = session.value.businessName.ifBlank { session.value.displayName }
                val request = EmailRequest(
                    to_email = customer.email,
                    customer_name = customer.name,
                    merchant_name = merchantName,
                    merchant_phone = session.value.phone,
                    amount = Math.abs(customer.balance),
                    balance = customer.balance,
                    tx_type = "Payment Reminder",
                    action_url = "https://hisabkhata.sumanonline.com/customer/share/${customer.id}",
                    type = "PAYMENT_REMINDER"
                )
                val response = backendApi.sendEmail(request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Payment reminder email sent to ${customer.email}!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to send email: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
