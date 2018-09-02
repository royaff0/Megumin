package com.sqrtf.common.cache

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils

import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.sqrtf.common.BuildConfig

class PreferencesUtil private constructor(context: Context) {
    private val preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences(
                TAG_KEY,
                Context.MODE_PRIVATE
        )
    }

    fun putLong(k: String, v: Long) {
        preferences.edit().putLong(k, v).apply()
    }

    fun getLong(k: String, defValue: Long): Long {
        return preferences.getLong(k, defValue)
    }

    fun putInt(k: String, v: Int) {
        preferences.edit().putInt(k, v).apply()
    }

    fun remove(k: String) {
        preferences.edit().remove(k).apply()
    }

    fun getInt(k: String, defValue: Int): Int {
        return preferences.getInt(k, defValue)
    }

    fun putString(k: String, v: String) {
        preferences.edit().putString(k, v).apply()
    }

    fun getString(k: String, defValue: String): String {
        return preferences.getString(k, defValue)
    }

    fun putBoolean(k: String, v: Boolean) {
        preferences.edit().putBoolean(k, v).apply()
    }

    fun getBoolean(k: String, defValue: Boolean?): Boolean? {
        return preferences.getBoolean(k, defValue!!)
    }

    fun putObject(k: String, v: Any) {
        val json = JsonUtil.toJson(v)
        preferences.edit().putString("Object" + k, json).apply()
    }

    fun <T> getObject(k: String, classOfT: Class<T>): T? {
        val json = preferences.getString("Object" + k, null)
        if (TextUtils.isEmpty(json)) return null
        var output: T? = null
        try {
            output = JsonUtil.fromJson(json!!, classOfT)
        } catch (ignored: JsonParseException) {
        }

        return output
    }

    fun putObject(k: String, src: List<*>) {
        val json = JsonUtil.toJson(src)
        preferences.edit().putString("List" + k, json).apply()
    }

    fun <T> getObject(k: String, typeToken: TypeToken<*>): T? {
        val json = preferences.getString("List" + k, null)
        if (TextUtils.isEmpty(json)) return null
        var output: T? = null
        try {
            output = JsonUtil.fromJson<T>(json!!, typeToken)
        } catch (ignored: JsonParseException) {
        }

        return output
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        private val TAG_KEY = BuildConfig.APPLICATION_ID
        private var mInstance: PreferencesUtil? = null

        fun init(context: Context) {
            mInstance = PreferencesUtil(context)
        }

        fun getInstance(): PreferencesUtil {
            if (mInstance == null) {
                throw IllegalStateException("PreferencesUtil Not initialed")
            }

            return mInstance!!
        }
    }
}
