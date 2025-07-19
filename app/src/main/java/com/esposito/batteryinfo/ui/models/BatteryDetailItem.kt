/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.ui.models

data class BatteryDetailItem(
    val title: String,
    val value: String,
    val unit: String = ""
) {
    fun getFormattedValue(): String {
        return if (unit.isNotEmpty()) {
            "$value $unit"
        } else {
            value
        }
    }
}
