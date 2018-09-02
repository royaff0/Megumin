package com.sqrtf.common.cache

import android.text.TextUtils

/**
 * Created by roya on 2017/5/26.
 */
object MeguminPreferences {
    const val KEY_MP_SERVER = "MeguminPreferences:KEY_MP_SERVER"
    const val KEY_MP_USERNAME = "MeguminPreferences:KEY_MP_USERNAME"
    const val KEY_MP_PASSWORD = "MeguminPreferences:KEY_MP_PASSWORD"


    fun configured(): Boolean {
        return !(TextUtils.isEmpty(getServer()) || TextUtils.isEmpty(getUsername()))
    }

    fun setServer(server: String) {
        PreferencesUtil.getInstance().putString(KEY_MP_SERVER, server)
    }

    fun getServer(): String {
        return PreferencesUtil.getInstance().getString(KEY_MP_SERVER, "")
    }

    fun setUsername(server: String) {
        PreferencesUtil.getInstance().putString(KEY_MP_USERNAME, server)
    }

    fun getUsername(): String {
        return PreferencesUtil.getInstance().getString(KEY_MP_USERNAME, "")
    }

}