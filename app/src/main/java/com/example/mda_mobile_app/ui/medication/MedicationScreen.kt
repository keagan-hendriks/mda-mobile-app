
package com.example.mda_mobile_app.ui.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mda_mobile_app.data.model.crud.Medication
import com.example.mda_mobile_app.data.model.crud.MedicationLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Local testing only.
// Replace userId with the authenticated user's ID once login is connected.
@Composable
fun MedicationScreen(userId: Int) {

    require(userId > 0) {
        "userId must be positive"
    }

    val vm: MedicationViewModel = viewModel(
        key = "medication-$userId",
        factory = MedicationViewModel.factory(userId)
    )

    val state by vm.state.collectAsState()

    var search by rememberSaveable { mutableStateOf("") }
    var medicationEditor by remember { mutableStateOf<Medication?>(null) }
    var showAddMedication by remember { mutableStateOf(false) }
    var logEditor by remember { mutableStateOf<MedicationLog?>(null) }
    var showLogEditor by remember { mutableStateOf(false) }
    var medicationToDelete by remember { mutableStateOf<Medication?>(null) }
    var logToDelete by remember { mutableStateOf<MedicationLog?>(null) }

    val matching = state.medications.filter {
        it.medicationName.contains(search, ignoreCase = true)
    }

    val names = state.medications.associateBy {
        it.medicationId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicationColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // ------------------------------------------------
        // HEADER
        // ------------------------------------------------

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "MY MEDICATIONS",
                color = MedicationColors.Primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Your medication routine",
                color = MedicationColors.Dark,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Keep track of your medications and stay consistent with your health routine.",
                color = MedicationColors.Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // ------------------------------------------------
        // STATUS AND REFRESH
        // ------------------------------------------------

        if (state.loading || state.saving) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = MedicationColors.Primary,
                trackColor = MedicationColors.Border
            )
        }

        state.error?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MedicationColors.ErrorBackground
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Error: $it",
                    modifier = Modifier.padding(16.dp),
                    color = MedicationColors.Danger,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        state.notice?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MedicationColors.SoftGreen
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp),
                    color = MedicationColors.Primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = vm::refresh,
                enabled = !state.saving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MedicationColors.Primary
                )
            ) {
                Text("Refresh")
            }
        }

        // ------------------------------------------------
        // MEDICATION MANAGEMENT
        // ------------------------------------------------

        SectionHeading(
            number = "01",
            title = "Manage medications",
            subtitle = "Add and manage the medications in your routine."
        )

        // Search field

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = {
                Text("Search your medications")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MedicationColors.Primary,
                unfocusedBorderColor = MedicationColors.Border,
                focusedLabelColor = MedicationColors.Primary,
                cursorColor = MedicationColors.Primary
            )
        )

        // Add medication button

        Button(
            onClick = {
                medicationEditor = null
                showAddMedication = true
            },
            enabled = !state.saving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MedicationColors.Primary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "+  Add medication",
                fontWeight = FontWeight.SemiBold
            )
        }

        // Medication cards

        if (matching.isEmpty()) {

            EmptyStateCard(
                title = if (search.isBlank()) {
                    "No medications yet"
                } else {
                    "No medications found"
                },
                message = if (search.isBlank()) {
                    "Add your first medication to start managing your routine."
                } else {
                    "Try searching with a different medication name."
                }
            )

        } else {

            matching.forEach { item ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MedicationColors.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = item.medicationName,
                                modifier = Modifier.weight(1f),
                                color = MedicationColors.Dark,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = if (item.isActive) {
                                    MedicationColors.SoftGreen
                                } else {
                                    MedicationColors.InactiveBackground
                                }
                            ) {
                                Text(
                                    text = if (item.isActive) {
                                        "Active"
                                    } else {
                                        "Inactive"
                                    },
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    color = if (item.isActive) {
                                        MedicationColors.Primary
                                    } else {
                                        MedicationColors.Muted
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        item.notes
                            ?.takeIf { it.isNotBlank() }
                            ?.let {
                                Text(
                                    text = it,
                                    color = MedicationColors.Muted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                        HorizontalDivider(
                            color = MedicationColors.Border
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            OutlinedButton(
                                onClick = {
                                    medicationEditor = item
                                    showAddMedication = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MedicationColors.Primary
                                )
                            ) {
                                Text("Edit")
                            }

                            OutlinedButton(
                                onClick = {
                                    medicationToDelete = item
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MedicationColors.Danger
                                )
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }

        // ------------------------------------------------
        // ADHERENCE HISTORY
        // ------------------------------------------------

        Spacer(modifier = Modifier.height(4.dp))

        SectionHeading(
            number = "02",
            title = "Adherence history",
            subtitle = "Review your recorded medication intake and correct any mistakes."
        )

        if (state.logs.isEmpty()) {

            EmptyStateCard(
                title = "No intake history",
                message = "Your recorded medication intake will appear here."
            )

        } else {

            state.logs
                .sortedByDescending { it.scheduledAt }
                .forEach { log ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MedicationColors.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Text(
                                text = names[log.medicationId]?.medicationName
                                    ?: "Medication #${log.medicationId}",
                                color = MedicationColors.Dark,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = log.scheduledAt,
                                    color = MedicationColors.Muted,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = when (log.status.lowercase()) {
                                        "taken" -> MedicationColors.SoftGreen
                                        "skipped" -> MedicationColors.InactiveBackground
                                        else -> MedicationColors.ErrorBackground
                                    }
                                ) {
                                    Text(
                                        text = log.status.replaceFirstChar {
                                            it.uppercase()
                                        },
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 5.dp
                                        ),
                                        color = when (log.status.lowercase()) {
                                            "taken" -> MedicationColors.Primary
                                            "skipped" -> MedicationColors.Muted
                                            else -> MedicationColors.Danger
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            log.notes
                                ?.takeIf { it.isNotBlank() }
                                ?.let {
                                    Text(
                                        text = it,
                                        color = MedicationColors.Muted,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                            HorizontalDivider(
                                color = MedicationColors.Border
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {

                                OutlinedButton(
                                    onClick = {
                                        logEditor = log
                                        showLogEditor = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MedicationColors.Primary
                                    )
                                ) {
                                    Text("Correct")
                                }

                                OutlinedButton(
                                    onClick = {
                                        logToDelete = log
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MedicationColors.Danger
                                    )
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
        }

        // ------------------------------------------------
        // RECORD MEDICATION INTAKE
        // ------------------------------------------------

        Spacer(modifier = Modifier.height(4.dp))

        SectionHeading(
            number = "03",
            title = "Record medication intake",
            subtitle = "Keep your medication history up to date."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MedicationColors.SoftGreen
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Have you taken your medication?",
                    color = MedicationColors.Dark,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Record your intake to keep an accurate history of your medication routine.",
                    color = MedicationColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = {
                        logEditor = null
                        showLogEditor = true
                    },
                    enabled = !state.saving &&
                            state.medications.any { it.isActive },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MedicationColors.Primary
                    )
                ) {
                    Text(
                        text = "Log medication intake",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (state.medications.none { it.isActive }) {
                    Text(
                        text = "Add an active medication before logging intake.",
                        color = MedicationColors.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // ------------------------------------------------
    // ADD / EDIT MEDICATION DIALOG
    // ------------------------------------------------

    if (showAddMedication) {

        MedicationEditorDialog(
            original = medicationEditor,
            onDismiss = {
                showAddMedication = false
            },
            onSave = { name, notes, active ->

                medicationEditor?.let {
                    vm.editMedication(it, name, notes, active)
                } ?: vm.addMedication(name, notes)

                showAddMedication = false
            }
        )
    }

    // ------------------------------------------------
    // ADD / EDIT INTAKE LOG DIALOG
    // ------------------------------------------------

    if (showLogEditor) {

        LogEditorDialog(
            original = logEditor,
            medications = state.medications,
            onDismiss = {
                showLogEditor = false
            },
            onSave = { medicationId, whenAt, status, notes ->

                logEditor?.let {
                    vm.editLog(it, whenAt, status, notes)
                } ?: vm.addLog(medicationId, whenAt, status, notes)

                showLogEditor = false
            }
        )
    }

    // ------------------------------------------------
    // DELETE MEDICATION CONFIRMATION
    // ------------------------------------------------

    medicationToDelete?.let { item ->

        ConfirmDeleteDialog(
            title = "Permanently delete ${item.medicationName}?",
            details = "This also deletes its reminders and intake history because of database cascading deletes. Consider editing and marking it inactive instead.",
            onCancel = {
                medicationToDelete = null
            },
            onConfirm = {
                vm.deleteMedication(item)
                medicationToDelete = null
            }
        )
    }

    // ------------------------------------------------
    // DELETE INTAKE LOG CONFIRMATION
    // ------------------------------------------------

    logToDelete?.let { item ->

        ConfirmDeleteDialog(
            title = "Delete this intake record?",
            details = "This deletes the recorded ${item.status} event. It cannot be undone.",
            onCancel = {
                logToDelete = null
            },
            onConfirm = {
                vm.deleteLog(item)
                logToDelete = null
            }
        )
    }
}

// ----------------------------------------------------
// REUSABLE SECTION HEADING
// ----------------------------------------------------

@Composable
private fun SectionHeading(
    number: String,
    title: String,
    subtitle: String
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MedicationColors.Primary
            ) {
                Text(
                    text = number,
                    modifier = Modifier.padding(
                        horizontal = 11.dp,
                        vertical = 7.dp
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(
                text = title,
                color = MedicationColors.Dark,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = subtitle,
            color = MedicationColors.Muted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ----------------------------------------------------
// EMPTY STATE CARD
// ----------------------------------------------------

@Composable
private fun EmptyStateCard(
    title: String,
    message: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MedicationColors.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = title,
                color = MedicationColors.Dark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message,
                color = MedicationColors.Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ----------------------------------------------------
// MEDICATION EDITOR DIALOG
// ----------------------------------------------------

@Composable
private fun MedicationEditorDialog(
    original: Medication?,
    onDismiss: () -> Unit,
    onSave: (String, String?, Boolean) -> Unit
) {

    var name by remember(original) {
        mutableStateOf(original?.medicationName.orEmpty())
    }

    var notes by remember(original) {
        mutableStateOf(original?.notes.orEmpty())
    }

    var active by remember(original) {
        mutableStateOf(original?.isActive ?: true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = if (original == null) {
                    "Add medication"
                } else {
                    "Edit medication"
                },
                color = MedicationColors.Dark,
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text("Medication name")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = {
                        Text("Notes (optional)")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (original != null) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = active,
                            onCheckedChange = { active = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MedicationColors.Primary
                            )
                        )

                        Text(
                            text = "Active",
                            color = MedicationColors.Dark
                        )
                    }
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {
                    onSave(
                        name.trim(),
                        notes.trim().ifEmpty { null },
                        active
                    )
                },
                enabled = name.isNotBlank() &&
                        name.length <= 150 &&
                        notes.length <= 255
            ) {
                Text(
                    "Save",
                    color = MedicationColors.Primary
                )
            }
        },

        dismissButton = {

            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },

        containerColor = MedicationColors.White
    )
}

// ----------------------------------------------------
// DATE AND TIME HELPERS
// ----------------------------------------------------

private fun nowForApi(): String =
    SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        Locale.US
    ).format(Date())

private fun validDateTime(value: String): Boolean {

    if (!Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")
            .matches(value)
    ) {
        return false
    }

    return runCatching {
        SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.US
        ).apply {
            isLenient = false
        }.parse(value) != null
    }.getOrDefault(false)
}

// ----------------------------------------------------
// LOG EDITOR DIALOG
// ----------------------------------------------------

@Composable
fun LogEditorDialog(
    original: MedicationLog?,
    medications: List<Medication>,
    onDismiss: () -> Unit,
    onSave: (Int, String, String, String?) -> Unit
) {

    val choices = if (original == null) {
        medications.filter { it.isActive }
    } else {
        medications
    }

    var medicationId by remember(original) {
        mutableIntStateOf(
            original?.medicationId
                ?: choices.firstOrNull()?.medicationId
                ?: -1
        )
    }

    var whenAt by remember(original) {
        mutableStateOf(original?.scheduledAt ?: nowForApi())
    }

    var status by remember(original) {
        mutableStateOf(original?.status ?: "taken")
    }

    var notes by remember(original) {
        mutableStateOf(original?.notes.orEmpty())
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = if (original == null) {
                    "Log medication intake"
                } else {
                    "Correct intake log"
                },
                color = MedicationColors.Dark,
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                if (original == null) {

                    Box {

                        OutlinedButton(
                            onClick = {
                                expanded = true
                            }
                        ) {
                            Text(
                                choices.firstOrNull {
                                    it.medicationId == medicationId
                                }?.medicationName ?: "Select medication"
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {

                            choices.forEach { medication ->

                                DropdownMenuItem(
                                    text = {
                                        Text(medication.medicationName)
                                    },
                                    onClick = {
                                        medicationId = medication.medicationId
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                } else {

                    Text(
                        text = "Medication: ${
                            medications.firstOrNull {
                                it.medicationId == original.medicationId
                            }?.medicationName ?: original.medicationId
                        }"
                    )
                }

                OutlinedTextField(
                    value = whenAt,
                    onValueChange = { whenAt = it },
                    label = {
                        Text("Scheduled date/time")
                    },
                    supportingText = {
                        Text("YYYY-MM-DD HH:MM:SS (local time)")
                    },
                    isError = !validDateTime(whenAt),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Intake status",
                    color = MedicationColors.Dark,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    listOf("taken", "skipped", "missed").forEach { option ->

                        FilterChip(
                            selected = status == option,
                            onClick = {
                                status = option
                            },
                            label = {
                                Text(option.replaceFirstChar {
                                    it.uppercase()
                                })
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedicationColors.SoftGreen,
                                selectedLabelColor = MedicationColors.Primary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = {
                        Text("Notes (optional)")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick = {
                    onSave(
                        medicationId,
                        whenAt.trim(),
                        status,
                        notes.trim().ifEmpty { null }
                    )
                },
                enabled = medicationId > 0 &&
                        validDateTime(whenAt) &&
                        notes.length <= 255
            ) {
                Text(
                    "Save",
                    color = MedicationColors.Primary
                )
            }
        },

        dismissButton = {

            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },

        containerColor = MedicationColors.White
    )
}

// ----------------------------------------------------
// DELETE CONFIRMATION DIALOG
// ----------------------------------------------------

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    details: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onCancel,

        title = {
            Text(
                text = title,
                color = MedicationColors.Dark,
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Text(
                text = details,
                color = MedicationColors.Muted
            )
        },

        confirmButton = {

            TextButton(onClick = onConfirm) {
                Text(
                    "Delete permanently",
                    color = MedicationColors.Danger
                )
            }
        },

        dismissButton = {

            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },

        containerColor = MedicationColors.White
    )
}

// ----------------------------------------------------
// MEDICATION SCREEN COLOUR PALETTE
// ----------------------------------------------------

private object MedicationColors {

    val Background = Color(0xFFF3F7F3)
    val White = Color(0xFFFFFFFF)

    val Primary = Color(0xFF426B57)
    val Dark = Color(0xFF263A33)
    val Muted = Color(0xFF718078)

    val SoftGreen = Color(0xFFE5EFE7)
    val Border = Color(0xFFDCE6DE)

    val InactiveBackground = Color(0xFFEDEFEF)
    val ErrorBackground = Color(0xFFFBEAEA)
    val Danger = Color(0xFFB85C5C)
}
