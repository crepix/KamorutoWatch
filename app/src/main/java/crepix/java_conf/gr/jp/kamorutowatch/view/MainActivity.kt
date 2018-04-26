package crepix.java_conf.gr.jp.kamorutowatch.view

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.google.gson.Gson
import crepix.java_conf.gr.jp.kamorutowatch.R
import crepix.java_conf.gr.jp.kamorutowatch.databinding.ActivityMainBinding
import crepix.java_conf.gr.jp.kamorutowatch.domain.AlarmItem
import crepix.java_conf.gr.jp.kamorutowatch.domain.NotificationService
import crepix.java_conf.gr.jp.kamorutowatch.service.AlarmService
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val service = NotificationService(this)
        val list = service.getList()
        val adapter = AlarmAdapter(list, object: AlarmAdapter.Listener {
            override fun onStatusChanged(item: AlarmItem) {
                service.update(item)
                if (item.isEnabled) {
                    setTimer(item)
                }
            }

            override fun onSwitchChanged(item: AlarmItem) {
                service.update(item)
                if (item.isEnabled) {
                    setTimer(item)
                } else {
                    removeTimer(item.id)
                }
            }

            override fun onItemDeleted(id: Int) {
                service.remove(id)
                removeTimer(id)
                binding.add.visibility = View.VISIBLE
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
                    setTimer(it)
                    if (service.getList().size >= service.max) {
                        binding.add.visibility = View.GONE
                    }
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            dialog.show()
        }
        this.supportActionBar?.hide()
    }

    private fun setTimer(item: AlarmItem) {
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
        val millis = System.currentTimeMillis() + (minute * 60 * 1000 + hour * 60 * 60 * 1000).toLong()

        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i =  Intent(this, AlarmService::class.java)
        val gson = Gson()
        i.putExtra("alarmItem", gson.toJson(item))
        val intent = PendingIntent.getService(this, item.id + 1000, i, 0)
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
