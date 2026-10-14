package com.example.mda_mobile_app.data.model.preferences

data class UpdatePreferencesRequest(
    val notificationsEnabled: Boolean,
    val theme: String
)