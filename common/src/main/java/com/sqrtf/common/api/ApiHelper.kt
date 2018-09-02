package com.sqrtf.common.api

import com.sqrtf.common.cache.MeguminPreferences

/**
 * Created by roya on 2017/6/4.
 */

object ApiHelper {
    fun fixHttpUrl(url: String): String {
        if (url.startsWith("http")) {
            return url
        }

        return MeguminPreferences.getServer() + if (url.startsWith("/")) url.substring(1) else url
    }
}
