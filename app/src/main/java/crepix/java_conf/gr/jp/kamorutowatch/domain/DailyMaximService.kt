package crepix.java_conf.gr.jp.kamorutowatch.domain

import android.content.Context
import crepix.java_conf.gr.jp.kamorutowatch.R
import java.util.*

class DailyMaximService(private val context: Context) {

    fun getDailyMaxim(currentRuto: Int): String {
        val pref = context.getSharedPreferences(DailyMaximPreference.name, Context.MODE_PRIVATE)
        val lastTime = pref.getInt(DailyMaximPreference.lastTime, DailyMaximPreference.lastTimeDefault)
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        if (calendar.get(Calendar.DAY_OF_YEAR) != lastTime) {
            val rand = Random(System.currentTimeMillis())
            val num = rand.nextInt(items.size)
            val editor = pref.edit()
            editor.putInt(DailyMaximPreference.lastTime, calendar.get(Calendar.DAY_OF_YEAR))
            editor.putInt(DailyMaximPreference.current, num)
            editor.apply()
        }
        return when(currentRuto) {
            3 -> context.getString(sleepyItems[pref.getInt(DailyMaximPreference.current, DailyMaximPreference.currentDefault) % 10])
            2 -> context.getString(sleepItems[pref.getInt(DailyMaximPreference.current, DailyMaximPreference.currentDefault) % 10])
            else -> context.getString(items[pref.getInt(DailyMaximPreference.current, DailyMaximPreference.currentDefault)])
        }
    }

    private val items = arrayListOf(
            R.string.maxim_0,
            R.string.maxim_1,
            R.string.maxim_2,
            R.string.maxim_3,
            R.string.maxim_4,
            R.string.maxim_5,
            R.string.maxim_6,
            R.string.maxim_7,
            R.string.maxim_8,
            R.string.maxim_9,
            R.string.maxim_10,
            R.string.maxim_11,
            R.string.maxim_12,
            R.string.maxim_13,
            R.string.maxim_14,
            R.string.maxim_15,
            R.string.maxim_16,
            R.string.maxim_17,
            R.string.maxim_18,
            R.string.maxim_19,
            R.string.maxim_20,
            R.string.maxim_21,
            R.string.maxim_22,
            R.string.maxim_23,
            R.string.maxim_24,
            R.string.maxim_25,
            R.string.maxim_26,
            R.string.maxim_27,
            R.string.maxim_28,
            R.string.maxim_29)

    private val sleepyItems = arrayListOf(
            R.string.sleepy_0,
            R.string.sleepy_1,
            R.string.sleepy_2,
            R.string.sleepy_3,
            R.string.sleepy_4,
            R.string.sleepy_5,
            R.string.sleepy_6,
            R.string.sleepy_7,
            R.string.sleepy_8,
            R.string.sleepy_9)

    private val sleepItems = arrayListOf(
            R.string.sleep_0,
            R.string.sleep_1,
            R.string.sleep_2,
            R.string.sleep_3,
            R.string.sleep_4,
            R.string.sleep_5,
            R.string.sleep_6,
            R.string.sleep_7,
            R.string.sleep_8,
            R.string.sleep_9)
}
