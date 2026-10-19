package com.mda.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Saved user preferences. They are stored on the phone itself, so no
 * database or API is needed and they survive closing the app.
 *
 * Reminders / notifications teammates can read them like this:
 *   if (AppSettings.notificationsEnabled(context)) { /* show the notification */ }
 *   val snooze = AppSettings.snoozeMinutes(context)
 */
object AppSettings {
    private const val FILE = "mda_settings"
    private const val KEY_NOTIFICATIONS = "notifications_enabled"
    private const val KEY_VIBRATION = "vibration_enabled"
    private const val KEY_SNOOZE = "snooze_minutes"

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun notificationsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NOTIFICATIONS, true)

    fun setNotificationsEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATIONS, value).apply()
    }

    fun vibrationEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_VIBRATION, true)

    fun setVibrationEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_VIBRATION, value).apply()
    }

    fun snoozeMinutes(context: Context): Int =
        prefs(context).getInt(KEY_SNOOZE, 10)

    fun setSnoozeMinutes(context: Context, minutes: Int) {
        prefs(context).edit().putInt(KEY_SNOOZE, minutes).apply()
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    var notifications by remember { mutableStateOf(AppSettings.notificationsEnabled(context)) }
    var vibration by remember { mutableStateOf(AppSettings.vibrationEnabled(context)) }
    var snooze by remember { mutableStateOf(AppSettings.snoozeMinutes(context)) }
    var showPrivacy by remember { mutableStateOf(false) }

    ScreenColumn {
        ScreenHeader("Settings", "Adjust the system to fit your rhythm")

        SectionLabel("PREFERENCES")
        ToggleRow("App Notifications", "Turn on push medication alarms", notifications) {
            notifications = it
            AppSettings.setNotificationsEnabled(context, it)
        }
        ToggleRow(
            "Vibration",
            "Vibrate when a reminder goes off",
            vibration,
            enabled = notifications
        ) {
            vibration = it
            AppSettings.setVibrationEnabled(context, it)
        }
        SnoozeRow(selected = snooze) {
            snooze = it
            AppSettings.setSnoozeMinutes(context, it)
        }

        Spacer(Modifier.height(16.dp))
        SectionLabel("DATA & LEGAL")
        LinkRow("Export Log History") {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Medication log history")
                putExtra(Intent.EXTRA_TEXT, buildLogExport())
            }
            context.startActivity(Intent.createChooser(send, "Export log history"))
        }
        LinkRow("Privacy Policy") { showPrivacy = true }
        Spacer(Modifier.height(24.dp))
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            containerColor = Color.White,
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold, color = AppColors.Ink) },
            text = {
                // TODO: replace this placeholder with the group's final policy text
                Text(
                    "Your medication information is kept for your own use. " +
                            "It is not shared with other people or organisations. " +
                            "You can export or delete your data at any time.\n\n" +
                            "(Placeholder text. Replace with the final policy.)",
                    color = AppColors.Muted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }) { Text("Close") }
            }
        )
    }
}

// TODO (API / database teammate): build this from the real log history
private fun buildLogExport(): String = buildString {
    appendLine("Medication log history")
    appendLine("Medication,Dose,Time,Taken")
    PlaceholderData.reminders.forEach {
        val dose = it.detail.replace(" \u2022 ", " - ")
        appendLine("${it.name},$dose,${it.time},${if (it.taken) "Yes" else "No"}")
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) AppColors.Ink else AppColors.Muted
            )
            Text(subtitle, fontSize = 12.sp, color = AppColors.Muted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
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

@Composable
private fun SnoozeRow(selected: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text("Snooze length", fontWeight = FontWeight.SemiBold, color = AppColors.Ink)
        Text("How long a reminder waits when you tap Snooze", fontSize = 12.sp, color = AppColors.Muted)
        Spacer(Modifier.height(10.dp))
        Row {
            listOf(5, 10, 15).forEach { minutes ->
                ChoicePill("$minutes min", selected = minutes == selected) { onSelect(minutes) }
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun ChoicePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) AppColors.Ink else Color.White,
        border = BorderStroke(1.dp, if (selected) AppColors.Ink else AppColors.Border),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = if (selected) Color.White else AppColors.Muted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
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
            Icon(AppIcons.ChevronRight, contentDescription = null, tint = AppColors.Muted, modifier = Modifier.size(22.dp))
        }
    }
}