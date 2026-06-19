package com.smartaccounts.app.data.repository

import android.net.Uri

interface BackupRepository {

    suspend fun exportTo(uri: Uri): Result<Unit>

    suspend fun restoreFrom(uri: Uri): Result<Unit>
}
