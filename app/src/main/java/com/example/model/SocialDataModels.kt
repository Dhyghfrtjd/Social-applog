package com.example.model

import androidx.compose.ui.graphics.Color

data class ChatMessage(
    val id: String,
    val senderName: String,
    val isMe: Boolean,
    val text: String,
    val timeFormatted: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = true
)

data class ChatThread(
    val id: String,
    val platformId: String,
    val contactName: String,
    val contactHandle: String,
    val avatarBgColor: Color,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val statusText: String = "Hey there! I am using WhatsApp.",
    val messages: List<ChatMessage> = emptyList()
)

data class SocialPost(
    val id: String,
    val platformId: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarColor: Color,
    val timeAgo: String,
    val contentText: String,
    val mediaType: String? = null, // "image", "video", null
    val mediaBgColor: Color? = null,
    val mediaLabel: String? = null,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false
)

data class SocialStory(
    val id: String,
    val platformId: String,
    val userName: String,
    val avatarColor: Color,
    val hasUnseenStory: Boolean = true,
    val caption: String = ""
)

data class SocialCall(
    val id: String,
    val contactName: String,
    val avatarColor: Color,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val isMissed: Boolean,
    val timeFormatted: String
)
