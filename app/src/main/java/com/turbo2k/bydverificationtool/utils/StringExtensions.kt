package com.turbo2k.bydverificationtool.utils

fun List<String>.containsIgnoreCase(target: String): Boolean {
    return this.any { it.equals(target, ignoreCase = true) }
}