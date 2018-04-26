package crepix.java_conf.gr.jp.kamorutowatch.service

import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import com.google.gson.Gson
import crepix.java_conf.gr.jp.kamorutowatch.R
import crepix.java_conf.gr.jp.kamorutowatch.domain.AlarmItem
import crepix.java_conf.gr.jp.kamorutowatch.domain.NotificationService
import crepix.java_conf.gr.jp.kamorutowatch.view.MainActivity




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
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setLights(Color.WHITE, 2000, 1000)
        builder.setAutoCancel(true)
        val service = NotificationService(this)
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