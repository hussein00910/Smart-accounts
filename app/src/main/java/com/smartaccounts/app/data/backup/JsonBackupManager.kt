package com.smartaccounts.app.data.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonBackupManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun serialize(payload: BackupPayload): String = json.encodeToString(payload)

    fun deserialize(content: String): BackupPayload = json.decodeFromString(content)

    suspend fun writeToUri(context: Context, uri: Uri, payload: BackupPayload) =
        withContext(Dispatchers.IO) {
            val content = serialize(payload)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            } ?: error("Unable to open output stream for $uri")
        }

    suspend fun readFromUri(context: Context, uri: Uri): BackupPayload =
        withContext(Dispatchers.IO) {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            } ?: error("Unable to open input stream for $uri")
            deserialize(content)
        }
}
