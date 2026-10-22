package com.example.mda_mobile_app.data.model.users

data class LoginData(
    val token: String,
    val expiresIn: Int,
    val user: User
)