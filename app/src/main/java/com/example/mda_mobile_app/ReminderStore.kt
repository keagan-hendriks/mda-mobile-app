package com.example.mda_mobile_app

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ReminderStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        "reminders",
        Context.MODE_PRIVATE
    )

    fun getAll(): List<Reminder> {
        val result = mutableListOf<Reminder>()

        try {
            val array = JSONArray(prefs.getString("list", "[]") ?: "[]")

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val days = mutableSetOf<Int>()
                val dayArray = item.optJSONArray("days") ?: JSONArray()

                for (d in 0 until dayArray.length()) {
                    days.add(dayArray.getInt(d))
                }

                result.add(
                    Reminder(
                        id = item.getInt("id"),
                        medicationName = item.getString("name"),
                        hour = item.getInt("hour"),
                        minute = item.getInt("minute"),
                        days = days
                    )
                )
            }
        } catch (e: JSONException) {
            return emptyList()
        }

        return result
    }

    fun find(id: Int): Reminder? =
        getAll().find { it.id == id }

    fun put(reminder: Reminder) {
        saveAll(getAll().filterNot { it.id == reminder.id } + reminder)
    }

    fun remove(id: Int) {
        saveAll(getAll().filterNot { it.id == id })
    }

    fun clear() {
        saveAll(emptyList())
    }

    fun replaceAll(reminders: List<Reminder>) {
        saveAll(reminders)
    }

    private fun saveAll(reminders: List<Reminder>) {
        val array = JSONArray()

        reminders.forEach { reminder ->
            val item = JSONObject().apply {
                put("id", reminder.id)
                put("name", reminder.medicationName)
                put("hour", reminder.hour)
                put("minute", reminder.minute)
                put("days", JSONArray(reminder.days.toList()))
            }

            array.put(item)
        }

        prefs.edit()
            .putString("list", array.toString())
            .apply()
    }
}