package com.example.mda_mobile_app.data.model.users

data class UpdateUserRequest(
    val userId: Int,
    val fullName: String,
    val email: String
)