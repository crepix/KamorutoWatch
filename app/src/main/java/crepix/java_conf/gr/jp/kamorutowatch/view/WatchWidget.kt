package crepix.java_conf.gr.jp.kamorutowatch.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.os.SystemClock
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
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val pref = context.getSharedPreferences(DailyMaximPreference.name, Context.MODE_PRIVATE)
            val isShowing = pref.getBoolean(DailyMaximPreference.isShowing, DailyMaximPreference.isShowingDefault)
            updateAppWidget(context, appWidgetManager, appWidgetId, isShowing)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        val action = intent?.action
        // かもるとが押された
        if (action == buttonFilter) {
            context?.let {
                val pref = context.getSharedPreferences(DailyMaximPreference.name, Context.MODE_PRIVATE)
                val isShowing = pref.getBoolean(DailyMaximPreference.isShowing, DailyMaximPreference.isShowingDefault)
                updateAll(it, AppWidgetManager.getInstance(context), !isShowing)
                val editor = pref.edit()
                editor.putBoolean(DailyMaximPreference.isShowing, !isShowing)
                editor.apply()
            }
        }
        if (action == clockFilter) {
            context?.let {
                val pref = context.getSharedPreferences(DailyMaximPreference.name, Context.MODE_PRIVATE)
                val isShowing = pref.getBoolean(DailyMaximPreference.isShowing, DailyMaximPreference.isShowingDefault)
                updateAll(it, AppWidgetManager.getInstance(context), isShowing)
            }
        }
    }

    override fun onEnabled(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(clockFilter),
                PendingIntent.FLAG_UPDATE_CURRENT)
        manager.setRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 60 * 1000,
                60 * 1000,
                pendingIntent)
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(clockFilter),
                PendingIntent.FLAG_UPDATE_CURRENT)
        manager.cancel(pendingIntent)
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        private const val buttonFilter = "crepix.java_conf.gr.jp.kamorutowatch.button"
        private const val clockFilter = "crepix.java_conf.gr.jp.kamorutowatch.clock"

        internal fun updateAll(context: Context, appWidgetManager: AppWidgetManager, isTalkShowing: Boolean) {
            val views = RemoteViews(context.packageName, R.layout.watch_widget)
            views.setOnClickPendingIntent(R.id.kamoruto, PendingIntent.getBroadcast(
                    context,
                    1,
                    Intent(buttonFilter),
                    PendingIntent.FLAG_UPDATE_CURRENT))

            val calendar = Calendar.getInstance(TimeZone.getDefault())
            if (isTalkShowing) {
                showTalk(views, context)
            } else {
                resetDate(views, calendar)
            }
            changeRuto(views, calendar)
            appWidgetManager.updateAppWidget(ComponentName(context, WatchWidget::class.java), views)
        }

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int, isTalkShowing: Boolean) {

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.watch_widget)

            views.setOnClickPendingIntent(R.id.kamoruto, PendingIntent.getBroadcast(
                    context,
                    1,
                    Intent(buttonFilter),
                    PendingIntent.FLAG_UPDATE_CURRENT))

            val calendar = Calendar.getInstance(TimeZone.getDefault())
            if (isTalkShowing) {
                showTalk(views, context)
            } else {
                resetDate(views, calendar)
            }
            changeRuto(views, calendar)
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun changeRuto(views: RemoteViews, calendar: Calendar) {
            when (calendar.get(Calendar.HOUR_OF_DAY)) {
                15 -> views.setImageViewResource(R.id.kamoruto, R.drawable.ruto_selector_1)
                else -> views.setImageViewResource(R.id.kamoruto, R.drawable.ruto_selector_0)
            }
        }

        private fun showTalk(views: RemoteViews, context: Context) {
            views.setViewVisibility(R.id.talk, View.VISIBLE)
            views.setViewVisibility(R.id.talk_text, View.VISIBLE)
            val service = DailyMaximService(context)
            views.setTextViewText(R.id.talk_text, service.getDailyMaxim())
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
            when (calendar.get(Calendar.MONTH % 10)) {
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
            when (calendar.get(Calendar.MONTH) / 10) {
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

