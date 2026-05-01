package com.example.myottapp.core.navigation

sealed class Screen(val route: String) {
    object Splash        : Screen("splash")
    object Login         : Screen("login")
    object Home          : Screen("home")
    object Error         : Screen("error")
    object Search        : Screen("search")
    object Settings      : Screen("settings")
    object Library       : Screen("library")
    object Subscriptions : Screen("subscriptions")

    object Player : Screen("player/{videoUrl}") {
        fun createRoute(url: String) =
            "player/${android.net.Uri.encode(url)}"
    }
}