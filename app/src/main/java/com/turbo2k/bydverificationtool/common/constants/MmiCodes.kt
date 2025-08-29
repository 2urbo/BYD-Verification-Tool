package com.turbo2k.bydverificationtool.common.constants

import android.net.Uri
import androidx.core.net.toUri

object MmiCodes {
    private const val DEVELOPMENT_TOOLS_CODE = "*#91532547#*"

    fun getDevelopmentToolsDialerUri(): Uri {
        return ("tel:" + Uri.encode(DEVELOPMENT_TOOLS_CODE)).toUri()
    }
}