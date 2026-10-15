package com.mda.screens

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

enum class BottomTab(val label: String, val icon: String) {
    Home("Home", "\uD83C\uDFE0"),
    Search("Search", "\uD83D\uDD0D"),
    Alerts("Alerts", "\uD83D\uDD14"),
    Profile("Profile", "\uD83D\uDC64"),
    Settings("Settings", "\u2699\uFE0F"),
}

@Composable
fun BottomNavBar(selected: BottomTab, onSelect: (BottomTab) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Text(tab.icon, fontSize = 20.sp) },
                label = { Text(tab.label, fontSize = 11.sp) }
            )
        }
    }
}