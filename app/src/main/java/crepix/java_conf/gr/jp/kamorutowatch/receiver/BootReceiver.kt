package crepix.java_conf.gr.jp.kamorutowatch.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import crepix.java_conf.gr.jp.kamorutowatch.domain.NotificationService
import crepix.java_conf.gr.jp.kamorutowatch.utility.AlarmNotificationUtility

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == p1?.action && p0 != null) {
            val service = NotificationService(p0)
            val list = service.getList()
            list.forEach {
                if (it.isEnabled) {
                    val manager = p0.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    AlarmNotificationUtility.setTimer(it, manager, p0)
                }
            }
        }
    }
}
