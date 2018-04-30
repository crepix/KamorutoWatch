package crepix.java_conf.gr.jp.kamorutowatch.utility

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.gson.Gson
import crepix.java_conf.gr.jp.kamorutowatch.domain.AlarmItem
import crepix.java_conf.gr.jp.kamorutowatch.service.AlarmService
import java.util.*

class AlarmNotificationUtility {
    companion object {
        fun setTimer(item: AlarmItem, manager: AlarmManager, context: Context, nextDay: Int = 0) {
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            val hour = if (item.hour >= calendar.get(Calendar.HOUR_OF_DAY)) {
                item.hour - calendar.get(Calendar.HOUR_OF_DAY)
            } else {
                24 + item.hour - calendar.get(Calendar.HOUR_OF_DAY)
            }
            val minute = if (item.minute >= calendar.get(Calendar.MINUTE)) {
                item.minute - calendar.get(Calendar.MINUTE)
            } else {
                60 + item.minute - calendar.get(Calendar.MINUTE)
            }
            val millis = System.currentTimeMillis() + (minute * 60 * 1000 + (hour + nextDay * 24) * 60 * 60 * 1000).toLong()

            val i = Intent(context, AlarmService::class.java)
            val gson = Gson()
            i.putExtra("alarmItem", gson.toJson(item))
            val intent = PendingIntent.getService(context, item.id + 1000, i, PendingIntent.FLAG_CANCEL_CURRENT)
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    setClockL(millis, manager, intent)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                    setClockM(millis, manager, intent)
                }
                else -> {
                    setClock(millis, manager, intent)
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private fun setClockL(millis: Long, manager: AlarmManager, intent: PendingIntent) {
            manager.setAlarmClock(AlarmManager.AlarmClockInfo(millis, null), intent)
        }

        @TargetApi(Build.VERSION_CODES.M)
        private fun setClockM(millis: Long, manager: AlarmManager, intent: PendingIntent) {
            manager.setExact(AlarmManager.RTC_WAKEUP, millis, intent)
        }

        private fun setClock(millis: Long, manager: AlarmManager, intent: PendingIntent) {
            manager.set(AlarmManager.RTC_WAKEUP, millis, intent)
        }
    }
}