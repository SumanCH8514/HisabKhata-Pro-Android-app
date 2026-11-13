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
    val currencyFormat: String = "INR (₹)", // "INR (₹)", "USD ($)", "EUR (€)"
    val dateFormat: String = "dd MMM yyyy", // "dd MMM yyyy", "dd/MM/yyyy", "yyyy-MM-dd"
    val autoSyncWifiOnly: Boolean = false,
    val notificationAlerts: Boolean = true,
    val soundVibration: Boolean = true,
    val lockTimeout: String = "Immediately", // "Immediately", "1 Minute", "5 Minutes", "15 Minutes"
    val screenSecurity: Boolean = false
)

@Singleton
class AppPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hisabkhata_app_preferences", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<AppPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): AppPreferences {
        return AppPreferences(
            themeMode = prefs.getString("theme_mode", "System") ?: "System",
            accentColor = prefs.getString("accent_color", "Brand Blue") ?: "Brand Blue",
            fontSize = prefs.getString("font_size", "Standard") ?: "Standard",
            fontFamily = prefs.getString("font_family", "System Default") ?: "System Default",
            currencyFormat = prefs.getString("currency_format", "INR (₹)") ?: "INR (₹)",
            dateFormat = prefs.getString("date_format", "dd MMM yyyy") ?: "dd MMM yyyy",
            autoSyncWifiOnly = prefs.getBoolean("auto_sync_wifi_only", false),
            notificationAlerts = prefs.getBoolean("notification_alerts", true),
            soundVibration = prefs.getBoolean("sound_vibration", true),
            lockTimeout = prefs.getString("lock_timeout", "Immediately") ?: "Immediately",
            screenSecurity = prefs.getBoolean("screen_security", false)
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
    }

    fun setDateFormat(format: String) {
        prefs.edit().putString("date_format", format).apply()
        _preferences.value = _preferences.value.copy(dateFormat = format)
    }

    fun setAutoSyncWifiOnly(enabled: Boolean) {
        prefs.edit().putBoolean("auto_sync_wifi_only", enabled).apply()
        _preferences.value = _preferences.value.copy(autoSyncWifiOnly = enabled)
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
}
