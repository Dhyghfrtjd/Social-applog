package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SocialPlatform
import com.example.viewmodel.AppNavDestination
import com.example.viewmodel.OmniViewModel

data class BottomNavItem(
    val destination: AppNavDestination,
    val label: String,
    val icon: ImageVector,
    val brandColor: Color? = null,
    val testTag: String
)

@Composable
fun OmniBottomNav(
    currentDestination: AppNavDestination,
    viewModel: OmniViewModel,
    onNavigate: (AppNavDestination) -> Unit
) {
    val whatsappUnread by viewModel.whatsappUnread.collectAsState()
    val instagramUnread by viewModel.instagramUnread.collectAsState()
    val facebookUnread by viewModel.facebookUnread.collectAsState()
    val messengerUnread by viewModel.messengerUnread.collectAsState()

    val navItems = listOf(
        BottomNavItem(
            destination = AppNavDestination.DASHBOARD,
            label = "Home",
            icon = Icons.Default.Dashboard,
            testTag = "nav_dashboard"
        ),
        BottomNavItem(
            destination = AppNavDestination.WHATSAPP,
            label = "WhatsApp",
            icon = Icons.Default.Forum,
            brandColor = SocialPlatform.WHATSAPP.brandColor,
            testTag = "nav_whatsapp"
        ),
        BottomNavItem(
            destination = AppNavDestination.INSTAGRAM,
            label = "Instagram",
            icon = Icons.Default.CameraAlt,
            brandColor = SocialPlatform.INSTAGRAM.brandColor,
            testTag = "nav_instagram"
        ),
        BottomNavItem(
            destination = AppNavDestination.FACEBOOK,
            label = "Facebook",
            icon = Icons.Default.People,
            brandColor = SocialPlatform.FACEBOOK.brandColor,
            testTag = "nav_facebook"
        ),
        BottomNavItem(
            destination = AppNavDestination.MESSENGER,
            label = "Messenger",
            icon = Icons.Default.Chat,
            brandColor = SocialPlatform.MESSENGER.brandColor,
            testTag = "nav_messenger"
        ),
        BottomNavItem(
            destination = AppNavDestination.SETTINGS,
            label = "Tools",
            icon = Icons.Default.Shield,
            testTag = "nav_settings"
        )
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("omni_bottom_nav")
    ) {
        navItems.forEach { item ->
            val isSelected = currentDestination == item.destination
            val unread = when (item.destination) {
                AppNavDestination.WHATSAPP -> whatsappUnread
                AppNavDestination.INSTAGRAM -> instagramUnread
                AppNavDestination.FACEBOOK -> facebookUnread
                AppNavDestination.MESSENGER -> messengerUnread
                else -> 0
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.destination) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (unread > 0) {
                                Badge(
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (unread > 9) "9+" else unread.toString(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        maxLines = 1,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = item.brandColor ?: MaterialTheme.colorScheme.primary,
                    selectedTextColor = item.brandColor ?: MaterialTheme.colorScheme.primary,
                    indicatorColor = (item.brandColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
