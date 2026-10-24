package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AccentTheme
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("omnisocial_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_ACCENT_THEME = "pref_accent_theme"
        private const val KEY_PIN_HASH = "pref_pin_hash"
        private const val KEY_PIN_ENABLED = "pref_pin_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "pref_biometric_enabled"
        private const val KEY_AUTO_LOCK_MINUTES = "pref_auto_lock_minutes"
        private const val KEY_CLOUD_SYNC_ENABLED = "pref_cloud_sync_enabled"
        private const val KEY_LAST_SYNC_TIME = "pref_last_sync_time"
        private const val KEY_INCOGNITO_MODE = "pref_incognito_mode"
        private const val KEY_BLOCK_TRACKERS = "pref_block_trackers"
        private const val KEY_HIGH_CONTRAST = "pref_high_contrast"
        private const val KEY_DESKTOP_WHATSAPP = "pref_desktop_whatsapp"
    }

    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _accentTheme = MutableStateFlow(getAccentTheme())
    val accentTheme: StateFlow<AccentTheme> = _accentTheme.asStateFlow()

    private val _isPinEnabled = MutableStateFlow(prefs.getBoolean(KEY_PIN_ENABLED, false))
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isCloudSyncEnabled = MutableStateFlow(prefs.getBoolean(KEY_CLOUD_SYNC_ENABLED, true))
    val isCloudSyncEnabled: StateFlow<Boolean> = _isCloudSyncEnabled.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(prefs.getLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis() - 3600000))
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _incognitoMode = MutableStateFlow(prefs.getBoolean(KEY_INCOGNITO_MODE, false))
    val incognitoMode: StateFlow<Boolean> = _incognitoMode.asStateFlow()

    private val _blockTrackers = MutableStateFlow(prefs.getBoolean(KEY_BLOCK_TRACKERS, true))
    val blockTrackers: StateFlow<Boolean> = _blockTrackers.asStateFlow()

    private val _highContrastMode = MutableStateFlow(prefs.getBoolean(KEY_HIGH_CONTRAST, false))
    val highContrastMode: StateFlow<Boolean> = _highContrastMode.asStateFlow()

    private val _desktopWhatsApp = MutableStateFlow(prefs.getBoolean(KEY_DESKTOP_WHATSAPP, true))
    val desktopWhatsApp: StateFlow<Boolean> = _desktopWhatsApp.asStateFlow()

    fun getThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun getAccentTheme(): AccentTheme {
        val name = prefs.getString(KEY_ACCENT_THEME, AccentTheme.CLASSIC_SOCIAL.name) ?: AccentTheme.CLASSIC_SOCIAL.name
        return try {
            AccentTheme.valueOf(name)
        } catch (_: Exception) {
            AccentTheme.CLASSIC_SOCIAL
        }
    }

    fun setAccentTheme(theme: AccentTheme) {
        prefs.edit().putString(KEY_ACCENT_THEME, theme.name).apply()
        _accentTheme.value = theme
    }

    fun getPinHash(): String? {
        return prefs.getString(KEY_PIN_HASH, null)
    }

    fun setPin(pinHash: String?) {
        if (pinHash != null) {
            prefs.edit()
                .putString(KEY_PIN_HASH, pinHash)
                .putBoolean(KEY_PIN_ENABLED, true)
                .apply()
            _isPinEnabled.value = true
        } else {
            prefs.edit()
                .remove(KEY_PIN_HASH)
                .putBoolean(KEY_PIN_ENABLED, false)
                .putBoolean(KEY_BIOMETRIC_ENABLED, false)
                .apply()
            _isPinEnabled.value = false
            _isBiometricEnabled.value = false
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CLOUD_SYNC_ENABLED, enabled).apply()
        _isCloudSyncEnabled.value = enabled
    }

    fun updateLastSyncTime(timestamp: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIME, timestamp).apply()
        _lastSyncTimestamp.value = timestamp
    }

    fun setIncognitoMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_INCOGNITO_MODE, enabled).apply()
        _incognitoMode.value = enabled
    }

    fun setBlockTrackers(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCK_TRACKERS, enabled).apply()
        _blockTrackers.value = enabled
    }

    fun setHighContrastMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
        _highContrastMode.value = enabled
    }

    fun setDesktopWhatsApp(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DESKTOP_WHATSAPP, enabled).apply()
        _desktopWhatsApp.value = enabled
    }
}
