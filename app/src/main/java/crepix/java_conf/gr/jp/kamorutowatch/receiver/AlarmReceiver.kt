package crepix.java_conf.gr.jp.kamorutowatch.receiver

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.support.v4.app.NotificationCompat
import com.google.gson.Gson
import crepix.java_conf.gr.jp.kamorutowatch.R
import crepix.java_conf.gr.jp.kamorutowatch.domain.AlarmItem
import crepix.java_conf.gr.jp.kamorutowatch.domain.NotificationService
import crepix.java_conf.gr.jp.kamorutowatch.utility.AlarmNotificationUtility
import crepix.java_conf.gr.jp.kamorutowatch.view.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val context = p0 ?: return
        val intent = p1 ?: return
        val gson = Gson()
        val item = gson.fromJson(intent.getStringExtra("alarmItem"), AlarmItem::class.java)
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
        val i = Intent(context.applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 2, i, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setSmallIcon(R.drawable.notification)
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
        builder.setContentText(context.getString(R.string.notification))
        builder.setContentIntent(pendingIntent)
        builder.setDefaults(Notification.DEFAULT_VIBRATE)
        builder.setLights(Color.WHITE, 2000, 1000)
        builder.setAutoCancel(true)
        val service = NotificationService(context)
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val r = RingtoneManager.getRingtone(context.applicationContext, notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && service.getIsAlarmAllTime()) {
            val attr = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            r.audioAttributes = attr
        }
        r.play()

        if (item.isRepeated) {
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            AlarmNotificationUtility.setTimer(item, manager, context.applicationContext)
        } else {
            item.isEnabled = false
            service.update(item)
            service.setShouldRefresh(true)
        }

        val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(item.id + 1000, builder.build())

        Handler().postDelayed({
            r.stop()
        }, 10000)
    }

    companion object {
        private const val NOTIFICATION_CHANNEL ="channel_ruto"
    }
}
