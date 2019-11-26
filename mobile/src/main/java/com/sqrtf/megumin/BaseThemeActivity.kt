package com.sqrtf.megumin

import android.os.Build
import android.os.Bundle
import android.view.View

import com.sqrtf.common.activity.BaseActivity
import com.sqrtf.common.cache.PreferencesUtil

abstract class BaseThemeActivity : BaseActivity() {

    protected val isWhiteTheme: Boolean
        get() = PreferencesUtil.getInstance().getBoolean("whiteTheme", false)!!

    open fun themeWhite(): Int {
        return R.style.AppThemeWhite
    }

    open fun themeStand(): Int {
        return R.style.AppTheme
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(if (isWhiteTheme) themeWhite() else themeStand())
        super.onCreate(savedInstanceState)
    }

    protected fun themeChanged() {
        PreferencesUtil.getInstance().putBoolean("whiteTheme", !isWhiteTheme)
        recreate()
    }
}
