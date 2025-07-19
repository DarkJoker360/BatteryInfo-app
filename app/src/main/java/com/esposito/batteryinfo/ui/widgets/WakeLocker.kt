/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.ui.widgets

import android.content.Context
import android.os.PowerManager
import android.util.Log

object WakeLocker {

    private var wakeLock: PowerManager.WakeLock? = null

    fun acquire(context: Context) {
        release()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BatteryWidgets::WakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(2000)
        }
        Log.d("WakeLocker", "Wake lock acquired for 2 seconds.")
    }

    fun release() {
        wakeLock?.release()
        wakeLock = null
    }
}
