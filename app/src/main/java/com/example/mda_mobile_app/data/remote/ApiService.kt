package com.example.mda_mobile_app.data.remote

import com.example.mda_mobile_app.data.model.HealthResponse
import com.example.mda_mobile_app.data.model.TestRequest
import com.example.mda_mobile_app.data.model.TestResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("api/health.php")
    suspend fun healthCheck(): Response<HealthResponse>

    @POST("api/test-request.php")
    suspend fun testRequest(
        @Body request: TestRequest
    ): Response<TestResponse>
}