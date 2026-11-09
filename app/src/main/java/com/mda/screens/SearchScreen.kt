package com.mda.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mda_mobile_app.data.model.crud.Medication
import com.example.mda_mobile_app.ui.medication.MedicationViewModel

// TODO: replace the test userId (1) with the real logged-in user once login works.
@Composable
fun SearchScreen(userId: Int = 1) {
    val vm: MedicationViewModel = viewModel(
        key = "medication-$userId",
        factory = MedicationViewModel.factory(userId)
    )
    val state by vm.state.collectAsState()

    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }

    val results = state.medications.filter {
        it.medicationName.contains(query, ignoreCase = true) &&
                when (filter) {
                    "Active" -> it.isActive
                    "Inactive" -> !it.isActive
                    else -> true
                }
    }

    ScreenColumn {
        ScreenHeader("Search & Explore", "Browse your medications")

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search medications", color = AppColors.Muted) },
            leadingIcon = { Icon(AppIcons.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { query = "" }) {
                        Icon(AppIcons.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                    }
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(Modifier.horizontalScroll(rememberScrollState())) {
            listOf("All", "Active", "Inactive").forEach { name ->
                FilterPill(name, selected = name == filter, onClick = { filter = name })
                Spacer(Modifier.width(8.dp))
            }
        }

        SectionLabel("MATCHED ITEMS", Modifier.padding(top = 12.dp))

        if (state.loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = AppColors.Ink,
                trackColor = AppColors.Border
            )
            Spacer(Modifier.height(8.dp))
        }

        state.error?.let { message ->
            ErrorCard(message = message, onRetry = vm::refresh)
            Spacer(Modifier.height(8.dp))
        }

        results.forEach { item ->
            MedicationRow(item)
            Spacer(Modifier.height(8.dp))
        }

        if (!state.loading && state.error == null && results.isEmpty()) {
            Text(
                if (state.medications.isEmpty()) "No medications yet." else "No matches found. Try a different search.",
                color = AppColors.Muted,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    AppCard(color = Color(0xFFFEF2F2)) {
        Column(Modifier.padding(12.dp)) {
            Text("Couldn't load medications", fontWeight = FontWeight.SemiBold, color = AppColors.Danger)
            Text(message, fontSize = 12.sp, color = AppColors.Muted)
            TextButton(onClick = onRetry) { Text("Retry", color = AppColors.Ink) }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
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
private fun MedicationRow(item: Medication) {
    AppCard {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                border = BorderStroke(1.dp, AppColors.Border),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(AppIcons.Pill, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.medicationName, fontWeight = FontWeight.SemiBold, color = AppColors.Ink)
                Text(
                    item.notes?.takeIf { it.isNotBlank() } ?: "No notes",
                    fontSize = 12.sp,
                    color = AppColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                if (item.isActive) "Active" else "Inactive",
                fontSize = 13.sp,
                color = if (item.isActive) AppColors.Success else AppColors.Muted
            )
        }
    }
}