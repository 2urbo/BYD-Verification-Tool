package com.turbo2k.bydverificationtool.interfaces

interface PlatformCompatibilityProvider {
    fun ensurePlatformCompatibility(): Boolean
}