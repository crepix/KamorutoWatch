package crepix.java_conf.gr.jp.kamorutowatch.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.support.v4.app.NotificationCompat
import com.google.gson.Gson
import crepix.java_conf.gr.jp.kamorutowatch.R
import crepix.java_conf.gr.jp.kamorutowatch.domain.AlarmItem
import crepix.java_conf.gr.jp.kamorutowatch.domain.NotificationService
import crepix.java_conf.gr.jp.kamorutowatch.view.MainActivity
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import crepix.java_conf.gr.jp.kamorutowatch.utility.AlarmNotificationUtility
import java.util.*


class AlarmService : IntentService("AlarmService") {
    override fun onHandleIntent(p0: Intent?) {
        val intent = p0 ?: return
        val gson = Gson()
        val item = gson.fromJson(intent.getStringExtra("alarmItem"), AlarmItem::class.java)
        val builder = NotificationCompat.Builder(this, "alarm")
        val i = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 2, i, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setSmallIcon(R.drawable.notification)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        builder.setContentText(getString(R.string.notification))
        builder.setContentIntent(pendingIntent)
        builder.setDefaults(Notification.DEFAULT_VIBRATE)
        builder.setLights(Color.WHITE, 2000, 1000)
        builder.setAutoCancel(true)
        val service = NotificationService(this)
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val r = RingtoneManager.getRingtone(applicationContext, notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && service.getIsAlarmAllTime()) {
            val attr = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            r.audioAttributes = attr
        }
        r.play()

        if (item.isRepeated) {
            val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            AlarmNotificationUtility.setTimer(item, manager, applicationContext)
        } else {
            item.isEnabled = false
            service.update(item)
            service.setShouldRefresh(true)
        }

        val manager = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(item.id + 1000, builder.build())
    }
}
