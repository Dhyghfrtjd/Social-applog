package com.example.model

import androidx.compose.ui.graphics.Color

enum class SocialPlatform(
    val id: String,
    val displayName: String,
    val mobileUrl: String,
    val desktopUrl: String,
    val brandColor: Color,
    val accentHex: String,
    val defaultIsDesktop: Boolean = false,
    val iconDescription: String
) {
    WHATSAPP(
        id = "whatsapp",
        displayName = "WhatsApp",
        // WhatsApp Web requires Desktop User-Agent on mobile browsers to show QR code / web client
        mobileUrl = "https://web.whatsapp.com",
        desktopUrl = "https://web.whatsapp.com",
        brandColor = Color(0xFF25D366),
        accentHex = "#25D366",
        defaultIsDesktop = true,
        iconDescription = "WhatsApp Web"
    ),
    INSTAGRAM(
        id = "instagram",
        displayName = "Instagram",
        mobileUrl = "https://www.instagram.com",
        desktopUrl = "https://www.instagram.com",
        brandColor = Color(0xFFE1306C),
        accentHex = "#E1306C",
        defaultIsDesktop = false,
        iconDescription = "Instagram"
    ),
    FACEBOOK(
        id = "facebook",
        displayName = "Facebook",
        mobileUrl = "https://m.facebook.com",
        desktopUrl = "https://www.facebook.com",
        brandColor = Color(0xFF1877F2),
        accentHex = "#1877F2",
        defaultIsDesktop = false,
        iconDescription = "Facebook"
    ),
    MESSENGER(
        id = "messenger",
        displayName = "Messenger",
        mobileUrl = "https://www.messenger.com",
        desktopUrl = "https://www.messenger.com",
        brandColor = Color(0xFF0084FF),
        accentHex = "#0084FF",
        defaultIsDesktop = false,
        iconDescription = "Messenger"
    );

    fun getInitialUrl(useDesktop: Boolean): String {
        return if (useDesktop) desktopUrl else mobileUrl
    }
}
