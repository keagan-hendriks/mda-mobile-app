package com.example.mda_mobile_app

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


object StreakManager {

    fun calculateStreak(takenDates: List<String>, today: Calendar = Calendar.getInstance()): Int {
        val loggedDays = takenDates.map { it.take(10) }.toSet()


        val day = today.clone() as Calendar
        // ...but if today isn't logged yet, start from yesterday instead.
        if (format(day) !in loggedDays) {
            day.add(Calendar.DAY_OF_YEAR, -1)
        }


        var streak = 0
        while (format(day) in loggedDays) {
            streak++
            day.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }


    fun dateString(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return format(cal)
    }

    private fun format(cal: Calendar): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}
