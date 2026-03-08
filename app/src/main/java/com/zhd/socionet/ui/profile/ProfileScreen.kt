package com.zhd.socionet.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.zhd.socionet.R
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.ui.common.LocalNavPadding
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SocioPasswordField
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    userId: String,
    currentViewType: String = "resident",
    onSwitchRole: (String) -> Unit = {},
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val navPadding = LocalNavPadding.current
    val layoutDirection = LocalLayoutDirection.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.uploadProfilePicture(it) }
        }
    )

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) viewModel.loadProfile(userId)
    }

    if (uiState.successMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text("Success", fontWeight = FontWeight.Bold) },
            text = { Text(uiState.successMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessages() }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SoftTheme.surface,
            shape = MaterialTheme.shapes.large
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout_confirmation), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.are_you_sure_you_want_to_logout)) },
            confirmButton = {
                TextButton(onClick = { 
                    showLogoutDialog = false
                    viewModel.logout(onLogout) 
                }) {
                    Text(stringResource(R.string.yes_logout), fontWeight = FontWeight.Bold, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = SoftTheme.surface,
            shape = MaterialTheme.shapes.large
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftTheme.bg)
    ) {
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
                    title = {
                        Text(
                            "My Profile",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showLogoutDialog = true }) {
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
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = 24.dp + padding.calculateStartPadding(layoutDirection) + navPadding.calculateStartPadding(layoutDirection),
                                top = padding.calculateTopPadding() + navPadding.calculateTopPadding(),
                                end = 24.dp + padding.calculateEndPadding(layoutDirection) + navPadding.calculateEndPadding(layoutDirection),
                                bottom = 32.dp + padding.calculateBottomPadding() + navPadding.calculateBottomPadding()
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Avatar
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    Color.White.copy(alpha = 0.5f)
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (user.profilePhotoUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = user.profilePhotoUrl,
                                            contentDescription = stringResource(R.string.profile_picture),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = user.fullName.take(1).uppercase(),
                                            style = MaterialTheme.typography.displayMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            // Edit Icon Overlay
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = SoftTheme.primary,
                                tonalElevation = 4.dp
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = stringResource(R.string.change_photo),
                                    tint = Color.White,
                                    modifier = Modifier.padding(6.dp)
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

                        val isSuperAdmin = user.roles.contains(UserRole.SUPER_ADMIN)
                        val isOwner =
                            user.roles.contains(UserRole.OWNER) || user.roles.contains(UserRole.TENANT)

                        if (isSuperAdmin && isOwner) {
                            SoftCard(
                                backgroundColor = SoftTheme.primary.copy(alpha = 0.05f),
                                onClick = {
                                    if (currentViewType == "admin") onSwitchRole("resident") else onSwitchRole(
                                        "admin"
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (currentViewType == "admin") Icons.Default.Home else Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = SoftTheme.primary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = if (currentViewType == "admin") stringResource(R.string.switch_to_resident_view, uiState.displayFlatId ?: "") else stringResource(R.string.switch_to_admin_console),
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.primary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Icon(Icons.Default.SwapHoriz, null, tint = SoftTheme.primary)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        // Details Card
                        SoftCard {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                ProfileInfoRow(Icons.Default.Person, stringResource(R.string.full_name), user.fullName)
                                HorizontalDivider(color = SoftTheme.textSecondary.copy(alpha = 0.1f))
                                ProfileInfoRow(
                                    Icons.Default.Phone,
                                    stringResource(R.string.phone_number),
                                    user.phoneNumber
                                )
                                HorizontalDivider(color = SoftTheme.textSecondary.copy(alpha = 0.1f))

                                // Wing and Flat Info
                                val isStaffOrGuard = user.roles.contains(UserRole.SECURITY_GUARD) || user.roles.contains(UserRole.SERVICE_STAFF)
                                if (!isStaffOrGuard) {
                                    val flatNo = user.flatNumber ?: "Not Assigned"
                                    val wing = user.wingId ?: "N/A"

                                    ProfileInfoRow(
                                        Icons.Default.LocationCity,
                                        stringResource(R.string.wing_block),
                                        stringResource(R.string.wing, wing)
                                    )
                                    HorizontalDivider(color = SoftTheme.textSecondary.copy(alpha = 0.1f))
                                    ProfileInfoRow(
                                        Icons.Default.MeetingRoom,
                                        stringResource(R.string.flat_number),
                                        stringResource(R.string.flat, flatNo)
                                    )
                                } else {
                                    ProfileInfoRow(
                                        Icons.Default.Badge,
                                        "Role Status",
                                        "Society Employee"
                                    )
                                }

                                // Society Info
                                (user.societyName ?: uiState.society?.name)?.let {
                                    HorizontalDivider(color = SoftTheme.textSecondary.copy(alpha = 0.1f))
                                    ProfileInfoRow(
                                        Icons.Default.Apartment,
                                        "Society",
                                        it
                                    )
                                }

                                uiState.society?.let { society ->
                                    if (society.address.isNotEmpty()) {
                                        HorizontalDivider(color = SoftTheme.textSecondary.copy(alpha = 0.1f))
                                        ProfileInfoRow(
                                            Icons.Default.LocationOn,
                                            "Address",
                                            society.address
                                        )
                                    }
                                    if (society.registrationNumber.isNotEmpty()) {
                                        HorizontalDivider(color = SoftTheme.textSecondary.copy(alpha = 0.1f))
                                        ProfileInfoRow(
                                            Icons.Default.Badge,
                                            "Registration No.",
                                            society.registrationNumber
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // Actions
                        SocioButton(
                            text = stringResource(R.string.edit_profile_info),
                            onClick = { showEditDialog = true }
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { showPasswordDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                SoftTheme.primary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.change_password),
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.primary
                            )
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
            title = { Text(stringResource(R.string.edit_profile), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SocioTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Name",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(
                                FocusDirection.Down
                            )
                        })
                    )
                    SocioTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = stringResource(R.string.phone),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
            },
            confirmButton = {
                SocioButton(
                    text = "Save Changes",
                    onClick = { viewModel.updateProfile(name, phone); showEditDialog = false },
                    modifier = Modifier
                        .width(140.dp)
                        .height(44.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.cancel), color = SoftTheme.textSecondary)
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
            title = { Text(stringResource(R.string.update_password), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SocioPasswordField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = stringResource(R.string.current_password),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(
                                FocusDirection.Down
                            )
                        })
                    )
                    SocioPasswordField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = stringResource(R.string.new_password),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
            },
            confirmButton = {
                SocioButton(
                    text = "Update",
                    onClick = {
                        if (oldPass.isNotEmpty() && newPass.isNotEmpty()) {
                            viewModel.changePassword(oldPass, newPass); showPasswordDialog = false
                        }
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(44.dp)
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
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
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
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = SoftTheme.textSecondary
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                color = SoftTheme.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}