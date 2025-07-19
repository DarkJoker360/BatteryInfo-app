/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */
package com.esposito.batteryinfo.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.esposito.batteryinfo.utils.BatteryUtils
import com.esposito.batteryinfo.ui.models.BatteryInfo

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _currentBatteryInfo = MutableLiveData<BatteryInfo>()
    val currentBatteryInfo: LiveData<BatteryInfo> = _currentBatteryInfo
    private val _isRefreshing = MutableLiveData<Boolean>()
    
    fun updateBatteryInfo() {
        _isRefreshing.value = true
        _currentBatteryInfo.value = BatteryUtils.getBatteryInfo(getApplication())!!
        _isRefreshing.value = false
    }
}
