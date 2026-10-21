package com.example.mda_mobile_app.data.model.users

data class UpdatePasswordRequest(
    val userId: Int,
    val currentPassword: String,
    val newPassword: String
)