package com.example.mda_mobile_app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.mda_mobile_app.data.model.crud.CreateReminderRequest
import com.example.mda_mobile_app.data.model.crud.Medication
import com.example.mda_mobile_app.data.model.crud.Reminder as ApiReminder
import com.example.mda_mobile_app.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ReminderActivity : Activity() {

    companion object {
        // The screen that opens this Activity must pass the logged-in user's ID.
        const val EXTRA_USER_ID = "user_id"
    }

    private val activityJob = SupervisorJob()
    private val activityScope =
        CoroutineScope(activityJob + Dispatchers.Main)

    private var userId: Int = -1
    private var medications: List<Medication> = emptyList()

    private lateinit var medicationSpinner: Spinner
    private lateinit var timePicker: TimePicker
    private lateinit var remindersContainer: LinearLayout
    private lateinit var noRemindersText: TextView
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        medicationSpinner = findViewById(R.id.medicationSpinner)
        timePicker = findViewById(R.id.timePicker)
        remindersContainer = findViewById(R.id.remindersContainer)
        noRemindersText = findViewById(R.id.noRemindersText)
        saveButton = findViewById(R.id.saveReminderButton)

        userId = intent.getIntExtra(EXTRA_USER_ID, -1)

        Reminders.createChannel(this)
        timePicker.setIs24HourView(true)

        saveButton.setOnClickListener { saveReminder() }

        findViewById<Button>(R.id.testNotificationButton)
            .setOnClickListener { sendTestNotification() }

        if (userId <= 0) {
            Toast.makeText(
                this,
                "User ID is missing. Connect this screen to the logged-in user first.",
                Toast.LENGTH_LONG
            ).show()

            saveButton.isEnabled = false
            medicationSpinner.isEnabled = false
            return
        }

        loadMedications()
    }

    private fun loadMedications() {
        activityScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.listMedications(userId)
                }

                val body = response.body()

                if (!response.isSuccessful || body?.success != true) {
                    showError(body?.message ?: "Could not load medications.")
                    return@launch
                }

                medications = body.data.orEmpty()

                val adapter = ArrayAdapter(
                    this@ReminderActivity,
                    android.R.layout.simple_spinner_item,
                    medications.map { it.medicationName }
                )

                adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )

                medicationSpinner.adapter = adapter

                saveButton.isEnabled = medications.isNotEmpty()

                if (medications.isEmpty()) {
                    showError("No medications found. Add a medication first.")
                    renderReminders(emptyList())
                } else {
                    loadReminders()
                }

            } catch (e: Exception) {
                showError("Could not connect to the server: ${e.message}")
            }
        }
    }

    private fun saveReminder() {
        val selectedIndex = medicationSpinner.selectedItemPosition

        if (selectedIndex !in medications.indices) {
            showError("Please select a medication.")
            return
        }

        val medication = medications[selectedIndex]
        val hour = timePicker.hour
        val minute = timePicker.minute
        val time = String.format(
            Locale.US,
            "%02d:%02d:00",
            hour,
            minute
        )

        saveButton.isEnabled = false

        activityScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.createReminder(
                        CreateReminderRequest(
                            medicationId = medication.medicationId,
                            reminderTime = time,
                            repeatDaily = true,
                            isEnabled = true
                        )
                    )
                }

                val body = response.body()
                val savedReminder = body?.data

                if (!response.isSuccessful ||
                    body?.success != true ||
                    savedReminder == null
                ) {
                    showError(body?.message ?: "Could not save reminder.")
                    return@launch
                }

                // Use the server-generated reminder ID for the local alarm.
                val localReminder = Reminder(
                    id = savedReminder.reminderId,
                    medicationName = medication.medicationName,
                    hour = hour,
                    minute = minute
                )

                Reminders.save(this@ReminderActivity, localReminder)

                Toast.makeText(
                    this@ReminderActivity,
                    "Reminder saved for ${formatTime(hour, minute)}.",
                    Toast.LENGTH_LONG
                ).show()

                checkPermissions()
                loadReminders()

            } catch (e: Exception) {
                showError("Could not save reminder: ${e.message}")
            } finally {
                saveButton.isEnabled = medications.isNotEmpty()
            }
        }
    }

    private fun loadReminders() {
        activityScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.listReminders(userId)
                }

                val body = response.body()

                if (!response.isSuccessful || body?.success != true) {
                    showError(body?.message ?: "Could not load reminders.")
                    return@launch
                }

                val apiReminders: List<ApiReminder> =
                    body.data.orEmpty()

                val medicationNames = medications.associate {
                    it.medicationId to it.medicationName
                }

                val localReminders = apiReminders.mapNotNull { reminder ->
                    val medicationName =
                        medicationNames[reminder.medicationId]
                            ?: return@mapNotNull null

                    val parts = reminder.reminderTime.split(":")
                    if (parts.size < 2) return@mapNotNull null

                    val hour = parts[0].toIntOrNull()
                        ?: return@mapNotNull null
                    val minute = parts[1].toIntOrNull()
                        ?: return@mapNotNull null

                    Reminder(
                        id = reminder.reminderId,
                        medicationName = medicationName,
                        hour = hour,
                        minute = minute
                    )
                }

                // Keep local notification storage in sync with the server.
                Reminders.replaceAll(
                    this@ReminderActivity,
                    localReminders
                )

                renderReminders(localReminders)

            } catch (e: Exception) {
                showError("Could not load reminders: ${e.message}")
            }
        }
    }

    private fun renderReminders(reminders: List<Reminder>) {
        remindersContainer.removeAllViews()

        noRemindersText.visibility =
            if (reminders.isEmpty()) View.VISIBLE else View.GONE

        reminders.sortedWith(
            compareBy<Reminder>({ it.hour }, { it.minute })
        ).forEach { reminder ->

            val row = layoutInflater.inflate(
                R.layout.item_reminder,
                remindersContainer,
                false
            )

            row.findViewById<TextView>(R.id.reminderTime).text =
                formatTime(reminder.hour, reminder.minute)

            row.findViewById<TextView>(R.id.reminderName).text =
                "${reminder.medicationName} - every day"

            row.findViewById<Button>(R.id.deleteButton)
                .setOnClickListener {
                    deleteReminder(reminder)
                }

            remindersContainer.addView(row)
        }
    }

    private fun deleteReminder(reminder: Reminder) {
        activityScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.deleteReminder(reminder.id)
                }

                val body = response.body()

                if (!response.isSuccessful || body?.success != true) {
                    showError(body?.message ?: "Could not delete reminder.")
                    return@launch
                }

                Reminders.remove(this@ReminderActivity, reminder.id)

                Toast.makeText(
                    this@ReminderActivity,
                    "Reminder deleted.",
                    Toast.LENGTH_SHORT
                ).show()

                loadReminders()

            } catch (e: Exception) {
                showError("Could not delete reminder: ${e.message}")
            }
        }
    }

    private fun formatTime(hour: Int, minute: Int): String =
        String.format(Locale.US, "%02d:%02d", hour, minute)

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun checkPermissions() {
        when {
            !Reminders.notificationsAllowed(this) -> {
                if (
                    Build.VERSION.SDK_INT >= 33 &&
                    checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        100
                    )
                } else {
                    showNotificationsBlockedDialog()
                }
            }

            else -> Reminders.offerExactAlarmPermission(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

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
            .setMessage(
                "Your reminder is saved, but notifications are disabled."
            )
            .setPositiveButton("Open settings") { _, _ ->
                Reminders.openNotificationSettings(this)
            }
            .setNegativeButton("Not now", null)
            .show()
    }

    private fun sendTestNotification() {
        if (Reminders.notificationsAllowed(this)) {
            Reminders.showNotification(
                this,
                1,
                "Test medication"
            )
            Toast.makeText(
                this,
                "Test notification sent.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            checkPermissions()
        }
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }
}