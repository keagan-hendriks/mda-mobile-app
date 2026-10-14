package com.example.mda_mobile_app

import android.app.Activity


class GamificationManager(private val prefs: GamificationPrefs) {


    var enabled = true


    var onBadgeEarned: ((Badge) -> Unit)? = null


    fun onDoseLogged(activity: Activity, takenDates: List<String>) {
        if (!enabled) return

        activity.runOnUiThread { handle(activity, takenDates) }
    }

    private fun handle(activity: Activity, takenDates: List<String>) {

        if (activity.isFinishing || activity.isDestroyed) return

        val streak = StreakManager.calculateStreak(takenDates)
        val previousStreak = prefs.lastStreak
        val bestStreak = maxOf(prefs.bestStreak, streak)


        val alreadyEarned = prefs.earnedBadgeIds
        val newBadges = Badges.all.filter { it.streakRequired <= streak && it.id !in alreadyEarned }


        prefs.lastStreak = streak
        prefs.bestStreak = bestStreak

        when {
            newBadges.isNotEmpty() -> {
                prefs.earnedBadgeIds = alreadyEarned + newBadges.map { it.id }
                newBadges.forEach { onBadgeEarned?.invoke(it) }

                PopUps.showBadgeEarned(activity, newBadges.last(), streak)
            }
            streak < previousStreak -> PopUps.showWelcomeBack(activity, bestStreak)
            streak > previousStreak -> PopUps.showStreakToast(activity, streak)
        }
    }
}
