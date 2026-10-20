package com.example.mda_mobile_app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import java.util.Locale


class ReminderActivity : Activity() {

    private lateinit var nameInput: EditText
    private lateinit var timePicker: TimePicker
    private lateinit var remindersContainer: LinearLayout
    private lateinit var noRemindersText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        nameInput = findViewById(R.id.medicationNameInput)
        timePicker = findViewById(R.id.timePicker)
        remindersContainer = findViewById(R.id.remindersContainer)
        noRemindersText = findViewById(R.id.noRemindersText)

        Reminders.createChannel(this)
        timePicker.setIs24HourView(true) // 24-hour clock: 20:30 = 8:30 pm

        findViewById<Button>(R.id.saveReminderButton).setOnClickListener { saveReminder() }
        findViewById<Button>(R.id.testNotificationButton).setOnClickListener { sendTestNotification() }

        showSavedReminders()
    }

    // SAVE

    private fun saveReminder() {
        val name = nameInput.text.toString().trim()
        if (name.isEmpty()) {
            nameInput.error = "Enter the medication name"
            return
        }
        val hour = timePicker.hour
        val minute = timePicker.minute

        //  API

        val reminder_id = reminders()


        Reminders.save(this, Reminder(reminder_id, name, hour, minute))

        nameInput.text.clear()
        Toast.makeText(
            this, "Reminder set for ${formatTime(hour, minute)} every day", Toast.LENGTH_LONG
        ).show()
        showSavedReminders()
        checkPermissions()
    }


    private fun reminders(): Int {
        val highest = ReminderStore(this).getAll().maxOfOrNull { it.id } ?: 0
        return maxOf(100, highest + 1)
    }

    // THE LIST OF SAVED REMINDERS

    private fun showSavedReminders() {
        val reminders = ReminderStore(this).getAll()
            .sortedWith(compareBy<Reminder>({ it.hour }, { it.minute }))

        remindersContainer.removeAllViews()
        noRemindersText.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE

        for (reminder in reminders) {
            val row = layoutInflater.inflate(R.layout.item_reminder, remindersContainer, false)
            val when_ = if (reminder.days.isEmpty()) "every day" else "selected days"
            row.findViewById<TextView>(R.id.reminderTime).text = formatTime(reminder.hour, reminder.minute)
            row.findViewById<TextView>(R.id.reminderName).text = "${reminder.medicationName} - $when_"
            row.findViewById<Button>(R.id.deleteButton).setOnClickListener {

                //  CHANGE WHEN THE API IS READY: also delete the reminder on the server here.
                Reminders.remove(this, reminder_id)
                showSavedReminders()
            }
            remindersContainer.addView(row)
        }
    }

    private fun formatTime(hour: Int, minute: Int): String =
        String.format(Locale.US, "%02d:%02d", hour, minute)


    // PERMISSIONS (without these the notifications would never appear)


    private fun checkPermissions() {
        when {
            Reminders.notificationsAllowed(this) -> Reminders.offerExactAlarmPermission(this)

            Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ->
                Reminders.askForNotificationPermission(this)

            else -> showNotificationsBlockedDialog()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (Reminders.notificationsAllowed(this)) {
                Reminders.offerExactAlarmPermission(this)
            } else {
                showNotificationsBlockedDialog()
            }
        }
    }

    private fun showNotificationsBlockedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notifications are off")
            .setMessage("Your reminder is saved, but you won't see it until notifications are allowed for this app.")
            .setPositiveButton("Open settings") { _, _ -> Reminders.openNotificationSettings(this) }
            .setNegativeButton("Not now", null)
            .show()
    }

    // TEST BUTTON ( delete this button and function later)


    private fun sendTestNotification() {
        if (Reminders.notificationsAllowed(this)) {
            Reminders.showNotification(this, 1, "Test medication")
            Toast.makeText(this, "Sent! Swipe down from the top of the screen.", Toast.LENGTH_LONG).show()
        } else {
            checkPermissions()
        }
    }
}
