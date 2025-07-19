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
import com.esposito.batteryinfo.utils.BatteryUtils
import com.esposito.batteryinfo.R
import com.esposito.batteryinfo.utils.UiUtils
import com.esposito.batteryinfo.utils.WidgetUtils

class EnhancedBatteryWidget : AppWidgetProvider() {

    private val TAG = "EnhancedBatteryWidget"

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_BATTERY_LOW,
            Intent.ACTION_BATTERY_OKAY -> {
                WidgetUtils.updateAllWidgets(context, EnhancedBatteryWidget::class.java, ::updateAppWidget)
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
        Log.d(TAG, "All widgets removed!")
        val alarmHandler = AlarmHandler(context)
        alarmHandler.cancelAlarmManager()
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val batteryInfo = BatteryUtils.getBatteryInfo(context)
            val views = RemoteViews(context.packageName, R.layout.enhanced_battery_widget).apply {
                if (batteryInfo != null) {
                    setTextViewText(R.id.txtWidgetBatteryLevel, "${batteryInfo.batteryLevel}%")
                    setProgressBar(R.id.progressWidgetBattery, 100, batteryInfo.batteryLevel, false)
                    setTextViewText(R.id.txtWidgetBatteryStatus, batteryInfo.batteryStatus)
                    setTextViewText(R.id.txtWidgetTemperature, BatteryUtils.formatTemperature(batteryInfo.batteryTemperature))
                    setTextViewText(R.id.txtWidgetHealth, "${batteryInfo.batteryHealthPercentage}%")
                    setImageViewResource(R.id.imgWidgetBatteryIcon, UiUtils.getBatteryIcon(batteryInfo.batteryLevel, batteryInfo.isCharging))
                } else {
                    setTextViewText(R.id.txtWidgetBatteryLevel, "?%")
                    setTextViewText(R.id.txtWidgetBatteryStatus, "Unknown")
                    setTextViewText(R.id.txtWidgetTemperature, "?°C")
                    setTextViewText(R.id.txtWidgetHealth, "?%")
                    setImageViewResource(R.id.imgWidgetBatteryIcon, R.drawable.battery_0)
                }
            }
            WidgetUtils.enableClick(context, views)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
