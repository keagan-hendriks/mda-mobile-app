package com.example.mda_mobile_app.data.model

//data class to match PHP JSON output
data class HealthResponse(
    val success: Boolean,
    val message: String,
    val timestamp: String,
    val responseCode: Int
)