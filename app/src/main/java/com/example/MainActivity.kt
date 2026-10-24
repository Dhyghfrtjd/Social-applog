package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.NotificationHelper
import com.example.model.SocialPlatform
import com.example.ui.OmniMainApp
import com.example.viewmodel.AppNavDestination
import com.example.viewmodel.OmniViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: OmniViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleNotificationIntent(intent)

        setContent {
            OmniMainApp(viewModel = viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val targetPlatform = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_PLATFORM)
        if (!targetPlatform.isNullOrBlank()) {
            when (targetPlatform.lowercase()) {
                SocialPlatform.WHATSAPP.id -> viewModel.navigateTo(AppNavDestination.WHATSAPP)
                SocialPlatform.INSTAGRAM.id -> viewModel.navigateTo(AppNavDestination.INSTAGRAM)
                SocialPlatform.FACEBOOK.id -> viewModel.navigateTo(AppNavDestination.FACEBOOK)
                SocialPlatform.MESSENGER.id -> viewModel.navigateTo(AppNavDestination.MESSENGER)
            }
        }
    }
}
