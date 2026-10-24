package com.example.security

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import com.example.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class SecurityManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val salt = "omnisocial_secure_salt_#2026"

    private val _isLocked = MutableStateFlow(preferencesManager.isPinEnabled.value)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _unlockAttempts = MutableStateFlow(0)
    val unlockAttempts: StateFlow<Int> = _unlockAttempts.asStateFlow()

    fun hashPin(pin: String): String {
        val bytes = "$pin$salt".toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun verifyPin(inputPin: String): Boolean {
        val storedHash = preferencesManager.getPinHash() ?: return true
        val inputHash = hashPin(inputPin)
        val matches = storedHash.equals(inputHash, ignoreCase = true)
        if (matches) {
            _isLocked.value = false
            _unlockAttempts.value = 0
        } else {
            _unlockAttempts.value += 1
        }
        return matches
    }

    fun setupNewPin(newPin: String) {
        val hash = hashPin(newPin)
        preferencesManager.setPin(hash)
        _isLocked.value = false
    }

    fun disablePin() {
        preferencesManager.setPin(null)
        _isLocked.value = false
    }

    fun lockApp() {
        if (preferencesManager.isPinEnabled.value) {
            _isLocked.value = true
        }
    }

    fun unlockWithoutPin() {
        _isLocked.value = false
        _unlockAttempts.value = 0
    }

    fun canUseBiometrics(): Boolean {
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return keyguard?.isDeviceSecure == true
    }
}
