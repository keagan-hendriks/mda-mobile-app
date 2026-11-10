package com.example.mda_mobile_app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    //don't touch the base URL ever please - Keagan
//    private const val BASE_URL = "http://10.0.2.2/mda_backend/"//for now localhost

    //for docker, will change to hosting link once hosted
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}