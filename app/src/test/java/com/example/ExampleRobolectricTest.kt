package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PreferencesManager
import com.example.model.SocialPlatform
import com.example.model.ThemeMode
import com.example.security.SecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OmniSocial", appName)
    }

    @Test
    fun `verify social platforms initial configuration`() {
        assertEquals("whatsapp", SocialPlatform.WHATSAPP.id)
        assertEquals("instagram", SocialPlatform.INSTAGRAM.id)
        assertEquals("facebook", SocialPlatform.FACEBOOK.id)
        assertEquals("messenger", SocialPlatform.MESSENGER.id)

        // WhatsApp defaults to desktop mode for QR code web client
        assertTrue(SocialPlatform.WHATSAPP.defaultIsDesktop)
    }

    @Test
    fun `verify preferences manager theme operations`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)

        prefs.setThemeMode(ThemeMode.PURE_AMOLED)
        assertEquals(ThemeMode.PURE_AMOLED, prefs.getThemeMode())
    }

    @Test
    fun `verify security manager pin hashing and validation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)
        val security = SecurityManager(context, prefs)

        security.setupNewPin("1234")
        assertTrue(prefs.isPinEnabled.value)
        assertTrue(security.verifyPin("1234"))
        assertFalse(security.verifyPin("9999"))
    }
}
