package com.example.mda_mobile_app

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class ReminderStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("reminders", Context.MODE_PRIVATE)

    fun getAll(): List<Reminder> {
        val result = mutableListOf<Reminder>()
        try {
            val array = JSONArray(prefs.getString("list", "[]") ?: "[]")
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val days = mutableSetOf<Int>()
                val dayArray = o.getJSONArray("days")
                for (d in 0 until dayArray.length()) days.add(dayArray.getInt(d))
                result.add(
                    Reminder(o.getInt("id"), o.getString("name"), o.getInt("hour"), o.getInt("minute"), days)
                )
            }
        } catch (e: JSONException) {

            return emptyList()
        }
        return result
    }

    fun find(id: Int): Reminder? = getAll().find { it.id == id }

    
    fun put(reminder: Reminder) = saveAll(getAll().filter { it.id != reminder.id } + reminder)

    fun remove(id: Int) = saveAll(getAll().filter { it.id != id })

    fun clear() = saveAll(emptyList())

    private fun saveAll(list: List<Reminder>) {
        val array = JSONArray()
        for (r in list) {
            val o = JSONObject()
            o.put("id", r.id)
            o.put("name", r.medicationName)
            o.put("hour", r.hour)
            o.put("minute", r.minute)
            o.put("days", JSONArray(r.days.toList()))
            array.put(o)
        }
        prefs.edit().putString("list", array.toString()).apply()
    }
}
