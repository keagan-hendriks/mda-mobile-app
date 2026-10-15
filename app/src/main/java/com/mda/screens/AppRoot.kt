package com.mda.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

enum class Route { Login, Register, Main }

/**
 * Entry point for all screens. MainActivity just calls AppRoot().
 *
 * Navigation is a simple "which screen am I on" variable so no extra
 * libraries are needed. If the team later adds Navigation Compose,
 * only this file has to change.
 */
@Composable
fun AppRoot() {
    // UI teammate: swap this MaterialTheme for the project's real theme when it's ready.
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AppColors.Ink,
            background = Color.White,
            surface = Color.White,
            onBackground = AppColors.Ink,
            onSurface = AppColors.Ink
        )
    ) {
        var route by remember { mutableStateOf(Route.Login) }

        when (route) {
            Route.Login -> LoginScreen(
                onLogin = { route = Route.Main },
                onGoToRegister = { route = Route.Register }
            )
            Route.Register -> RegistrationScreen(
                onRegister = { route = Route.Main },
                onGoToLogin = { route = Route.Login }
            )
            Route.Main -> MainScaffold(onLogout = { route = Route.Login })
        }
    }
}

/** The 5 tabs with the bottom bar. */
@Composable
fun MainScaffold(onLogout: () -> Unit) {
    var tab by remember { mutableStateOf(BottomTab.Home) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(selected = tab, onSelect = { tab = it }) }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when (tab) {
                BottomTab.Home -> HomeScreen()
                BottomTab.Search -> SearchScreen()
                BottomTab.Alerts -> NotificationsScreen()
                BottomTab.Profile -> ProfileScreen(onLogout = onLogout)
                BottomTab.Settings -> SettingsScreen()
            }
        }
    }
}
