package com.example.claudwecho.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface NeteaseApi {
    @GET("/login/qr/key")
    suspend fun getQrKey(@Query("timestamp") timestamp: Long = System.currentTimeMillis()): QrKeyResponse
}

@Serializable
data class QrKeyResponse(
    val data: QrKeyData,
    val code: Int
)

@Serializable
data class QrKeyData(
    val unikey: String,
    val code: Int
)
