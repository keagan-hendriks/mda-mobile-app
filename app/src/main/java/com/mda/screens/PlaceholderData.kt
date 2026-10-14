package com.mda.screens

// ---------------------------------------------------------------
// ALL FAKE DATA LIVES HERE.
// API / database teammates: replace these values with real data
// (or delete this file and feed the screens from your own source).
// ---------------------------------------------------------------

data class Reminder(val name: String, val detail: String, val time: String, val taken: Boolean)
data class AlertItem(val message: String, val time: String, val unread: Boolean)
data class MedItem(val name: String, val subtitle: String, val dose: String, val category: String)

object PlaceholderData {
    const val fullName = "Sarah Jenkins"
    const val firstName = "Sarah"
    const val initials = "SJ"
    const val email = "sarah.j@workspace.com"
    const val doctor = "Dr. Robert Chen"
    const val memberSince = "Member since Jan 2026"

    const val adherencePercent = 85
    const val streakDays = 12

    val reminders = listOf(
        Reminder("Amoxicillin", "500mg \u2022 After meals", "8:00 AM", taken = true),
        Reminder("Lisinopril", "10mg \u2022 Daily pill", "12:30 PM", taken = false),
    )

    val alerts = listOf(
        AlertItem("Refill alert: Your Amoxicillin pack is low (2 units remaining)", "10 mins ago", unread = true),
        AlertItem("Weekly adherence summary: 94% score achieved! \uD83C\uDF89", "2 hours ago", unread = true),
        AlertItem("Doctor Chen modified your dosage for Lisinopril", "Yesterday", unread = false),
        AlertItem("Security Alert: Successful login from secondary device", "3 days ago", unread = false),
    )

    val searchCategories = listOf("All", "Prescriptions", "Vitamins", "Supplements", "OTC")

    val medications = listOf(
        MedItem("Ibuprofen Liquid Gels", "Pain reliever \u2022 OTC", "200mg", "OTC"),
        MedItem("Vitamin D3 Daily", "Bone Health \u2022 Supplement", "2000 IU", "Vitamins"),
        MedItem("Metformin Extended", "Blood sugar \u2022 Prescription", "500mg", "Prescriptions"),
    )
}
