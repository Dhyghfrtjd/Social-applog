package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.SocialPlatform
import com.example.ui.components.OmniBottomNav
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OfflineDraftsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.screens.native.NativeFacebookScreen
import com.example.ui.screens.native.NativeInstagramScreen
import com.example.ui.screens.native.NativeMessengerScreen
import com.example.ui.screens.native.NativeWhatsAppScreen
import com.example.ui.screens.native.WebBridgeDialog
import com.example.ui.theme.OmniSocialTheme
import com.example.viewmodel.AppNavDestination
import com.example.viewmodel.OmniViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniMainApp(
    viewModel: OmniViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val accentTheme by viewModel.accentTheme.collectAsState()
    val highContrast by viewModel.highContrastMode.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val currentDestination by viewModel.currentDestination.collectAsState()
    val showWebBridge by viewModel.showWebBridge.collectAsState()

    var isSettingUpPin by remember { mutableStateOf(false) }

    OmniSocialTheme(
        themeMode = themeMode,
        accentTheme = accentTheme,
        highContrast = highContrast
    ) {
        if (isLocked) {
            LockScreen(
                securityManager = viewModel.securityManager,
                isInitialSetup = false,
                onUnlocked = { viewModel.securityManager.unlockWithoutPin() }
            )
        } else if (isSettingUpPin) {
            LockScreen(
                securityManager = viewModel.securityManager,
                isInitialSetup = true,
                onUnlocked = { isSettingUpPin = false }
            )
        } else {
            // Handle back button for secondary screens
            BackHandler(enabled = currentDestination != AppNavDestination.DASHBOARD) {
                viewModel.navigateTo(AppNavDestination.DASHBOARD)
            }

            Scaffold(
                topBar = {
                    if (currentDestination in listOf(
                            AppNavDestination.NOTIFICATIONS,
                            AppNavDestination.VAULT,
                            AppNavDestination.OFFLINE_DRAFTS,
                            AppNavDestination.SETTINGS
                        )
                    ) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = currentDestination.title,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { viewModel.navigateTo(AppNavDestination.DASHBOARD) }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back to Dashboard"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                },
                bottomBar = {
                    OmniBottomNav(
                        currentDestination = currentDestination,
                        viewModel = viewModel,
                        onNavigate = { dest -> viewModel.navigateTo(dest) }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = currentDestination,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { target ->
                        when (target) {
                            AppNavDestination.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                            AppNavDestination.WHATSAPP -> NativeWhatsAppScreen(viewModel = viewModel)
                            AppNavDestination.INSTAGRAM -> NativeInstagramScreen(viewModel = viewModel)
                            AppNavDestination.FACEBOOK -> NativeFacebookScreen(viewModel = viewModel)
                            AppNavDestination.MESSENGER -> NativeMessengerScreen(viewModel = viewModel)
                            AppNavDestination.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                            AppNavDestination.VAULT -> VaultScreen(viewModel = viewModel)
                            AppNavDestination.OFFLINE_DRAFTS -> OfflineDraftsScreen(viewModel = viewModel)
                            AppNavDestination.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                onOpenPinSetup = { isSettingUpPin = true }
                            )
                        }
                    }

                    // Optional Web Bridge Sheet (when user explicitly requests QR code sync)
                    if (showWebBridge) {
                        WebBridgeDialog(
                            platform = when (currentDestination) {
                                AppNavDestination.WHATSAPP -> SocialPlatform.WHATSAPP
                                AppNavDestination.INSTAGRAM -> SocialPlatform.INSTAGRAM
                                AppNavDestination.FACEBOOK -> SocialPlatform.FACEBOOK
                                AppNavDestination.MESSENGER -> SocialPlatform.MESSENGER
                                else -> SocialPlatform.WHATSAPP
                            },
                            viewModel = viewModel,
                            onDismiss = { viewModel.toggleWebBridge(false) }
                        )
                    }
                }
            }
        }
    }
}
