
package com.mda.screens

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.mda_mobile_app.R
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onGoToRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Login screen colours only.
    val primaryGreen = Color(0xFF2E7967)
    val darkGreen = Color(0xFF263A33)
    val mutedGreen = Color(0xFF718078)
    val softGreen = Color(0xFFE5F0EA)

    ScreenColumn {

        Spacer(Modifier.height(32.dp))

        // Small app branding section.
        Image(
            painter = painterResource(
                id = R.mipmap.ic_launcher_foreground
            ),
            contentDescription = "MediTrack logo",
            modifier = Modifier.size(65.dp)
        )

        Spacer(Modifier.height(28.dp))

        // Welcome heading.
        Text(
            text = "Welcome to MediTrack!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = darkGreen
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Log in to stay on track with your medication routine.",
            fontSize = 14.sp,
            color = mutedGreen,
            lineHeight = 21.sp
        )

        Spacer(Modifier.height(32.dp))

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
            "Enter your password",
            isPassword = true
        )

        Text(
            text = "Forgot password?",
            color = primaryGreen,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clickable {
                    onForgotPassword()
                }
                .padding(bottom = 24.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color(0xFFDC4444),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Login button with a screen-specific colour.
        AppButton(
            text = "Log in",
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
            },
            modifier = Modifier
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account? ",
                color = mutedGreen,
                fontSize = 14.sp
            )

            Text(
                text = "Register",
                color = primaryGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable(
                    onClick = onGoToRegister
                )
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}
