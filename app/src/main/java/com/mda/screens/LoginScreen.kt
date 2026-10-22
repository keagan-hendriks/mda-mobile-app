
package com.mda.screens

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onGoToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    ScreenColumn {
        Spacer(Modifier.height(48.dp))

        FormField(
            "Email Address",
            email,
            { email = it },
            "you@email.com"
        )

        FormField(
            "Password",
            password,
            { password = it },
            "Password",
            isPassword = true
        )

        Text(
            "Forgot password?",
            color = AppColors.Link,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clickable {
                    errorMessage = "Forgot password is not available yet."
                }
                .padding(bottom = 24.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = AppColors.Ink,
                fontSize = 14.sp
            )
        }

        AppButton(
            "Log in",
            onClick = {
                when {
                    email.isBlank() ->
                        errorMessage = "Please enter your email address."

                    !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                        errorMessage = "Please enter a valid email address."

                    password.isBlank() ->
                        errorMessage = "Please enter your password."

                    else -> {
                        errorMessage = ""
                        onLogin(email.trim(), password)
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
                "Don't have an account? ",
                color = AppColors.Muted,
                fontSize = 14.sp
            )

            Text(
                "Register",
                color = AppColors.Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onGoToRegister)
            )
        }
    }
}
