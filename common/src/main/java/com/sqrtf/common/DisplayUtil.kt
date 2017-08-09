package com.sqrtf.common

import android.content.res.Resources

/**
 * Created by roya on 2017/8/9.
 */

class DisplayUtil {
    companion object {
        fun dp2px(res: Resources, dp: Int): Int {
            return (res.displayMetrics.density * dp).toInt()
        }

        fun px2dp(res: Resources, px: Float): Int {
            return (px / res.displayMetrics.density).toInt()
        }
    }
}
