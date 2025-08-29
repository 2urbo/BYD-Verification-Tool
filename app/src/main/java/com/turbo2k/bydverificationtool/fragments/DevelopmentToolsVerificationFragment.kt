package com.turbo2k.bydverificationtool.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.turbo2k.bydverificationtool.R
import com.turbo2k.bydverificationtool.common.constants.MmiCodes
import com.turbo2k.bydverificationtool.common.constants.PlatformConstants
import com.turbo2k.bydverificationtool.common.constants.StrategyConfigurationConstants
import com.turbo2k.bydverificationtool.databinding.FragmentDevelopmentToolsVerificationBinding
import com.turbo2k.bydverificationtool.interfaces.SystemPropertyProvider
import com.turbo2k.bydverificationtool.strategy.StrategyManagerWrapper
import com.turbo2k.bydverificationtool.utils.AppUtils

class DevelopmentToolsVerificationFragment : Fragment() {
    companion object {
        private const val DISABLED_ALPHA = 0.4f
    }
    private val tag = this::class.java.simpleName

    private var _binding: FragmentDevelopmentToolsVerificationBinding? = null

    private val binding get() = _binding!!

    private var systemPropertyProvider: SystemPropertyProvider? = null

    private var strategyManagerWrapper: StrategyManagerWrapper? = null

    private var configStrategyKey: String? = null
    private var developmentToolsVerifyEnableKey: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevelopmentToolsVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is SystemPropertyProvider) {
            systemPropertyProvider = context
        } else {
            throw ClassCastException("$context must implement SystemPropertyProvider")
        }

        try {
            strategyManagerWrapper = StrategyManagerWrapper(context)
            configStrategyKey = strategyManagerWrapper?.getStaticString(StrategyConfigurationConstants.CONFIG_STRATEGY_FIELD_NAME)
            developmentToolsVerifyEnableKey = strategyManagerWrapper?.getStaticString((StrategyConfigurationConstants.DEVELOPMENT_TOOLS_VERIFY_ENABLE_FIELD_NAME))
        } catch (e: Exception) {
            Log.e(tag, "failed to initialize StrategyManager", e)
        }
    }

    override fun onDetach() {
        super.onDetach()
        systemPropertyProvider = null
        strategyManagerWrapper = null
        configStrategyKey = null
        developmentToolsVerifyEnableKey = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!configStrategyKey.isNullOrEmpty() && !developmentToolsVerifyEnableKey.isNullOrEmpty()) {
            updateDevelopmentToolsVerificationSwitchState()

            binding.developmentToolsVerificationSwitch.setOnClickListener { switch ->
                onDevelopmentToolsVerificationSwitchToggled(switch as SwitchCompat)
            }
        } else {
            binding.developmentToolsVerificationSwitch.alpha = DISABLED_ALPHA
            binding.developmentToolsVerificationSwitch.setOnTouchListener { _, _ ->
                Toast.makeText(requireContext(), getString(R.string.feature_not_supported_message), Toast.LENGTH_SHORT).show()
                true
            }
        }

        binding.developmentToolsButton.setOnClickListener { openDevelopmentTools() }

        val unknownValue = getString(R.string.unknown_value)

        val platformName = systemPropertyProvider
            ?.readSystemProperty(PlatformConstants.DEVICE_PROPERTY_KEY)
            ?.takeIf { it.isNotEmpty() } ?: unknownValue
        binding.platformNameText.text = getString(R.string.platform_name_label, platformName)

        val firmwareVersion = systemPropertyProvider
            ?.readSystemProperty(PlatformConstants.FIRMWARE_VERSION_PROPERTY_KEY)
            ?.takeIf { it.isNotEmpty() } ?: unknownValue
        binding.firmwareVersionText.text = getString(R.string.firmware_version_label, firmwareVersion)

        val appVersion = AppUtils.getAppVersion(requireContext())
        binding.appVersionText.text = getString(R.string.app_version_label, appVersion.first, appVersion.second.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onDevelopmentToolsVerificationSwitchToggled(switch: SwitchCompat) {
        try {
            val configKey = configStrategyKey ?: return
            val configItemKey = developmentToolsVerifyEnableKey ?: return
            val strategyManager = strategyManagerWrapper ?: return

            val strategy = strategyManager.getStrategy(configKey, listOf(configItemKey))

            strategy.getStrategyConfigs()[configItemKey] = if (!switch.isChecked) {
                StrategyConfigurationConstants.DISABLED_VALUE
            } else {
                StrategyConfigurationConstants.ENABLED_VALUE
            }

            strategyManager.setStrategy(configKey, strategy)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.unexpected_failure_message), Toast.LENGTH_SHORT).show()
            Log.e(tag, "failed to update strategy", e)
        } finally {
            updateDevelopmentToolsVerificationSwitchState()
        }
    }

    private fun updateDevelopmentToolsVerificationSwitchState() {
        binding.developmentToolsVerificationSwitch.isChecked = try {
            val configKey = configStrategyKey
            val configItemKey = developmentToolsVerifyEnableKey
            val strategyManager = strategyManagerWrapper

            if (configKey == null || configItemKey == null || strategyManager == null) {
                false
            } else {
                val strategy = strategyManager.getStrategy(configKey, listOf(configItemKey))
                val currentValue = strategy.getStrategyConfigs()
                    .getOrDefault(configItemKey, StrategyConfigurationConstants.ENABLED_VALUE)
                currentValue == StrategyConfigurationConstants.DISABLED_VALUE
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun openDevelopmentTools() {
        val intent = Intent(Intent.ACTION_DIAL, MmiCodes.getDevelopmentToolsDialerUri())
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(context, getString(R.string.no_dialer_app_message), Toast.LENGTH_SHORT).show()
        }
    }
}