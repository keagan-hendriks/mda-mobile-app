package com.example.mda_mobile_app.data.repository

import com.example.mda_mobile_app.data.model.common.ApiResponse
import com.example.mda_mobile_app.data.model.crud.*
import com.example.mda_mobile_app.data.remote.ApiService
import com.example.mda_mobile_app.data.remote.RetrofitClient
import retrofit2.Response

class MedicationRepository(private val api: ApiService = RetrofitClient.api) {
    private fun <T> unwrap(response: Response<ApiResponse<T>>): T {
        val body = response.body()
        if (!response.isSuccessful) {
            throw IllegalStateException(body?.message ?: "HTTP ${response.code()}: ${response.errorBody()?.string().orEmpty().take(160)}")
        }
        if (body?.success != true) throw IllegalStateException(body?.message ?: "Request failed")
        return body.data ?: throw IllegalStateException("API returned no data")
    }

    // The PHP handler responds with data:null for a successful DELETE.
    private fun <T> unwrapDelete(response: Response<ApiResponse<T>>) {
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            throw IllegalStateException(body?.message ?: "Delete failed (HTTP ${response.code()})")
        }
    }

    suspend fun medications(userId: Int): List<Medication> = unwrap(api.listMedications(userId))
    suspend fun logs(userId: Int): List<MedicationLog> = unwrap(api.listMedicationLogs(userId))

    suspend fun addMedication(userId: Int, name: String, notes: String?): Medication =
        unwrap(api.createMedication(CreateMedicationRequest(userId, name, notes)))

    suspend fun editMedication(id: Int, name: String, notes: String?, active: Boolean): Medication =
        unwrap(api.updateMedication(UpdateMedicationRequest(
            medicationId = id, medicationName = name, notes = notes, isActive = active
        )))

    suspend fun deleteMedication(id: Int) { unwrapDelete(api.deleteMedication(id)) }

    suspend fun addLog(medicationId: Int, scheduledAt: String, status: String, notes: String?): MedicationLog =
        unwrap(api.createMedicationLog(CreateMedicationLogRequest(
            medicationId = medicationId, scheduledAt = scheduledAt, status = status, notes = notes
        )))

    suspend fun editLog(id: Int, scheduledAt: String, status: String, notes: String?): MedicationLog =
        unwrap(api.updateMedicationLog(UpdateMedicationLogRequest(
            logId = id, scheduledAt = scheduledAt, status = status, notes = notes
        )))

    suspend fun deleteLog(id: Int) { unwrapDelete(api.deleteMedicationLog(id)) }
}
