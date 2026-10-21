package com.example.mda_mobile_app.data.model.preferences

//removed to do because can now call database
data class Preferences(
    val userId: Int,
    val notificationsEnabled: Boolean,
    val soundEnabled: Boolean,
    val trustedContactSharing: Boolean
)