/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.ui.models

data class BatteryInfo(
    var batteryLevel: Int = 0,
    var batteryHealth: Int = 0,
    var batteryHealthPercentage: Int = 0,
    var batteryTemperature: Int = 0,
    var batteryVoltage: Int = 0,
    var batteryCurrentCapacity: Int = 0,
    var batteryDesignCapacity: Int = 0,
    var batteryStatus: String = "",
    var chargingSource: String = "",
    var batteryTechnology: String = "",
    var cycleCount: Int = 0,
    var chargingTimeRemaining: Long = 0,
    var isCharging: Boolean = false,
    var isPresent: Boolean = false,
    var isOverheating: Boolean = false,
    var isHealthy: Boolean = false,
    var chargingSpeed: Float = 0f,
    var estimatedTimeRemaining: Long = 0
)