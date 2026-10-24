package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.model.SocialNotification
import com.example.model.SocialPlatform
import com.example.viewmodel.AppNavDestination
import com.example.viewmodel.OmniViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(viewModel: OmniViewModel) {
    val allNotifications by viewModel.allNotifications.collectAsState()
    val selectedFilter by viewModel.selectedPlatformFilter.collectAsState()
    var showSendDialog by remember { mutableStateOf(false) }

    // Android 13+ permission launcher for notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission state handled gracefully
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val filteredNotifications = remember(allNotifications, selectedFilter) {
        if (selectedFilter == null || selectedFilter == "all") {
            allNotifications
        } else {
            allNotifications.filter { it.platformId == selectedFilter }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("notifications_screen")
    ) {
        // Top Action Bar
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Push Notification Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Real-time alerts for all connected accounts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showSendDialog = true },
                            modifier = Modifier.testTag("trigger_push_dialog_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAlert,
                                contentDescription = "Simulate New Push Alert",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (allNotifications.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearAllNotifications() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear All",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Platform Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { viewModel.setPlatformFilter("all") },
                        label = { Text("All Alerts (${allNotifications.size})") },
                        colors = FilterChipDefaults.filterChipColors()
                    )

                    SocialPlatform.entries.forEach { platform ->
                        val count = allNotifications.count { it.platformId == platform.id }
                        FilterChip(
                            selected = selectedFilter == platform.id,
                            onClick = { viewModel.setPlatformFilter(platform.id) },
                            label = { Text("${platform.displayName} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = platform.brandColor.copy(alpha = 0.2f),
                                selectedLabelColor = platform.brandColor
                            )
                        )
                    }
                }
            }
        }

        // Notification List
        if (filteredNotifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Notifications Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Trigger a push alert test to verify the heads-up notification channel.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showSendDialog = true },
                        modifier = Modifier.testTag("empty_simulate_push_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AddAlert, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Trigger Simulated Push Alert")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredNotifications, key = { it.id }) { notif ->
                    NotificationCard(
                        notification = notif,
                        onMarkRead = { viewModel.markNotificationAsRead(notif.id) },
                        onOpenPlatform = {
                            viewModel.markNotificationAsRead(notif.id)
                            when (notif.platformId) {
                                SocialPlatform.WHATSAPP.id -> viewModel.navigateTo(AppNavDestination.WHATSAPP)
                                SocialPlatform.INSTAGRAM.id -> viewModel.navigateTo(AppNavDestination.INSTAGRAM)
                                SocialPlatform.FACEBOOK.id -> viewModel.navigateTo(AppNavDestination.FACEBOOK)
                                SocialPlatform.MESSENGER.id -> viewModel.navigateTo(AppNavDestination.MESSENGER)
                                else -> viewModel.navigateTo(AppNavDestination.DASHBOARD)
                            }
                        }
                    )
                }
            }
        }
    }

    // Trigger Push Dialog
    if (showSendDialog) {
        SimulatePushDialog(
            onDismiss = { showSendDialog = false },
            onSend = { platform, title, message ->
                viewModel.triggerSimulatedPush(platform, title, message)
                showSendDialog = false
            }
        )
    }
}

@Composable
fun NotificationCard(
    notification: SocialNotification,
    onMarkRead: () -> Unit,
    onOpenPlatform: () -> Unit
) {
    val platform = SocialPlatform.entries.find { it.id == notification.platformId } ?: SocialPlatform.WHATSAPP
    val timeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(notification.timestamp))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_card_${notification.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(platform.brandColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = platform.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = platform.brandColor
                    )
                    if (!notification.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                "NEW",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.snippet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!notification.isRead) {
                    TextButton(onClick = onMarkRead) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Read")
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onOpenPlatform,
                    colors = ButtonDefaults.buttonColors(containerColor = platform.brandColor)
                ) {
                    Text("Open Chat", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SimulatePushDialog(
    onDismiss: () -> Unit,
    onSend: (SocialPlatform, String, String) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(SocialPlatform.WHATSAPP) }
    var title by remember { mutableStateOf("David Miller") }
    var message by remember { mutableStateOf("Can you send me the latest project files?") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Simulate Push Notification") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Select an account to trigger real Android notification bar heads-up alert with custom vibration:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Platform selector pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SocialPlatform.entries.forEach { platform ->
                        FilterChip(
                            selected = selectedPlatform == platform,
                            onClick = {
                                selectedPlatform = platform
                                title = when (platform) {
                                    SocialPlatform.WHATSAPP -> "David Miller"
                                    SocialPlatform.INSTAGRAM -> "@nature_explorer"
                                    SocialPlatform.FACEBOOK -> "Android Developers"
                                    SocialPlatform.MESSENGER -> "Team Sync Group"
                                }
                                message = when (platform) {
                                    SocialPlatform.WHATSAPP -> "Voice call missed at 2:45 PM."
                                    SocialPlatform.INSTAGRAM -> "Tagged you in a reel: 'Best dev setups!'"
                                    SocialPlatform.FACEBOOK -> "New post: 'Kotlin 2.2 features announced'"
                                    SocialPlatform.MESSENGER -> "Alex: Ready for the demo call?"
                                }
                            },
                            label = { Text(platform.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = platform.brandColor.copy(alpha = 0.25f),
                                selectedLabelColor = platform.brandColor
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Sender Name / Channel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Snippet") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(selectedPlatform, title, message) },
                colors = ButtonDefaults.buttonColors(containerColor = selectedPlatform.brandColor),
                modifier = Modifier.testTag("send_simulated_push_confirm")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send Alert", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
