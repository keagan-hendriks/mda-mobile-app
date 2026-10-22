
package com.mda.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Patterns

@Composable
fun RegistrationScreen(
    onRegister: (String, String, String) -> Unit,
    onGoToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    ScreenColumn {
        Spacer(Modifier.height(48.dp))

        FormField(
            "Full Name",
            fullName,
            { fullName = it },
            "Your full name"
        )

        FormField(
            "Email",
            email,
            { email = it },
            "you@example.com"
        )

        FormField(
            "Password",
            password,
            { password = it },
            "Password",
            isPassword = true
        )

        FormField(
            "Confirm Password",
            confirm,
            { confirm = it },
            "Confirm password",
            isPassword = true
        )

        Spacer(Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = AppColors.Ink,
                fontSize = 14.sp
            )
        }

        AppButton(
            "Register",
            onClick = {
                when {
                    fullName.isBlank() -> {
                        errorMessage = "Please enter your full name."
                    }

                    email.isBlank() -> {
                        errorMessage = "Please enter your email."
                    }

                    !Patterns.EMAIL_ADDRESS
                        .matcher(email.trim())
                        .matches() -> {
                        errorMessage = "Please enter a valid email address."
                    }

                    password.length < 8 -> {
                        errorMessage = "Password must be at least 8 characters."
                    }

                    password.length > 72 -> {
                        errorMessage = "Password must not exceed 72 characters."
                    }

                    password != confirm -> {
                        errorMessage = "Passwords do not match."
                    }

                    else -> {
                        errorMessage = ""
                        onRegister(
                            fullName.trim(),
                            email.trim(),
                            password
                        )
                    }
                }
            }
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Already have an account? ",
                color = AppColors.Muted,
                fontSize = 14.sp
            )

            Text(
                "Log in",
                color = AppColors.Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable(
                    onClick = onGoToLogin
                )
            )
        }
    }
}
