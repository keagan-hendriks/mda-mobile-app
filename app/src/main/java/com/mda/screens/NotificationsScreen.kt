package com.mda.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationsScreen() {
    // TODO (API teammate): load real alerts instead of placeholder data
    var alerts by remember { mutableStateOf(PlaceholderData.alerts) }

    ScreenColumn {
        ScreenHeader("Notifications", "Stay updated with your daily treatment log")

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel("RECENT ALERTS")
            Text(
                "Mark all as read",
                fontSize = 12.sp,
                color = AppColors.Muted,
                modifier = Modifier.clickable { alerts = alerts.map { it.copy(unread = false) } }
            )
        }

        alerts.forEach { alert ->
            AlertCard(alert)
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AlertCard(alert: AlertItem) {
    AppCard(color = if (alert.unread) AppColors.Surface else Color.White) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            if (alert.unread) {
                Box(
                    Modifier.padding(top = 6.dp, end = 8.dp).size(6.dp).background(AppColors.Ink, CircleShape)
                )
            }
            Column {
                Text(
                    alert.message,
                    fontSize = 14.sp,
                    fontWeight = if (alert.unread) FontWeight.SemiBold else FontWeight.Normal,
                    color = AppColors.Ink
                )
                Text(alert.time, fontSize = 12.sp, color = AppColors.Muted)
            }
        }
    }
}
