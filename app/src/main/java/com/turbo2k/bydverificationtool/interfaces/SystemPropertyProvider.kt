package com.turbo2k.bydverificationtool.interfaces

interface SystemPropertyProvider {
    fun readSystemProperty(propertyName: String): String
}