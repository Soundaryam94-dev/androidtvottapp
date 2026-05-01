package com.example.myottapp.core.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myottapp.features.detail.DetailScreen
import com.example.myottapp.features.home.NetflixHomeScreen
import com.example.myottapp.features.movielist.MovieListScreen
import com.example.myottapp.features.movielist.MoviesScreen
import com.example.myottapp.features.movielist.MovieListType
import com.example.myottapp.features.mylist.MyListScreen
import com.example.myottapp.features.notifications.NotificationScreen
import com.example.myottapp.features.player.PlayerScreen
import com.example.myottapp.features.profile.ProfileScreen
import com.example.myottapp.features.search.SearchScreen
import com.example.myottapp.features.settings.SettingsScreen

object Routes {
    const val HOME          = "home"
    const val SEARCH        = "search"
    const val MYLIST        = "library"
    const val PROFILE       = "profile"
    const val SETTINGS      = "settings"
    const val NOTIFICATIONS = "notifications"
    const val PLAYER        = "player/{movieId}/{movieTitle}/{playerMode}"
    const val DETAIL        = "detail/{movieId}"
    const val MOVIE_LIST    = "movielist/{category}"     // ✅ See All screen

    fun movieList(category: String) = "movielist/$category"

    fun playMovie(movieId: Int, title: String)   = "player/$movieId/${Uri.encode(title)}/movie"
    fun playTrailer(movieId: Int, title: String) = "player/$movieId/${Uri.encode(title)}/trailer"
    // ✅ Resume from last position — startPosition in milliseconds
    fun resumeMovie(movieId: Int, title: String, startPosition: Long) =
        "player/$movieId/${Uri.encode(title)}/movie?start=$startPosition"
    fun detail(movieId: Int) = "detail/$movieId"
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        NavHost(
            navController    = navController,
            startDestination = Routes.HOME,
            modifier         = Modifier.fillMaxSize()
        ) {

            // ── Home ──────────────────────────────────────────────────────
            composable(Routes.HOME) {
                NetflixHomeScreen(
                    onPlayMovie   = { id, title -> navController.navigate(Routes.playMovie(id, title)) },
                    onPlayTrailer = { id, title -> navController.navigate(Routes.playTrailer(id, title)) },
                    onDetails     = { id -> navController.navigate(Routes.detail(id)) },
                    onSearch      = { navController.navigate(Routes.SEARCH) { launchSingleTop = true } },
                    onProfile     = { navController.navigate(Routes.PROFILE) { launchSingleTop = true } },
                    onNavigate    = { route ->
                        if (route != Routes.HOME)
                            navController.navigate(route) { launchSingleTop = true }
                    }
                )
            }

            // ── Search ────────────────────────────────────────────────────
            composable(Routes.SEARCH) {
                SearchScreen(
                    onMovieClick = { id, title -> navController.navigate(Routes.playTrailer(id, title)) },
                    onBack       = { navController.popBackStack() }
                )
            }

            // ── My List ───────────────────────────────────────────────────
            composable(Routes.MYLIST) {
                MyListScreen(
                    onMovieClick = { id, title -> navController.navigate(Routes.playTrailer(id, title)) },
                    onBack       = { navController.popBackStack() }
                )
            }

            // ── Profile ───────────────────────────────────────────────────
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onBack   = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                    }
                )
            }

            // ── Settings ──────────────────────────────────────────────────
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack   = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                    }
                )
            }

            // ── Notifications ─────────────────────────────────────────────
            composable(Routes.NOTIFICATIONS) {
                NotificationScreen(onBack = { navController.popBackStack() })
            }

            // ── Detail ────────────────────────────────────────────────────
            composable(
                route     = Routes.DETAIL,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType; defaultValue = 0 }
                )
            ) { back ->
                val movieId = back.arguments?.getInt("movieId") ?: 0
                DetailScreen(
                    movieId       = movieId,
                    onBack        = { navController.popBackStack() },
                    onPlayMovie   = { id, title -> navController.navigate(Routes.playMovie(id, title)) },
                    onPlayTrailer = { id, title -> navController.navigate(Routes.playTrailer(id, title)) },
                    onMovieClick  = { id, _ -> navController.navigate(Routes.detail(id)) }
                )
            }

            // ── Movie List (See All) ─────────────────────────────────────
            composable(
                route     = Routes.MOVIE_LIST,
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType; defaultValue = "trending" }
                )
            ) { back ->
                val category = back.arguments?.getString("category") ?: "trending"
                // ✅ Convert category string → MovieListType enum
                val listType = when (category) {
                    "popular"  -> MovieListType.POPULAR
                    "toprated" -> MovieListType.TOP_RATED
                    else       -> MovieListType.TRENDING
                }
                MovieListScreen(
                    listType     = listType,
                    onBack       = { navController.popBackStack() },
                    onMovieClick = { id, _ -> navController.navigate(Routes.detail(id)) }
                )
            }

            // ── Player ────────────────────────────────────────────────────
            composable(
                route     = Routes.PLAYER,
                arguments = listOf(
                    navArgument("movieId")    { type = NavType.IntType;    defaultValue = 0 },
                    navArgument("movieTitle") { type = NavType.StringType; defaultValue = "" },
                    navArgument("playerMode") { type = NavType.StringType; defaultValue = "trailer" }
                )
            ) { back ->
                val movieId    = back.arguments?.getInt("movieId") ?: 0
                val movieTitle = Uri.decode(back.arguments?.getString("movieTitle") ?: "")
                val playerMode = back.arguments?.getString("playerMode") ?: "trailer"
                PlayerScreen(
                    movieId    = movieId,
                    title      = movieTitle,
                    playerMode = playerMode,
                    onBack     = { navController.popBackStack() },
                    onRetry    = { navController.popBackStack() }
                )
            }
        }
    }
}
