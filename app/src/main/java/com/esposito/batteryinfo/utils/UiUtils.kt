/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.utils

import android.content.res.Configuration
import android.os.Build
import android.view.Window
import android.view.WindowInsetsController
import com.esposito.batteryinfo.R

object UiUtils {

    fun adjustStatusbarIconColor(window: Window) {
        val light = (window.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                if (light) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
    }

    fun getBatteryIcon(level: Int, isCharging: Boolean): Int {
        return if (isCharging) {
            R.drawable.battery_charging
        } else when {
            level >= 95 -> R.drawable.battery_full
            level >= 70 -> R.drawable.battery_6
            level >= 50 -> R.drawable.battery_5
            level >= 30 -> R.drawable.battery_4
            level >= 20 -> R.drawable.battery_3
            level >= 10 -> R.drawable.battery_2
            level >= 5 -> R.drawable.battery_1
            else -> R.drawable.battery_0
        }
    }
}