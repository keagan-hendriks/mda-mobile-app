package com.mda.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mda_mobile_app.ui.medication.LogEditorDialog
import com.example.mda_mobile_app.ui.medication.MedicationViewModel

//TODO: replace temp hardcoded user when Karabo is done with Login
@Composable
fun HomeScreen(userId: Int = 1) {
    val medicationViewModel: MedicationViewModel = viewModel(
        key = "medication-$userId",
        factory = MedicationViewModel.factory(userId)
    )
    val medicationState by medicationViewModel.state.collectAsState()
    var showLogDialog by remember { mutableStateOf(false) }

    ScreenColumn {
        ScreenHeader("Welcome, ${PlaceholderData.firstName}", "Here is your overview for today")

        // Streak remains unchanged until the team agrees on the calculation.
        AppCard {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).background(Color(0xFFE6F7EF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${PlaceholderData.adherencePercent}%",
                        color = AppColors.Success,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Adherence Streak", fontWeight = FontWeight.SemiBold, color = AppColors.Ink)
                    Text(
                        "You are doing great! ${PlaceholderData.streakDays} days in a row",
                        fontSize = 13.sp,
                        color = AppColors.Muted
                    )
                }
            }
        }

        SectionLabel("UPCOMING REMINDERS", Modifier.padding(top = 16.dp))
        PlaceholderData.reminders.forEach { reminder ->
            ReminderRow(reminder)
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(8.dp))

        if (medicationState.error != null) {
            Text("Medication error: ${medicationState.error}", color = Color(0xFFB00020))
        }
        if (medicationState.notice == "Intake recorded") {
            Text("Intake recorded", color = AppColors.Success)
        }
        if (medicationState.medications.none { it.isActive }) {
            Text(
                if (medicationState.loading) "Loading medications…"
                else "Add an active medication on the Meds tab before logging intake.",
                fontSize = 13.sp, color = AppColors.Muted
            )
        }
        AppButton(
            "+  Log a quick medication take",
            onClick = { showLogDialog = true }
        )
        Spacer(Modifier.height(24.dp))
    }

    if (showLogDialog) {
        LogEditorDialog(
            original = null,
            medications = medicationState.medications,
            onDismiss = { showLogDialog = false },
            onSave = { medicationId, whenAt, status, notes ->
                medicationViewModel.addLog(medicationId, whenAt, status, notes)
                showLogDialog = false
            }
        )
    }
}

@Composable
private fun ReminderRow(reminder: Reminder) {
    AppCard {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (reminder.taken) {
                Box(
                    Modifier.size(22.dp).background(AppColors.Success, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(Modifier.size(20.dp).border(1.5.dp, AppColors.Muted, CircleShape))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(reminder.name, fontWeight = FontWeight.SemiBold, color = AppColors.Ink)
                Text(reminder.detail, fontSize = 12.sp, color = AppColors.Muted)
            }
            Text(reminder.time, fontSize = 13.sp, color = AppColors.Ink)
        }
    }
}
