
package com.mda.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    fullName: String,
    email: String,
    onLogout: () -> Unit
) {
    ScreenColumn {
        ScreenHeader(
            "Account Profile",
            "Manage your biological & app preferences"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val initials = fullName
                .trim()
                .split("\\s+".toRegex())
                .filter { it.isNotBlank() }
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
                .ifBlank { "U" }

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(AppColors.Surface, CircleShape)
                    .border(2.dp, AppColors.Border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Ink
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = fullName.ifBlank { "User" },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Ink
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = email,
                fontSize = 13.sp,
                color = AppColors.Muted
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        SectionLabel("PERSONAL INFORMATION")

        AppCard(color = AppColors.Surface) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                InfoRow("Full Name", fullName)

                Spacer(modifier = Modifier.height(12.dp))

                InfoRow("Email Address", email)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        SectionLabel("ACCOUNT")

        OutlineButton(
            "Log Out from Device",
            onClick = onLogout,
            color = AppColors.Danger
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
