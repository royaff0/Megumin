package com.sqrtf.common.api

import android.content.Context
import android.util.Log
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.IllegalStateException


/**
 * Created by roya on 2017/5/22.
 */

object ApiClient {
    private var instance: ApiService? = null
    private var retrofit: Retrofit? = null
    private var cookieJar: PersistentCookieJar? = null

    fun init(context: Context, server: String) {
        instance = create(context, server)
    }

    fun deinit() {
        cookieJar?.clear()
        cookieJar = null
        retrofit = null
        instance = null
    }

    fun getInstance(): ApiService {
        if (instance != null) {
            return instance as ApiService
        }

        throw IllegalStateException("ApiClient Not being initialized")
    }

    fun converterErrorBody(error: ResponseBody): MessageResponse? {
        if (retrofit == null) {
            throw IllegalStateException("ApiClient Not being initialized")
        }

        val errorConverter: Converter<ResponseBody, MessageResponse>
                = (retrofit as Retrofit).responseBodyConverter(MessageResponse::class.java, arrayOfNulls<Annotation>(0))

        try {
            return errorConverter.convert(error)
        } catch (e: Throwable) {
            return null
        }

    }

    private fun create(context: Context, server: String): ApiService {
        cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))

        val okHttp = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor {
                    val request = it.request()
                    val response = it.proceed(request)
                    val body = response.body()
                    val bodyString = body?.string()
                    Log.i("TAG", response.toString() + " Body:" + bodyString)
                    response.newBuilder()
                            .headers(response.headers())
                            .body(ResponseBody.create(body?.contentType(), bodyString))
                            .build()
                }
                .build()

        retrofit = Retrofit.Builder()
                .client(okHttp)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(server)
                .build()

        return (retrofit as Retrofit).create(ApiService::class.java)
    }
}
