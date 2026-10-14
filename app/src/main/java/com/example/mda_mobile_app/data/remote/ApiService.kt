package com.example.mda_mobile_app.data.remote

import com.example.mda_mobile_app.data.model.HealthResponse
import com.example.mda_mobile_app.data.model.TestRequest
import com.example.mda_mobile_app.data.model.TestResponse
import com.example.mda_mobile_app.data.model.common.ApiResponse
import com.example.mda_mobile_app.data.model.preferences.Preferences
import com.example.mda_mobile_app.data.model.preferences.UpdatePreferencesRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {

    @GET("api/health.php")
    suspend fun healthCheck(): Response<HealthResponse>

    @POST("api/test-request.php")
    suspend fun testRequest(
        @Body request: TestRequest
    ): Response<TestResponse>

    @GET("api/preferences/get.php")
    suspend fun getPreferences():
            Response<ApiResponse<Preferences>>

    @PUT("api/preferences/update.php")
    suspend fun updatePreferences(
        @Body request: UpdatePreferencesRequest
    ): Response<ApiResponse<Preferences>>
}