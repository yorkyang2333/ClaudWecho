package com.example.claudwecho.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface NeteaseApi {
    @GET("/login/qr/key")
    suspend fun getQrKey(@Query("timestamp") timestamp: Long = System.currentTimeMillis()): QrKeyResponse

    @GET("/login/qr/create")
    suspend fun createQr(
        @Query("key") key: String,
        @Query("qrimg") qrimg: Boolean = true,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): QrCreateResponse

    @GET("/login/qr/check")
    suspend fun checkQr(
        @Query("key") key: String,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): QrCheckResponse

    @GET("/login/cellphone")
    suspend fun loginCellphonePassword(
        @Query("phone") phone: String,
        @Query("password") password: String,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): LoginResponse

    @GET("/login/cellphone")
    suspend fun loginCellphoneCaptcha(
        @Query("phone") phone: String,
        @Query("captcha") captcha: String,
        @Query("timestamp") timestamp: Long = System.currentTimeMillis()
    ): LoginResponse
}

@Serializable
data class QrKeyResponse(val data: QrKeyData, val code: Int)

@Serializable
data class QrKeyData(val unikey: String, val code: Int)

@Serializable
data class QrCreateResponse(val data: QrCreateData, val code: Int)

@Serializable
data class QrCreateData(val qrurl: String, val qrimg: String)

@Serializable
data class QrCheckResponse(val code: Int, val message: String? = null, val cookie: String? = null)

@Serializable
data class LoginResponse(val code: Int, val message: String? = null, val cookie: String? = null)
