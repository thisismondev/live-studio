package id.co.mondo.livestudio.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.co.mondo.livestudio.core.util.PermissionHelper
import id.co.mondo.livestudio.domain.model.isActive
import id.co.mondo.livestudio.ui.screens.home.HomeScreen
import id.co.mondo.livestudio.ui.screens.login.LoginScreen
import id.co.mondo.livestudio.ui.screens.permissions.PermissionsOverviewScreen
import id.co.mondo.livestudio.ui.screens.status.StreamStatusScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sessionState by appViewModel.sessionState.collectAsState()

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionState) {
        when (sessionState) {
            SessionState.Loading -> Unit
            SessionState.LoggedIn -> {
                if (startDestination == null) {
                    startDestination = when {
                        appViewModel.streamStatus.value.isActive() -> Screen.StreamStatus.route
                        PermissionHelper.hasRequired(context) -> Screen.Home.route
                        else -> Screen.Permissions.route
                    }
                }
            }
            SessionState.LoggedOut -> {
                if (startDestination == null) {
                    startDestination = Screen.Login.route
                } else if (navController.currentDestination?.route != Screen.Login.route) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    val start = startDestination
    if (start == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    NavHost(
        navController = navController,
        startDestination = start,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    if (navController.currentDestination?.route == Screen.Login.route) {
                        val target = if (PermissionHelper.hasRequired(context)) {
                            Screen.Home.route
                        } else {
                            Screen.Permissions.route
                        }
                        navController.navigate(target) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Screen.Permissions.route) {
            PermissionsOverviewScreen(
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onStartBroadcast = {
                    navController.navigate(Screen.StreamStatus.route)
                }
            )
        }
        composable(Screen.StreamStatus.route) {
            StreamStatusScreen(
                onStreamFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}