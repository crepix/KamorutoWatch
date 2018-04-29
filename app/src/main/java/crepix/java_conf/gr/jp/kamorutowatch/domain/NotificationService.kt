package crepix.java_conf.gr.jp.kamorutowatch.domain

import android.content.Context
import com.google.gson.Gson


class NotificationService(private val context: Context) {
    val max = 5
    val pref = context.getSharedPreferences(NotificationPreference.name, Context.MODE_PRIVATE)

    fun getList(): List<AlarmItem> {
        val source = pref.getString(NotificationPreference.list, "")
        if (source.isEmpty()) {
            return listOf()
        }
        val gson = Gson()
        return gson.fromJson(source, List::class.java).map { gson.fromJson(it.toString(), AlarmItem::class.java) }
    }

    fun create(hour: Int, minute: Int): AlarmItem? {
        val list = getList()
        if (list.size >= max) {
            return null
        }
        val notification = AlarmItem()
        notification.id = list.sortedBy { it.id }.lastOrNull()?.id?.plus(1) ?: 0
        notification.hour = hour
        notification.minute = minute
        val gson = Gson()
        val editor = pref.edit()
        editor.putString(NotificationPreference.list, gson.toJson(list + notification))
        editor.apply()
        return notification
    }

    fun update(notification: AlarmItem) {
        val list = getList()
        list.find { it.id == notification.id }?.paste(notification)
        val editor = pref.edit()
        val gson = Gson()
        editor.putString(NotificationPreference.list, gson.toJson(list))
        editor.apply()
    }

    fun remove(id: Int) {
        val list = getList().filter { it.id != id }
        val editor = pref.edit()
        val gson = Gson()
        editor.putString(NotificationPreference.list, gson.toJson(list))
        editor.apply()
    }

    fun getIsAlarmAllTime(): Boolean {
        return pref.getBoolean(NotificationPreference.isAlarmAllTime, NotificationPreference.isAlarmAllTimeDefault)
    }

    fun setIsAlarmAllTime(isAllTime: Boolean) {
        val editor = pref.edit()
        editor.putBoolean(NotificationPreference.isAlarmAllTime, isAllTime)
        editor.apply()
    }

    fun getShouldRefresh(): Boolean {
        return pref.getBoolean(NotificationPreference.shouldRefresh, NotificationPreference.shouldRefreshDefault)
    }

    fun setShouldRefresh(should: Boolean) {
        val editor = pref.edit()
        editor.putBoolean(NotificationPreference.shouldRefresh, should)
        editor.apply()
    }
}
