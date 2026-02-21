package com.zhd.socionet.ui.directory

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zhd.socionet.R
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyDirectoryScreen(
    viewModel: SocietyDirectoryViewModel = hiltViewModel(),
    isAdmin: Boolean,
    societyId: String,
    flatId: String,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var userToRemove by remember { mutableStateOf<UserProfile?>(null) }
    var staffToRemove by remember { mutableStateOf<ServiceStaff?>(null) }
    var showAddStaffDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Residents", "Guards", "Staff")

    LaunchedEffect(societyId, flatId) {
        viewModel.setFlatId(flatId)
        if (societyId.isNotBlank()) {
            viewModel.loadDirectory(societyId)
        }
    }

    if (userToRemove != null) {
        AlertDialog(
            onDismissRequest = { userToRemove = null },
            title = { Text("Remove ${userToRemove?.fullName}?") },
            text = { Text("Are you sure you want to remove this member from the society?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeUser(userToRemove!!.id, societyId)
                        userToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { userToRemove = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = SoftTheme.surface,
            titleContentColor = SoftTheme.textPrimary,
            textContentColor = SoftTheme.textSecondary
        )
    }

    if (staffToRemove != null) {
        AlertDialog(
            onDismissRequest = { staffToRemove = null },
            title = { Text("Remove ${staffToRemove?.name}?") },
            text = { Text("Are you sure you want to remove this staff member from the society?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeStaff(staffToRemove!!.id, societyId)
                        staffToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { staffToRemove = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = SoftTheme.surface,
            titleContentColor = SoftTheme.textPrimary,
            textContentColor = SoftTheme.textSecondary
        )
    }

    if (showAddStaffDialog) {
        AddStaffDialog(
            onDismiss = { showAddStaffDialog = false },
            onAdd = { name, phone, role ->
                viewModel.addStaffMember(societyId, name, phone, role)
                showAddStaffDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(SoftTheme.bg)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.society_directory), fontWeight = FontWeight.Bold, color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                    PrimaryTabRow(
                        selectedTabIndex = uiState.selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        indicator = { 
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(uiState.selectedTab),
                                color = Color.White,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = uiState.selectedTab == index,
                                onClick = { viewModel.onTabSelected(index) },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                selectedContentColor = Color.White,
                                unselectedContentColor = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (isAdmin && uiState.selectedTab == 2) {
                    FloatingActionButton(
                        onClick = { showAddStaffDialog = true },
                        containerColor = SoftTheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Staff")
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SocioTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        label = "Search by Name or Role",
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = SoftTheme.primary) }
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SoftTheme.primary)
                    }
                } else if (uiState.selectedTab == 2 && uiState.filteredStaff.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.People, null, tint = SoftTheme.textSecondary.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No staff members found", color = SoftTheme.textSecondary)
                        }
                    }
                } else if (uiState.selectedTab != 2 && uiState.filteredResidents.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.People, null, tint = SoftTheme.textSecondary.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No members found", color = SoftTheme.textSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (uiState.selectedTab == 2) {
                            items(uiState.filteredStaff) { staff ->
                                val isLinked = staff.employerFlatIds.contains(flatId)
                                DirectoryStaffItem(
                                    staff = staff,
                                    showContactInfo = isAdmin || isLinked,
                                    isAdmin = isAdmin,
                                    isLinkedToMyFlat = isLinked,
                                    flatId = flatId,
                                    onCall = {
                                        val intent = Intent(Intent.ACTION_DIAL,
                                            "tel:${staff.phoneNumber}".toUri())
                                        context.startActivity(intent)
                                    },
                                    onRemoveFromSociety = {
                                        staffToRemove = staff
                                    },
                                    onToggleLink = {
                                        viewModel.toggleStaffLink(staff, flatId, societyId)
                                    }
                                )
                            }
                        } else {
                            items(uiState.filteredResidents) { user ->
                                DirectoryUserItem(
                                    user = user,
                                    showContactInfo = isAdmin,
                                    isAdmin = isAdmin,
                                    onCall = {
                                        val intent = Intent(Intent.ACTION_DIAL,
                                            "tel:${user.phoneNumber}".toUri())
                                        context.startActivity(intent)
                                    },
                                    onEmail = {
                                        if (user.email.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_SENDTO,
                                                "mailto:${user.email}".toUri())
                                            context.startActivity(intent)
                                        }
                                    },
                                    onRemove = {
                                        userToRemove = user
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddStaffDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, role: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Society Staff", fontWeight = FontWeight.Bold, color = SoftTheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SocioTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = SoftTheme.primary) }
                )
                SocioTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = SoftTheme.primary) }
                )
                SocioTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = "Role (e.g. Maid, Plumber)",
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = SoftTheme.primary) }
                )
                Text(
                    text = "Note: Staff members added here do not have app access.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.textSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, phone, role) },
                enabled = name.isNotBlank() && phone.isNotBlank() && role.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.primary)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SoftTheme.textSecondary)
            }
        },
        containerColor = SoftTheme.surface,
        titleContentColor = SoftTheme.textPrimary
    )
}

@Composable
fun DirectoryUserItem(
    user: UserProfile,
    showContactInfo: Boolean,
    isAdmin: Boolean,
    onCall: () -> Unit,
    onEmail: () -> Unit,
    onRemove: () -> Unit,
) {
    val roleName = user.roles.firstOrNull()?.name ?: "Resident"
    val displayRole = when (roleName) {
        "OWNER" -> "Owner"
        "TENANT" -> "Tenant"
        "FAMILY_MEMBER" -> "Family Member"
        "SECURITY_GUARD" -> "Security Guard"
        "SERVICE_STAFF" -> "Staff"
        else -> roleName
    }

    SoftCard {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                shape = CircleShape,
                color = SoftTheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.fullName.take(1),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.primary
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = SoftTheme.textSecondary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = displayRole,
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftTheme.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = if (user.assignedFlatId?.endsWith("_GUARD") == true || user.assignedFlatId?.endsWith("_STAFF") == true) "Society Employee" else "Flat ${user.assignedFlatId?.replace("_", " ")?.split(" ")?.last() ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(12.dp))

                if (showContactInfo) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Phone, null, tint = Color(0xFF4CAF50))
                        }
                        Text(
                            text = user.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.textSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    if (user.email.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(onClick = onEmail, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Email, null, tint = Color(0xFF2196F3))
                            }
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftTheme.textSecondary,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = SoftTheme.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Contact details hidden for privacy",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.textSecondary.copy(alpha = 0.5f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            if (isAdmin) {
                IconButton(onClick = onRemove, modifier = Modifier.padding(start = 8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun DirectoryStaffItem(
    staff: ServiceStaff,
    showContactInfo: Boolean,
    isAdmin: Boolean,
    isLinkedToMyFlat: Boolean,
    flatId: String,
    onCall: () -> Unit,
    onRemoveFromSociety: () -> Unit,
    onToggleLink: () -> Unit,
) {
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = if (isLinkedToMyFlat) MaterialTheme.colorScheme.primaryContainer else SoftTheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = staff.name.take(1),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.primary
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = staff.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = SoftTheme.textSecondary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Staff",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftTheme.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = staff.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(12.dp))

                if (showContactInfo) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Phone, null, tint = Color(0xFF4CAF50))
                        }
                        Text(
                            text = staff.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.textSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = SoftTheme.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Contact details hidden for privacy",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.textSecondary.copy(alpha = 0.5f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            if (isAdmin) {
                IconButton(onClick = onRemoveFromSociety, modifier = Modifier.padding(start = 8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            } else if (flatId.isNotBlank()) {
                Button(
                    onClick = onToggleLink,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLinkedToMyFlat) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) else SoftTheme.primary.copy(alpha = 0.1f),
                        contentColor = if (isLinkedToMyFlat) MaterialTheme.colorScheme.error else SoftTheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (isLinkedToMyFlat) stringResource(R.string.remove) else stringResource(R.string.add),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
