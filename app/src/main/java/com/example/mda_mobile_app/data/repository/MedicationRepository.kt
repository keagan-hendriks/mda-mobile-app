
package com.example.mda_mobile_app.data.repository

import com.example.mda_mobile_app.data.model.common.ApiResponse
import com.example.mda_mobile_app.data.model.crud.*
import com.example.mda_mobile_app.data.remote.ApiService
import com.example.mda_mobile_app.data.remote.RetrofitClient
import retrofit2.Response

class MedicationRepository(
    private val api: ApiService = RetrofitClient.api
) {

    private fun <T> unwrap(response: Response<ApiResponse<T>>): T {
        val body = response.body()

        if (!response.isSuccessful) {
            throw IllegalStateException(
                body?.message
                    ?: "HTTP ${response.code()}: ${response.errorBody()?.string().orEmpty().take(160)}"
            )
        }

        if (body?.success != true) {
            throw IllegalStateException(body?.message ?: "Request failed")
        }

        return body.data
            ?: throw IllegalStateException("API returned no data")
    }

    // The PHP handler returns data:null for a successful DELETE.
    private fun <T> unwrapDelete(response: Response<ApiResponse<T>>) {
        val body = response.body()

        if (!response.isSuccessful || body?.success != true) {
            throw IllegalStateException(
                body?.message ?: "Delete failed (HTTP ${response.code()})"
            )
        }
    }

    suspend fun medications(userId: Int): List<Medication> =
        unwrap(api.listMedications(userId))

    suspend fun reminders(userId: Int): List<Reminder> =
        unwrap(api.listReminders(userId))

    // Retrieve every medication log, not just the first 100.
    suspend fun logs(userId: Int): List<MedicationLog> {
        val allLogs = mutableListOf<MedicationLog>()
        val pageSize = 100
        var offset = 0

        while (true) {
            val page = unwrap(
                api.listMedicationLogs(
                    userId = userId,
                    limit = pageSize,
                    offset = offset
                )
            )

            allLogs.addAll(page)

            // A short page means there are no more records.
            if (page.size < pageSize) {
                break
            }

            offset += pageSize
        }

        return allLogs
    }

    // REWARDS

    suspend fun userRewards(userId: Int): UserRewards? =
        unwrap(api.listUserRewards(userId)).firstOrNull()

    suspend fun createUserRewards(userId: Int): UserRewards =
        unwrap(
            api.createUserRewards(
                CreateUserRewardsRequest(userId = userId)
            )
        )

    suspend fun updateUserRewards(
        userId: Int,
        totalPoints: Int,
        currentStreak: Int,
        longestStreak: Int
    ): UserRewards =
        unwrap(
            api.updateUserRewards(
                mapOf(
                    "userId" to userId,
                    "totalPoints" to totalPoints,
                    "currentStreak" to currentStreak,
                    "longestStreak" to longestStreak
                )
            )
        )

    // MEDICATIONS

    suspend fun addMedication(
        userId: Int,
        name: String,
        notes: String?
    ): Medication =
        unwrap(
            api.createMedication(
                CreateMedicationRequest(userId, name, notes)
            )
        )

    suspend fun editMedication(
        id: Int,
        name: String,
        notes: String?,
        active: Boolean
    ): Medication =
        unwrap(
            api.updateMedication(
                UpdateMedicationRequest(
                    medicationId = id,
                    medicationName = name,
                    notes = notes,
                    isActive = active
                )
            )
        )

    suspend fun deleteMedication(id: Int) {
        unwrapDelete(api.deleteMedication(id))
    }

    // MEDICATION LOGS

    suspend fun addLog(
        medicationId: Int,
        scheduledAt: String,
        status: String,
        notes: String?
    ): MedicationLog =
        unwrap(
            api.createMedicationLog(
                CreateMedicationLogRequest(
                    medicationId = medicationId,
                    scheduledAt = scheduledAt,
                    status = status,
                    notes = notes
                )
            )
        )

    suspend fun editLog(
        id: Int,
        scheduledAt: String,
        status: String,
        notes: String?
    ): MedicationLog =
        unwrap(
            api.updateMedicationLog(
                UpdateMedicationLogRequest(
                    logId = id,
                    scheduledAt = scheduledAt,
                    status = status,
                    notes = notes
                )
            )
        )

    suspend fun deleteLog(id: Int) {
        unwrapDelete(api.deleteMedicationLog(id))
    }
}
