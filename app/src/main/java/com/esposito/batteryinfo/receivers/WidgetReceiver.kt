/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.receivers

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.esposito.batteryinfo.ui.widgets.BatteryWidget
import com.esposito.batteryinfo.ui.widgets.EnhancedBatteryWidget
import com.esposito.batteryinfo.ui.widgets.WakeLocker

class WidgetReceiver : BroadcastReceiver() {
    private  val TAG = "WidgetReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "System event received, starting widget update service")
                val serviceIntent = Intent(context, com.esposito.batteryinfo.services.WidgetUpdateService::class.java)
                context.startService(serviceIntent)
            }
        }
        
        WakeLocker.acquire(context)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetPairs = listOf(
            Triple(BatteryWidget::class.java, "basic", BatteryWidget::updateAppWidget),
            Triple(EnhancedBatteryWidget::class.java, "enhanced", EnhancedBatteryWidget::updateAppWidget)
        )

        widgetPairs.forEach { (widgetClass, label, updateFn) ->
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, widgetClass))
            if (ids.isNotEmpty()) {
                Log.d(TAG, "Updating ${ids.size} $label battery widgets")
                ids.forEach { updateFn(context, appWidgetManager, it) }
            }
        }

        WakeLocker.release()
    }
}