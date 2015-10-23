package com.jikexueyuan.mobile.address;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

interface API {

    @GET("login")
    Call<String> getLoginPageHtml();

    @FormUrlEncoded
    @POST("login")
    Call<String> login(@Field("utf8") String uft8,
                       @Field("authenticity_token") String authenticity_token,
                       @Field("username") String username,
                       @Field("password") String password,
                       @Field("login") String login);

    @GET("address_books")
    Call<String> addressBooks(@Query("page") int page, @Query("per_page") int per_page);
}