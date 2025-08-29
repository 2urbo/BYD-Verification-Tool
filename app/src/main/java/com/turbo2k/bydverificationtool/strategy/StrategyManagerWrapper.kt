package com.turbo2k.bydverificationtool.strategy

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.locks.Lock

@SuppressLint("PrivateApi")
class StrategyManagerWrapper(context: Context) {
    companion object {
        private const val STRATEGY_MANAGER_CLASS_NAME = "android.strategyservice.StrategyManager"

        private const val STRATEGY_MANAGER_ACQUISITION_STRATEGY_FIELD_NAME = "ACQUISITION_STRATEGY"
        private const val STRATEGY_MANAGER_CONFIG_STRATEGY_FIELD_NAME = "CONFIG_STRATEGY"
        private const val STRATEGY_MANAGER_PERMISSION_STRATEGY_FIELD_NAME = "PERMISSION_STRATEGY"

        private const val STRATEGY_MANAGER_GET_STRATEGY_METHOD_NAME = "getStrategy"
        private const val STRATEGY_MANAGER_SET_STRATEGY_METHOD_NAME = "setStrategy"

        private const val STRATEGY_MANAGER_IS_LEGAL_METHOD_NAME = "isLeagl"

        private const val STRATEGY_MANAGER_LOCK_FIELD_NAME = "mLock"
        private const val STRATEGY_MANAGER_SERVICE_FIELD_NAME = "mService"
        private const val STRATEGY_MANAGER_SERVICE_GET_STRATEGY_SERVICE_METHOD_NAME = "getStrategyService"

        private const val STRATEGY_SERVICE_SET_STRATEGY_METHOD_NAME = "setStrategy"
    }

    private val tag = this::class.java.simpleName

    private val strategyManagerClass: Class<*> = Class.forName(STRATEGY_MANAGER_CLASS_NAME)

    private val acquisitionStrategy: String
    private val configStrategy: String
    private val permissionStrategy: String

    private val getStrategyManagerStrategy: (name: String, items: List<String>) -> Any?
    private val setStrategyManagerStrategy: (name: String, strategy: Any) -> Unit

    private val isLegal: (apiMethod: String) -> Boolean

    private val lock: Lock

    private val getStrategyService: () -> Any?
    private val setStrategyServiceStrategy: (service: Any?, name: String, value: String) -> Unit
    private val initStrategyService: () -> Unit

    init {
        val strategyManagerConstructor = strategyManagerClass.declaredConstructors.first()
        val strategyManager = strategyManagerConstructor.newInstance(context)

        acquisitionStrategy = getStaticString(STRATEGY_MANAGER_ACQUISITION_STRATEGY_FIELD_NAME)
        configStrategy = getStaticString(STRATEGY_MANAGER_CONFIG_STRATEGY_FIELD_NAME)
        permissionStrategy = getStaticString(STRATEGY_MANAGER_PERMISSION_STRATEGY_FIELD_NAME)

        val getStrategyMethod: Method = strategyManagerClass.getDeclaredMethod(
            STRATEGY_MANAGER_GET_STRATEGY_METHOD_NAME,
            String::class.java,
            List::class.java
        )
        getStrategyManagerStrategy = { name, items -> getStrategyMethod.invoke(strategyManager, name, items) }

        val setStrategyMethod: Method = strategyManagerClass.getDeclaredMethod(
            STRATEGY_MANAGER_SET_STRATEGY_METHOD_NAME,
            String::class.java,
            Class.forName(StrategyWrapper.STRATEGY_CLASS_NAME)
        )
        setStrategyManagerStrategy = { name, strategy -> setStrategyMethod.invoke(strategyManager, name, strategy) }

        isLegal = try {
            val isLegalMethod: Method = strategyManagerClass.getDeclaredMethod(
                STRATEGY_MANAGER_IS_LEGAL_METHOD_NAME,
                String::class.java).apply {
                    isAccessible = true
            };
            { apiMethod -> isLegalMethod.invoke(strategyManager, apiMethod) as Boolean }
        }
        catch (e: Throwable) {
            Log.w(tag, "the legality of the execution cannot be verified", e);
            { true }
        }

        val lockField: Field = strategyManagerClass.getDeclaredField(STRATEGY_MANAGER_LOCK_FIELD_NAME).apply {
            isAccessible = true
        }
        lock = lockField.get(strategyManager) as Lock

        val strategyServiceField: Field = strategyManagerClass.getDeclaredField(STRATEGY_MANAGER_SERVICE_FIELD_NAME).apply {
            isAccessible = true
        }
        getStrategyService = { strategyServiceField.get(strategyManager); }
        val strategyService = getStrategyService()
        val strategyServiceClass = strategyService?.javaClass
        val setStrategyServiceStrategyMethod = strategyServiceClass?.getDeclaredMethod(
            STRATEGY_SERVICE_SET_STRATEGY_METHOD_NAME,
            String::class.java,
            String::class.java
        )
        setStrategyServiceStrategy = { service, name, value -> setStrategyServiceStrategyMethod?.invoke(service, name, value) }

        val getStrategyServiceMethod: Method = strategyManagerClass.getDeclaredMethod(STRATEGY_MANAGER_SERVICE_GET_STRATEGY_SERVICE_METHOD_NAME).apply {
            isAccessible = true
        }
        initStrategyService = { getStrategyServiceMethod.invoke(strategyManager) }
    }

    fun getStrategy(name: String, items: List<String>): StrategyWrapper {
        val strategy = getStrategyManagerStrategy(name, items)
        return StrategyWrapper(strategy)
    }

    fun setStrategy(name: String, strategyWrapper: StrategyWrapper) {
        val isLegal = isLegal(STRATEGY_MANAGER_SET_STRATEGY_METHOD_NAME)
        if (isLegal) {
            setStrategyManagerStrategy(name, strategyWrapper.strategy)
        }
        else {
            setServiceStrategy(name, strategyWrapper)
        }
    }

    fun getStaticString(fieldName: String): String {
        val staticStringField = strategyManagerClass.getDeclaredField(fieldName)
        return staticStringField.get(null) as String
    }

    private fun setServiceStrategy(name: String, strategyWrapper: StrategyWrapper) {
        try {
            try {
                lock.lock()
            } catch (e: Exception) {
                Log.e(tag, "setServiceStrategy fail", e)
            }

            var service = getStrategyService()
            if (service == null) {
                initStrategyService()
                service = getStrategyService()
            }
            if (service != null) {
                var setStrategy = ""
                if (configStrategy == name) {
                    setStrategy = JSONObject(strategyWrapper.configs).toString()
                } else if (permissionStrategy == name) {
                    setStrategy = JSONObject(strategyWrapper.permissions).toString()
                } else if (acquisitionStrategy == name) {
                    setStrategy = strategyWrapper.strategyData
                }

                if (setStrategy.isNotEmpty()) {
                    setStrategyServiceStrategy(service, name, setStrategy)
                }
            }
        } finally {
            lock.unlock()
            Log.d(tag, "setServiceStrategy: finally unlock")
        }
    }
}