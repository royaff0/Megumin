package com.sqrtf.common.api

import com.sqrtf.common.model.Bangumi
import retrofit2.HttpException
import java.util.*

/**
 * Created by roya on 2017/5/23.
 */

open class BaseResponse {}

open class MessageResponse : BaseResponse() {
    private var msg: String? = null
    private var message: String? = null

    fun message(): String? = msg ?: message

    override fun toString(): String {
        return "message:" + message()
    }
}

class DataResponse<T> : MessageResponse() {
    private var data: T? = null

    fun getData(): T {
        if (data != null) {
            return data as T
        }else{
            throw ServerException("NoData")
        }
    }

    override fun toString(): String {
        return super.toString() + " data:" + data?.toString()
    }
}

class ListResponse<T> : BaseResponse() {
    private var data: List<T>? = null

    fun getData(): List<T> {
        if (data != null) {
            return data as List<T>
        }

        return Collections.emptyList()
    }

    override fun toString(): String {
        return super.toString() + " list:" + getData().toString()
    }
}