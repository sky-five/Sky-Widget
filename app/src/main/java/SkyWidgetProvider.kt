package com.sky.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import android.app.PendingIntent
import com.example.skywidget.R

class SkyWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d("SkyWidgetProvider", "onReceive called with action: ${intent.action}")

        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            Log.d("SkyWidgetProvider", "Tap update received")

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, SkyWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            onUpdate(context, appWidgetManager, appWidgetIds)
        } else if (intent.action == Intent.ACTION_BATTERY_CHANGED ||
            intent.action == Intent.ACTION_POWER_CONNECTED ||
            intent.action == Intent.ACTION_POWER_DISCONNECTED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, SkyWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

            for (widgetId in widgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d("SkyWidgetProvider", "onUpdate called for widget IDs: ${appWidgetIds.joinToString()}")
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            Log.d("SkyWidgetProvider", "updateAppWidget called for widget ID: $appWidgetId")
            val batteryPct = getBatteryPercentage(context)

            val views = RemoteViews(context.packageName, R.layout.widget_sky).apply {
                setTextViewText(R.id.batteryPercentage, "$batteryPct%")

                val pandaIconRes = when {
                    batteryPct >= 80 -> R.drawable.panda_full
                    batteryPct >= 30 -> R.drawable.panda_med
                    else -> R.drawable.panda_low
                }
                setImageViewResource(R.id.pandaIcon, pandaIconRes)

                setProgressBar(R.id.batteryBar, 100, batteryPct, false)

                val intent = Intent(context, SkyWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                }

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, flags)

                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d("SkyWidgetProvider", "Battery percentage: $batteryPct%, panda icon and progress bar updated, tap refresh set.")
        }

        private fun getBatteryPercentage(context: Context): Int {
            Log.d("SkyWidgetProvider", "getBatteryPercentage called")
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, ifilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (scale > 0) (level * 100) / scale else -1
            Log.d("SkyWidgetProvider", "Battery level: $level, scale: $scale, calculated percentage: $batteryPct")
            return batteryPct
        }
    }
}
