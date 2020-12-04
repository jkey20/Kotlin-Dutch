package com.dutch2019.network

import com.dutch2019.data.DetailData
import com.dutch2019.data.Info
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Service {

    @GET("pois/{poiId}?version=1&appKey=l7xx75e02f3eccaa4f56b3f269cb4a9f2b43")
    fun getDetailInfo(@Path("poiId") poiId: String): Call<DetailData>
}