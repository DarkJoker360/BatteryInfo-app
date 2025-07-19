/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.esposito.batteryinfo.R
import com.esposito.batteryinfo.ui.activities.MainActivity

object WidgetUtils {
    fun <T : AppWidgetProvider> updateAllWidgets(context: Context, widgetClass: Class<T>, update: (Context, AppWidgetManager, Int) -> Unit) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, widgetClass))
        appWidgetIds.forEach {
            update(context, appWidgetManager, it)
        }
    }

    fun enableClick(context: Context, views: RemoteViews) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)
        views.setOnClickPendingIntent(R.id.batteryIcon, pendingIntent)
    }
}
