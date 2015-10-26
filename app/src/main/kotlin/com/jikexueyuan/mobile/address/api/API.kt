package com.jikexueyuan.mobile.address.api

import retrofit.Call
import retrofit.http.Field
import retrofit.http.FormUrlEncoded
import retrofit.http.GET
import retrofit.http.POST
import retrofit.http.Query

internal interface API {

    @GET("login")
    fun loginPageHtml(): Call<String>

    @FormUrlEncoded
    @POST("login")
    fun login(@Field("utf8") uft8: String,
              @Field("authenticity_token") authenticity_token: String,
              @Field("username") username: String,
              @Field("password") password: String,
              @Field("login") login: String): Call<String>

    @GET("address_books")
    fun addressBooks(@Query("page") page: Int, @Query("per_page") per_page: Int): Call<String>
}