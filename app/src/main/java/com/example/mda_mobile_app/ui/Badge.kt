package com.example.mda_mobile_app


data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val streakRequired: Int,
    val iconRes: Int
)


object Badges {
    val all = listOf(
        Badge("streak_3", "Getting Started", "Log a dose 3 days in a row", 3, R.drawable.ic_badge_bronze),
        Badge("streak_7", "Week Warrior", "Log a dose 7 days in a row", 7, R.drawable.ic_badge_silver),
        Badge("streak_14", "Two-Week Champion", "Log a dose 14 days in a row", 14, R.drawable.ic_badge_gold),
        Badge("streak_30", "Habit Hero", "Log a dose 30 days in a row", 30, R.drawable.ic_badge_platinum)
    )


    fun findById(id: String): Badge? = all.find { it.id == id }
}
