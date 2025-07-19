/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.ui.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.esposito.batteryinfo.R
import com.esposito.batteryinfo.utils.BatteryUtils
import com.esposito.batteryinfo.utils.UiUtils
import com.esposito.batteryinfo.utils.WidgetUtils

class BatteryWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d(TAG, "Widget received intent: ${intent.action}")
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_BATTERY_LOW,
            Intent.ACTION_BATTERY_OKAY -> {
                Log.d(TAG, "Updating widget due to battery event")
                WidgetUtils.updateAllWidgets(context, BatteryWidget::class.java, ::updateAppWidget)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "Widget enabled, starting services")
        val alarmHandler = AlarmHandler(context)
        alarmHandler.setAlarmManager()
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        val alarmHandler = AlarmHandler(context)
        alarmHandler.cancelAlarmManager()
        Log.d(TAG, "All widgets removed!")
    }

    companion object {
        private const val TAG = "BatteryWidget"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val batteryInfo = BatteryUtils.getBatteryInfo(context)
            val views = RemoteViews(context.packageName, R.layout.battery_widget).apply {
                if (batteryInfo != null) {
                    setImageViewResource(R.id.batteryIcon, UiUtils.getBatteryIcon(batteryInfo.batteryLevel, batteryInfo.isCharging))
                    setTextViewText(R.id.appwidget_text, "${batteryInfo.batteryLevel}%")
                } else {
                    setImageViewResource(R.id.batteryIcon, R.drawable.battery_0)
                    setTextViewText(R.id.appwidget_text, "??%")
                }
            }
            WidgetUtils.enableClick(context, views)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}