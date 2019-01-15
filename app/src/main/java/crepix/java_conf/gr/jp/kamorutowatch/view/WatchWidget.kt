package crepix.java_conf.gr.jp.kamorutowatch.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.view.View
import android.widget.RemoteViews

import crepix.java_conf.gr.jp.kamorutowatch.R
import crepix.java_conf.gr.jp.kamorutowatch.domain.DailyMaximPreference
import crepix.java_conf.gr.jp.kamorutowatch.domain.DailyMaximService
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class WatchWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        resetTimerIfNeed(context)
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val pref = context.getSharedPreferences(DailyMaximPreference.name, Context.MODE_PRIVATE)
            updateAppWidget(context, appWidgetManager, appWidgetId, pref)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val action = intent?.action
        // るとが押された
        if (action == buttonFilter) {
            context?.let {
                resetTimerIfNeed(context)
                val pref = context.getSharedPreferences(DailyMaximPreference.name, Context.MODE_PRIVATE)
                val editor = pref.edit()
                editor.putBoolean(
                        DailyMaximPreference.isShowing,
                        !pref.getBoolean(DailyMaximPreference.isShowing, DailyMaximPreference.isShowingDefault))
                editor.apply()
                updateAll(it, AppWidgetManager.getInstance(context), pref)
            }
        }
        if (action == clockFilter) {
            context?.let {
                val pref = context.getSharedPreferences(DailyMaximPreference.name, Context.MODE_PRIVATE)
                updateAll(it, AppWidgetManager.getInstance(context), pref)
            }
        }
    }

    override fun onEnabled(context: Context) {
        resetTimerIfNeed(context)
        // Enter relevant functionality for when the first widget is created
    }

    private fun resetTimerIfNeed(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WatchWidget::class.java)
        intent.action = clockFilter
        // 作成されていない場合は作成する
        if (PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE) == null) {
            val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            // 極力更新間隔を実際の分が進む時間に合わせる
            // setRepeatingはムラがあるので実際には無意味だが……
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            val initialDelay = 60100 - calendar.get(Calendar.SECOND) * 1000
            manager.setRepeating(
                    AlarmManager.RTC,
                    System.currentTimeMillis() + initialDelay,
                    60 * 1000,
                    pendingIntent)
        }
    }

    override fun onDisabled(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WatchWidget::class.java)
        intent.action = clockFilter
        val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        manager.cancel(pendingIntent)
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        private const val buttonFilter = "crepix.java_conf.gr.jp.kamorutowatch.button"
        private const val clockFilter = "crepix.java_conf.gr.jp.kamorutowatch.clock"

        internal fun updateAll(
                context: Context,
                appWidgetManager: AppWidgetManager,
                preferences: SharedPreferences) {
            val isShowing = preferences.getBoolean(DailyMaximPreference.isShowing, DailyMaximPreference.isShowingDefault)
            val views = RemoteViews(context.packageName, R.layout.watch_widget)
            val intent = Intent(context, WatchWidget::class.java)
            intent.action = buttonFilter
            val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.kamoruto, pendingIntent)
            views.setOnClickPendingIntent(R.id.kamoruto_eat, pendingIntent)
            views.setOnClickPendingIntent(R.id.kamoruto_sleep, pendingIntent)
            views.setOnClickPendingIntent(R.id.kamoruto_sleepy, pendingIntent)

            val activityIntent = PendingIntent.getActivity(
                    context,
                    10,
                    Intent(context, MainActivity::class.java),
                    0)
            views.setOnClickPendingIntent(R.id.widget_layout, activityIntent)

            val calendar = Calendar.getInstance(TimeZone.getDefault())
            val currentRuto = changeRuto(views, calendar)
            if (isShowing) {
                showTalk(views, context, currentRuto)
            } else {
                resetDate(views, calendar)
            }
            appWidgetManager.updateAppWidget(ComponentName(context, WatchWidget::class.java), views)
        }

        internal fun updateAppWidget(
                context: Context,
                appWidgetManager: AppWidgetManager,
                appWidgetId: Int,
                preferences: SharedPreferences) {
            // Construct the RemoteViews object
            val isShowing = preferences.getBoolean(DailyMaximPreference.isShowing, DailyMaximPreference.isShowingDefault)
            val views = RemoteViews(context.packageName, R.layout.watch_widget)
            val intent = Intent(context, WatchWidget::class.java)
            intent.action = buttonFilter
            val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.kamoruto, pendingIntent)
            views.setOnClickPendingIntent(R.id.kamoruto_eat, pendingIntent)
            views.setOnClickPendingIntent(R.id.kamoruto_sleep, pendingIntent)
            views.setOnClickPendingIntent(R.id.kamoruto_sleepy, pendingIntent)

            val activityIntent = PendingIntent.getActivity(
                    context,
                    10,
                    Intent(context, MainActivity::class.java),
                    0)
            views.setOnClickPendingIntent(R.id.widget_layout, activityIntent)

            val calendar = Calendar.getInstance(TimeZone.getDefault())
            val currentRuto = changeRuto(views, calendar)
            if (isShowing) {
                showTalk(views, context, currentRuto)
            } else {
                resetDate(views, calendar)
            }
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun changeRuto(views: RemoteViews, calendar: Calendar): Int {
            return when (calendar.get(Calendar.HOUR_OF_DAY)) {
                23 -> {
                    views.setViewVisibility(R.id.kamoruto, View.INVISIBLE)
                    views.setViewVisibility(R.id.kamoruto_eat, View.INVISIBLE)
                    views.setViewVisibility(R.id.kamoruto_sleep, View.INVISIBLE)
                    views.setViewVisibility(R.id.kamoruto_sleepy, View.VISIBLE)
                    3
                }
                in 0..6 -> {
                    views.setViewVisibility(R.id.kamoruto, View.INVISIBLE)
                    views.setViewVisibility(R.id.kamoruto_eat, View.INVISIBLE)
                    views.setViewVisibility(R.id.kamoruto_sleep, View.VISIBLE)
                    views.setViewVisibility(R.id.kamoruto_sleepy, View.INVISIBLE)
                    2
                }
                15 -> {
                    views.setViewVisibility(R.id.kamoruto, View.INVISIBLE)
                    views.setViewVisibility(R.id.kamoruto_eat, View.VISIBLE)
                    views.setViewVisibility(R.id.kamoruto_sleep, View.INVISIBLE)
                    views.setViewVisibility(R.id.kamoruto_sleepy, View.INVISIBLE)
                    1
                }
                else -> {
                    views.setViewVisibility(R.id.kamoruto, View.VISIBLE)
                    views.setViewVisibility(R.id.kamoruto_eat, View.INVISIBLE)
                    views.setViewVisibility(R.id.kamoruto_sleep, View.INVISIBLE)
                    views.setViewVisibility(R.id.kamoruto_sleepy, View.INVISIBLE)
                    0
                }
            }
        }

        private fun showTalk(views: RemoteViews, context: Context, currentRuto: Int) {
            views.setViewVisibility(R.id.talk, View.VISIBLE)
            views.setViewVisibility(R.id.talk_text, View.VISIBLE)
            val service = DailyMaximService(context)
            views.setTextViewText(R.id.talk_text, service.getDailyMaxim(currentRuto))
        }

        private fun resetDate(views: RemoteViews, calendar: Calendar) {
            views.setViewVisibility(R.id.talk, View.INVISIBLE)
            views.setViewVisibility(R.id.talk_text, View.INVISIBLE)
            when (calendar.get(Calendar.DAY_OF_MONTH) % 10) {
                0 -> views.setImageViewResource(R.id.day_one, R.drawable.time_0)
                1 -> views.setImageViewResource(R.id.day_one, R.drawable.time_1)
                2 -> views.setImageViewResource(R.id.day_one, R.drawable.time_2)
                3 -> views.setImageViewResource(R.id.day_one, R.drawable.time_3)
                4 -> views.setImageViewResource(R.id.day_one, R.drawable.time_4)
                5 -> views.setImageViewResource(R.id.day_one, R.drawable.time_5)
                6 -> views.setImageViewResource(R.id.day_one, R.drawable.time_6)
                7 -> views.setImageViewResource(R.id.day_one, R.drawable.time_7)
                8 -> views.setImageViewResource(R.id.day_one, R.drawable.time_8)
                9 -> views.setImageViewResource(R.id.day_one, R.drawable.time_9)
            }
            views.setViewVisibility(R.id.day_ten, View.VISIBLE)
            views.setViewVisibility(R.id.time_hour_ten, View.VISIBLE)
            when (calendar.get(Calendar.DAY_OF_MONTH) / 10) {
                0 -> views.setViewVisibility(R.id.day_ten, View.GONE)
                1 -> views.setImageViewResource(R.id.day_ten, R.drawable.time_1)
                2 -> views.setImageViewResource(R.id.day_ten, R.drawable.time_2)
                3 -> views.setImageViewResource(R.id.day_ten, R.drawable.time_3)
            }
            when (calendar.get(Calendar.DAY_OF_WEEK)) {
                1 -> views.setImageViewResource(R.id.week, R.drawable.week_sunday)
                2 -> views.setImageViewResource(R.id.week, R.drawable.week_monday)
                3 -> views.setImageViewResource(R.id.week, R.drawable.week_tuesday)
                4 -> views.setImageViewResource(R.id.week, R.drawable.week_wednesday)
                5 -> views.setImageViewResource(R.id.week, R.drawable.week_thursday)
                6 -> views.setImageViewResource(R.id.week, R.drawable.week_friday)
                7 -> views.setImageViewResource(R.id.week, R.drawable.week_saturday)
            }
            val month = calendar.get(Calendar.MONTH)
            when (month % 10) {
                0 -> views.setImageViewResource(R.id.month_one, R.drawable.time_1)
                1 -> views.setImageViewResource(R.id.month_one, R.drawable.time_2)
                2 -> views.setImageViewResource(R.id.month_one, R.drawable.time_3)
                3 -> views.setImageViewResource(R.id.month_one, R.drawable.time_4)
                4 -> views.setImageViewResource(R.id.month_one, R.drawable.time_5)
                5 -> views.setImageViewResource(R.id.month_one, R.drawable.time_6)
                6 -> views.setImageViewResource(R.id.month_one, R.drawable.time_7)
                7 -> views.setImageViewResource(R.id.month_one, R.drawable.time_8)
                8 -> views.setImageViewResource(R.id.month_one, R.drawable.time_9)
                9 -> views.setImageViewResource(R.id.month_one, R.drawable.time_0)
            }
            when (month / 9) {
                0 -> views.setViewVisibility(R.id.month_ten, View.INVISIBLE)
                1 -> views.setViewVisibility(R.id.month_ten, View.VISIBLE)
            }

            when (calendar.get(Calendar.MINUTE) % 10) {
                0 -> views.setImageViewResource(R.id.time_minute_one, R.drawable.time_0)
                1 -> views.setImageViewResource(R.id.time_minute_one, R.drawable.time_1)
                2 -> views.setImageViewResource(R.id.time_minute_one, R.drawable.time_2)
                3 -> views.setImageViewResource(R.id.time_minute_one, R.drawable.time_3)
                4 -> views.setImageViewResource(R.id.time_minute_one, R.drawable.time_4)
                5 -> views.setImageViewResource(R.id.time_minute_one, R.drawable.time_5)
                6 -> views.setImageViewResource(R.id.time_minute_one, R.drawable.time_6)
                7 -> views.setImageViewResource(R.id.time_minute_one, R.drawable.time_7)
                8 -> views.setImageViewResource(R.id.time_minute_one, R.drawable.time_8)
                9 -> views.setImageViewResource(R.id.time_minute_one, R.drawable.time_9)
            }
            when (calendar.get(Calendar.MINUTE) / 10) {
                0 -> views.setImageViewResource(R.id.time_minute_ten, R.drawable.time_0)
                1 -> views.setImageViewResource(R.id.time_minute_ten, R.drawable.time_1)
                2 -> views.setImageViewResource(R.id.time_minute_ten, R.drawable.time_2)
                3 -> views.setImageViewResource(R.id.time_minute_ten, R.drawable.time_3)
                4 -> views.setImageViewResource(R.id.time_minute_ten, R.drawable.time_4)
                5 -> views.setImageViewResource(R.id.time_minute_ten, R.drawable.time_5)
            }

            when (calendar.get(Calendar.HOUR_OF_DAY) % 10) {
                0 -> views.setImageViewResource(R.id.time_hour_one, R.drawable.time_0)
                1 -> views.setImageViewResource(R.id.time_hour_one, R.drawable.time_1)
                2 -> views.setImageViewResource(R.id.time_hour_one, R.drawable.time_2)
                3 -> views.setImageViewResource(R.id.time_hour_one, R.drawable.time_3)
                4 -> views.setImageViewResource(R.id.time_hour_one, R.drawable.time_4)
                5 -> views.setImageViewResource(R.id.time_hour_one, R.drawable.time_5)
                6 -> views.setImageViewResource(R.id.time_hour_one, R.drawable.time_6)
                7 -> views.setImageViewResource(R.id.time_hour_one, R.drawable.time_7)
                8 -> views.setImageViewResource(R.id.time_hour_one, R.drawable.time_8)
                9 -> views.setImageViewResource(R.id.time_hour_one, R.drawable.time_9)
            }
            when (calendar.get(Calendar.HOUR_OF_DAY) / 10) {
                0 -> views.setViewVisibility(R.id.time_hour_ten, View.INVISIBLE)
                1 -> views.setImageViewResource(R.id.time_hour_ten, R.drawable.time_1)
                2 -> views.setImageViewResource(R.id.time_hour_ten, R.drawable.time_2)
            }
        }
    }
}

