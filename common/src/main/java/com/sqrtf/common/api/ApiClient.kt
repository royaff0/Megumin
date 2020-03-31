package com.sqrtf.common.api

import android.annotation.SuppressLint
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
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion
import android.os.Build
import javax.net.ssl.SSLContext


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

    @SuppressLint("ObsoleteSdkInt")
    private fun OkHttpClient.Builder.enableTls12OnPreLollipop(): OkHttpClient.Builder {
        if (Build.VERSION.SDK_INT in 16..21) {
            try {
                val sc = SSLContext.getInstance("TLSv1.2")
                sc.init(null, null, null)
                this.sslSocketFactory(Tls12SocketFactory(sc.socketFactory))

                val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .supportsTlsExtensions(true)
                        .build()

                val specs = ArrayList<ConnectionSpec>()
                specs.add(cs)
                specs.add(ConnectionSpec.COMPATIBLE_TLS)
                specs.add(ConnectionSpec.CLEARTEXT)

                this.connectionSpecs(specs)
            } catch (exc: Exception) {
                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc)
            }

        }

        return this
    }

    private fun create(context: Context, server: String): ApiService {
        cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))

        val okHttp = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
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
                .enableTls12OnPreLollipop()
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
