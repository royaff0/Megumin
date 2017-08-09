package com.sqrtf.common

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by roya on 2017/7/20.
 */

class StringUtil {

    companion object {
        private var dayFormatter = SimpleDateFormat("EE", Locale.getDefault())
        private var msFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())
        private val oneDay = 86400000

        init {
            msFormatter.timeZone = TimeZone.getTimeZone("UTC")
        }


        fun dayOfWeek(day: Int): String {
            return dayFormatter.format(day * oneDay + 3 * oneDay)
        }

        fun microsecondFormat(ms: Long): String {
            return msFormatter.format(ms)
        }
    }
}