package com.sqrtf.common.api

/**
 * Created by roya on 2017/5/22.
 */
data class LoginRequest(val name: String, val password: String, val remmember: Boolean)

val FavoriteStatus_DEF = 0
val FavoriteStatus_WISH = 1
val FavoriteStatus_WATCHED = 2
val FavoriteStatus_WATCHING = 3
val FavoriteStatus_PAUSE = 4
val FavoriteStatus_ABANDONED = 5

data class FavoriteChangeRequest(val status: Int)

data class HistoryChangeRequest(val bangumi_id: String, val last_watch_position: Long, val percentage: Float, val is_finished: Boolean)
