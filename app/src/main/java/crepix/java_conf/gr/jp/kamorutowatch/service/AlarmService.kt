package crepix.java_conf.gr.jp.kamorutowatch.service

import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.gson.Gson
import crepix.java_conf.gr.jp.kamorutowatch.R
import crepix.java_conf.gr.jp.kamorutowatch.domain.AlarmItem
import crepix.java_conf.gr.jp.kamorutowatch.domain.NotificationService
import crepix.java_conf.gr.jp.kamorutowatch.view.MainActivity
import android.media.RingtoneManager
import android.media.Ringtone
import android.net.Uri
import android.os.Build


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

        // TODO リピートがオンの場合は通知を再セットする
        if (item.isRepeated) {

        } else {
            item.isEnabled = false
            service.update(item)
        }

        val manager = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(item.id + 1000, builder.build())
    }
}