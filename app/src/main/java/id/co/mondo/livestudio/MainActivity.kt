package id.co.mondo.livestudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import id.co.mondo.livestudio.ui.navigation.AppNavigation
import id.co.mondo.livestudio.ui.theme.LiveStudioTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveStudioTheme {
                AppNavigation()
            }
        }
    }
}