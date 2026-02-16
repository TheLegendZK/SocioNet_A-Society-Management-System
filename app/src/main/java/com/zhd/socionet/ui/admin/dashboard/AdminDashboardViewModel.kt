package com.zhd.socionet.ui.admin.dashboard

import androidx.lifecycle.ViewModel
import com.zhd.socionet.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    val currentUserId: String?
        get() = repository.getCurrentUserId()
}