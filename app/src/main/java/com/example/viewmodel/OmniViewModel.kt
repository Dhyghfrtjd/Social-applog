package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NotificationHelper
import com.example.data.PreferencesManager
import com.example.data.local.OmniDatabase
import com.example.model.AccentTheme
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.OfflineDraft
import com.example.model.PlatformMetric
import com.example.model.SocialCall
import com.example.model.SocialNotification
import com.example.model.SocialPlatform
import com.example.model.SocialPost
import com.example.model.SocialStory
import com.example.model.ThemeMode
import com.example.model.VaultItem
import com.example.security.SecurityManager
import com.example.web.WebViewManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppNavDestination(val title: String) {
    DASHBOARD("Dashboard"),
    WHATSAPP("WhatsApp"),
    INSTAGRAM("Instagram"),
    FACEBOOK("Facebook"),
    MESSENGER("Messenger"),
    NOTIFICATIONS("Notifications"),
    VAULT("Security Vault"),
    OFFLINE_DRAFTS("Offline Hub"),
    SETTINGS("Settings")
}

enum class WhatsAppSubTab { CHATS, STATUS, CALLS }
enum class InstagramSubTab { FEED, DM }
enum class FacebookSubTab { FEED, GROUPS, WATCH }

class OmniViewModel(application: Application) : AndroidViewModel(application) {

    val database = OmniDatabase.getDatabase(application)
    val dao = database.omniDao()
    val preferencesManager = PreferencesManager(application)
    val notificationHelper = NotificationHelper(application)
    val securityManager = SecurityManager(application, preferencesManager)
    val webViewManager = WebViewManager(application)

    private val _currentDestination = MutableStateFlow(AppNavDestination.DASHBOARD)
    val currentDestination: StateFlow<AppNavDestination> = _currentDestination.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _selectedPlatformFilter = MutableStateFlow<String?>("all")
    val selectedPlatformFilter: StateFlow<String?> = _selectedPlatformFilter.asStateFlow()

    // Preferences forwarded
    val themeMode = preferencesManager.themeMode
    val accentTheme = preferencesManager.accentTheme
    val highContrastMode = preferencesManager.highContrastMode
    val isPinEnabled = preferencesManager.isPinEnabled
    val isBiometricEnabled = preferencesManager.isBiometricEnabled
    val isCloudSyncEnabled = preferencesManager.isCloudSyncEnabled
    val lastSyncTimestamp = preferencesManager.lastSyncTimestamp
    val incognitoMode = preferencesManager.incognitoMode
    val blockTrackers = preferencesManager.blockTrackers

    // Security
    val isLocked = securityManager.isLocked
    val unlockAttempts = securityManager.unlockAttempts

    // Notifications from DB
    val allNotifications = dao.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalUnreadCount = dao.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val whatsappUnread = dao.getUnreadCountForPlatform(SocialPlatform.WHATSAPP.id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val instagramUnread = dao.getUnreadCountForPlatform(SocialPlatform.INSTAGRAM.id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val facebookUnread = dao.getUnreadCountForPlatform(SocialPlatform.FACEBOOK.id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val messengerUnread = dao.getUnreadCountForPlatform(SocialPlatform.MESSENGER.id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Drafts
    val activeDrafts = dao.getActiveDrafts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Vault
    val vaultItems = dao.getAllVaultItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics
    val platformMetrics = dao.getAllMetrics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Native Social Client States
    private val _whatsAppSubTab = MutableStateFlow(WhatsAppSubTab.CHATS)
    val whatsAppSubTab: StateFlow<WhatsAppSubTab> = _whatsAppSubTab.asStateFlow()

    private val _instagramSubTab = MutableStateFlow(InstagramSubTab.FEED)
    val instagramSubTab: StateFlow<InstagramSubTab> = _instagramSubTab.asStateFlow()

    private val _activeChatThread = MutableStateFlow<ChatThread?>(null)
    val activeChatThread: StateFlow<ChatThread?> = _activeChatThread.asStateFlow()

    private val _showWebBridge = MutableStateFlow(false)
    val showWebBridge: StateFlow<Boolean> = _showWebBridge.asStateFlow()

    // In-memory interactive native feeds and chat threads
    private val _whatsAppThreads = MutableStateFlow(initialWhatsAppThreads())
    val whatsAppThreads: StateFlow<List<ChatThread>> = _whatsAppThreads.asStateFlow()

    private val _messengerThreads = MutableStateFlow(initialMessengerThreads())
    val messengerThreads: StateFlow<List<ChatThread>> = _messengerThreads.asStateFlow()

    private val _instagramThreads = MutableStateFlow(initialInstagramThreads())
    val instagramThreads: StateFlow<List<ChatThread>> = _instagramThreads.asStateFlow()

    private val _instagramPosts = MutableStateFlow(initialInstagramPosts())
    val instagramPosts: StateFlow<List<SocialPost>> = _instagramPosts.asStateFlow()

    private val _facebookPosts = MutableStateFlow(initialFacebookPosts())
    val facebookPosts: StateFlow<List<SocialPost>> = _facebookPosts.asStateFlow()

    private val _socialStories = MutableStateFlow(initialSocialStories())
    val socialStories: StateFlow<List<SocialStory>> = _socialStories.asStateFlow()

    private val _callLogs = MutableStateFlow(initialCallLogs())
    val callLogs: StateFlow<List<SocialCall>> = _callLogs.asStateFlow()

    // Filtered search across notifications, drafts, and chats
    val searchResults = combine(
        searchQuery,
        allNotifications,
        activeDrafts,
        vaultItems
    ) { query, notifs, drafts, vault ->
        if (query.isBlank()) {
            SearchResults(emptyList(), emptyList(), emptyList())
        } else {
            val q = query.trim().lowercase()
            SearchResults(
                notifications = notifs.filter { it.title.lowercase().contains(q) || it.snippet.lowercase().contains(q) },
                drafts = drafts.filter { it.targetUserOrGroup.lowercase().contains(q) || it.content.lowercase().contains(q) },
                vaultItems = vault.filter { it.title.lowercase().contains(q) || it.accountIdentifier.lowercase().contains(q) }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    init {
        monitorNetwork()
        seedInitialDemoDataIfNeeded()
    }

    private fun monitorNetwork() {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val initialCapabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        _isOnline.value = initialCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }
        })
    }

    private fun seedInitialDemoDataIfNeeded() {
        viewModelScope.launch {
            SocialPlatform.entries.forEach { platform ->
                val existing = dao.getMetric(platform.id)
                if (existing == null) {
                    dao.insertOrUpdateMetric(
                        PlatformMetric(
                            platformId = platform.id,
                            minutesActiveToday = when (platform) {
                                SocialPlatform.WHATSAPP -> 42
                                SocialPlatform.INSTAGRAM -> 31
                                SocialPlatform.FACEBOOK -> 18
                                SocialPlatform.MESSENGER -> 22
                            },
                            sessionCount = when (platform) {
                                SocialPlatform.WHATSAPP -> 8
                                SocialPlatform.INSTAGRAM -> 6
                                SocialPlatform.FACEBOOK -> 3
                                SocialPlatform.MESSENGER -> 5
                            }
                        )
                    )
                }
            }
        }
    }

    fun navigateTo(destination: AppNavDestination) {
        _currentDestination.value = destination
        _activeChatThread.value = null // reset chat selection on tab change
        _showWebBridge.value = false

        when (destination) {
            AppNavDestination.WHATSAPP -> recordPlatformSession(SocialPlatform.WHATSAPP.id)
            AppNavDestination.INSTAGRAM -> recordPlatformSession(SocialPlatform.INSTAGRAM.id)
            AppNavDestination.FACEBOOK -> recordPlatformSession(SocialPlatform.FACEBOOK.id)
            AppNavDestination.MESSENGER -> recordPlatformSession(SocialPlatform.MESSENGER.id)
            else -> {}
        }
    }

    fun setWhatsAppSubTab(tab: WhatsAppSubTab) {
        _whatsAppSubTab.value = tab
    }

    fun setInstagramSubTab(tab: InstagramSubTab) {
        _instagramSubTab.value = tab
    }

    fun openChatThread(thread: ChatThread) {
        _activeChatThread.value = thread
        // Mark as read
        val list = when (thread.platformId) {
            SocialPlatform.WHATSAPP.id -> _whatsAppThreads
            SocialPlatform.MESSENGER.id -> _messengerThreads
            else -> _instagramThreads
        }
        list.value = list.value.map {
            if (it.id == thread.id) it.copy(unreadCount = 0) else it
        }
    }

    fun closeChatThread() {
        _activeChatThread.value = null
    }

    fun toggleWebBridge(show: Boolean? = null) {
        _showWebBridge.value = show ?: !_showWebBridge.value
    }

    fun sendChatMessage(threadId: String, platformId: String, text: String) {
        if (text.isBlank()) return
        val timeNow = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = "Me",
            isMe = true,
            text = text,
            timeFormatted = timeNow,
            timestamp = System.currentTimeMillis()
        )

        val targetState = when (platformId) {
            SocialPlatform.WHATSAPP.id -> _whatsAppThreads
            SocialPlatform.MESSENGER.id -> _messengerThreads
            else -> _instagramThreads
        }

        targetState.value = targetState.value.map { thread ->
            if (thread.id == threadId) {
                val updatedMessages = thread.messages + newMsg
                val updatedThread = thread.copy(
                    lastMessage = text,
                    lastMessageTime = timeNow,
                    messages = updatedMessages
                )
                if (_activeChatThread.value?.id == threadId) {
                    _activeChatThread.value = updatedThread
                }
                updatedThread
            } else thread
        }

        recordPlatformSession(platformId)

        // Queue to offline drafts if offline
        if (!_isOnline.value) {
            createDraft(platformId, _activeChatThread.value?.contactName ?: "Contact", text)
        } else {
            // Simulate friendly auto-reply after 1.5 seconds
            viewModelScope.launch {
                delay(1500)
                val replyText = when (platformId) {
                    SocialPlatform.WHATSAPP.id -> "Got it! Thanks for keeping me posted on WhatsApp."
                    SocialPlatform.MESSENGER.id -> "Awesome, I see your message. Catch up in a bit!"
                    else -> "Love this! Thanks for reaching out 🙌"
                }
                val replyMsg = ChatMessage(
                    id = "reply_${System.currentTimeMillis()}",
                    senderName = _activeChatThread.value?.contactName ?: "Friend",
                    isMe = false,
                    text = replyText,
                    timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
                    timestamp = System.currentTimeMillis()
                )
                targetState.value = targetState.value.map { thread ->
                    if (thread.id == threadId) {
                        val withReply = thread.messages + replyMsg
                        val refreshed = thread.copy(
                            lastMessage = replyText,
                            lastMessageTime = replyMsg.timeFormatted,
                            messages = withReply
                        )
                        if (_activeChatThread.value?.id == threadId) {
                            _activeChatThread.value = refreshed
                        }
                        refreshed
                    } else thread
                }
            }
        }
    }

    fun toggleLikePost(postId: String, platformId: String) {
        val list = if (platformId == SocialPlatform.INSTAGRAM.id) _instagramPosts else _facebookPosts
        list.value = list.value.map { post ->
            if (post.id == postId) {
                val newLiked = !post.isLiked
                post.copy(
                    isLiked = newLiked,
                    likesCount = if (newLiked) post.likesCount + 1 else post.likesCount - 1
                )
            } else post
        }
    }

    fun toggleSavePost(postId: String, platformId: String) {
        val list = if (platformId == SocialPlatform.INSTAGRAM.id) _instagramPosts else _facebookPosts
        list.value = list.value.map { post ->
            if (post.id == postId) post.copy(isSaved = !post.isSaved) else post
        }
    }

    fun createSocialPost(platformId: String, content: String) {
        if (content.isBlank()) return
        val newPost = SocialPost(
            id = "post_${System.currentTimeMillis()}",
            platformId = platformId,
            authorName = "You",
            authorHandle = "@omni_user",
            authorAvatarColor = Color(0xFF6366F1),
            timeAgo = "Just now",
            contentText = content,
            likesCount = 1,
            commentsCount = 0,
            isLiked = true
        )
        if (platformId == SocialPlatform.INSTAGRAM.id) {
            _instagramPosts.value = listOf(newPost) + _instagramPosts.value
        } else {
            _facebookPosts.value = listOf(newPost) + _facebookPosts.value
        }
        recordPlatformSession(platformId)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPlatformFilter(filter: String?) {
        _selectedPlatformFilter.value = filter
    }

    private fun recordPlatformSession(platformId: String) {
        viewModelScope.launch {
            val metric = dao.getMetric(platformId) ?: PlatformMetric(platformId = platformId)
            dao.insertOrUpdateMetric(
                metric.copy(
                    sessionCount = metric.sessionCount + 1,
                    minutesActiveToday = metric.minutesActiveToday + 2,
                    lastAccessedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun triggerSimulatedPush(platform: SocialPlatform, customTitle: String? = null, customMessage: String? = null) {
        viewModelScope.launch {
            val title = customTitle ?: when (platform) {
                SocialPlatform.WHATSAPP -> "Alex Rivera (WhatsApp)"
                SocialPlatform.INSTAGRAM -> "@design_studio (Instagram)"
                SocialPlatform.FACEBOOK -> "Sarah Jenkins (Facebook)"
                SocialPlatform.MESSENGER -> "Dev Squad Group (Messenger)"
            }

            val snippet = customMessage ?: when (platform) {
                SocialPlatform.WHATSAPP -> "Hey! Did you check out the new design mockup for OmniSocial?"
                SocialPlatform.INSTAGRAM -> "Liked your story and mentioned you in a comment: 'Super fast!'"
                SocialPlatform.FACEBOOK -> "Shared a post in Android Developers Community: 'Top 5 Tips for 2026'"
                SocialPlatform.MESSENGER -> "Meeting moved to 3:00 PM. Let us know if you're joining."
            }

            val notification = SocialNotification(
                platformId = platform.id,
                title = title,
                snippet = snippet,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            val newId = dao.insertNotification(notification)
            val fullNotification = notification.copy(id = newId)

            notificationHelper.showNotification(fullNotification)
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch { dao.markNotificationRead(id) }
    }

    fun markPlatformAsRead(platformId: String) {
        viewModelScope.launch { dao.markPlatformNotificationsRead(platformId) }
    }

    fun clearAllNotifications() {
        viewModelScope.launch { dao.clearAllNotifications() }
    }

    fun createDraft(platformId: String, recipient: String, content: String) {
        viewModelScope.launch {
            dao.insertDraft(
                OfflineDraft(
                    platformId = platformId,
                    targetUserOrGroup = recipient,
                    content = content
                )
            )
        }
    }

    fun deleteDraft(draft: OfflineDraft) {
        viewModelScope.launch { dao.deleteDraft(draft) }
    }

    fun addVaultItem(title: String, identifier: String, secret: String, platformId: String, notes: String) {
        viewModelScope.launch {
            dao.insertVaultItem(
                VaultItem(
                    title = title,
                    accountIdentifier = identifier,
                    encryptedSecret = secret,
                    platformId = platformId,
                    notes = notes
                )
            )
        }
    }

    fun deleteVaultItem(item: VaultItem) {
        viewModelScope.launch { dao.deleteVaultItem(item) }
    }

    fun performCloudSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            delay(1200)
            dao.markAllDraftsSynced()
            preferencesManager.updateLastSyncTime()
            _isSyncing.value = false
        }
    }

    fun clearCacheAndCookies() {
        webViewManager.clearAllCookiesAndCache()
    }

    override fun onCleared() {
        super.onCleared()
        webViewManager.destroy()
    }

    // Initial Mock Seed Data for Native View
    private fun initialWhatsAppThreads(): List<ChatThread> = listOf(
        ChatThread(
            id = "wa_1",
            platformId = SocialPlatform.WHATSAPP.id,
            contactName = "Alex Rivera",
            contactHandle = "+1 (555) 019-2834",
            avatarBgColor = Color(0xFF10B981),
            lastMessage = "Sent the updated presentation deck 📁",
            lastMessageTime = "11:42 AM",
            unreadCount = 2,
            isOnline = true,
            statusText = "At work • Available on WhatsApp",
            messages = listOf(
                ChatMessage("m1", "Alex", false, "Hey, how is the all-in-one app coming along?", "11:38 AM"),
                ChatMessage("m2", "Me", true, "It is super smooth! Instant tab switching and offline caching work great.", "11:40 AM"),
                ChatMessage("m3", "Alex", false, "Sent the updated presentation deck 📁", "11:42 AM")
            )
        ),
        ChatThread(
            id = "wa_2",
            platformId = SocialPlatform.WHATSAPP.id,
            contactName = "Tech Developers Group",
            contactHandle = "32 participants",
            avatarBgColor = Color(0xFF0284C7),
            lastMessage = "Elena: Kotlin 2.2 release notes look incredible!",
            lastMessageTime = "10:15 AM",
            unreadCount = 5,
            isOnline = false,
            messages = listOf(
                ChatMessage("m4", "Elena", false, "Anyone tried Jetpack Compose 1.8 yet?", "10:10 AM"),
                ChatMessage("m5", "Elena", false, "Kotlin 2.2 release notes look incredible!", "10:15 AM")
            )
        ),
        ChatThread(
            id = "wa_3",
            platformId = SocialPlatform.WHATSAPP.id,
            contactName = "Sarah Jenkins",
            contactHandle = "+1 (555) 234-8901",
            avatarBgColor = Color(0xFF8B5CF6),
            lastMessage = "See you tomorrow at 10 AM!",
            lastMessageTime = "Yesterday",
            unreadCount = 0,
            isOnline = false,
            messages = listOf(
                ChatMessage("m6", "Sarah", false, "Are we still meeting up for coffee?", "Yesterday 4:00 PM"),
                ChatMessage("m7", "Me", true, "Yes, absolutely! At the corner cafe.", "Yesterday 4:10 PM"),
                ChatMessage("m8", "Sarah", false, "See you tomorrow at 10 AM!", "Yesterday 4:12 PM")
            )
        ),
        ChatThread(
            id = "wa_4",
            platformId = SocialPlatform.WHATSAPP.id,
            contactName = "Family Hub",
            contactHandle = "Mom, Dad, Sis",
            avatarBgColor = Color(0xFFF59E0B),
            lastMessage = "Dad: Dinner is at 7 tonight",
            lastMessageTime = "Yesterday",
            unreadCount = 0,
            isOnline = false,
            messages = listOf(
                ChatMessage("m9", "Dad", false, "Dinner is at 7 tonight", "Yesterday 6:00 PM")
            )
        )
    )

    private fun initialMessengerThreads(): List<ChatThread> = listOf(
        ChatThread(
            id = "msg_1",
            platformId = SocialPlatform.MESSENGER.id,
            contactName = "Dev Squad",
            contactHandle = "Active 5m ago",
            avatarBgColor = Color(0xFF0084FF),
            lastMessage = "Meeting link posted in chat",
            lastMessageTime = "12:05 PM",
            unreadCount = 1,
            isOnline = true,
            messages = listOf(
                ChatMessage("mm1", "Jordan", false, "Ready for the sprint review?", "12:01 PM"),
                ChatMessage("mm2", "Jordan", false, "Meeting link posted in chat", "12:05 PM")
            )
        ),
        ChatThread(
            id = "msg_2",
            platformId = SocialPlatform.MESSENGER.id,
            contactName = "Emily Zhang",
            contactHandle = "Active now",
            avatarBgColor = Color(0xFFE1306C),
            lastMessage = "Thanks for the quick reply! 👍",
            lastMessageTime = "9:20 AM",
            unreadCount = 0,
            isOnline = true,
            messages = listOf(
                ChatMessage("mm3", "Emily", false, "Did you find that article?", "9:18 AM"),
                ChatMessage("mm4", "Me", true, "Yes, just sent it over to your email.", "9:19 AM"),
                ChatMessage("mm5", "Emily", false, "Thanks for the quick reply! 👍", "9:20 AM")
            )
        ),
        ChatThread(
            id = "msg_3",
            platformId = SocialPlatform.MESSENGER.id,
            contactName = "Marcus Vance",
            contactHandle = "Active 2h ago",
            avatarBgColor = Color(0xFF10B981),
            lastMessage = "Sounds good, catch you later",
            lastMessageTime = "Sunday",
            unreadCount = 0,
            isOnline = false,
            messages = listOf(
                ChatMessage("mm6", "Marcus", false, "Sounds good, catch you later", "Sunday")
            )
        )
    )

    private fun initialInstagramThreads(): List<ChatThread> = listOf(
        ChatThread(
            id = "ig_1",
            platformId = SocialPlatform.INSTAGRAM.id,
            contactName = "Maya Lin",
            contactHandle = "@mayadesigns",
            avatarBgColor = Color(0xFFE1306C),
            lastMessage = "Sent a reel • 2h ago",
            lastMessageTime = "2h",
            unreadCount = 1,
            isOnline = true,
            messages = listOf(
                ChatMessage("ig1", "Maya", false, "Look at this UI animation concept!", "10:00 AM"),
                ChatMessage("ig2", "Maya", false, "Sent a reel • 2h ago", "10:01 AM")
            )
        ),
        ChatThread(
            id = "ig_2",
            platformId = SocialPlatform.INSTAGRAM.id,
            contactName = "Pixel Studio",
            contactHandle = "@pixel_studio_official",
            avatarBgColor = Color(0xFF833AB4),
            lastMessage = "Collab request approved ✨",
            lastMessageTime = "1d",
            unreadCount = 0,
            isOnline = false,
            messages = listOf(
                ChatMessage("ig3", "Pixel Studio", false, "Collab request approved ✨", "Yesterday")
            )
        )
    )

    private fun initialInstagramPosts(): List<SocialPost> = listOf(
        SocialPost(
            id = "ig_post_1",
            platformId = SocialPlatform.INSTAGRAM.id,
            authorName = "Design Odyssey",
            authorHandle = "@design.odyssey",
            authorAvatarColor = Color(0xFFE1306C),
            timeAgo = "1 hour ago",
            contentText = "Minimalism is not the lack of something. It's simply the perfect amount of everything. 📱✨ Building seamless social experiences in Kotlin & Jetpack Compose.",
            mediaType = "image",
            mediaBgColor = Color(0xFF1E293B),
            mediaLabel = "⚡ OmniSocial Native UI",
            likesCount = 1248,
            commentsCount = 64,
            isLiked = false
        ),
        SocialPost(
            id = "ig_post_2",
            platformId = SocialPlatform.INSTAGRAM.id,
            authorName = "Android Creators",
            authorHandle = "@android.creators",
            authorAvatarColor = Color(0xFF10B981),
            timeAgo = "3 hours ago",
            contentText = "Say goodbye to switching between 4 different heavy social apps. All your chats, feeds, and DMs in one streamlined AMOLED dark experience! 🚀🔥 #AndroidDev #OmniSocial #Productivity",
            mediaType = "image",
            mediaBgColor = Color(0xFF0F172A),
            mediaLabel = "🚀 All-In-One Unified Suite",
            likesCount = 3892,
            commentsCount = 142,
            isLiked = true
        ),
        SocialPost(
            id = "ig_post_3",
            platformId = SocialPlatform.INSTAGRAM.id,
            authorName = "Aesthetic Workspace",
            authorHandle = "@workspace.vibe",
            authorAvatarColor = Color(0xFFF59E0B),
            timeAgo = "6 hours ago",
            contentText = "Late-night coding setup with deep AMOLED dark mode to eliminate eye strain. Work peacefully in the dark without glare. 🌙💻",
            mediaType = "image",
            mediaBgColor = Color(0xFF18181B),
            mediaLabel = "🌙 Pitch Black AMOLED Theme",
            likesCount = 875,
            commentsCount = 28,
            isLiked = false
        )
    )

    private fun initialFacebookPosts(): List<SocialPost> = listOf(
        SocialPost(
            id = "fb_post_1",
            platformId = SocialPlatform.FACEBOOK.id,
            authorName = "Android Developers Community",
            authorHandle = "Public Group • 145K members",
            authorAvatarColor = Color(0xFF1877F2),
            timeAgo = "2 hrs • 🌐",
            contentText = "Excited to share the launch of the new unified social architecture! It aggregates WhatsApp, Instagram, Facebook, and Messenger into a single lightweight client with encrypted local storage and offline sync.",
            mediaType = "image",
            mediaBgColor = Color(0xFF1E3A8A),
            mediaLabel = "📊 OmniSocial Hub v1.0",
            likesCount = 320,
            commentsCount = 47,
            isLiked = true
        ),
        SocialPost(
            id = "fb_post_2",
            platformId = SocialPlatform.FACEBOOK.id,
            authorName = "Tech World Updates",
            authorHandle = "Verified News",
            authorAvatarColor = Color(0xFF0284C7),
            timeAgo = "4 hrs • 🌐",
            contentText = "Why lightweight all-in-one social suites are taking over in 2026: reduced memory usage, unified push notifications, and built-in offline draft queuing for uninterrupted travel.",
            likesCount = 184,
            commentsCount = 19,
            isLiked = false
        )
    )

    private fun initialSocialStories(): List<SocialStory> = listOf(
        SocialStory("st_0", "all", "Your Story", Color(0xFF6366F1), hasUnseenStory = false, caption = "Add update"),
        SocialStory("st_1", "whatsapp", "Alex Rivera", Color(0xFF10B981), hasUnseenStory = true, caption = "At the design conference!"),
        SocialStory("st_2", "instagram", "Maya Lin", Color(0xFFE1306C), hasUnseenStory = true, caption = "New art drop today ✨"),
        SocialStory("st_3", "facebook", "Sarah Jenkins", Color(0xFF1877F2), hasUnseenStory = true, caption = "Weekend getaway 🌲"),
        SocialStory("st_4", "messenger", "Jordan Dev", Color(0xFF0084FF), hasUnseenStory = true, caption = "Shipped the update! 🚀")
    )

    private fun initialCallLogs(): List<SocialCall> = listOf(
        SocialCall("c1", "Alex Rivera", Color(0xFF10B981), isVideo = false, isIncoming = true, isMissed = false, timeFormatted = "Today, 11:20 AM"),
        SocialCall("c2", "Sarah Jenkins", Color(0xFF8B5CF6), isVideo = true, isIncoming = false, isMissed = false, timeFormatted = "Yesterday, 3:15 PM"),
        SocialCall("c3", "Tech Developers", Color(0xFF0284C7), isVideo = true, isIncoming = true, isMissed = true, timeFormatted = "October 21, 6:40 PM")
    )
}

data class SearchResults(
    val notifications: List<SocialNotification> = emptyList(),
    val drafts: List<OfflineDraft> = emptyList(),
    val vaultItems: List<VaultItem> = emptyList()
)
