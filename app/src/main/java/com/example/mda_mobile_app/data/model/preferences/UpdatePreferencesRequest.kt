package com.example.mda_mobile_app.data.model.preferences

data class UpdatePreferencesRequest(
    val userId: Int,
    val notificationsEnabled: Boolean,
    val soundEnabled: Boolean,
    val trustedContactSharing: Boolean
)