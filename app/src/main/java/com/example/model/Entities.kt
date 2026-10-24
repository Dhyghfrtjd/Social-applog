package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class SocialNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val platformId: String,
    val title: String,
    val snippet: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val deepLinkUrl: String? = null
)

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val accountIdentifier: String,
    val encryptedSecret: String,
    val platformId: String,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_drafts")
data class OfflineDraft(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val platformId: String,
    val targetUserOrGroup: String,
    val content: String,
    val isSentOrArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isSyncedToCloud: Boolean = false
)

@Entity(tableName = "platform_metrics")
data class PlatformMetric(
    @PrimaryKey
    val platformId: String,
    val minutesActiveToday: Int = 0,
    val sessionCount: Int = 0,
    val lastAccessedTimestamp: Long = System.currentTimeMillis()
)

enum class ThemeMode(val label: String) {
    SYSTEM("Auto (System)"),
    PURE_AMOLED("Pure AMOLED Dark"),
    MIDNIGHT("Midnight Blue"),
    SOFT_DARK("Soft Dark"),
    LIGHT("Clean Light")
}

enum class AccentTheme(val label: String, val primaryHex: Long) {
    CLASSIC_SOCIAL("Social Adaptive", 0xFF6366F1),
    EMERALD("WhatsApp Emerald", 0xFF10B981),
    CYBER_PINK("Instagram Rose", 0xFFE1306C),
    OCEAN_BLUE("Messenger Blue", 0xFF0084FF),
    PURPLE("Royal Indigo", 0xFF6366F1),
    AMBER("Warm Amber", 0xFFF59E0B)
}
