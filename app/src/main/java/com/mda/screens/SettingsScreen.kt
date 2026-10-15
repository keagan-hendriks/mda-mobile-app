package com.mda.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {
    // TODO (database teammate): load / save these preferences
    var notifications by remember { mutableStateOf(true) }
    var healthSync by remember { mutableStateOf(true) }
    var biometric by remember { mutableStateOf(false) }

    ScreenColumn {
        ScreenHeader("Settings", "Adjust the system to fit your rhythm")

        SectionLabel("PREFERENCES")
        ToggleRow("App Notifications", "Turn on push medication alarms", notifications) { notifications = it }
        ToggleRow("Health Sync", "Share logs automatically with clinic", healthSync) { healthSync = it }
        ToggleRow("Biometric Unlock", "Use face or fingerprint identity", biometric) { biometric = it }

        Spacer(Modifier.height(16.dp))
        SectionLabel("DATA & LEGAL")
        LinkRow("Export Log History") { /* TODO */ }
        LinkRow("Privacy Policy") { /* TODO */ }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = AppColors.Ink)
                Text(subtitle, fontSize = 12.sp, color = AppColors.Muted)
            }
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = AppColors.Ink,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = AppColors.Border,
                    uncheckedThumbColor = Color.White,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun LinkRow(label: String, onClick: () -> Unit) {
    Column {
        HorizontalDivider(color = AppColors.Border)
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.SemiBold, color = AppColors.Ink, modifier = Modifier.weight(1f))
            Text("\u203A", fontSize = 22.sp, color = AppColors.Muted)
        }
    }
}