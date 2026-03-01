package com.zhd.socionet.ui.resident.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.model.EmergencyAlert
import com.zhd.socionet.data.repository.AuthRepository
import com.zhd.socionet.data.repository.SosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SosViewModel @Inject constructor(
    private val repository: SosRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    fun triggerEmergency(flatId: String, userId: String) {
        viewModelScope.launch {
            // Denormalization: Get profile to store name and flat number in alert
            val profile = authRepository.getUserProfile(userId)
            
            val alert = EmergencyAlert(
                type = "SOS",
                subType = "General Emergency",
                raisedByUserId = userId,
                raisedByUserName = profile?.fullName,
                flatId = flatId,
                flatNumber = profile?.flatNumber,
                message = "EMERGENCY ALERT FROM ${profile?.flatNumber ?: flatId}"
            )
            
            repository.triggerSos(alert)
        }
    }
}
