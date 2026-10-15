package com.mda.screens

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
fun LoginScreen(onLogin: () -> Unit, onGoToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    ScreenColumn {
        Spacer(Modifier.height(48.dp))
        FormField("Email Address", email, { email = it }, "you@email.com")
        FormField("Password", password, { password = it }, "Password", isPassword = true)

        Text(
            "Forgot password?",
            color = AppColors.Link,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { /* TODO: forgot password flow */ }
                .padding(bottom = 24.dp)
        )

        // TODO (API teammate): check email + password against the backend, then call onLogin()
        AppButton("Log in", onClick = onLogin)

        Row(
            Modifier.fillMaxWidth().padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account? ", color = AppColors.Muted, fontSize = 14.sp)
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
