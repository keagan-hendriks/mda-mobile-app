package com.mda.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }

    // TODO (API / database teammate): replace with a real search
    val results = PlaceholderData.medications.filter {
        it.name.contains(query, ignoreCase = true) &&
                (category == "All" || it.category == category)
    }

    ScreenColumn {
        ScreenHeader("Search & Explore", "Browse health logs and treatments")

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
            PlaceholderData.searchCategories.forEach { name ->
                FilterPill(name, selected = name == category, onClick = { category = name })
                Spacer(Modifier.width(8.dp))
            }
        }

        SectionLabel("MATCHED ITEMS", Modifier.padding(top = 12.dp))

        results.forEach { item ->
            MedicationRow(item)
            Spacer(Modifier.height(8.dp))
        }
        if (results.isEmpty()) {
            Text("No matches found. Try a different search.", color = AppColors.Muted, fontSize = 14.sp)
        }
        Spacer(Modifier.height(24.dp))
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
private fun MedicationRow(item: MedItem) {
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
                Text(item.name, fontWeight = FontWeight.SemiBold, color = AppColors.Ink)
                Text(item.subtitle, fontSize = 12.sp, color = AppColors.Muted)
            }
            Text(item.dose, fontSize = 13.sp, color = AppColors.Ink)
        }
    }
}