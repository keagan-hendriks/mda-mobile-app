//package com.example.mda_mobile_app
//
//import java.util.Calendar
//
//
//data class Reminder(
//    val id: Int,
//    val medicationName: String,
//    val hour: Int,
//    val minute: Int,
//    val days: Set<Int> = emptySet()
//)
//
//// Works out WHEN a reminder should ring next
//object ReminderTime {
//
//
//    fun nextTrigger(
//        hour: Int,
//        minute: Int,
//        days: Set<Int>,
//        now: Calendar = Calendar.getInstance()
//    ): Calendar {
//        val candidate = now.clone() as Calendar
//        candidate.set(Calendar.HOUR_OF_DAY, hour)
//        candidate.set(Calendar.MINUTE, minute)
//        candidate.set(Calendar.SECOND, 0)
//        candidate.set(Calendar.MILLISECOND, 0)
//
//        // Check today, then each following day
//        repeat(8) {
//            val dayIsAllowed = days.isEmpty() || candidate.get(Calendar.DAY_OF_WEEK) in days
//            if (dayIsAllowed && candidate.after(now)) return candidate
//            candidate.add(Calendar.DAY_OF_YEAR, 1)
//        }
//        return candidate
//    }
//}
