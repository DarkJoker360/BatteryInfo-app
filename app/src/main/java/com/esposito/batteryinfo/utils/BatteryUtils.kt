/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import com.esposito.batteryinfo.R
import com.esposito.batteryinfo.ui.models.BatteryInfo
import java.util.Locale
import kotlin.math.abs

object BatteryUtils {

    private const val TAG = "BatteryUtils"

    fun getBatteryInfo(context: Context): BatteryInfo? {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter) ?: return null
        val batteryInfo = BatteryInfo()
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = ((level / scale.toFloat()) * 100f).toInt()
        batteryInfo.batteryLevel = batteryPct

        val health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
        batteryInfo.batteryHealth = health

        val temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        batteryInfo.batteryTemperature = temperature
        batteryInfo.isOverheating = temperature > 450

        val voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        batteryInfo.batteryVoltage = voltage

        val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
        batteryInfo.isCharging = isCharging
        batteryInfo.batteryStatus = getBatteryStatusString(context, status)

        val plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        batteryInfo.chargingSource = getChargingSourceString(context, plugged)

        val technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        batteryInfo.batteryTechnology = technology ?: context.getString(R.string.unknown)

        val currentCapacity = getBatteryCurrentCapacity(context)
        val designCapacity = getBatteryDesignCapacity(context)
        batteryInfo.batteryCurrentCapacity = currentCapacity
        batteryInfo.batteryDesignCapacity = designCapacity

        if (currentCapacity > 0 && designCapacity > 0) {
            val healthPercentage = ((currentCapacity / designCapacity.toFloat()) * 100f).toInt()
            batteryInfo.batteryHealthPercentage = healthPercentage
            batteryInfo.isHealthy = healthPercentage >= 80
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val cycles = batteryStatus.getIntExtra("android.os.extra.CYCLE_COUNT", -1)
            batteryInfo.cycleCount = cycles
        }

        val chargingTimeRemaining = getChargingTimeRemaining(context)
        batteryInfo.chargingTimeRemaining = chargingTimeRemaining

        if (isCharging) {
            val chargingSpeed = calculateChargingSpeed(context)
            batteryInfo.chargingSpeed = chargingSpeed
        }

        val isPresent = batteryStatus.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)
        batteryInfo.isPresent = isPresent

        val estimatedTimeRemaining = calculateEstimatedTimeRemaining(batteryPct, isCharging)
        batteryInfo.estimatedTimeRemaining = estimatedTimeRemaining

        return batteryInfo
    }

    fun getBatteryCurrentCapacity(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return try {
            val chargeCounter = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val capacity = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            if (chargeCounter > 0 && capacity > 0) {
                (((chargeCounter / capacity.toFloat()) * 100f) / 1000).toInt()
            } else {
                -1
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current battery capacity: ${e.message}")
            -1
        }
    }

    @SuppressLint("PrivateApi")
    fun getBatteryDesignCapacity(context: Context): Int {
        return try {
            val powerProfile = Class.forName("com.android.internal.os.PowerProfile")
                .getConstructor(Context::class.java).newInstance(context)
            val batteryCapacity = Class.forName("com.android.internal.os.PowerProfile")
                .getMethod("getAveragePower", String::class.java)
                .invoke(powerProfile, "battery.capacity") as Double
            batteryCapacity.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get battery design capacity: ${e.message}")
            -1
        }
    }

    fun getChargingTimeRemaining(context: Context): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            try {
                batteryManager.computeChargeTimeRemaining()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get charging time remaining: ${e.message}")
                -1
            }
        } else {
            -1
        }
    }

    private fun getBatteryStatusString(context: Context, status: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.battery_status_charging)
            BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.battery_status_discharging)
            BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.battery_status_full)
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.battery_status_not_charging)
            BatteryManager.BATTERY_STATUS_UNKNOWN -> context.getString(R.string.unknown)
            else -> context.getString(R.string.unknown)
        }
    }

    private fun getChargingSourceString(context: Context, plugged: Int): String {
        return when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.charging_source_ac)
            BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.charging_source_usb)
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.charging_source_wireless)
            BatteryManager.BATTERY_PLUGGED_DOCK -> context.getString(R.string.charging_source_dock)
            else -> context.getString(R.string.unknown)
        }
    }

    private fun calculateChargingSpeed(context: Context): Float {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return try {
            val currentNow = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            abs(currentNow) / 1000f
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate charging speed: ${e.message}")
            0f
        }
    }

    private fun calculateEstimatedTimeRemaining(batteryLevel: Int, isCharging: Boolean): Long {
        return if (isCharging) {
            val remainingPercentage = 100 - batteryLevel
            (remainingPercentage * 60 * 1000).toLong()
        } else {
            (batteryLevel * 120 * 1000).toLong()
        }
    }

    fun formatTemperature(temperature: Int): String {
        val tempCelsius = temperature / 10f
        val locale = Locale.getDefault()
        return if (locale.country == "US" || locale.country == "LR" || locale.country == "MM") {
            val tempFahrenheit = (tempCelsius * 9 / 5) + 32
            String.Companion.format(Locale.US, "%.1f°F", tempFahrenheit)
        } else {
            String.format(locale, "%.1f°C", tempCelsius)
        }
    }

    fun formatVoltage(voltage: Int): String {
        return String.Companion.format(Locale.US,"%.2fV", voltage / 1000f)
    }

    fun formatCapacity(capacity: Int): String {
        return "$capacity mAh"
    }

    fun formatDuration(milliseconds: Long): String {
        if (milliseconds < 0) return ""

        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> String.Companion.format(Locale.US,"%dd %02dh %02dm", days, hours % 24, minutes % 60)
            hours > 0 -> String.Companion.format(Locale.US,"%dh %02dm", hours, minutes % 60)
            minutes > 0 -> String.Companion.format(Locale.US,"%dm %02ds", minutes, seconds % 60)
            else -> String.Companion.format(Locale.US,"%ds", seconds)
        }
    }
}