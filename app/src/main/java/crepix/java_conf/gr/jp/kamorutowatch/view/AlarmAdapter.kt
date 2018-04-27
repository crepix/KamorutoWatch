package crepix.java_conf.gr.jp.kamorutowatch.view

import android.app.TimePickerDialog
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import crepix.java_conf.gr.jp.kamorutowatch.R
import crepix.java_conf.gr.jp.kamorutowatch.databinding.AlarmItemBinding
import crepix.java_conf.gr.jp.kamorutowatch.databinding.CountItemBinding
import crepix.java_conf.gr.jp.kamorutowatch.databinding.EmptyItemBinding
import crepix.java_conf.gr.jp.kamorutowatch.domain.AlarmItem


class AlarmAdapter(list: List<AlarmItem>, private val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var alarms = list

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when(holder) {
            is AlarmViewHolder -> {
                val binding = holder.binding ?: return
                val item = alarms[position]

                setTimeTwo(binding.timeMinuteTen, binding.timeMinuteOne, item.minute)
                setTimeTwo(binding.timeHourTen, binding.timeHourOne, item.hour)

                if (item.isRepeated) {
                    binding.repeatCheck.isChecked = true
                    showWeek(binding, item)
                } else {
                    binding.repeatCheck.isChecked = false
                    hideWeek(binding)
                }
                val onClickListener = View.OnClickListener {
                    val dialog = TimePickerDialog(binding.root.context, { _, hour, minute ->
                        item.hour = hour
                        item.minute = minute
                        setTimeTwo(binding.timeMinuteTen, binding.timeMinuteOne, item.minute)
                        setTimeTwo(binding.timeHourTen, binding.timeHourOne, item.hour)
                        listener.onStatusChanged(item)
                    }, item.hour, item.minute, true)
                    dialog.show()
                }



                binding.timeMinuteOne.setOnClickListener(onClickListener)
                binding.timeMinuteTen.setOnClickListener(onClickListener)
                binding.timeHourOne.setOnClickListener(onClickListener)
                binding.timeHourTen.setOnClickListener(onClickListener)
                binding.timeColon.setOnClickListener(onClickListener)
                binding.alarmSwitch.isChecked = item.isEnabled
                binding.alarmSwitch.setOnCheckedChangeListener { _, checked ->
                    item.isEnabled = checked
                    listener.onSwitchChanged(item)
                }
                binding.repeatCheck.setOnCheckedChangeListener { _, checked ->
                    item.isRepeated = checked
                    if (checked) {
                        showWeek(binding, item)
                        binding.weekSunday.visibility = View.VISIBLE
                        binding.weekMonday.visibility = View.VISIBLE
                        binding.weekTuesday.visibility = View.VISIBLE
                        binding.weekWednesday.visibility = View.VISIBLE
                        binding.weekThursday.visibility = View.VISIBLE
                        binding.weekFriday.visibility = View.VISIBLE
                        binding.weekSaturday.visibility = View.VISIBLE
                    } else {
                        binding.weekSunday.visibility = View.GONE
                        binding.weekMonday.visibility = View.GONE
                        binding.weekTuesday.visibility = View.GONE
                        binding.weekWednesday.visibility = View.GONE
                        binding.weekThursday.visibility = View.GONE
                        binding.weekFriday.visibility = View.GONE
                        binding.weekSaturday.visibility = View.GONE
                    }
                }
                binding.delete.setOnClickListener {
                    remove(item.id)
                    listener.onItemDeleted(item.id)
                }
                binding.weekSunday.setOnClickListener {
                    item.notifySunday = !item.notifySunday
                    setAlpha(binding.weekSunday, item.notifySunday)
                    listener.onStatusChanged(item)
                }
                binding.weekMonday.setOnClickListener {
                    item.notifyMonday = !item.notifyMonday
                    setAlpha(binding.weekMonday, item.notifyMonday)
                    listener.onStatusChanged(item)
                }
                binding.weekTuesday.setOnClickListener {
                    item.notifyTuesday = !item.notifyTuesday
                    setAlpha(binding.weekTuesday, item.notifyTuesday)
                    listener.onStatusChanged(item)
                }
                binding.weekWednesday.setOnClickListener {
                    item.notifyWednesday = !item.notifyWednesday
                    setAlpha(binding.weekWednesday, item.notifyWednesday)
                    listener.onStatusChanged(item)
                }
                binding.weekThursday.setOnClickListener {
                    item.notifyThursday = !item.notifyThursday
                    setAlpha(binding.weekThursday, item.notifyThursday)
                    listener.onStatusChanged(item)
                }
                binding.weekFriday.setOnClickListener {
                    item.notifyFriday = !item.notifyFriday
                    setAlpha(binding.weekFriday, item.notifyFriday)
                    listener.onStatusChanged(item)
                }
                binding.weekSaturday.setOnClickListener {
                    item.notifySaturday = !item.notifySaturday
                    setAlpha(binding.weekSaturday, item.notifySaturday)
                    listener.onStatusChanged(item)
                }
            }
            is CountViewHolder -> {
                val binding = holder.binding ?: return
                binding.rest.text = binding.root.context.getString(R.string.rest, alarms.size)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (alarms.isEmpty()) {
            2
        } else {
            alarms.size + 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        val context = parent?.context ?: return null
        return when(viewType) {
            ViewType.Alarm.value -> AlarmViewHolder(LayoutInflater.from(context).inflate(R.layout.alarm_item, parent, false))
            ViewType.Empty.value -> EmptyViewHolder(LayoutInflater.from(context).inflate(R.layout.empty_item, parent, false))
            ViewType.Count.value -> CountViewHolder(LayoutInflater.from(context).inflate(R.layout.count_item, parent, false))
            else -> EmptyViewHolder(LayoutInflater.from(context).inflate(R.layout.empty_item, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (alarms.isEmpty()) {
            when (position) {
                0 -> ViewType.Empty.value
                else -> ViewType.Count.value
            }
        } else {
            when {
                alarms.size <= position -> ViewType.Count.value
                else -> ViewType.Alarm.value
            }
        }
    }

    fun add(item: AlarmItem) {
        if (alarms.isEmpty()) {
            alarms += item
            notifyItemRangeChanged(0, 2)
        } else {
            alarms += item
            notifyItemInserted(alarms.size - 1)
            notifyItemChanged(alarms.size)
        }
    }

    private fun remove(id: Int) {
        val item = alarms.find { id == it.id } ?: return
        val position = alarms.indexOf(item)
        notifyItemRemoved(position)
        alarms -= item
        notifyItemChanged(alarms.size)
    }

    private fun setTimeTwo(ten: ImageView, one: ImageView, time: Int) {
        setTime(ten, time / 10)
        setTime(one, time % 10)
    }

    private fun setTime(view: ImageView, time: Int) {
        when(time) {
            0 -> view.setImageResource(R.drawable.time_0)
            1 -> view.setImageResource(R.drawable.time_1)
            2 -> view.setImageResource(R.drawable.time_2)
            3 -> view.setImageResource(R.drawable.time_3)
            4 -> view.setImageResource(R.drawable.time_4)
            5 -> view.setImageResource(R.drawable.time_5)
            6 -> view.setImageResource(R.drawable.time_6)
            7 -> view.setImageResource(R.drawable.time_7)
            8 -> view.setImageResource(R.drawable.time_8)
            9 -> view.setImageResource(R.drawable.time_9)
        }
    }

    private fun showWeek(binding: AlarmItemBinding, item: AlarmItem) {
        binding.weekSunday.visibility = View.VISIBLE
        binding.weekMonday.visibility = View.VISIBLE
        binding.weekTuesday.visibility = View.VISIBLE
        binding.weekWednesday.visibility = View.VISIBLE
        binding.weekThursday.visibility = View.VISIBLE
        binding.weekFriday.visibility = View.VISIBLE
        binding.weekSaturday.visibility = View.VISIBLE
        setAlpha(binding.weekSunday, item.notifySunday)
        setAlpha(binding.weekMonday, item.notifyMonday)
        setAlpha(binding.weekTuesday, item.notifyTuesday)
        setAlpha(binding.weekWednesday, item.notifyWednesday)
        setAlpha(binding.weekThursday, item.notifyThursday)
        setAlpha(binding.weekFriday, item.notifyFriday)
        setAlpha(binding.weekSaturday, item.notifySaturday)
    }

    private fun setAlpha(imageView: ImageView, notify: Boolean) {
        if (notify) {
            imageView.alpha = 1f
        } else {
            imageView.alpha = 0.3f
        }
    }

    private fun hideWeek(binding: AlarmItemBinding) {
        binding.weekSunday.visibility = View.GONE
        binding.weekMonday.visibility = View.GONE
        binding.weekTuesday.visibility = View.GONE
        binding.weekWednesday.visibility = View.GONE
        binding.weekThursday.visibility = View.GONE
        binding.weekFriday.visibility = View.GONE
        binding.weekSaturday.visibility = View.GONE
    }

    enum class ViewType(val value: Int) {
        Alarm(0), Empty(1), Count(2)
    }

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<AlarmItemBinding>(itemView)
    }

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<EmptyItemBinding>(itemView)
    }

    class CountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<CountItemBinding>(itemView)
    }

    interface Listener {
        fun onStatusChanged(item: AlarmItem)
        fun onSwitchChanged(item: AlarmItem)
        fun onItemDeleted(id: Int)
    }
}
