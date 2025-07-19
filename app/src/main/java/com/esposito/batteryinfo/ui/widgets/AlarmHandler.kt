/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.ui.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.esposito.batteryinfo.receivers.WidgetReceiver

class AlarmHandler(private val context: Context) {

    fun setAlarmManager() {
        val intent = Intent(context, WidgetReceiver::class.java)
        val sender = PendingIntent.getBroadcast(
            context, 2, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val triggerTime = System.currentTimeMillis() + 30_000L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am?.canScheduleExactAlarms()!!) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, sender)
            } else {
                am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, 60000, sender)
            }
        } else {
            am?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, sender)
        }
    }

    fun cancelAlarmManager() {
        val intent = Intent(context, WidgetReceiver::class.java)
        val sender = PendingIntent.getBroadcast(
            context, 2, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        am?.cancel(sender)
    }
}