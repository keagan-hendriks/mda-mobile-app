package com.example.mda_mobile_app

import android.content.Context


class GamificationPrefs(context: Context, userId: String) {

    private val prefs = context.applicationContext
        .getSharedPreferences("gamification_$userId", Context.MODE_PRIVATE)

    var lastStreak: Int
        get() = prefs.getInt("last_streak", 0)
        set(value) { prefs.edit().putInt("last_streak", value).apply() }

    var bestStreak: Int
        get() = prefs.getInt("best_streak", 0)
        set(value) { prefs.edit().putInt("best_streak", value).apply() }

    var earnedBadgeIds: Set<String>
        get() = prefs.getStringSet("earned_badges", emptySet()) ?: emptySet()
        set(value) { prefs.edit().putStringSet("earned_badges", value).apply() }

    /** Wipes everything (used by the demo's Reset button). */
    fun clear() {
        prefs.edit().clear().apply()
    }
}
