package com.sumanonline.hisabkhata.feature.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")

    data object Customers : Screen("customers")
    data object CustomerLedger : Screen("customer_ledger/{customerId}") {
        fun createRoute(customerId: String) = "customer_ledger/$customerId"
    }
    data object Reports : Screen("reports")
    data object CustomerReport : Screen("customer_report/{customerId}") {
        fun createRoute(customerId: String) = "customer_report/$customerId"
    }
    data object Payments : Screen("payments")
    data object PaymentVerification : Screen("payment_verification/{paymentId}") {
        fun createRoute(paymentId: String) = "payment_verification/$paymentId"
    }
    data object Reminders : Screen("reminders")
    data object AutoReminder : Screen("auto_reminder")
    data object Profile : Screen("profile")
    data object AccountSettings : Screen("account_settings")
    data object ApplicationSettings : Screen("application_settings")
    data object PreferencesSettings : Screen("preferences_settings")
    data object AboutProject : Screen("about_project")
}
