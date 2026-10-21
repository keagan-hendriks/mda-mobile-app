package com.example.mda_mobile_app.data.model.users

data class CreateUserRequest(
    val fullName: String,
    val email: String,
    val password: String
)