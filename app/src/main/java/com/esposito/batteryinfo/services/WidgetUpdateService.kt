/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.services

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.esposito.batteryinfo.receivers.WidgetReceiver
import com.esposito.batteryinfo.ui.widgets.BatteryWidget
import com.esposito.batteryinfo.ui.widgets.EnhancedBatteryWidget

class WidgetUpdateService : Service() {
    private val TAG = "WidgetUpdateService"

    private var widgetReceiver: WidgetReceiver? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WidgetUpdateService created")
        widgetReceiver = WidgetReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
        }

        registerReceiver(widgetReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "WidgetUpdateService destroyed")
        unregisterReceiver(widgetReceiver)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WidgetUpdateService started")
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val batteryWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(this, BatteryWidget::class.java)
        )
        val enhancedWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(this, EnhancedBatteryWidget::class.java)
        )
        
        if (batteryWidgetIds.isEmpty() && enhancedWidgetIds.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
