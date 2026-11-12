
package com.example.mda_mobile_app.ui.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mda_mobile_app.data.model.crud.Medication
import com.example.mda_mobile_app.data.model.crud.MedicationLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MedicationScreen(userId: Int) {

    require(userId > 0) { "userId must be positive" }

    val vm: MedicationViewModel = viewModel(
        key = "medication-$userId",
        factory = MedicationViewModel.factory(userId)
    )

    val state by vm.state.collectAsState()

    var search by rememberSaveable { mutableStateOf("") }

    var medicationEditor by remember {
        mutableStateOf<Medication?>(null)
    }

    var showAddMedication by remember {
        mutableStateOf(false)
    }

    var logEditor by remember {
        mutableStateOf<MedicationLog?>(null)
    }

    var showLogEditor by remember {
        mutableStateOf(false)
    }

    var medicationToDelete by remember {
        mutableStateOf<Medication?>(null)
    }

    var logToDelete by remember {
        mutableStateOf<MedicationLog?>(null)
    }

    val matching = state.medications.filter {
        it.medicationName.contains(search, ignoreCase = true)
    }

    val names = state.medications.associateBy {
        it.medicationId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        // SCREEN HEADER
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "My Medications",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${state.medications.size} medications tracked",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // LOADING
        if (state.loading || state.saving) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
            )
        }

        // ERROR
        state.error?.let {
            MessageCard(message = it, isError = true)
        }

        // SUCCESS
        state.notice?.let {
            MessageCard(message = it, isError = false)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = vm::refresh,
                enabled = !state.saving
            ) {
                Text("Refresh")
            }
        }

        // MEDICATIONS
        SectionHeading(
            number = "01",
            title = "Your medications",
            subtitle = "Keep track of the medication you're taking."
        )

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search medications") },
            placeholder = { Text("Enter medication name") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Button(
            onClick = {
                medicationEditor = null
                showAddMedication = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !state.saving,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                "+  Add medication",
                fontWeight = FontWeight.SemiBold
            )
        }

        if (matching.isEmpty()) {

            EmptyStateCard(
                title = if (search.isBlank()) {
                    "No medications yet"
                } else {
                    "No matching medications"
                },
                description = if (search.isBlank()) {
                    "Add your first medication to start tracking your routine."
                } else {
                    "Try searching with a different medication name."
                }
            )

        } else {

            matching.forEach { item ->

                MedicationCard(
                    medication = item,
                    onEdit = {
                        medicationEditor = item
                        showAddMedication = true
                    },
                    onDelete = {
                        medicationToDelete = item
                    }
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // ADHERENCE HISTORY
        SectionHeading(
            number = "02",
            title = "Adherence history",
            subtitle = "Review and manage your recorded medication intake."
        )

        if (state.logs.isEmpty()) {

            EmptyStateCard(
                title = "No intake history",
                description = "Your recorded medication intake will appear here."
            )

        } else {

            state.logs
                .sortedByDescending { it.scheduledAt }
                .forEach { log ->

                    IntakeHistoryCard(
                        log = log,
                        medicationName = names[log.medicationId]
                            ?.medicationName
                            ?: "Medication #${log.medicationId}",
                        onEdit = {
                            logEditor = log
                            showLogEditor = true
                        },
                        onDelete = {
                            logToDelete = log
                        }
                    )
                }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // RECORD INTAKE
        SectionHeading(
            number = "03",
            title = "Record medication intake",
            subtitle = "Keep your adherence history up to date."
        )

        Button(
            onClick = {
                logEditor = null
                showLogEditor = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = state.medications.any { it.isActive },
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                "＋  Log medication intake",
                fontWeight = FontWeight.SemiBold
            )
        }

        if (state.medications.none { it.isActive }) {
            Text(
                text = "Add an active medication before recording intake.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(20.dp))
    }

    // ADD / EDIT MEDICATION
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

    // ADD / EDIT INTAKE LOG
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
                } ?: vm.addLog(
                    medicationId,
                    whenAt,
                    status,
                    notes
                )

                showLogEditor = false
            }
        )
    }

    // DELETE MEDICATION
    medicationToDelete?.let { item ->

        ConfirmDeleteDialog(
            title = "Delete medication?",
            details = "This will permanently remove ${item.medicationName}, its reminders and related intake history. This action cannot be undone.",
            onCancel = {
                medicationToDelete = null
            },
            onConfirm = {
                vm.deleteMedication(item)
                medicationToDelete = null
            }
        )
    }

    // DELETE INTAKE LOG
    logToDelete?.let { item ->

        ConfirmDeleteDialog(
            title = "Delete intake record?",
            details = "This will permanently delete this ${item.status} intake record.",
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


// --------------------------------------------------
// SECTION HEADING
// --------------------------------------------------

@Composable
private fun SectionHeading(
    number: String,
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    number,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


// --------------------------------------------------
// MEDICATION CARD
// --------------------------------------------------

@Composable
private fun MedicationCard(
    medication: Medication,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardAlpha = if (medication.isActive) 1f else 0.65f

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = cardAlpha
            )
        )
    ) {
        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "💊",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            medication.medicationName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        StatusLabel(
                            text = if (medication.isActive) "Active" else "Inactive",
                            active = medication.isActive
                        )
                    }

                    medication.notes
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                    Text(
                        "Medication ID: ${medication.medicationId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}


// --------------------------------------------------
// INTAKE HISTORY CARD
// --------------------------------------------------

@Composable
private fun IntakeHistoryCard(
    log: MedicationLog,
    medicationName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    medicationName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                StatusLabel(
                    text = log.status.replaceFirstChar { it.uppercase() },
                    active = log.status == "taken"
                )
            }

            Text(
                text = log.scheduledAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            log.notes?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Correct")
                }

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}


// --------------------------------------------------
// STATUS LABEL
// --------------------------------------------------

@Composable
private fun StatusLabel(
    text: String,
    active: Boolean
) {
    Surface(
        color = if (active) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(7.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (active) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}


// --------------------------------------------------
// EMPTY STATE
// --------------------------------------------------

@Composable
private fun EmptyStateCard(
    title: String,
    description: String
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                "💊",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// --------------------------------------------------
// MESSAGE CARD
// --------------------------------------------------

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}


// --------------------------------------------------
// MEDICATION EDITOR
// --------------------------------------------------

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
                if (original == null) "Add medication" else "Edit medication",
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    if (original == null) {
                        "Add a medication to your tracking list."
                    } else {
                        "Update your medication details."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                if (original != null) {
                    HorizontalDivider()

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = active,
                            onCheckedChange = { active = it }
                        )

                        Column {
                            Text(
                                "Medication active",
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                "Uncheck to mark this medication inactive.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {
            Button(
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
                Text("Save")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },

        shape = RoundedCornerShape(22.dp)
    )
}


// --------------------------------------------------
// DATE/TIME HELPERS FOR INTAKE LOGS
// --------------------------------------------------

private fun nowForApi(): String =
    SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        Locale.US
    ).format(Date())


private fun validDateTime(value: String): Boolean {
    if (
        !Regex("""\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""")
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


// --------------------------------------------------
// INTAKE LOG EDITOR
// --------------------------------------------------

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
                if (original == null) {
                    "Log medication intake"
                } else {
                    "Correct intake log"
                },
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (original == null) {
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
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
                        "Medication: ${
                            medications.firstOrNull {
                                it.medicationId == original.medicationId
                            }?.medicationName ?: original.medicationId
                        }"
                    )
                }

                OutlinedTextField(
                    value = whenAt,
                    onValueChange = { whenAt = it },
                    label = { Text("Scheduled date/time") },
                    supportingText = {
                        Text("YYYY-MM-DD HH:MM:SS (local time)")
                    },
                    isError = !validDateTime(whenAt),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    "Intake status",
                    fontWeight = FontWeight.Medium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    listOf("taken", "skipped", "missed").forEach { option ->
                        FilterChip(
                            selected = status == option,
                            onClick = { status = option },
                            label = {
                                Text(
                                    option.replaceFirstChar {
                                        it.uppercase()
                                    }
                                )
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },

        confirmButton = {
            Button(
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
                Text("Save")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },

        shape = RoundedCornerShape(22.dp)
    )
}


// --------------------------------------------------
// DELETE CONFIRMATION
// --------------------------------------------------

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
                title,
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Text(details)
        },

        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },

        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },

        shape = RoundedCornerShape(22.dp)
    )
}
