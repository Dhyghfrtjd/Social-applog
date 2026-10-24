package com.example.ui.screens.native

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatThread
import com.example.model.SocialPlatform
import com.example.viewmodel.OmniViewModel
import com.example.viewmodel.WhatsAppSubTab

@Composable
fun NativeWhatsAppScreen(viewModel: OmniViewModel) {
    val activeChat by viewModel.activeChatThread.collectAsState()
    val subTab by viewModel.whatsAppSubTab.collectAsState()
    val threads by viewModel.whatsAppThreads.collectAsState()
    val stories by viewModel.socialStories.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    val showWebBridge by viewModel.showWebBridge.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    if (activeChat != null && activeChat?.platformId == SocialPlatform.WHATSAPP.id) {
        NativeChatConversationScreen(
            thread = activeChat!!,
            onBack = { viewModel.closeChatThread() },
            onSendMessage = { text ->
                viewModel.sendChatMessage(activeChat!!.id, SocialPlatform.WHATSAPP.id, text)
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("native_whatsapp_screen")
    ) {
        // WhatsApp Top Bar Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF075E54))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WhatsApp",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Web QR Bridge") },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleWebBridge(true)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Simulate Incoming Message") },
                                onClick = {
                                    showMenu = false
                                    viewModel.triggerSimulatedPush(SocialPlatform.WHATSAPP)
                                }
                            )
                        }
                    }
                }
            }
        }

        // WhatsApp Tabs: CHATS, STATUS, CALLS
        TabRow(
            selectedTabIndex = subTab.ordinal,
            containerColor = Color(0xFF075E54),
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[subTab.ordinal]),
                    color = Color(0xFF25D366),
                    height = 3.dp
                )
            }
        ) {
            WhatsAppSubTab.entries.forEach { tab ->
                val unreadTotal = if (tab == WhatsAppSubTab.CHATS) threads.sumOf { it.unreadCount } else 0
                Tab(
                    selected = subTab == tab,
                    onClick = { viewModel.setWhatsAppSubTab(tab) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tab.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (subTab == tab) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                            if (unreadTotal > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF25D366)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unreadTotal.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (subTab) {
                WhatsAppSubTab.CHATS -> {
                    WhatsAppChatsList(
                        threads = threads,
                        onThreadClick = { viewModel.openChatThread(it) }
                    )
                }
                WhatsAppSubTab.STATUS -> {
                    WhatsAppStatusList(stories = stories)
                }
                WhatsAppSubTab.CALLS -> {
                    WhatsAppCallsList(calls = callLogs)
                }
            }

            // WhatsApp Floating Action Button
            FloatingActionButton(
                onClick = {
                    if (threads.isNotEmpty()) {
                        viewModel.openChatThread(threads.first())
                    }
                },
                containerColor = Color(0xFF25D366),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("whatsapp_fab_new_chat")
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "New Chat",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun WhatsAppChatsList(
    threads: List<ChatThread>,
    onThreadClick: (ChatThread) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(threads, key = { it.id }) { thread ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThreadClick(thread) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(thread.avatarBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = thread.contactName.take(1),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = thread.contactName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = thread.lastMessageTime,
                            fontSize = 11.sp,
                            color = if (thread.unreadCount > 0) Color(0xFF25D366) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = if (thread.unreadCount == 0) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = thread.lastMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (thread.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF25D366)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = thread.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppStatusList(stories: List<com.example.model.SocialStory>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            // My status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("You", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text("My status", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Tap to add status update", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Text(
                text = "Recent updates",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(stories.filter { it.userName != "Your Story" }) { story ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366).copy(alpha = 0.2f))
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(story.avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(story.userName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(story.userName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(story.caption, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun WhatsAppCallsList(calls: List<com.example.model.SocialCall>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(calls) { call ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(call.avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(call.contactName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(call.contactName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (call.isIncoming) Icons.Default.CallReceived else Icons.Default.CallMade,
                                contentDescription = null,
                                tint = if (call.isMissed) Color(0xFFEF4444) else Color(0xFF25D366),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(call.timeFormatted, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color(0xFF075E54)
                    )
                }
            }
        }
    }
}
