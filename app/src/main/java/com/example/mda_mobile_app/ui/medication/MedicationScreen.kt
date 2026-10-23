package com.example.mda_mobile_app.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mda_mobile_app.data.model.crud.Medication
import com.example.mda_mobile_app.data.model.crud.MedicationLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//TODO: replace user with real user when Login code works
//local testing only replace userID with real user after login code is added
@Composable
fun MedicationScreen(userId: Int) {
    require(userId > 0) { "userId must be positive" }
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
    val matching = state.medications.filter { it.medicationName.contains(search, ignoreCase = true) }
    val names = state.medications.associateBy { it.medicationId }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("My Medications", style = MaterialTheme.typography.headlineSmall)
        Text("Manage your medications and medication intake history")
        if (state.loading || state.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
        state.notice?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        TextButton(onClick = vm::refresh, enabled = !state.saving) { Text("Refresh") }

        HorizontalDivider()
        Text("1. Manage Medications", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = search, onValueChange = { search = it },
            label = { Text("Search your medications") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(onClick = { medicationEditor = null; showAddMedication = true }, enabled = !state.saving) {
            Text("Add medication")
        }
        if (matching.isEmpty()) Text("No medications found.")
        matching.forEach { item ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.medicationName, fontWeight = FontWeight.Bold)
                    Text(if (item.isActive) "Active" else "Inactive")
                    item.notes?.takeIf { it.isNotBlank() }?.let { Text(it) }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { medicationEditor = item; showAddMedication = true }) { Text("Edit") }
                        TextButton(onClick = { medicationToDelete = item }) { Text("Delete") }
                    }
                }
            }
        }

        HorizontalDivider()
        Text("2. Adherence History", style = MaterialTheme.typography.titleLarge)
        Text("Edit mistaken entries or delete incorrect records. These are individual intake logs, not aggregated reports.")
        if (state.logs.isEmpty()) Text("No intake has been logged yet.")
        state.logs.sortedByDescending { it.scheduledAt }.forEach { log ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(names[log.medicationId]?.medicationName ?: "Medication #${log.medicationId}", fontWeight = FontWeight.Bold)
                    Text("${log.scheduledAt} • ${log.status.replaceFirstChar { it.uppercase() }}")
                    log.notes?.takeIf { it.isNotBlank() }?.let { Text(it) }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { logEditor = log; showLogEditor = true }) { Text("Correct") }
                        TextButton(onClick = { logToDelete = log }) { Text("Delete") }
                    }
                }
            }
        }

        HorizontalDivider()
        Text("3. Record Medication Intake", style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { showLogEditor = true }) {
                Text("Log intake")
            }
        }
        if (state.medications.none { it.isActive }) Text("Add an active medication before logging intake.")
        Spacer(Modifier.height(20.dp))
    }

    if (showAddMedication) {
        MedicationEditorDialog(
            original = medicationEditor,
            onDismiss = { showAddMedication = false },
            onSave = { name, notes, active ->
                medicationEditor?.let { vm.editMedication(it, name, notes, active) }
                    ?: vm.addMedication(name, notes)
                showAddMedication = false
            }
        )
    }
    if (showLogEditor) {
        LogEditorDialog(
            original = logEditor,
            medications = state.medications,
            onDismiss = { showLogEditor = false },
            onSave = { medicationId, whenAt, status, notes ->
                logEditor?.let { vm.editLog(it, whenAt, status, notes) }
                    ?: vm.addLog(medicationId, whenAt, status, notes)
                showLogEditor = false
            }
        )
    }
    medicationToDelete?.let { item ->
        ConfirmDeleteDialog(
            title = "Permanently delete ${item.medicationName}?",
            details = "This also deletes its reminders and intake history because of database cascading deletes. Consider editing and marking it inactive instead.",
            onCancel = { medicationToDelete = null },
            onConfirm = { vm.deleteMedication(item); medicationToDelete = null }
        )
    }
    logToDelete?.let { item ->
        ConfirmDeleteDialog(
            title = "Delete this intake record?",
            details = "This deletes the recorded ${item.status} event. It cannot be undone.",
            onCancel = { logToDelete = null },
            onConfirm = { vm.deleteLog(item); logToDelete = null }
        )
    }
}

@Composable
private fun MedicationEditorDialog(original: Medication?, onDismiss: () -> Unit, onSave: (String, String?, Boolean) -> Unit) {
    var name by remember(original) { mutableStateOf(original?.medicationName.orEmpty()) }
    var notes by remember(original) { mutableStateOf(original?.notes.orEmpty()) }
    var active by remember(original) { mutableStateOf(original?.isActive ?: true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (original == null) "Add medication" else "Edit medication") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Medication name") }, singleLine = true)
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") })
                if (original != null) Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = active, onCheckedChange = { active = it })
                    Text("Active")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), notes.trim().ifEmpty { null }, active) },
                enabled = name.isNotBlank() && name.length <= 150 && notes.length <= 255
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun nowForApi(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
private fun validDateTime(value: String): Boolean {
    if (!Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}").matches(value)) return false
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply { isLenient = false }.parse(value) != null
    }.getOrDefault(false)
}

@Composable
fun LogEditorDialog(
    original: MedicationLog?, medications: List<Medication>,
    onDismiss: () -> Unit, onSave: (Int, String, String, String?) -> Unit
) {
    val choices = if (original == null) medications.filter { it.isActive } else medications
    var medicationId by remember(original) { mutableIntStateOf(original?.medicationId ?: choices.firstOrNull()?.medicationId ?: -1) }
    var whenAt by remember(original) { mutableStateOf(original?.scheduledAt ?: nowForApi()) }
    var status by remember(original) { mutableStateOf(original?.status ?: "taken") }
    var notes by remember(original) { mutableStateOf(original?.notes.orEmpty()) }
    var expanded by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (original == null) "Log medication intake" else "Correct intake log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (original == null) {
                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text(choices.firstOrNull { it.medicationId == medicationId }?.medicationName ?: "Select medication")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            choices.forEach { medication ->
                                DropdownMenuItem(
                                    text = { Text(medication.medicationName) },
                                    onClick = { medicationId = medication.medicationId; expanded = false }
                                )
                            }
                        }
                    }
                } else {
                    Text("Medication: ${medications.firstOrNull { it.medicationId == original.medicationId }?.medicationName ?: original.medicationId}")
                }
                OutlinedTextField(
                    whenAt, { whenAt = it },
                    label = { Text("Scheduled date/time") },
                    supportingText = { Text("YYYY-MM-DD HH:MM:SS (local time)") },
                    isError = !validDateTime(whenAt)
                )
                Text("Intake status")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("taken", "skipped", "missed").forEach { option ->
                        FilterChip(selected = status == option, onClick = { status = option }, label = { Text(option) })
                    }
                }
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(medicationId, whenAt.trim(), status, notes.trim().ifEmpty { null }) },
                enabled = medicationId > 0 && validDateTime(whenAt) && notes.length <= 255
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ConfirmDeleteDialog(title: String, details: String, onCancel: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) }, text = { Text(details) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete permanently") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}
