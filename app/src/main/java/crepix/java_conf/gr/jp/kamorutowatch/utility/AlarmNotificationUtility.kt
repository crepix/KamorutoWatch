package crepix.java_conf.gr.jp.kamorutowatch.utility

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.gson.Gson
import crepix.java_conf.gr.jp.kamorutowatch.domain.AlarmItem
import crepix.java_conf.gr.jp.kamorutowatch.receiver.AlarmReceiver
import java.util.*

class AlarmNotificationUtility {
    companion object {
        fun setTimer(item: AlarmItem, manager: AlarmManager, context: Context) {
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            val hour = if (
                    item.hour > calendar.get(Calendar.HOUR_OF_DAY) ||
                    item.hour == calendar.get(Calendar.HOUR_OF_DAY) && item.minute > calendar.get(Calendar.MINUTE)) {
                item.hour - calendar.get(Calendar.HOUR_OF_DAY)
            } else {
                24 + item.hour - calendar.get(Calendar.HOUR_OF_DAY)
            }
            val minute = item.minute - calendar.get(Calendar.MINUTE)
            val millis = System.currentTimeMillis() + (minute * 60 * 1000 + hour * 60 * 60 * 1000).toLong()

            var counter = 0
            if (item.isRepeated) {
                val nextCalendar = Calendar.getInstance(TimeZone.getDefault())
                nextCalendar.timeInMillis = millis
                val week = nextCalendar.get(Calendar.DAY_OF_WEEK)
                loop@ while (counter != 7) {
                    val w = (week - 1 + counter) % 7 + 1
                    if (
                            (w == Calendar.SUNDAY && item.notifySunday) ||
                            (w == Calendar.MONDAY && item.notifyMonday) ||
                            (w == Calendar.TUESDAY && item.notifyTuesday) ||
                            (w == Calendar.WEDNESDAY && item.notifyWednesday) ||
                            (w == Calendar.THURSDAY && item.notifyThursday) ||
                            (w == Calendar.FRIDAY && item.notifyFriday) ||
                            (w == Calendar.SATURDAY && item.notifySaturday)) {
                        break@loop
                    }
                    counter++
                }
                // もし一週間全てがセット不可能な場合はアラームは解除する
                if (counter == 7) {
                    val intent = PendingIntent.getService(
                            context,
                            item.id + 1000,
                            Intent(context, AlarmReceiver::class.java),
                            0)
                    manager.cancel(intent)
                    return
                }
            }

            val i = Intent(context, AlarmReceiver::class.java)
            val gson = Gson()
            i.putExtra("alarmItem", gson.toJson(item))

            val intent = PendingIntent.getBroadcast(context, item.id + 1000, i, PendingIntent.FLAG_CANCEL_CURRENT)
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    setClockM(millis + counter * 24 * 60 * 60 * 1000, manager, intent)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                    setClockL(millis + counter * 24 * 60 * 60 * 1000, manager, intent)
                }
                else -> {
                    setClock(millis + counter * 24 * 60 * 60 * 1000, manager, intent)
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        private fun setClockM(millis: Long, manager: AlarmManager, intent: PendingIntent) {
            manager.setAlarmClock(AlarmManager.AlarmClockInfo(millis, intent), intent)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private fun setClockL(millis: Long, manager: AlarmManager, intent: PendingIntent) {
            manager.setExact(AlarmManager.RTC_WAKEUP, millis, intent)
        }

        private fun setClock(millis: Long, manager: AlarmManager, intent: PendingIntent) {
            manager.set(AlarmManager.RTC_WAKEUP, millis, intent)
        }
    }
}