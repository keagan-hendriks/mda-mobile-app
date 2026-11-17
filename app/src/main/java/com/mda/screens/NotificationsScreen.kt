
package com.mda.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mda_mobile_app.ReminderActivity

@Composable
fun NotificationsScreen(userId: Int) {

    // TODO (API teammate): Load real alerts instead of placeholder data.
    var alerts by remember {
        mutableStateOf(PlaceholderData.alerts)
    }

    val context = LocalContext.current
    val unreadCount = alerts.count { it.unread }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NotificationColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // -----------------------------------------
        // HEADER
        // -----------------------------------------

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "YOUR UPDATES",
                color = NotificationColors.Primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                text = "Notifications",
                color = NotificationColors.Dark,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Stay informed and keep track of your medication routine.",
                color = NotificationColors.Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // -----------------------------------------
        // UNREAD SUMMARY
        // -----------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = NotificationColors.SoftGreen
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {

                    Text(
                        text = "Your notification centre",
                        color = NotificationColors.Dark,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = if (unreadCount == 0) {
                            "You're all caught up!"
                        } else {
                            "You have $unreadCount unread notification(s)."
                        },
                        color = NotificationColors.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = NotificationColors.Primary
                ) {

                    Text(
                        text = unreadCount.toString(),
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 10.dp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // -----------------------------------------
        // MANAGE REMINDERS
        // -----------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = NotificationColors.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = "Medication reminders",
                    color = NotificationColors.Dark,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Manage your medication reminders and keep your daily routine on track.",
                    color = NotificationColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = {
                        val intent = Intent(
                            context,
                            ReminderActivity::class.java
                        )

                        intent.putExtra(
                            ReminderActivity.EXTRA_USER_ID,
                            userId
                        )

                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NotificationColors.Primary,
                        contentColor = Color.White
                    )
                ) {

                    Text(
                        text = "Manage Medication Reminders",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // -----------------------------------------
        // RECENT ALERTS
        // -----------------------------------------

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Text(
                text = "RECENT ACTIVITY",
                color = NotificationColors.Primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                text = "Recent alerts",
                color = NotificationColors.Dark,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Your latest notifications and updates.",
                color = NotificationColors.Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Mark all as read

        if (unreadCount > 0) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                TextButton(
                    onClick = {
                        alerts = alerts.map {
                            it.copy(unread = false)
                        }
                    }
                ) {

                    Text(
                        text = "Mark all as read",
                        color = NotificationColors.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Alert list

        if (alerts.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NotificationColors.White
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "You're all caught up!",
                        color = NotificationColors.Dark,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "New notifications will appear here.",
                        color = NotificationColors.Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

        } else {

            alerts.forEach { alert ->

                AlertCard(alert = alert)

            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ---------------------------------------------
// INDIVIDUAL NOTIFICATION CARD
// ---------------------------------------------

@Composable
private fun AlertCard(alert: AlertItem) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.unread) {
                NotificationColors.SoftGreen
            } else {
                NotificationColors.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {

            // Unread indicator

            Box(
                modifier = Modifier
                    .padding(top = 5.dp, end = 12.dp)
                    .size(9.dp)
                    .background(
                        color = if (alert.unread) {
                            NotificationColors.Primary
                        } else {
                            Color.Transparent
                        },
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = if (alert.unread) {
                            "NEW UPDATE"
                        } else {
                            "NOTIFICATION"
                        },
                        color = NotificationColors.Primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    if (alert.unread) {

                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = NotificationColors.Primary
                        ) {

                            Text(
                                text = "New",
                                modifier = Modifier.padding(
                                    horizontal = 9.dp,
                                    vertical = 4.dp
                                ),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Text(
                    text = alert.message,
                    color = NotificationColors.Dark,
                    fontSize = 14.sp,
                    fontWeight = if (alert.unread) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
                )

                Text(
                    text = alert.time,
                    color = NotificationColors.Muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ---------------------------------------------
// NOTIFICATIONS COLOUR PALETTE
// ---------------------------------------------

private object NotificationColors {

    val Background = Color(0xFFF3F7F3)
    val White = Color(0xFFFFFFFF)

    val Primary = Color(0xFF426B57)
    val Dark = Color(0xFF263A33)
    val Muted = Color(0xFF718078)

    val SoftGreen = Color(0xFFE5EFE7)
}
