package com.mda.screens

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

enum class BottomTab(val label: String, val icon: ImageVector) {
    Home("Home", AppIcons.Home),
    Medications("Meds", AppIcons.Pill),
    Alerts("Alerts", AppIcons.Bell),
    Profile("Profile", AppIcons.Person),
    Settings("Settings", AppIcons.Settings),
}

@Composable
fun BottomNavBar(selected: BottomTab, onSelect: (BottomTab) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Ink,
                    selectedTextColor = AppColors.Ink,
                    indicatorColor = AppColors.ButtonFill,
                    unselectedIconColor = AppColors.Muted,
                    unselectedTextColor = AppColors.Muted
                )
            )
        }
    }
}