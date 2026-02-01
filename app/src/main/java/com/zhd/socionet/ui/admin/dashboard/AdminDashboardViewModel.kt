package com.zhd.socionet.ui.admin.dashboard

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {
    // This property will hold the current user's ID
    val currentUserId: String?
        get() = auth.currentUser?.uid
}