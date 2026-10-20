package id.co.mondo.livestudio.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Permissions : Screen("permissions")
    data object Home : Screen("home")
}