package com.example.mda_mobile_app

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast


object PopUps {

    fun showBadgeEarned(activity: Activity, badge: Badge, streak: Int) {
        val builder = AlertDialog.Builder(activity)

        val view = LayoutInflater.from(builder.context).inflate(R.layout.dialog_badge_earned, null)

        view.findViewById<ImageView>(R.id.badgeIcon).setImageResource(badge.iconRes)
        view.findViewById<TextView>(R.id.badgeTitle).text = badge.title
        view.findViewById<TextView>(R.id.badgeDescription).text =
            "${badge.description}\nCurrent streak: $streak"

        val dialog = builder.setView(view).create()
        view.findViewById<Button>(R.id.dismissButton).setOnClickListener { dialog.dismiss() }
        dialog.show()


        val icon = view.findViewById<ImageView>(R.id.badgeIcon)
        icon.scaleX = 0f
        icon.scaleY = 0f
        icon.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    fun showWelcomeBack(activity: Activity, bestStreak: Int) {
        AlertDialog.Builder(activity)
            .setTitle("Welcome back")
            .setMessage(
                "Missing a day happens to everyone. Your best streak so far is $bestStreak days, " +
                    "and you've just started a new one."
            )
            .setPositiveButton("Let's go", null)
            .show()
    }

    fun showStreakToast(activity: Activity, streak: Int) {
        val days = if (streak == 1) "day" else "days"
        Toast.makeText(activity, "Dose logged - $streak $days in a row", Toast.LENGTH_SHORT).show()
    }
}
