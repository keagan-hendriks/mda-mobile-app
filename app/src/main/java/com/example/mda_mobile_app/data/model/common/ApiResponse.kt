package com.example.mda_mobile_app.data.model.common

//generic format for api responses from PHP server, do not change
//make specific API endpoints using this class but replacing the gerneric Type
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)