package com.example.mda_mobile_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


  // MEDICATION REMINDERS AND NOTIFICATIONS

object Reminders {

    private const val CHANNEL_ID = "medication_reminders"

    // Extra information attached to the notification, so the app can open the right screen.
    const val EXTRA_ID = "reminder_id"
    const val EXTRA_NAME = "medication_name"
    const val EXTRA_OPENED_FROM_REMINDER = "opened_from_reminder"


    // 1. PERMISSIONS AND SET-UP


    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Medication reminders", NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Reminders to take your medication"
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }


    fun askForNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 33 &&
            activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
    }


    fun notificationsAllowed(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()


    fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }


    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }


    fun offerExactAlarmPermission(activity: Activity) {
        if (canScheduleExact(activity)) return
        AlertDialog.Builder(activity)
            .setTitle("Allow exact reminders?")
            .setMessage("To remind you at exactly the right time, switch on 'Alarms & reminders' for this app on the next screen.")
            .setPositiveButton("Open settings") { _, _ -> openExactAlarmSettings(activity) }
            .setNegativeButton("Not now", null)
            .show()
    }


    // 2. THE FUNCTIONS THE REST OF THE APP CALLS



    fun save(context: Context, reminder: Reminder) {
        ReminderStore(context).put(reminder)
        schedule(context, reminder)
    }


    fun remove(context: Context, reminderId: Int) {
        cancelAlarm(context, reminderId)
        ReminderStore(context).remove(reminderId)
    }


    fun replaceAll(context: Context, reminders: List<Reminder>) {
        clearAll(context)
        reminders.forEach { save(context, it) }
    }


    fun clearAll(context: Context) {
        val store = ReminderStore(context)
        store.getAll().forEach { cancelAlarm(context, it.id) }
        store.clear()
    }


    fun rescheduleAll(context: Context) {
        ReminderStore(context).getAll().forEach { schedule(context, it) }
    }

    // Older names, kept so the demo screen keeps working.
    fun scheduleDaily(context: Context, reminderId: Int, medicationName: String, hour: Int, minute: Int) =
        save(context, Reminder(reminderId, medicationName, hour, minute))

    fun cancel(context: Context, reminderId: Int) = remove(context, reminderId)

    // 3. SCHEDULING (the alarm)


    //Asks Android to wake the app at this reminder's NEXT time.
    fun schedule(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextTime = ReminderTime.nextTrigger(reminder.hour, reminder.minute, reminder.days).timeInMillis
        val pendingIntent = buildPendingIntent(context, reminder.id)

        try {
            if (canScheduleExact(context)) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent)
            } else {

                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent)
            }
        } catch (e: SecurityException) {

            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context, reminderId))
    }


    private fun buildPendingIntent(context: Context, reminderId: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_ID, reminderId)
        return PendingIntent.getBroadcast(
            context, reminderId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // 4. THE NOTIFICATION


    @SuppressLint("MissingPermission") //  check that notifications are allowed on the first line
    fun showNotification(context: Context, reminderId: Int, medicationName: String) {
        if (!notificationsAllowed(context)) return
        createChannel(context)

        // PRIVACY: on a locked screen only show a generic message, never the medication name.
        val lockScreenVersion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reminder")
            .setContentText("You have a reminder")
            .build()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for your medication")
            .setContentText("Whenever you're ready: $medicationName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(lockScreenVersion)
            .setAutoCancel(true)

        // Tapping the notification opens the app, carrying which reminder it was.
        // The app's start screen can read these extras and jump to the "log a dose" screen.
        val openApp = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (openApp != null) {
            openApp.putExtra(EXTRA_OPENED_FROM_REMINDER, true)
            openApp.putExtra(EXTRA_ID, reminderId)
            openApp.putExtra(EXTRA_NAME, medicationName)
            builder.setContentIntent(
                PendingIntent.getActivity(
                    context, reminderId, openApp,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

        NotificationManagerCompat.from(context).notify(reminderId, builder.build())
    }
}


class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(Reminders.EXTRA_ID, -1)

        // Look the reminder up fresh, so edits are respected. If it was deleted, do nothing.
        val reminder = ReminderStore(context).find(id) ?: return

        Reminders.showNotification(context, reminder.id, reminder.medicationName)

        // Set the alarm for the NEXT time, so it keeps ringing every day.
        Reminders.schedule(context, reminder)
    }
}
