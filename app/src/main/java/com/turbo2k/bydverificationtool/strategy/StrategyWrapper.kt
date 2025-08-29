package com.turbo2k.bydverificationtool.strategy

import android.annotation.SuppressLint
import java.lang.reflect.Field

@SuppressLint("PrivateApi")
class StrategyWrapper(strategyObject: Any?) {

    companion object {
        const val STRATEGY_CLASS_NAME = "android.strategyservice.Strategy"

        private const val STRATEGY_CONFIGS_FIELD_NAME = "configs"
        private const val STRATEGY_PERMISSIONS_FIELD_NAME = "permissions"
        private const val STRATEGY_STRATEGY_DATA_FIELD_NAME = "strategyData"
    }

    val strategy: Any

    val configs: HashMap<*, *>
    val permissions: HashMap<*, *>
    val strategyData: String

    init {
        val strategyClass = Class.forName(STRATEGY_CLASS_NAME)

        requireNotNull(strategyObject)
        require(strategyClass.isInstance(strategyObject))

        strategy = strategyObject

        val configsField: Field = strategyClass.getDeclaredField(STRATEGY_CONFIGS_FIELD_NAME)
        configs = configsField.get(strategy) as HashMap<*, *>

        val permissionsField: Field = strategyClass.getDeclaredField(STRATEGY_PERMISSIONS_FIELD_NAME)
        permissions = permissionsField.get(strategy) as HashMap<*, *>

        val strategyDataField: Field = strategyClass.getDeclaredField(STRATEGY_STRATEGY_DATA_FIELD_NAME)
        strategyData = strategyDataField.get(strategy) as? String ?: ""
    }

    fun getStrategyConfigs(): HashMap<String, String> {
        @Suppress("UNCHECKED_CAST")
        return configs as HashMap<String, String>
    }

    fun getStrategyPermissions(): HashMap<String, List<String>> {
        @Suppress("UNCHECKED_CAST")
        return permissions as HashMap<String, List<String>>
    }
}