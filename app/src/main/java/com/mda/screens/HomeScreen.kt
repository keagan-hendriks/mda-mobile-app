package com.mda.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mda_mobile_app.data.model.crud.Reminder
import com.example.mda_mobile_app.ui.medication.MedicationViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(firstName: String, userId: Int) {

    val medicationViewModel: MedicationViewModel = viewModel(
        key = "medication-$userId",
        factory = MedicationViewModel.factory(userId)
    )

    val medicationState by medicationViewModel.state.collectAsState()

    // Refresh the countdown every minute.
    var clockNow by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            clockNow = System.currentTimeMillis()
        }
    }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .format(Date(clockNow))

    val currentCalendar = Calendar.getInstance().apply {
        timeInMillis = clockNow
    }

    val hour = currentCalendar.get(Calendar.HOUR_OF_DAY)

    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }

    val dateLabel = SimpleDateFormat(
        "EEEE, MMMM d",
        Locale.getDefault()
    ).format(Date(clockNow))

    // Today's recorded medication logs.
    val todayLogs = medicationState.logs.filter {
        it.scheduledAt.startsWith(today)
    }

    val todayTaken = todayLogs.count {
        it.status.equals("taken", ignoreCase = true)
    }

    val todayTotal = todayLogs.size

    val adherence = if (todayTotal > 0) {
        (todayTaken * 100) / todayTotal
    } else {
        0
    }

    val motivation = when {
        adherence >= 80 -> "You're doing great today. Keep it up!"
        adherence >= 50 -> "Great progress. Keep going!"
        adherence > 0 -> "Every recorded intake counts."
        else -> "Let's get started with your medication routine."
    }

    // Real reward information from the backend.
    val currentStreak = medicationState.rewards?.currentStreak ?: 0
    val totalPoints = medicationState.rewards?.totalPoints ?: 0

    // Active reminders.
    val activeReminders = medicationState.reminders
        .filter { it.isEnabled }
        .sortedBy { it.reminderTime }

    // Find the next reminder and calculate its countdown.
    val now = Calendar.getInstance().apply {
        timeInMillis = clockNow
    }

    val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 +
            now.get(Calendar.MINUTE)

    val nextReminder = activeReminders.firstOrNull {
        val parts = it.reminderTime.split(":")
        val reminderMinutes =
            parts[0].toIntOrNull()?.times(60)?.plus(
                parts.getOrNull(1)?.toIntOrNull() ?: 0
            ) ?: 0

        reminderMinutes > currentMinutes
    } ?: activeReminders.firstOrNull()

    val nextReminderCalendar = nextReminder?.let { reminder ->
        val parts = reminder.reminderTime.split(":")

        Calendar.getInstance().apply {
            timeInMillis = clockNow
            set(Calendar.HOUR_OF_DAY, parts[0].toIntOrNull() ?: 0)
            set(Calendar.MINUTE, parts.getOrNull(1)?.toIntOrNull() ?: 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (!after(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    val countdownText = nextReminderCalendar?.let {
        val remainingMillis = it.timeInMillis - clockNow
        val totalMinutes = (remainingMillis / 60_000L).toInt()
        val hoursRemaining = totalMinutes / 60
        val minutesRemaining = totalMinutes % 60

        "In ${hoursRemaining}h ${minutesRemaining}m"
    }

    val reminderDayLabel = nextReminderCalendar?.let {
        val isToday = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(it.time) == today

        if (isToday) "Today" else "Tomorrow"
    }

    // Last seven days of recorded adherence.
    val weeklyData = remember(medicationState.logs, today) {
        val days = mutableListOf<WeeklyDay>()

        for (i in 6 downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = clockNow
            calendar.add(Calendar.DAY_OF_MONTH, -i)

            val date = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(calendar.time)

            val dayLogs = medicationState.logs.filter {
                it.scheduledAt.startsWith(date)
            }

            val taken = dayLogs.count {
                it.status.equals("taken", ignoreCase = true)
            }

            val hasData = dayLogs.isNotEmpty()

            val percentage = if (hasData) {
                (taken * 100) / dayLogs.size
            } else {
                0
            }

            val label = SimpleDateFormat(
                "EEEEE",
                Locale.getDefault()
            ).format(calendar.time)

            days.add(
                WeeklyDay(
                    label = label,
                    percentage = percentage,
                    isToday = date == today,
                    hasData = hasData
                )
            )
        }

        days
    }

    // Only calculate the average from days with recorded logs.
    val daysWithData = weeklyData.filter { it.hasData }

    val weeklyAverage = if (daysWithData.isNotEmpty()) {
        daysWithData.sumOf { it.percentage } / daysWithData.size
    } else {
        0
    }

    ScreenColumn {

        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    greeting,
                    color = AppColors.Muted,
                    fontSize = 14.sp
                )

                Text(
                    "$firstName 👋",
                    color = AppColors.Ink,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    dateLabel,
                    color = AppColors.Muted,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color(0xFFE6F7EF),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    firstName.firstOrNull()?.uppercase() ?: "M",
                    color = AppColors.Success,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // HERO: DAILY ADHERENCE
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.Success
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "DAILY ADHERENCE",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Spacer(Modifier.height(18.dp))

                ProgressRing(
                    percentage = adherence,
                    foreground = Color.White,
                    track = Color.White.copy(alpha = 0.25f),
                    textColor = Color.White
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    if (todayTotal > 0) {
                        "$todayTaken of $todayTotal doses"
                    } else {
                        "No doses recorded yet"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    motivation,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // STREAK AND POINTS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardStatCard(
                symbol = "🔥",
                value = "$currentStreak days",
                label = "Current streak",
                modifier = Modifier.weight(1f)
            )

            DashboardStatCard(
                symbol = "⭐",
                value = totalPoints.toString(),
                label = "Total points",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(22.dp))

        // NEXT MEDICATION
        SectionLabel("Next medication")

        if (nextReminder != null) {

            val medicationName = medicationState.medications
                .firstOrNull {
                    it.medicationId == nextReminder.medicationId
                }?.medicationName ?: "Medication"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFFE6F7EF),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💊", fontSize = 23.sp)
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            medicationName,
                            color = AppColors.Ink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "$reminderDayLabel at ${nextReminder.reminderTime.take(5)}",
                            color = AppColors.Muted,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            countdownText ?: "",
                            color = AppColors.Success,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        "⏰",
                        fontSize = 22.sp
                    )
                }
            }

        } else {
            AppCard {
                Text(
                    "No upcoming medication reminders.",
                    modifier = Modifier.padding(18.dp),
                    color = AppColors.Muted,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // WEEKLY TREND
        SectionLabel("Weekly trend")

        AppCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    if (daysWithData.isNotEmpty()) {
                        "$weeklyAverage% average adherence"
                    } else {
                        "No adherence data yet"
                    },
                    color = AppColors.Ink,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(18.dp))

                WeeklyLineChart(
                    days = weeklyData
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyData.forEach { day ->
                        Text(
                            day.label,
                            color = if (day.isToday) {
                                AppColors.Success
                            } else {
                                AppColors.Muted
                            },
                            fontSize = 11.sp,
                            fontWeight = if (day.isToday) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // TODAY'S MEDICATION TIMELINE
        SectionLabel("Today's medications")

        if (activeReminders.isEmpty()) {

            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💊", fontSize = 30.sp)

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "No active reminders yet",
                        color = AppColors.Ink,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "Your medication reminders will appear here.",
                        color = AppColors.Muted,
                        fontSize = 12.sp
                    )
                }
            }

        } else {

            activeReminders.forEachIndexed { index, reminder ->

                val medicationName = medicationState.medications
                    .firstOrNull {
                        it.medicationId == reminder.medicationId
                    }?.medicationName ?: "Unknown medication"

                val matchingLog = todayLogs.firstOrNull {
                    it.reminderId == reminder.reminderId
                } ?: todayLogs.firstOrNull {
                    it.reminderId == null &&
                            it.medicationId == reminder.medicationId
                }

                val status = matchingLog?.status ?: "Scheduled"

                ReminderTimelineItem(
                    reminder = reminder,
                    medicationName = medicationName,
                    status = status,
                    isLast = index == activeReminders.lastIndex
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

// ADHERENCE PROGRESS RING
@Composable
private fun ProgressRing(
    percentage: Int,
    foreground: Color = AppColors.Success,
    track: Color = Color(0xFFE8F1EC),
    textColor: Color = AppColors.Ink
) {
    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 11.dp.toPx()

            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            drawArc(
                color = foreground,
                startAngle = -90f,
                sweepAngle = 360f * percentage.coerceIn(0, 100) / 100f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        Text(
            "$percentage%",
            color = textColor,
            fontSize = 27.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// DASHBOARD STAT CARD
@Composable
private fun DashboardStatCard(
    symbol: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp)
        ) {
            Text(
                symbol,
                fontSize = 23.sp
            )

            Spacer(Modifier.height(10.dp))

            Text(
                value,
                color = AppColors.Ink,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(3.dp))

            Text(
                label,
                color = AppColors.Muted,
                fontSize = 12.sp
            )
        }
    }
}

// WEEKLY LINE CHART

@Composable
private fun WeeklyLineChart(days: List<WeeklyDay>) {

    val lineColor = AppColors.Success
    val inactiveColor = Color(0xFFE6EDE9)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
    ) {
        val chartHeight = size.height
        val chartWidth = size.width

        // Draw horizontal guide lines.
        for (level in 0..2) {
            val y = chartHeight * level / 2f

            drawLine(
                color = inactiveColor,
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Keep each point connected to its actual day index.
        val points = days.mapIndexedNotNull { index, day ->

            if (!day.hasData) {
                null
            } else {

                val x = chartWidth * index / 6f

                val y = chartHeight -
                        (day.percentage.coerceIn(0, 100) / 100f * chartHeight)

                Pair(index, Offset(x, y))
            }
        }

        // Draw the trend line.
        if (points.size >= 2) {

            val path = Path().apply {

                moveTo(
                    points.first().second.x,
                    points.first().second.y
                )

                points.drop(1).forEach { (_, point) ->
                    lineTo(point.x, point.y)
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // Draw the data points.
        points.forEach { (dayIndex, point) ->

            val isToday = days[dayIndex].isToday

            drawCircle(
                color = lineColor,
                radius = if (isToday) {
                    6.dp.toPx()
                } else {
                    4.dp.toPx()
                },
                center = point
            )

            drawCircle(
                color = Color.White,
                radius = if (isToday) {
                    2.5.dp.toPx()
                } else {
                    1.8.dp.toPx()
                },
                center = point
            )
        }
    }
}

// MEDICATION TIMELINE ITEM
@Composable
private fun ReminderTimelineItem(
    reminder: Reminder,
    medicationName: String,
    status: String,
    isLast: Boolean
) {
    val statusColor = when (status.lowercase()) {
        "taken" -> AppColors.Success
        "missed", "skipped" -> Color(0xFFD58A36)
        else -> AppColors.Muted
    }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .background(
                        statusColor,
                        CircleShape
                    )
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(65.dp)
                        .background(Color(0xFFE3EAE6))
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        reminder.reminderTime.take(5),
                        color = AppColors.Success,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        medicationName,
                        color = AppColors.Ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        if (reminder.repeatDaily) {
                            "Repeats daily"
                        } else {
                            "Scheduled reminder"
                        },
                        color = AppColors.Muted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    status.replaceFirstChar {
                        it.uppercase()
                    },
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class WeeklyDay(
    val label: String,
    val percentage: Int,
    val isToday: Boolean,
    val hasData: Boolean
)