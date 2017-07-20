package com.sqrtf.common

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by roya on 2017/7/20.
 */

class StringUtil {

    companion object {
        private var dayFormat = SimpleDateFormat("EE", Locale.getDefault())
        private val threeDays = 86400000 * 3

        fun dayOfWeek(day: Int): String {
            return dayFormat.format(day * 86400000 + threeDays)
        }
    }
}