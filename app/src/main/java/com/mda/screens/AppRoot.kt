
package com.mda.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.mda_mobile_app.ui.medication.MedicationScreen
import androidx.compose.ui.platform.LocalContext
import com.example.mda_mobile_app.data.model.users.CreateUserRequest
import com.example.mda_mobile_app.data.model.users.LoginRequest
import com.example.mda_mobile_app.data.remote.RetrofitClient
import kotlinx.coroutines.launch

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
        var firstName by remember { mutableStateOf("") }
        var accountFullName by remember { mutableStateOf("") }
        var accountEmail by remember { mutableStateOf("") }

        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        when (route) {
            Route.Login -> LoginScreen(
                onLogin = { email, password ->
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.login(
                                LoginRequest(
                                    email = email,
                                    password = password
                                )
                            )

                            val result = response.body()
                            val data = result?.data

                            if (
                                response.isSuccessful &&
                                result?.success == true &&
                                data != null
                            ) {
                                // Save the authentication token.
                                context.getSharedPreferences(
                                    "mda_session",
                                    Context.MODE_PRIVATE
                                ).edit()
                                    .putString("auth_token", data.token)
                                    .apply()

                                // Save the user's details from the API response.
                                accountFullName = data.user.fullName
                                accountEmail = data.user.email
                                firstName = accountFullName
                                    .trim()
                                    .substringBefore(" ")

                                Toast.makeText(
                                    context,
                                    "Login successful!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                route = Route.Main
                            } else {
                                Toast.makeText(
                                    context,
                                    result?.message
                                        ?: "Login failed. Check your details.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Could not connect to the server. Check that XAMPP is running.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                onGoToRegister = {
                    route = Route.Register
                }
            )

            Route.Register -> RegistrationScreen(
                onRegister = { fullName, email, password ->
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.createUser(
                                CreateUserRequest(
                                    fullName = fullName,
                                    email = email,
                                    password = password
                                )
                            )

                            val result = response.body()

                            if (
                                response.isSuccessful &&
                                result?.success == true
                            ) {
                                // Save the details entered during registration.
                                accountFullName = fullName.trim()
                                accountEmail = email.trim()
                                firstName = accountFullName
                                    .substringBefore(" ")

                                Toast.makeText(
                                    context,
                                    "Registration successful!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                route = Route.Main
                            } else {
                                Toast.makeText(
                                    context,
                                    result?.message ?: "Registration failed.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Could not connect to the server. Check that XAMPP is running.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                onGoToLogin = {
                    route = Route.Login
                }
            )

            Route.Main -> MainScaffold(
                firstName = firstName,
                fullName = accountFullName,
                email = accountEmail,
                onLogout = {
                    scope.launch {
                        val preferences = context.getSharedPreferences(
                            "mda_session",
                            Context.MODE_PRIVATE
                        )
                        val token = preferences.getString(
                            "auth_token",
                            null
                        )

                        try {
                            // Revoke the backend session.
                            if (!token.isNullOrBlank()) {
                                RetrofitClient.api.logout("Bearer $token")
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Could not contact the server. Logging out locally.",
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            // Clear the saved token.
                            preferences.edit()
                                .remove("auth_token")
                                .apply()

                            // Clear account details from the current app state.
                            firstName = ""
                            accountFullName = ""
                            accountEmail = ""

                            route = Route.Login
                        }
                    }
                }
            )
        }
    }
}

/**
 * The five tabs displayed in the main app.
 */
@Composable
fun MainScaffold(
    firstName: String,
    fullName: String,
    email: String,
    onLogout: () -> Unit
) {
    var tab by remember { mutableStateOf(BottomTab.Home) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                selected = tab,
                onSelect = { tab = it }
            )
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when (tab) {
                BottomTab.Home -> HomeScreen()
                BottomTab.Medications -> MedicationScreen()
                BottomTab.Home -> HomeScreen(
                    firstName = firstName
                )

                BottomTab.Search -> SearchScreen()

                BottomTab.Alerts -> NotificationsScreen()

                BottomTab.Profile -> ProfileScreen(
                    fullName = fullName,
                    email = email,
                    onLogout = onLogout
                )

                BottomTab.Settings -> SettingsScreen()
            }
        }
    }
}
