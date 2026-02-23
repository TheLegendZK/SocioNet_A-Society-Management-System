package com.zhd.socionet.ui.resident.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhd.socionet.data.repository.SosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SosViewModel @Inject constructor(
    private val repository: SosRepository
) : ViewModel() {

    fun triggerEmergency(flatId: String, userId: String) {
        viewModelScope.launch {
            repository.triggerSos(userId, flatId, "General Emergency")
        }
    }
}
