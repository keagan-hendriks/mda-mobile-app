package com.mda.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    ScreenColumn {
        ScreenHeader("Account Profile", "Manage your biological & app preferences")

        // Avatar + name
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(72.dp)
                    .background(AppColors.Surface, CircleShape)
                    .border(1.dp, AppColors.Border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(PlaceholderData.initials, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.Ink)
            }
            Spacer(Modifier.height(8.dp))
            Text(PlaceholderData.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.Ink)
        }

        Spacer(Modifier.height(24.dp))

        InfoRow("Full Name", PlaceholderData.fullName)
        InfoRow("Email Address", PlaceholderData.email)

        Spacer(Modifier.height(24.dp))
        // TODO (API teammate): clear the saved session / token before calling onLogout()
        OutlineButton("Log Out from Device", onClick = onLogout, color = AppColors.Danger)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp, color = AppColors.Muted)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.Ink)
        }
        HorizontalDivider(color = AppColors.Border)
    }
}