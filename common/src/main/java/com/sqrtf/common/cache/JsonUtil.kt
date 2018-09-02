package com.sqrtf.common.cache

import android.util.Log

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

object JsonUtil {
    private val gson = Gson()

    fun toJson(src: Any): String {
        return gson.toJson(src)
    }

    fun <T> fromJson(src: String, classOfT: Class<T>): T? {
        try {
            return gson.fromJson(src, classOfT)
        } catch (e: JsonParseException) {
            Log.w("JsonUtil", "JsonParseException", e)
        }

        return null
    }

    fun toJson(src: List<*>): String {
        return gson.toJson(src)
    }

    fun <T> fromJson(src: String, typeToken: TypeToken<*>): T {
        return gson.fromJson<T>(src, typeToken.type)
    }
}
