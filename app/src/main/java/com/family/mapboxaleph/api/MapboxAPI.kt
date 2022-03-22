package com.family.mapboxaleph.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MapboxAPI {

    @GET("{longitude},{latitude},15,0/400x250?")
    fun downloadMapboxStaticImg(
        @Path("longitude") longitude : String,
        @Path("latitude") latitude : String,
        @Query("access_token") token : String
    ): Call<ResponseBody>

}