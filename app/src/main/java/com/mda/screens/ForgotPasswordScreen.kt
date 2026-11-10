
package com.mda.screens

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.mda_mobile_app.data.model.users.ForgotPasswordRequest
import com.example.mda_mobile_app.data.remote.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Forgot Password?",
            fontSize = 26.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Enter the email address associated with your MediTrack account. We'll send you password-reset instructions."
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                message = ""
            },
            label = { Text("Email Address") },
            placeholder = { Text("you@email.com") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val enteredEmail = email.trim()

                if (!Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                    message = "Please enter a valid email address."
                    isError = true
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    message = ""

                    try {
                        val response = RetrofitClient.api.forgotPassword(
                            ForgotPasswordRequest(email = enteredEmail)
                        )

                        val result = response.body()

                        if (response.isSuccessful && result?.success == true) {
                            message = result.message
                                ?: "If an account exists with that email address, password-reset instructions will be sent."
                            isError = false
                        } else {
                            message = result?.message
                                ?: "Unable to process your request. Please try again."
                            isError = true
                        }

                    } catch (e: Exception) {
                        message = "Unable to connect. Please check your internet connection and try again."
                        isError = true

                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Send Reset Link")
            }
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                color = if (isError) Color.Red else Color(0xFF26834A),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Back to Login",
            color = Color(0xFF3978DB),
            modifier = Modifier.clickable(onClick = onBackToLogin)
        )
    }
}
