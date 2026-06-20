package com.yorkyang2333.claudwecho.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorkyang2333.claudwecho.data.LoginRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class LoginState {
    IDLE,
    LOADING_QR,
    QR_READY,
    PHONE_INPUT,
    LOGGING_IN,
    LOGGED_IN,
    ERROR
}

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState.IDLE)
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    private val _qrCodeBase64 = MutableStateFlow<String?>(null)
    val qrCodeBase64: StateFlow<String?> = _qrCodeBase64.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var qrCheckJob: Job? = null
    private var unikey: String? = null

    fun loadQrCode() {
        _uiState.value = LoginState.LOADING_QR
        viewModelScope.launch {
            val result = repository.getQrCodeData()
            if (result != null) {
                unikey = result.first
                _qrCodeBase64.value = result.second
                _uiState.value = LoginState.QR_READY
                startQrCheck()
            } else {
                _errorMessage.value = "Failed to load QR code"
                _uiState.value = LoginState.ERROR
            }
        }
    }

    private fun startQrCheck() {
        qrCheckJob?.cancel()
        qrCheckJob = viewModelScope.launch {
            while (isActive && unikey != null) {
                val status = repository.checkQrStatus(unikey!!)
                when (status) {
                    800 -> {
                        // Expired
                        loadQrCode()
                        break
                    }
                    803 -> {
                        // Success
                        _uiState.value = LoginState.LOGGED_IN
                        break
                    }
                    // 801 = Waiting, 802 = Waiting for confirmation
                }
                delay(2000) // Poll every 2 seconds
            }
        }
    }

    fun loginWithPhone(phone: String, pass: String) {
        _uiState.value = LoginState.LOGGING_IN
        viewModelScope.launch {
            val success = repository.loginWithPhone(phone, pass)
            if (success) {
                _uiState.value = LoginState.LOGGED_IN
            } else {
                _errorMessage.value = "Login failed"
                _uiState.value = LoginState.ERROR
            }
        }
    }

    fun sendCaptcha(phone: String, onSent: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.sendCaptcha(phone)
            onSent(success)
        }
    }

    fun loginWithCaptcha(phone: String, captcha: String) {
        _uiState.value = LoginState.LOGGING_IN
        viewModelScope.launch {
            val success = repository.loginWithCaptcha(phone, captcha)
            if (success) {
                _uiState.value = LoginState.LOGGED_IN
            } else {
                _errorMessage.value = "Login failed"
                _uiState.value = LoginState.ERROR
            }
        }
    }
    
    fun setPhoneInputState() {
        _uiState.value = LoginState.PHONE_INPUT
    }

    fun resetState() {
        _uiState.value = LoginState.IDLE
        _qrCodeBase64.value = null
        _errorMessage.value = null
        qrCheckJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        qrCheckJob?.cancel()
    }
}
