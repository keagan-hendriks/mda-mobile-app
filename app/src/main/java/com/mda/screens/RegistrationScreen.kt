package com.mda.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistrationScreen(onRegister: () -> Unit, onGoToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    ScreenColumn {
        Spacer(Modifier.height(48.dp))
        FormField("Email / Username", email, { email = it }, "you@workspace.com")
        FormField("Password", password, { password = it }, "Password", isPassword = true)
        FormField("Confirm Password", confirm, { confirm = it }, "Confirm password", isPassword = true)

        Spacer(Modifier.height(8.dp))

        // TODO (API teammate): create the account on the backend, then call onRegister()
        AppButton("Register", onClick = onRegister)

        Row(
            Modifier.fillMaxWidth().padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ", color = AppColors.Muted, fontSize = 14.sp)
            Text(
                "Log in",
                color = AppColors.Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onGoToLogin)
            )
        }
    }
}
