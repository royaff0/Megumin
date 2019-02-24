package com.sqrtf.common.model

import android.graphics.Color
import com.sqrtf.common.api.ApiHelper

public class CoverImage(
        var url: String?,
        var dominant_color: String?,
        var height: Int?,
        var width: Int?
) {
    init {
        fixedUrl()
        fixedColor()
    }

    var f_url: String? = null
    fun fixedUrl(): String? {
        if (f_url == null) {
            f_url = ApiHelper.fixHttpUrl(url)
        }
        return f_url
    }

    var f_color: Int? = null
    fun fixedColor(): Int {
        if (f_color == null) {
            f_color = Color.parseColor(dominant_color)
        }

        return f_color!!
    }

}
