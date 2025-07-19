/*
 * SPDX-FileCopyrightText: 2025 Simone Esposito
 * SPDX-License-Identifier: Apache-2.0
 */

package com.esposito.batteryinfo.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.esposito.batteryinfo.R
import com.esposito.batteryinfo.databinding.ActivityHomeBinding
import com.esposito.batteryinfo.ui.adapters.BatteryDetailAdapter
import com.esposito.batteryinfo.ui.models.BatteryDetailItem
import com.esposito.batteryinfo.ui.models.BatteryInfo
import com.esposito.batteryinfo.ui.viewmodels.BatteryViewModel
import com.esposito.batteryinfo.utils.BatteryUtils
import com.esposito.batteryinfo.utils.UiUtils
import com.google.android.material.card.MaterialCardView
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private lateinit var batteryViewModel: BatteryViewModel
    private lateinit var detailAdapter: BatteryDetailAdapter
    private var currentBatteryInfo: BatteryInfo? = null
    private val silkInterpolator = AccelerateDecelerateInterpolator()
    
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateBatteryInfo()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        UiUtils.adjustStatusbarIconColor(window)

        setupToolbar()

        batteryViewModel = ViewModelProvider(this)[BatteryViewModel::class.java]

        binding.cardBatterySettings.setOnClickListener {
            openBatterySettings()
        }

        setupRecyclerViews()
        setupObservers()
        updateBatteryInfo()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayShowTitleEnabled(true)
        }

        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val percentage = abs(verticalOffset).toFloat() / totalScrollRange.toFloat()
            val interpolatedPercentage = silkInterpolator.getInterpolation(percentage)
            val interpolatedAlpha = (1.0f - interpolatedPercentage).coerceIn(0.0f, 1.0f)
            val scale = (1.0f - (interpolatedPercentage * 0.02f)).coerceIn(0.95f, 1.0f)
            val translationY = interpolatedPercentage * -8f

            binding.toolbar.alpha = interpolatedAlpha
            binding.toolbar.scaleX = scale
            binding.toolbar.scaleY = scale
            binding.toolbar.translationY = translationY
        }
    }
    
    private fun setupRecyclerViews() {
        detailAdapter = BatteryDetailAdapter()
        binding.recyclerDetailedInfo.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = detailAdapter
        }
    }
    
    private fun setupObservers() {
        batteryViewModel.currentBatteryInfo.observe(this) { batteryInfo ->
            batteryInfo?.let {
                currentBatteryInfo = it
                updateUI(it)
            }
        }
    }
    
    private fun updateBatteryInfo() {
        val batteryInfo = BatteryUtils.getBatteryInfo(this)
        batteryInfo?.let {
            currentBatteryInfo = it
            batteryViewModel.updateBatteryInfo()
            updateUI(it)
        }
    }

    private fun updateUI(batteryInfo: BatteryInfo) {
        binding.txtBatteryLevel.text = getString(R.string.battery_level_percentage, batteryInfo.batteryLevel)
        binding.txtBatteryStatus.text = batteryInfo.batteryStatus
        binding.progressBattery.progress = batteryInfo.batteryLevel

        binding.imgBatteryIcon.setImageResource(UiUtils.getBatteryIcon(batteryInfo.batteryLevel, batteryInfo.isCharging))

        if (batteryInfo.isCharging && batteryInfo.chargingTimeRemaining > 0) {
            val timeRemaining = BatteryUtils.formatDuration(batteryInfo.chargingTimeRemaining)
            binding.txtTimeRemaining.text = timeRemaining
            binding.txtTimeRemaining.visibility = View.VISIBLE
            val parent = binding.txtTimeRemaining.parent as android.view.ViewGroup
            parent.visibility = View.VISIBLE
        } else {
            val parent = binding.txtTimeRemaining.parent as android.view.ViewGroup
            parent.visibility = View.GONE
        }
        
        binding.txtHealthValue.text = getString(R.string.battery_level_percentage, batteryInfo.batteryHealthPercentage)
        binding.txtTemperatureValue.text = BatteryUtils.formatTemperature(batteryInfo.batteryTemperature)

        updateBatteryHealthCard(batteryInfo.batteryHealthPercentage, batteryInfo.batteryDesignCapacity, batteryInfo.batteryCurrentCapacity)
        updateBatterySaverCard()
        updateBatteryWarning(batteryInfo.batteryLevel)
        updateDetailedInfo(batteryInfo)
    }

    private fun updateBatteryWarning(batteryLevel: Int) {
        val warningCard = findViewById<MaterialCardView>(R.id.cardBatteryWarning)
        
        if (batteryLevel <= 20) {
            warningCard?.visibility = View.VISIBLE
            val warningText = binding.txtBatteryWarningLvl
            warningText.text = getString(R.string.battery_level_low)
        } else {
            warningCard?.visibility = View.GONE
        }
    }
    
    private fun updateDetailedInfo(batteryInfo: BatteryInfo) {
        val detailItems = mutableListOf<BatteryDetailItem>().apply {
            add(BatteryDetailItem(getString(R.string.voltage), BatteryUtils.formatVoltage(batteryInfo.batteryVoltage)))
            add(BatteryDetailItem(getString(R.string.current_capacity), BatteryUtils.formatCapacity(batteryInfo.batteryCurrentCapacity)))
            add(BatteryDetailItem(getString(R.string.design_capacity), BatteryUtils.formatCapacity(batteryInfo.batteryDesignCapacity)))
            add(BatteryDetailItem(getString(R.string.technology), batteryInfo.batteryTechnology))
            add(BatteryDetailItem(getString(R.string.source), batteryInfo.chargingSource))
            
            if (batteryInfo.cycleCount > 0) {
                add(BatteryDetailItem(getString(R.string.cycles), batteryInfo.cycleCount.toString()))
            }
            if (batteryInfo.chargingSpeed > 0) {
                add(BatteryDetailItem(getString(R.string.charging_speed), String.format(Locale.US, getString(R.string.charging_speed_format), batteryInfo.chargingSpeed)))
            }
        }
        detailAdapter.submitList(detailItems)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        }
        registerReceiver(batteryReceiver, filter)
    }
    
    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryReceiver)
    }
    
    private fun openBatterySettings() {
        try {
            val intent = Intent().apply {
                setClassName("com.android.settings", "com.android.settings.Settings\$PowerUsageSummaryActivity")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("BatteryInfo", "Failed to open battery settings", e)
            Toast.makeText(this, getString(R.string.failed_to_open_battery_settings), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateBatterySaverCard() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val batterySaverCard = binding.cardBatterySaver
        if (powerManager.isPowerSaveMode) {
            batterySaverCard.visibility = View.VISIBLE
        } else {
            batterySaverCard.visibility = View.GONE
        }
    }
    
    private fun updateBatteryHealthCard(healthPercentage: Int, designCapacity: Int, currentCapacity: Int) {
        val batteryHealthCard = binding.cardBatteryHealth
        val isHealthDataFailed = healthPercentage <= 0 || (designCapacity <= 0 && currentCapacity != 0)
        if (healthPercentage < 80) {
            val txtBatteryHealthWarning = binding.txtBatteryHealthWarning
            batteryHealthCard.visibility = View.VISIBLE
            if (isHealthDataFailed) {
                txtBatteryHealthWarning.text = getString(R.string.battery_health_data_failed)
            } else {
                txtBatteryHealthWarning.text = getString(R.string.battery_assistance_required)
            }
        } else {
            batteryHealthCard.visibility = View.GONE
        }
    }
}