package com.zhd.socionet.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    userId: String,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState = viewModel.uiState
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) viewModel.loadProfile(userId)
    }

    if (uiState.successMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text("Success", fontWeight = FontWeight.Bold) },
            text = { Text(uiState.successMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessages() }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SoftTheme.surface,
            shape = MaterialTheme.shapes.large
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(SoftTheme.bg)) {
        // Gradient Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SoftTheme.primary,
                            SoftTheme.primary.copy(alpha = 0.7f),
                            SoftTheme.bg
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("My Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.logout(onLogout) }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color.Red)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                val user = uiState.userProfile
                if (user != null) {
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Avatar
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user.fullName.take(1).uppercase(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(Modifier.height(16.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = user.roles.joinToString(", ") { it.name },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        // Details Card
                        SoftCard {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                ProfileInfoRow(Icons.Default.Person, "Full Name", user.fullName)
                                HorizontalDivider(color = SoftTheme.textSecondary.copy(alpha = 0.1f))
                                ProfileInfoRow(Icons.Default.Phone, "Phone Number", user.phoneNumber)
                                HorizontalDivider(color = SoftTheme.textSecondary.copy(alpha = 0.1f))

                                val flatNo = user.assignedFlatId?.split("_")?.last() ?: "Not Assigned"
                                val wing = user.assignedFlatId?.split("_")?.getOrNull(1) ?: "N/A"

                                ProfileInfoRow(Icons.Default.LocationCity, "Wing / Block", "Wing $wing")
                                HorizontalDivider(color = SoftTheme.textSecondary.copy(alpha = 0.1f))
                                ProfileInfoRow(Icons.Default.MeetingRoom, "Flat Number", "Flat $flatNo")
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // Actions
                        SocioButton(
                            text = "Edit Profile Info",
                            onClick = { showEditDialog = true }
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { showPasswordDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SoftTheme.primary)
                        ) {
                            Text("Change Password", fontWeight = FontWeight.Bold, color = SoftTheme.primary)
                        }
                        
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        var name by remember { mutableStateOf(uiState.userProfile?.fullName ?: "") }
        var phone by remember { mutableStateOf(uiState.userProfile?.phoneNumber ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SocioTextField(value = name, onValueChange = { name = it }, label = "Name")
                    SocioTextField(value = phone, onValueChange = { phone = it }, label = "Phone")
                }
            },
            confirmButton = {
                SocioButton(
                    text = "Save Changes",
                    onClick = { viewModel.updateProfile(name, phone); showEditDialog = false },
                    modifier = Modifier.width(140.dp).height(44.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = SoftTheme.textSecondary)
                }
            },
            containerColor = SoftTheme.surface,
            shape = MaterialTheme.shapes.large
        )
    }

    if (showPasswordDialog) {
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Update Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SocioTextField(value = oldPass, onValueChange = { oldPass = it }, label = "Current Password", visualTransformation = PasswordVisualTransformation())
                    SocioTextField(value = newPass, onValueChange = { newPass = it }, label = "New Password", visualTransformation = PasswordVisualTransformation())
                }
            },
            confirmButton = {
                SocioButton(
                    text = "Update",
                    onClick = { if(oldPass.isNotEmpty() && newPass.isNotEmpty()) { viewModel.changePassword(oldPass, newPass); showPasswordDialog = false } },
                    modifier = Modifier.width(100.dp).height(44.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel", color = SoftTheme.textSecondary)
                }
            },
            containerColor = SoftTheme.surface,
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = SoftTheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = SoftTheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SoftTheme.textSecondary)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = SoftTheme.textPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
