package com.example.mda_mobile_app.data.model.crud

//to anyone reading the code I'm sorry about the poor naming was struggling with debugging
//this file contains the CRUD models for Adherence reports, AuthTokens and trusted contacts
//other file CrudModels.kt contains all the other CRUD models, users and preferences have their own
data class AdherenceReport(
    val reportId: Int,
    val userId: Int,
    val periodStart: String,
    val periodEnd: String,
    val totalScheduled: Int,
    val totalTaken: Int,
    val totalSkipped: Int,
    val totalMissed: Int,
    val adherencePercentage: Double,
    val generatedAt: String
)

data class CreateAdherenceReportRequest(
    val userId: Int,
    val periodStart: String,
    val periodEnd: String,
    val totalScheduled: Int,
    val totalTaken: Int,
    val totalSkipped: Int,
    val totalMissed: Int,
    val adherencePercentage: Double
)

data class UpdateAdherenceReportRequest(
    val reportId: Int,
    val userId: Int? = null,
    val periodStart: String? = null,
    val periodEnd: String? = null,
    val totalScheduled: Int? = null,
    val totalTaken: Int? = null,
    val totalSkipped: Int? = null,
    val totalMissed: Int? = null,
    val adherencePercentage: Double? = null
)

data class DeleteAdherenceReportRequest(val reportId: Int)

data class AuthToken(
    val tokenId: Int,
    val userId: Int,
    val tokenHash: String,
    val expiresAt: String,
    val createdAt: String
)

data class CreateAuthTokenRequest(
    val userId: Int,
    val tokenHash: String,
    val expiresAt: String
)

data class UpdateAuthTokenRequest(
    val tokenId: Int,
    val userId: Int? = null,
    val tokenHash: String? = null,
    val expiresAt: String? = null
)

data class DeleteAuthTokenRequest(val tokenId: Int)

data class TrustedContact(
    val contactId: Int,
    val userId: Int,
    val contactName: String,
    val contactEmail: String,
    val relationship: String?,
    val invitationStatus: String,
    val createdAt: String
)

data class CreateTrustedContactRequest(
    val userId: Int,
    val contactName: String,
    val contactEmail: String,
    val relationship: String?,
    val invitationStatus: String
)

data class UpdateTrustedContactRequest(
    val contactId: Int,
    val userId: Int? = null,
    val contactName: String? = null,
    val contactEmail: String? = null,
    val relationship: String? = null,
    val invitationStatus: String? = null
)

data class DeleteTrustedContactRequest(val contactId: Int)
