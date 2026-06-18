package com.example.claudwecho.data

import com.example.claudwecho.data.api.NeteaseApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginRepository(private val api: NeteaseApi) {
    suspend fun getQrCodeData(): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val keyRes = api.getQrKey()
            if (keyRes.code == 200) {
                val key = keyRes.data.unikey
                val qrRes = api.createQr(key)
                if (qrRes.code == 200) {
                    return@withContext Pair(key, qrRes.data.qrimg)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkQrStatus(key: String): Int = withContext(Dispatchers.IO) {
        try {
            val res = api.checkQr(key)
            res.code // 800, 801, 802, 803
        } catch (e: Exception) {
            -1
        }
    }
    
    suspend fun loginWithPhone(phone: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val res = api.loginCellphonePassword(phone, password)
            res.code == 200
        } catch (e: Exception) {
            false
        }
    }
}
