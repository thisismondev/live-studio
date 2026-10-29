package id.co.mondo.livestudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import id.co.mondo.livestudio.core.service.NotificationHelper
import id.co.mondo.livestudio.data.streaming.StreamEngineController
import id.co.mondo.livestudio.ui.navigation.AppNavigation
import id.co.mondo.livestudio.ui.theme.LiveStudioTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var streamEngineController: StreamEngineController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handlePickAppIntent(intent)
        setContent {
            LiveStudioTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePickAppIntent(intent)
    }

    /** Tombol "Ganti Aplikasi" di notifikasi siaran meminta pemilih aplikasi dibuka. */
    private fun handlePickAppIntent(intent: Intent?) {
        if (intent?.action == NotificationHelper.ACTION_PICK_APP) {
            streamEngineController.requestPicker()
        }
    }
}