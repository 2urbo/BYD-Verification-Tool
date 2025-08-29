package com.turbo2k.bydverificationtool

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.turbo2k.bydverificationtool.common.constants.PlatformConstants
import com.turbo2k.bydverificationtool.databinding.ActivityMainBinding
import com.turbo2k.bydverificationtool.interfaces.PlatformCompatibilityProvider
import com.turbo2k.bydverificationtool.interfaces.SystemPropertyProvider
import com.turbo2k.bydverificationtool.utils.containsIgnoreCase

class MainActivity : AppCompatActivity(), PlatformCompatibilityProvider, SystemPropertyProvider {
    companion object {
        init {
            System.loadLibrary("bydverificationtool")
        }
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    override fun ensurePlatformCompatibility(): Boolean {
        return try {
            if (BuildConfig.DEBUG) return true

            val deviceProperty = getSystemProperty(PlatformConstants.DEVICE_PROPERTY_KEY)
            if (PlatformConstants.SUPPORTED_PLATFORMS.containsIgnoreCase(deviceProperty)) {
                val firmwareVersion = getSystemProperty(PlatformConstants.FIRMWARE_VERSION_PROPERTY_KEY)
                PlatformConstants.SUPPORTED_FIRMWARE_VERSIONS.containsIgnoreCase(firmwareVersion)
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    override fun readSystemProperty(propertyName: String): String {
        return getSystemProperty(propertyName)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    private external fun getSystemProperty(propertyName: String): String
}