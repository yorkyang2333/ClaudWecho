package com.yorkyang2333.claudwecho.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorkyang2333.claudwecho.data.LoginRepository
import com.yorkyang2333.claudwecho.data.MainRepository
import com.yorkyang2333.claudwecho.data.api.PersistentCookieJar
import com.yorkyang2333.claudwecho.data.api.UserProfile
import com.yorkyang2333.claudwecho.data.api.VipInfoData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val mainRepository: MainRepository,
    private val loginRepository: LoginRepository,
    private val cookieJar: PersistentCookieJar
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _vipInfo = MutableStateFlow<VipInfoData?>(null)
    val vipInfo: StateFlow<VipInfoData?> = _vipInfo.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            // Should be instantly returned from cache
            val profile = mainRepository.getLoginStatus(forceRefresh = false)
            _userProfile.value = profile
            
            if (profile != null) {
                // Fetch VIP info
                _vipInfo.value = mainRepository.getVipInfo()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            loginRepository.logout()
            cookieJar.clear()
            _logoutEvent.emit(Unit)
        }
    }
}
