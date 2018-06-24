package crepix.java_conf.gr.jp.kamorutowatch.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import crepix.java_conf.gr.jp.kamorutowatch.R
import crepix.java_conf.gr.jp.kamorutowatch.databinding.ActivityMainBinding
import crepix.java_conf.gr.jp.kamorutowatch.domain.AlarmItem
import crepix.java_conf.gr.jp.kamorutowatch.domain.NotificationService
import crepix.java_conf.gr.jp.kamorutowatch.service.AlarmService
import crepix.java_conf.gr.jp.kamorutowatch.utility.AlarmNotificationUtility
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                if (item.isEnabled) {
                    AlarmNotificationUtility.setTimer(item, manager, this@MainActivity)
                } else {
                    removeTimer(item.id)
                }
            }

            override fun onItemDeleted(id: Int) {
                service.remove(id)
                removeTimer(id)
                binding.add.visibility = View.VISIBLE
            }

            override fun onAlarmChanged(isAllTime: Boolean) {
                service.setIsAlarmAllTime(isAllTime)
            }
        })
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        if (list.size >= service.max) {
            binding.add.visibility = View.GONE
        }
        binding.add.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dialog = TimePickerDialog(this, { _, hour, minute ->
                val item = service.create(hour, minute)
                item?.let {
                    adapter.add(it)
                    AlarmNotificationUtility.setTimer(item, manager, this@MainActivity)
                    if (service.getList().size >= service.max) {
                        binding.add.visibility = View.GONE
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
        val intent = PendingIntent.getService(
                this,
                id + 1000,
                Intent(this, AlarmService::class.java),
                0)
        manager.cancel(intent)
    }
}
