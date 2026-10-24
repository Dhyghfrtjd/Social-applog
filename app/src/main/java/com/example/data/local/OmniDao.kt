package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.OfflineDraft
import com.example.model.PlatformMetric
import com.example.model.SocialNotification
import com.example.model.VaultItem
import kotlinx.coroutines.flow.Flow

@Dao
interface OmniDao {
    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<SocialNotification>>

    @Query("SELECT * FROM notifications WHERE platformId = :platformId ORDER BY timestamp DESC")
    fun getNotificationsForPlatform(platformId: String): Flow<List<SocialNotification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE platformId = :platformId AND isRead = 0")
    fun getUnreadCountForPlatform(platformId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SocialNotification): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE platformId = :platformId")
    suspend fun markPlatformNotificationsRead(platformId: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // Vault
    @Query("SELECT * FROM vault_items ORDER BY updatedAt DESC")
    fun getAllVaultItems(): Flow<List<VaultItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItem): Long

    @Update
    suspend fun updateVaultItem(item: VaultItem)

    @Delete
    suspend fun deleteVaultItem(item: VaultItem)

    // Offline Drafts & Notes
    @Query("SELECT * FROM offline_drafts WHERE isSentOrArchived = 0 ORDER BY createdAt DESC")
    fun getActiveDrafts(): Flow<List<OfflineDraft>>

    @Query("SELECT * FROM offline_drafts ORDER BY createdAt DESC")
    fun getAllDrafts(): Flow<List<OfflineDraft>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: OfflineDraft): Long

    @Update
    suspend fun updateDraft(draft: OfflineDraft)

    @Delete
    suspend fun deleteDraft(draft: OfflineDraft)

    @Query("UPDATE offline_drafts SET isSyncedToCloud = 1 WHERE isSyncedToCloud = 0")
    suspend fun markAllDraftsSynced()

    // Metrics
    @Query("SELECT * FROM platform_metrics")
    fun getAllMetrics(): Flow<List<PlatformMetric>>

    @Query("SELECT * FROM platform_metrics WHERE platformId = :platformId LIMIT 1")
    suspend fun getMetric(platformId: String): PlatformMetric?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetric(metric: PlatformMetric)
}
