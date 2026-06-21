package com.smartaccounts.app.data.repository

interface DriveSyncRepository {

    fun isConfigured(): Boolean

    suspend fun signIn(): Result<Unit>

    suspend fun uploadBackup(): Result<Unit>

    suspend fun restoreLatestBackup(): Result<Unit>
}
