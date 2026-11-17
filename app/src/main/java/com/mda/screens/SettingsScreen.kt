
package com.mda.screens

import android.content.Context
import android.content.Intent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.mda_mobile_app.data.model.crud.Medication
import com.example.mda_mobile_app.data.model.crud.MedicationLog

// ----------------------------------------------------
// SETTINGS COLOURS
// ----------------------------------------------------

private object SettingsColors {
    val Background = Color(0xFFF3F7F3)
    val Primary = Color(0xFF426B57)
    val Dark = Color(0xFF263A33)
    val Muted = Color(0xFF718078)
    val SoftGreen = Color(0xFFE5EFE7)
    val Border = Color(0xFFE1E9E2)
    val White = Color.White
}

// ----------------------------------------------------
// LOCAL APP SETTINGS
// Preferences are saved on the user's phone.
// ----------------------------------------------------

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
        prefs(context).edit()
            .putBoolean(KEY_NOTIFICATIONS, value)
            .apply()
    }

    fun vibrationEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_VIBRATION, true)

    fun setVibrationEnabled(context: Context, value: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_VIBRATION, value)
            .apply()
    }

    fun snoozeMinutes(context: Context): Int =
        prefs(context).getInt(KEY_SNOOZE, 10)

    fun setSnoozeMinutes(context: Context, minutes: Int) {
        prefs(context).edit()
            .putInt(KEY_SNOOZE, minutes)
            .apply()
    }
}

// ----------------------------------------------------
// SETTINGS SCREEN
// ----------------------------------------------------

@Composable
fun SettingsScreen(
    medicationLogs: List<MedicationLog> = emptyList(),
    medications: List<Medication> = emptyList()
) {
    val context = LocalContext.current

    var notifications by remember {
        mutableStateOf(AppSettings.notificationsEnabled(context))
    }

    var vibration by remember {
        mutableStateOf(AppSettings.vibrationEnabled(context))
    }

    var snooze by remember {
        mutableStateOf(AppSettings.snoozeMinutes(context))
    }

    var showPrivacy by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SettingsColors.Background)
            .padding(horizontal = 20.dp)
    ) {

        Spacer(Modifier.height(24.dp))

        // Page heading
        Text(
            text = "Settings",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = SettingsColors.Dark
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "Adjust the system to fit your rhythm",
            fontSize = 14.sp,
            color = SettingsColors.Muted
        )

        Spacer(Modifier.height(28.dp))

        // Preferences section
        SettingsSectionTitle("PREFERENCES")

        SettingsCard {

            ToggleRow(
                title = "App Notifications",
                subtitle = "Turn on medication reminder alerts",
                checked = notifications
            ) {
                notifications = it
                AppSettings.setNotificationsEnabled(context, it)
            }

            SettingsDivider()

            ToggleRow(
                title = "Vibration",
                subtitle = "Vibrate when a reminder goes off",
                checked = vibration,
                enabled = notifications
            ) {
                vibration = it
                AppSettings.setVibrationEnabled(context, it)
            }

            SettingsDivider()

            SnoozeRow(selected = snooze) {
                snooze = it
                AppSettings.setSnoozeMinutes(context, it)
            }
        }

        Spacer(Modifier.height(26.dp))

        // Data and legal section
        SettingsSectionTitle("DATA & LEGAL")

        SettingsCard {

            LinkRow(
                title = "Export Log History",
                subtitle = "Share your medication history"
            ) {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"

                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        "Medication log history"
                    )

                    putExtra(
                        Intent.EXTRA_TEXT,
                        buildLogExport(medicationLogs, medications)
                    )
                }

                context.startActivity(
                    Intent.createChooser(send, "Export log history")
                )
            }

            SettingsDivider()

            LinkRow(
                title = "Privacy Policy",
                subtitle = "Learn how your information is handled"
            ) {
                showPrivacy = true
            }
        }

        Spacer(Modifier.height(30.dp))
    }

    // Privacy policy dialog
    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = {
                showPrivacy = false
            },
            containerColor = SettingsColors.White,
            shape = RoundedCornerShape(24.dp),

            title = {
                Text(
                    text = "Privacy Policy",
                    fontWeight = FontWeight.Bold,
                    color = SettingsColors.Dark
                )
            },

            text = {
                Text(
                    text = "Your medication information is kept for your own use. " +
                            "It is not shared with other people or organisations. " +
                            "You can export or delete your data at any time.\n\n" +
                            "(Placeholder text. Replace with the final policy.)",
                    color = SettingsColors.Muted,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showPrivacy = false
                    }
                ) {
                    Text(
                        text = "Close",
                        color = SettingsColors.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}

// ----------------------------------------------------
// SECTION TITLE
// ----------------------------------------------------

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = SettingsColors.Primary,
        letterSpacing = 1.2.sp
    )

    Spacer(Modifier.height(12.dp))
}

// ----------------------------------------------------
// SETTINGS CARD
// ----------------------------------------------------

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = SettingsColors.White,
        border = BorderStroke(1.dp, SettingsColors.Border)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            content = content
        )
    }
}

// ----------------------------------------------------
// DIVIDER
// ----------------------------------------------------

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = SettingsColors.Border,
        thickness = 1.dp
    )
}

// ----------------------------------------------------
// TOGGLE ROW
// ----------------------------------------------------

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) {
                    SettingsColors.Dark
                } else {
                    SettingsColors.Muted
                }
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SettingsColors.Muted,
                lineHeight = 17.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = SettingsColors.Primary,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = SettingsColors.Border,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

// ----------------------------------------------------
// SNOOZE SETTINGS
// ----------------------------------------------------

@Composable
private fun SnoozeRow(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {

        Text(
            text = "Snooze length",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = SettingsColors.Dark
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "How long a reminder waits when you tap Snooze",
            fontSize = 12.sp,
            color = SettingsColors.Muted,
            lineHeight = 17.sp
        )

        Spacer(Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(5, 10, 15).forEach { minutes ->

                ChoicePill(
                    label = "$minutes min",
                    selected = minutes == selected
                ) {
                    onSelect(minutes)
                }
            }
        }
    }
}

// ----------------------------------------------------
// SNOOZE OPTION PILL
// ----------------------------------------------------

@Composable
private fun ChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) {
            SettingsColors.Primary
        } else {
            SettingsColors.White
        },
        border = BorderStroke(
            1.dp,
            if (selected) {
                SettingsColors.Primary
            } else {
                SettingsColors.Border
            }
        )
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) {
                Color.White
            } else {
                SettingsColors.Muted
            },
            modifier = Modifier.padding(
                horizontal = 17.dp,
                vertical = 9.dp
            )
        )
    }
}

// ----------------------------------------------------
// DATA AND LEGAL LINK ROW
// ----------------------------------------------------

@Composable
private fun LinkRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SettingsColors.Dark
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SettingsColors.Muted
            )
        }

        Text(
            text = "›",
            fontSize = 28.sp,
            color = SettingsColors.Primary
        )
    }
}

// ----------------------------------------------------
// MEDICATION LOG EXPORT
// ----------------------------------------------------

private fun buildLogExport(
    medicationLogs: List<MedicationLog>,
    medications: List<Medication>
): String = buildString {

    appendLine("Medication,Date and Time,Status,Notes")

    medicationLogs.forEach { log ->

        val medicationName = medications
            .firstOrNull {
                it.medicationId == log.medicationId
            }
            ?.medicationName ?: "Unknown medication"

        val values = listOf(
            medicationName,
            log.scheduledAt,
            log.status,
            log.notes.orEmpty()
        ).map { value ->

            "\"${value.replace("\"", "\"\"")}\""
        }

        appendLine(values.joinToString(","))
    }
}
