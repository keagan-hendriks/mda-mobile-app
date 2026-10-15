package com.example.mda_mobile_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar


object Reminders {

    private const val CHANNEL_ID = "medication_reminders"


    const val EXTRA_ID = "reminder_id"
    const val EXTRA_NAME = "medication_name"
    const val EXTRA_HOUR = "hour"
    const val EXTRA_MINUTE = "minute"


    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Medication reminders", NotificationManager.IMPORTANCE_HIGH
            )
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


    @SuppressLint("MissingPermission")
    fun showNotification(context: Context, reminderId: Int, medicationName: String) {
        if (!notificationsAllowed(context)) return
        createChannel(context)


        val lockScreenVersion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reminder")
            .setContentText("You have a reminder")
            .build()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for your medication")
            .setContentText("Don't forget: $medicationName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(lockScreenVersion)
            .setAutoCancel(true)


        val openApp = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (openApp != null) {
            builder.setContentIntent(
                PendingIntent.getActivity(
                    context, reminderId, openApp,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

        NotificationManagerCompat.from(context).notify(reminderId, builder.build())
    }


    fun scheduleDaily(context: Context, reminderId: Int, medicationName: String, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        val pendingIntent = buildPendingIntent(context, reminderId, medicationName, hour, minute)

        if (canScheduleExact(context)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendingIntent)
        } else {

            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendingIntent)
        }
    }


    fun cancel(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context, reminderId, "", 0, 0))
    }


    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    private fun buildPendingIntent(
        context: Context, reminderId: Int, medicationName: String, hour: Int, minute: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_ID, reminderId)
            putExtra(EXTRA_NAME, medicationName)
            putExtra(EXTRA_HOUR, hour)
            putExtra(EXTRA_MINUTE, minute)
        }
        return PendingIntent.getBroadcast(
            context, reminderId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(Reminders.EXTRA_ID, 0)
        val name = intent.getStringExtra(Reminders.EXTRA_NAME) ?: "your medication"
        val hour = intent.getIntExtra(Reminders.EXTRA_HOUR, -1)
        val minute = intent.getIntExtra(Reminders.EXTRA_MINUTE, -1)

        Reminders.showNotification(context, id, name)


        if (hour >= 0 && minute >= 0) {
            Reminders.scheduleDaily(context, id, name, hour, minute)
        }
    }
}
