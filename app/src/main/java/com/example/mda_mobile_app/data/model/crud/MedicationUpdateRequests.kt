package com.example.mda_mobile_app.data.model.crud

//Explicit dataclasses to avoid Retrofit rejecting wildcard types in Map<String, Any?> that come from JVM
//weirdest bug I've ever dealt with

data class UpdateMedicationRequest(
    val medicationId: Int,
    val medicationName: String,
    val notes: String?,
    val isActive: Boolean
)

data class UpdateMedicationLogRequest(
    val logId: Int,
    val scheduledAt: String,
    val status: String,
    val notes: String?
)
