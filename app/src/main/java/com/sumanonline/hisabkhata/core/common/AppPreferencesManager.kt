package com.sumanonline.hisabkhata.core.common

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AppPreferences(
    val themeMode: String = "System", // "System", "Light", "Dark"
    val accentColor: String = "Brand Blue", // "Brand Blue", "Emerald Green", "Royal Purple", "Amber Gold", "Crimson Red"
    val fontSize: String = "Standard", // "Small", "Standard", "Large", "Extra Large"
    val fontFamily: String = "System Default", // "System Default", "Sans-Serif", "Serif", "Monospace"
    val currencyFormat: String = "Indian Rupee (₹ INR)", // "Indian Rupee (₹ INR)", "US Dollar ($ USD)", "Euro (€ EUR)", "British Pound (£ GBP)"
    val dateFormat: String = "dd MMM yyyy", // "dd MMM yyyy", "dd/MM/yyyy", "yyyy-MM-dd"
    val autoSyncWifiOnly: Boolean = false,
    val notificationAlerts: Boolean = true,
    val soundVibration: Boolean = true,
    val lockTimeout: String = "Immediately", // "Immediately", "1 Minute", "5 Minutes", "15 Minutes"
    val screenSecurity: Boolean = false,
    val appLanguage: String = "English (US)",
    val emailOtpVerification: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val autoCloudBackup: Boolean = true
)

@Singleton
class AppPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hisabkhata_app_preferences", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<AppPreferences> = _preferences.asStateFlow()

    init {
        val initial = _preferences.value
        CurrencyFormatter.activeCurrencyFormat = initial.currencyFormat
        DateUtils.activeDateFormat = initial.dateFormat
    }

    private fun loadPreferences(): AppPreferences {
        val rawCurrency = prefs.getString("currency_format", "Indian Rupee (₹ INR)") ?: "Indian Rupee (₹ INR)"
        val normalizedCurrency = if (rawCurrency == "INR (₹)") "Indian Rupee (₹ INR)" else rawCurrency
        return AppPreferences(
            themeMode = prefs.getString("theme_mode", "System") ?: "System",
            accentColor = prefs.getString("accent_color", "Brand Blue") ?: "Brand Blue",
            fontSize = prefs.getString("font_size", "Standard") ?: "Standard",
            fontFamily = prefs.getString("font_family", "System Default") ?: "System Default",
            currencyFormat = normalizedCurrency,
            dateFormat = prefs.getString("date_format", "dd MMM yyyy") ?: "dd MMM yyyy",
            autoSyncWifiOnly = prefs.getBoolean("auto_sync_wifi_only", false),
            notificationAlerts = prefs.getBoolean("notification_alerts", true),
            soundVibration = prefs.getBoolean("sound_vibration", true),
            lockTimeout = prefs.getString("lock_timeout", "Immediately") ?: "Immediately",
            screenSecurity = prefs.getBoolean("screen_security", false),
            appLanguage = prefs.getString("app_language", "English (US)") ?: "English (US)",
            emailOtpVerification = prefs.getBoolean("email_otp_verification", false),
            twoFactorEnabled = prefs.getBoolean("two_factor_enabled", false),
            autoCloudBackup = prefs.getBoolean("auto_cloud_backup", true)
        )
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _preferences.value = _preferences.value.copy(themeMode = mode)
    }

    fun setAccentColor(colorName: String) {
        prefs.edit().putString("accent_color", colorName).apply()
        _preferences.value = _preferences.value.copy(accentColor = colorName)
    }

    fun setFontSize(size: String) {
        prefs.edit().putString("font_size", size).apply()
        _preferences.value = _preferences.value.copy(fontSize = size)
    }

    fun setFontFamily(family: String) {
        prefs.edit().putString("font_family", family).apply()
        _preferences.value = _preferences.value.copy(fontFamily = family)
    }

    fun setCurrencyFormat(format: String) {
        prefs.edit().putString("currency_format", format).apply()
        _preferences.value = _preferences.value.copy(currencyFormat = format)
        CurrencyFormatter.activeCurrencyFormat = format
    }

    fun setDateFormat(format: String) {
        prefs.edit().putString("date_format", format).apply()
        _preferences.value = _preferences.value.copy(dateFormat = format)
        DateUtils.activeDateFormat = format
    }

    fun setAutoSyncWifiOnly(enabled: Boolean) {
        prefs.edit().putBoolean("auto_sync_wifi_only", enabled).apply()
        _preferences.value = _preferences.value.copy(autoSyncWifiOnly = enabled)
        com.sumanonline.hisabkhata.core.sync.SyncWorker.schedulePeriodicSync(context, enabled)
    }

    fun setNotificationAlerts(enabled: Boolean) {
        prefs.edit().putBoolean("notification_alerts", enabled).apply()
        _preferences.value = _preferences.value.copy(notificationAlerts = enabled)
    }

    fun setSoundVibration(enabled: Boolean) {
        prefs.edit().putBoolean("sound_vibration", enabled).apply()
        _preferences.value = _preferences.value.copy(soundVibration = enabled)
    }

    fun setLockTimeout(timeout: String) {
        prefs.edit().putString("lock_timeout", timeout).apply()
        _preferences.value = _preferences.value.copy(lockTimeout = timeout)
    }

    fun setScreenSecurity(enabled: Boolean) {
        prefs.edit().putBoolean("screen_security", enabled).apply()
        _preferences.value = _preferences.value.copy(screenSecurity = enabled)
    }

    fun setAppLanguage(language: String) {
        prefs.edit().putString("app_language", language).apply()
        _preferences.value = _preferences.value.copy(appLanguage = language)
    }

    fun setEmailOtpVerification(enabled: Boolean) {
        prefs.edit().putBoolean("email_otp_verification", enabled).apply()
        _preferences.value = _preferences.value.copy(emailOtpVerification = enabled)
    }

    fun setTwoFactorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("two_factor_enabled", enabled).apply()
        _preferences.value = _preferences.value.copy(twoFactorEnabled = enabled)
    }

    fun setAutoCloudBackup(enabled: Boolean) {
        prefs.edit().putBoolean("auto_cloud_backup", enabled).apply()
        _preferences.value = _preferences.value.copy(autoCloudBackup = enabled)
    }
}
