package com.sqrtf.common

import android.app.Application
import android.content.Context
import com.sqrtf.common.api.ApiClient
import com.sqrtf.common.cache.MeguminPreferences
import com.sqrtf.common.cache.PreferencesUtil
import android.content.Intent


/**
 * Created by roya on 2017/5/24.
 */

class MeguminApplocation : Application() {

    override fun onCreate() {
        super.onCreate()
        PreferencesUtil.init(this)
        if (MeguminPreferences.configured()) {
            ApiClient.init(this, MeguminPreferences.getServer())
        }
    }

    companion object {
        fun logout(context: Context) {
            PreferencesUtil.getInstance().clear()
            ApiClient.deinit()
            val i = context.applicationContext.packageManager.getLaunchIntentForPackage(context.applicationContext.packageName)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(i)
        }
    }
}