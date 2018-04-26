package crepix.java_conf.gr.jp.kamorutowatch.domain

import android.content.Context
import crepix.java_conf.gr.jp.kamorutowatch.R
import java.util.*

class DailyMaximService(private val context: Context) {

    fun getDailyMaxim(): String {
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
        return context.getString(items[pref.getInt(DailyMaximPreference.current, DailyMaximPreference.currentDefault)])
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
            R.string.maxim_10)
}
