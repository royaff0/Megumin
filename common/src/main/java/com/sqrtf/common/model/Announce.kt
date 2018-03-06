package com.sqrtf.common.model

/**
 * Created by roya on 2017/5/24.
 */

open class Announce(
        val id: String,
        val position: Long,

        val start_time: Long,
        val end_time: Long,
        val sort_order: Long,
        val content: String,
        val image_url: String,

        val bangumi: Bangumi)