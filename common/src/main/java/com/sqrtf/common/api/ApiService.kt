package com.sqrtf.common.api

import com.sqrtf.common.model.*
import io.reactivex.Observable
import retrofit2.http.*

/**
 * Created by roya on 2017/5/22.
 */

interface ApiService {

    /**
     * Users
     */

    @POST("api/user/login")
    fun login(@Body body: LoginRequest): Observable<MessageResponse>

    @POST("api/user/logout")
    fun logout(): Observable<MessageResponse>

    @GET("/api/user/info")
    fun getUserInfo(): Observable<DataResponse<UserInfo>>


    /**
     * Bangumi info
     */

    @GET("/api/home/my_bangumi")
    fun getMyBangumi(@Query("status") status: Int = 3): Observable<ListResponse<Bangumi>>

    @GET("/api/home/on_air")
    fun getAllBangumi(): Observable<ListResponse<Bangumi>>

    @GET("/api/home/bangumi")
    fun getSearchBangumi(
            @Query("page") page: Int,
            @Query("count") count: Int,
            @Query("sort_field") sortField: String,
            @Query("sort_order") sortOrder: String,
            @Query("name") name: String?
    ): Observable<ListResponse<Bangumi>>

    @GET("/api/home/bangumi/{id}")
    fun getBangumiDetail(@Path("id") id: String): Observable<DataResponse<BangumiDetail>>

    @GET("/api/home/episode/{id}")
    fun getEpisodeDetail(@Path("id") id: String): Observable<EpisodeDetail>

    /**
     * Favorite and history
     */

    @POST("/api/watch/favorite/bangumi/{bangumi_id}")
    fun uploadFavoriteStatus(@Path("bangumi_id") bangumiId: String, @Body body: FavoriteChangeRequest): Observable<MessageResponse>

    @POST("/api/watch/history/synchronize")
    fun uploadWatchHistory(@Body body: HistoryChangeRequest): Observable<MessageResponse>
}