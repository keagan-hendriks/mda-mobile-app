package com.example.mda_mobile_app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar


class ProgressActivity : Activity() {

    companion object {
        private const val DEMO_MODE = true
        private const val TEST_REMINDER_ID = 2
    }

    private lateinit var prefs: GamificationPrefs
    private lateinit var manager: GamificationManager
    private lateinit var streakText: TextView
    private lateinit var bestStreakText: TextView
    private lateinit var badgesContainer: LinearLayout

    // Only for the demo
    private var demoStreak = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)


        prefs = GamificationPrefs(this, if (DEMO_MODE) "demo" else "default")
        manager = GamificationManager(prefs)

        streakText = findViewById(R.id.streakCountText)
        bestStreakText = findViewById(R.id.bestStreakText)
        badgesContainer = findViewById(R.id.badgesContainer)

        // Notification set-up (channel + the "Allow notifications?" question).
        Reminders.createChannel(this)
        Reminders.askForNotificationPermission(this)

        val demoPanel = findViewById<View>(R.id.demoPanel)
        if (DEMO_MODE) {
            demoPanel.visibility = View.VISIBLE
            setUpDemoButtons()
        } else {
            demoPanel.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        refreshScreen()
    }


    private fun refreshScreen() {
        val takenDates = if (DEMO_MODE) demoDates() else loadTakenDates()
        val streak = StreakManager.calculateStreak(takenDates)

        streakText.text = streak.toString()
        bestStreakText.text = "Best streak: ${maxOf(prefs.bestStreak, streak)} days"

        // One row per badge. Not-yet-earned badges are faded out.
        val earned = prefs.earnedBadgeIds
        badgesContainer.removeAllViews()
        for (badge in Badges.all) {
            val row = layoutInflater.inflate(R.layout.item_badge, badgesContainer, false)
            row.findViewById<ImageView>(R.id.badgeIcon).setImageResource(badge.iconRes)
            row.findViewById<TextView>(R.id.badgeTitle).text = badge.title
            row.findViewById<TextView>(R.id.badgeDescription).text = badge.description
            row.alpha = if (badge.id in earned) 1f else 0.3f
            badgesContainer.addView(row)
        }
    }


    private fun loadTakenDates(): List<String> = emptyList()


    // DEMO CODE


    private fun demoDates(): List<String> =
        (0 until demoStreak).map { StreakManager.dateString(daysAgo = it) }

    private fun setUpDemoButtons() {

        findViewById<View>(R.id.demoLogButton).setOnClickListener {
            demoStreak++
            manager.onDoseLogged(this, demoDates())
            refreshScreen()
        }


        findViewById<View>(R.id.demoMissButton).setOnClickListener {
            demoStreak = 1
            manager.onDoseLogged(this, demoDates())
            refreshScreen()
        }

        findViewById<View>(R.id.demoNotifyButton).setOnClickListener {
            if (Reminders.notificationsAllowed(this)) {
                Reminders.showNotification(this, 1, "Test medication")
                toast("Sent! Swipe down from the top of the screen to see it.")
            } else {
                toast("Notifications are blocked. Allow them in Settings > Apps > this app > Notifications.")
            }
        }

        findViewById<View>(R.id.demoRemindButton).setOnClickListener { remindInOneMinute() }

        findViewById<View>(R.id.demoResetButton).setOnClickListener {
            demoStreak = 0
            prefs.clear()
            Reminders.cancel(this, TEST_REMINDER_ID)
            refreshScreen()
            toast("Demo reset")
        }
    }

    private fun remindInOneMinute() {

        if (!Reminders.canScheduleExact(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
                )
            }
            toast("Switch on 'Allow', go back, then tap this button again.")
            return
        }
        val soon = Calendar.getInstance().apply { add(Calendar.MINUTE, 1) }
        Reminders.scheduleDaily(
            this, TEST_REMINDER_ID, "Take medication",
            soon.get(Calendar.HOUR_OF_DAY), soon.get(Calendar.MINUTE)
        )
        toast("Reminder set. Press the Home button and wait about a minute.")
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
