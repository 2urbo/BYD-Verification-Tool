package com.turbo2k.bydverificationtool.common.constants

object PlatformConstants {
    const val DEVICE_PROPERTY_KEY = "ro.product.device"
    const val FIRMWARE_VERSION_PROPERTY_KEY = "persist.sys.version"

    val SUPPORTED_FIRMWARE_VERSIONS = listOf(
        "23.1.8.2502060.1",
        "23.1.83.2503026.1"
    )
    val SUPPORTED_PLATFORMS = listOf("DiLink5.0")
}