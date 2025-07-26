package SkyWidgetProvider.kt

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.skywidget.R  // Adjust import to match your app name

class SkyWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_sky)

            // Sample battery level (we'll update dynamically later)
            // val batteryLevel = 42

            // ✅ Get battery info
            val batteryIntent = context.registerReceiver(
                null,
                android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
            )

            val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1

            val batteryLevel = if (level >= 0 && scale > 0) {
                (level * 100) / scale
            } else {
                0
            }


            // ✅ Update UI
            views.setTextViewText(R.id.batteryPercentage, "$batteryLevel%")

            // Set correct panda face
            val pandaIconRes = when {
                batteryLevel >= 80 -> R.drawable.panda_full
                batteryLevel >= 30 -> R.drawable.panda_medium
                else -> R.drawable.panda_low
            }
            views.setImageViewResource(R.id.pandaIcon, pandaIconRes)

            // Update progress bar
            views.setProgressBar(R.id.batteryBar, 100, batteryLevel, false)

            // Push update to widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
