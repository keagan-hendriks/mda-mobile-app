package com.example.mda_mobile_app.data.remote

import com.example.mda_mobile_app.data.model.HealthResponse
import com.example.mda_mobile_app.data.model.TestRequest
import com.example.mda_mobile_app.data.model.TestResponse
import com.example.mda_mobile_app.data.model.common.ApiResponse
import com.example.mda_mobile_app.data.model.preferences.Preferences
import com.example.mda_mobile_app.data.model.preferences.UpdatePreferencesRequest
import com.example.mda_mobile_app.data.model.users.User
import com.example.mda_mobile_app.data.model.users.CreateUserRequest
import com.example.mda_mobile_app.data.model.users.UpdateUserRequest
import com.example.mda_mobile_app.data.model.users.UpdatePasswordRequest
import com.example.mda_mobile_app.data.model.crud.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.DELETE

interface ApiService {

    @GET("api/health.php")
    suspend fun healthCheck(): Response<HealthResponse>

    @POST("api/test-request.php")
    suspend fun testRequest(
        @Body request: TestRequest
    ): Response<TestResponse>

    //updated get preferences call to work with userID
    @GET("api/preferences/get.php")
    suspend fun getPreferences(
        @Query("userId") userId: Int
    ): Response<ApiResponse<Preferences>>

    @PUT("api/preferences/update.php")
    suspend fun updatePreferences(
        @Body request: UpdatePreferencesRequest
    ): Response<ApiResponse<Preferences>>

    //users
    @POST("api/users/create.php")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): Response<ApiResponse<User>>

    @GET("api/users/get.php")
    suspend fun getUser(
        @Query("userId") userId: Int
    ): Response<ApiResponse<User>>

    @PUT("api/users/update.php")
    suspend fun updateUser(
        @Body request: UpdateUserRequest
    ): Response<ApiResponse<User>>

    @PUT("api/users/update-password.php")
    suspend fun updatePassword(
        @Body request: UpdatePasswordRequest
    ): Response<ApiResponse<Any>>

    //badges
    @POST("api/badges/create.php")
    suspend fun createBadge(@Body request: CreateBadgeRequest): Response<ApiResponse<Badge>>

    @GET("api/badges/get.php")
    suspend fun getBadge(@Query("badgeId") badgeId: Int): Response<ApiResponse<Badge>>

    @GET("api/badges/get.php")
    suspend fun listBadges(@Query("limit") limit: Int? = null, @Query("offset") offset: Int? = null): Response<ApiResponse<List<Badge>>>

    // PUT updates any supplied writable fields. Include the primary key in the map.
    @PUT("api/badges/update.php")
    suspend fun updateBadge(@Body fields: Map<String, Any?>): Response<ApiResponse<Badge>>

    @DELETE("api/badges/delete.php")
    suspend fun deleteBadge(@Query("badgeId") badgeId: Int): Response<ApiResponse<Badge>>

    //medications
    @POST("api/medications/create.php")
    suspend fun createMedication(@Body request: CreateMedicationRequest): Response<ApiResponse<Medication>>

    @GET("api/medications/get.php")
    suspend fun getMedication(@Query("medicationId") medicationId: Int): Response<ApiResponse<Medication>>

    @GET("api/medications/get.php")
    suspend fun listMedications(@Query("userId") userId: Int, @Query("limit") limit: Int? = null, @Query("offset") offset: Int? = null): Response<ApiResponse<List<Medication>>>

    @PUT("api/medications/update.php")
    suspend fun updateMedication(@Body fields: Map<String, Any?>): Response<ApiResponse<Medication>>

    @DELETE("api/medications/delete.php")
    suspend fun deleteMedication(@Query("medicationId") medicationId: Int): Response<ApiResponse<Medication>>

    //reminders
    @POST("api/reminders/create.php")
    suspend fun createReminder(@Body request: CreateReminderRequest): Response<ApiResponse<Reminder>>

    @GET("api/reminders/get.php")
    suspend fun getReminder(@Query("reminderId") reminderId: Int): Response<ApiResponse<Reminder>>

    @GET("api/reminders/get.php")
    suspend fun listReminders(@Query("userId") userId: Int, @Query("limit") limit: Int? = null, @Query("offset") offset: Int? = null): Response<ApiResponse<List<Reminder>>>

    @PUT("api/reminders/update.php")
    suspend fun updateReminder(@Body fields: Map<String, Any?>): Response<ApiResponse<Reminder>>

    @DELETE("api/reminders/delete.php")
    suspend fun deleteReminder(@Query("reminderId") reminderId: Int): Response<ApiResponse<Reminder>>

    //medication logs
    @POST("api/medication_logs/create.php")
    suspend fun createMedicationLog(@Body request: CreateMedicationLogRequest): Response<ApiResponse<MedicationLog>>

    @GET("api/medication_logs/get.php")
    suspend fun getMedicationLog(@Query("logId") logId: Int): Response<ApiResponse<MedicationLog>>

    @GET("api/medication_logs/get.php")
    suspend fun listMedicationLogs(@Query("userId") userId: Int, @Query("limit") limit: Int? = null, @Query("offset") offset: Int? = null): Response<ApiResponse<List<MedicationLog>>>

    @PUT("api/medication_logs/update.php")
    suspend fun updateMedicationLog(@Body fields: Map<String, Any?>): Response<ApiResponse<MedicationLog>>

    @DELETE("api/medication_logs/delete.php")
    suspend fun deleteMedicationLog(@Query("logId") logId: Int): Response<ApiResponse<MedicationLog>>

    //user badges(composite key: userId + badgeId)
    @POST("api/user_badges/create.php")
    suspend fun createUserBadge(@Body request: CreateUserBadgeRequest): Response<ApiResponse<UserBadge>>

    @GET("api/user_badges/get.php")
    suspend fun getUserBadge(@Query("userId") userId: Int, @Query("badgeId") badgeId: Int): Response<ApiResponse<UserBadge>>

    @GET("api/user_badges/get.php")
    suspend fun listUserBadges(@Query("userId") userId: Int, @Query("limit") limit: Int? = null, @Query("offset") offset: Int? = null): Response<ApiResponse<List<UserBadge>>>

    @PUT("api/user_badges/update.php")
    suspend fun updateUserBadge(@Body fields: Map<String, Any?>): Response<ApiResponse<UserBadge>>

    @DELETE("api/user_badges/delete.php")
    suspend fun deleteUserBadge(@Query("userId") userId: Int, @Query("badgeId") badgeId: Int): Response<ApiResponse<UserBadge>>

    //user rewards (primary key is also userId)
    @POST("api/user_rewards/create.php")
    suspend fun createUserRewards(@Body request: CreateUserRewardsRequest): Response<ApiResponse<UserRewards>>

    @GET("api/user_rewards/get.php")
    suspend fun getUserRewards(@Query("userId") userId: Int): Response<ApiResponse<UserRewards>>

    // For this table, userId also identifies a single record; list=1 forces array response.
    @GET("api/user_rewards/get.php")
    suspend fun listUserRewards(@Query("userId") userId: Int, @Query("list") list: Int = 1): Response<ApiResponse<List<UserRewards>>>

    @PUT("api/user_rewards/update.php")
    suspend fun updateUserRewards(@Body fields: Map<String, Any?>): Response<ApiResponse<UserRewards>>

    @DELETE("api/user_rewards/delete.php")
    suspend fun deleteUserRewards(@Query("userId") userId: Int): Response<ApiResponse<UserRewards>>

    //adherence reports
    @POST("api/adherence_reports/create.php")
    suspend fun createAdherenceReport(
        @Body request: CreateAdherenceReportRequest
    ): Response<ApiResponse<AdherenceReport>>

    @GET("api/adherence_reports/get.php")
    suspend fun getAdherenceReport(
        @Query("reportId") reportId: Int
    ): Response<ApiResponse<AdherenceReport>>

    @GET("api/adherence_reports/get.php")
    suspend fun getAdherenceReports(
        @Query("userId") userId: Int? = null
    ): Response<ApiResponse<List<AdherenceReport>>>

    @PUT("api/adherence_reports/update.php")
    suspend fun updateAdherenceReport(
        @Body request: UpdateAdherenceReportRequest
    ): Response<ApiResponse<AdherenceReport>>

    @DELETE("api/adherence_reports/delete.php")
    suspend fun deleteAdherenceReport(
        @Body request: DeleteAdherenceReportRequest
    ): Response<ApiResponse<Any>>

    //auth tokens , Karabo needs to double check this part
    @POST("api/auth_tokens/create.php")
    suspend fun createAuthToken(
        @Body request: CreateAuthTokenRequest
    ): Response<ApiResponse<AuthToken>>

    @GET("api/auth_tokens/get.php")
    suspend fun getAuthToken(
        @Query("tokenId") tokenId: Int
    ): Response<ApiResponse<AuthToken>>

    @GET("api/auth_tokens/get.php")
    suspend fun getAuthTokens(
        @Query("userId") userId: Int? = null
    ): Response<ApiResponse<List<AuthToken>>>

    @PUT("api/auth_tokens/update.php")
    suspend fun updateAuthToken(
        @Body request: UpdateAuthTokenRequest
    ): Response<ApiResponse<AuthToken>>

    @DELETE("api/auth_tokens/delete.php")
    suspend fun deleteAuthToken(
        @Body request: DeleteAuthTokenRequest
    ): Response<ApiResponse<Any>>

    //trusted contacts
    @POST("api/trusted_contacts/create.php")
    suspend fun createTrustedContact(
        @Body request: CreateTrustedContactRequest
    ): Response<ApiResponse<TrustedContact>>

    @GET("api/trusted_contacts/get.php")
    suspend fun getTrustedContact(
        @Query("contactId") contactId: Int
    ): Response<ApiResponse<TrustedContact>>

    @GET("api/trusted_contacts/get.php")
    suspend fun getTrustedContacts(
        @Query("userId") userId: Int? = null
    ): Response<ApiResponse<List<TrustedContact>>>

    @PUT("api/trusted_contacts/update.php")
    suspend fun updateTrustedContact(
        @Body request: UpdateTrustedContactRequest
    ): Response<ApiResponse<TrustedContact>>

    @DELETE("api/trusted_contacts/delete.php")
    suspend fun deleteTrustedContact(
        @Body request: DeleteTrustedContactRequest
    ): Response<ApiResponse<Any>>
}