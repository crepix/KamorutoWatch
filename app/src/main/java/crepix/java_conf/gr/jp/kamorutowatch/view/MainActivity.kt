package crepix.java_conf.gr.jp.kamorutowatch.view

import android.app.*
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import crepix.java_conf.gr.jp.kamorutowatch.R
import crepix.java_conf.gr.jp.kamorutowatch.databinding.ActivityMainBinding
import crepix.java_conf.gr.jp.kamorutowatch.domain.AlarmItem
import crepix.java_conf.gr.jp.kamorutowatch.domain.NotificationService
import crepix.java_conf.gr.jp.kamorutowatch.utility.AlarmNotificationUtility
import java.util.*
import android.os.Build
import crepix.java_conf.gr.jp.kamorutowatch.receiver.AlarmReceiver


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                    "channel_ruto",
                    getString(R.string.channel_setting_name),
                    NotificationManager.IMPORTANCE_HIGH
            )

            channel.lightColor = Color.WHITE
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val service = NotificationService(this)
        val list = service.getList()
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        adapter = AlarmAdapter(list, service.getIsAlarmAllTime(), object: AlarmAdapter.Listener {
            override fun onStatusChanged(item: AlarmItem) {
                service.update(item)
                if (item.isEnabled) {
                    removeTimer(item.id)
                    AlarmNotificationUtility.setTimer(item, manager, this@MainActivity)
                }
            }

            override fun onSwitchChanged(item: AlarmItem) {
                service.update(item)
                removeTimer(item.id)
                if (item.isEnabled) {
                    AlarmNotificationUtility.setTimer(item, manager, this@MainActivity)
                }
            }

            override fun onItemDeleted(id: Int) {
                service.remove(id)
                removeTimer(id)
                binding.add.show()
            }

            override fun onAlarmChanged(isAllTime: Boolean) {
                service.setIsAlarmAllTime(isAllTime)
            }
        })
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        if (list.size >= service.max) {
            binding.add.hide()
        }
        binding.add.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dialog = TimePickerDialog(this, { _, hour, minute ->
                val item = service.create(hour, minute)
                item?.let { i ->
                    adapter.add(i)
                    AlarmNotificationUtility.setTimer(item, manager, this@MainActivity)
                    if (service.getList().size >= service.max) {
                        binding.add.hide()
                    }
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            dialog.show()
        }
        this.supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()
        val service = NotificationService(this)
        if (service.getShouldRefresh()) {
            adapter.refresh(service.getList())
            service.setShouldRefresh(false)
        }
    }

    private fun removeTimer(id: Int) {
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = PendingIntent.getBroadcast(
                this,
                id + 1000,
                Intent(this, AlarmReceiver::class.java),
                0)
        manager.cancel(intent)
    }
}
