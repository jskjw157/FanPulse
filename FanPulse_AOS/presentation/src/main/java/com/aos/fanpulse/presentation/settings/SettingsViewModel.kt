package com.aos.fanpulse.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aos.fanpulse.domain.repository.SettingRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingRepository: SettingRepository
): ViewModel(){
    val isNotificationEnabled: StateFlow<Boolean> = settingRepository.isNotificationEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun toggleNotification(isChecked: Boolean) {
        viewModelScope.launch {
            settingRepository.setNotificationEnabled(isChecked)

            if (isChecked) {
                FirebaseMessaging.getInstance().subscribeToTopic("fanpulse_charts")
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("fanpulse_charts")
            }
        }
    }
}