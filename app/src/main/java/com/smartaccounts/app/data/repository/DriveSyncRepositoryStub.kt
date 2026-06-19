package com.smartaccounts.app.data.repository

class DriveSyncRepositoryStub : DriveSyncRepository {

    override fun isConfigured(): Boolean = false

    override suspend fun signIn(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Google Drive sync is not configured yet."))

    override suspend fun uploadBackup(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Google Drive sync is not configured yet."))

    override suspend fun restoreLatestBackup(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Google Drive sync is not configured yet."))
}
