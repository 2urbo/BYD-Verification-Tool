package com.turbo2k.bydverificationtool.utils

import android.content.Context

object AppUtils {
    fun getAppVersion(context: Context): Pair<String, Long> {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName!! to packageInfo.longVersionCode
    }
}