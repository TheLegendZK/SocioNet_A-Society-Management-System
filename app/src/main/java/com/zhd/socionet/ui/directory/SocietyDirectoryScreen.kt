package com.zhd.socionet.ui.directory

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.zhd.socionet.R
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.ui.common.LocalNavPadding
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    var userToRemove by remember { mutableStateOf<UserProfile?>(null) }
    var staffToRemove by remember { mutableStateOf<ServiceStaff?>(null) }
    var showAddStaffDialog by remember { mutableStateOf(false) }
    val navPadding = LocalNavPadding.current
    val layoutDirection = LocalLayoutDirection.current

    val tabs = listOf("Residents", "Guards", "Staff")
    val pagerState = rememberPagerState { tabs.size }

    LaunchedEffect(societyId, flatId) {
        viewModel.setFlatId(flatId)
        if (societyId.isNotBlank()) {
            viewModel.loadDirectory(societyId)
        }
    }

    // Sync pager with ViewModel tab selection (for initial or programmatic changes)
    LaunchedEffect(uiState.selectedTab) {
        if (uiState.selectedTab != pagerState.currentPage) {
            pagerState.animateScrollToPage(uiState.selectedTab)
        }
    }

    // Sync ViewModel with pager swiping
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.onTabSelected(page)
        }
    }

    if (userToRemove != null) {
        AlertDialog(
            onDismissRequest = { userToRemove = null },
            title = { Text("Remove ${userToRemove?.fullName}?") },
            text = { Text(stringResource(R.string.are_you_sure_you_want_to_remove_this_member_from_the_society)) },
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
            text = { Text(stringResource(R.string.are_you_sure_you_want_to_remove_this_staff_member_from_the_society)) },
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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(SoftTheme.bg)) {
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
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        indicator = { 
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                                color = Color.White,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { 
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Medium
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
                if (isAdmin && pagerState.currentPage == 2) {
                    FloatingActionButton(
                        onClick = { showAddStaffDialog = true },
                        containerColor = SoftTheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.padding(bottom = navPadding.calculateBottomPadding())
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_staff))
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.padding(
                    start = 24.dp + padding.calculateStartPadding(layoutDirection) + navPadding.calculateStartPadding(layoutDirection),
                    top = padding.calculateTopPadding() + navPadding.calculateTopPadding(),
                    end = 24.dp + padding.calculateEndPadding(layoutDirection) + navPadding.calculateEndPadding(layoutDirection),
                    bottom = 0.dp
                )) {
                    SocioTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        label = stringResource(R.string.search_by_name_or_role),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = SoftTheme.primary) }
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SoftTheme.primary)
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1,
                        verticalAlignment = Alignment.Top
                    ) { pageIndex ->
                        val filteredResidents = remember(uiState.residents, uiState.searchQuery, pageIndex) {
                            if (pageIndex == 2) emptyList() else {
                                val roleFiltered = when (pageIndex) {
                                    1 -> uiState.residents.filter { it.roles.contains(UserRole.SECURITY_GUARD) }
                                    else -> uiState.residents.filter { !it.roles.contains(UserRole.SECURITY_GUARD) && !it.roles.contains(UserRole.SERVICE_STAFF) }
                                }
                                if (uiState.searchQuery.isBlank()) roleFiltered
                                else roleFiltered.filter {
                                    it.fullName.contains(uiState.searchQuery, ignoreCase = true) ||
                                            (it.flatId ?: "").contains(uiState.searchQuery, ignoreCase = true) ||
                                            (it.wingId ?: "").contains(uiState.searchQuery, ignoreCase = true) ||
                                            (it.flatNumber ?: "").contains(uiState.searchQuery, ignoreCase = true)
                                }
                            }
                        }

                        val filteredStaff = remember(uiState.serviceStaff, uiState.searchQuery, pageIndex) {
                            if (pageIndex != 2) emptyList() else {
                                val activeStaff = uiState.serviceStaff.filter { it.isActive }
                                val searched = if (uiState.searchQuery.isBlank()) activeStaff
                                else activeStaff.filter {
                                    it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                                            it.role.contains(uiState.searchQuery, ignoreCase = true)
                                }
                                searched.sortedByDescending { it.employerFlatIds.contains(uiState.currentFlatId) }
                            }
                        }

                        if (pageIndex == 2 && filteredStaff.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.People, null, tint = SoftTheme.textSecondary.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                                    Spacer(Modifier.height(16.dp))
                                    Text(stringResource(R.string.no_staff_members_found), color = SoftTheme.textSecondary)
                                }
                            }
                        } else if (pageIndex != 2 && filteredResidents.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.People, null, tint = SoftTheme.textSecondary.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                                    Spacer(Modifier.height(16.dp))
                                    Text(stringResource(R.string.no_members_found), color = SoftTheme.textSecondary)
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    start = 24.dp + padding.calculateStartPadding(layoutDirection) + navPadding.calculateStartPadding(layoutDirection),
                                    top = 0.dp,
                                    end = 24.dp + padding.calculateEndPadding(layoutDirection) + navPadding.calculateEndPadding(layoutDirection),
                                    bottom = 80.dp + padding.calculateBottomPadding() + navPadding.calculateBottomPadding()
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (pageIndex == 2) {
                                    items(filteredStaff) { staff ->
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
                                    items(filteredResidents) { user ->
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
        title = { Text(stringResource(R.string.add_society_staff), fontWeight = FontWeight.Bold, color = SoftTheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SocioTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.full_name),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = SoftTheme.primary) }
                )
                SocioTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = stringResource(R.string.phone_number),
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = SoftTheme.primary) }
                )
                SocioTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = stringResource(R.string.role_e_g_maid_plumber),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = SoftTheme.primary) }
                )
                Text(
                    text = stringResource(R.string.note_staff_members_added_here_do_not_have_app_access),
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
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = SoftTheme.textSecondary)
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
            if (user.profilePhotoUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(user.profilePhotoUrl),
                    contentDescription = stringResource(R.string.profile_photo),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
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

                val locationInfo = remember(user.wingId, user.flatNumber) {
                    buildString {
                        if (user.roles.contains(UserRole.SECURITY_GUARD) || user.roles.contains(UserRole.SERVICE_STAFF)) {
                            append("Society Employee")
                        } else {
                            if (!user.wingId.isNullOrEmpty()) append("Wing ${user.wingId}")
                            if (!user.wingId.isNullOrEmpty() && !user.flatNumber.isNullOrEmpty()) append(", ")
                            if (!user.flatNumber.isNullOrEmpty()) append("Flat ${user.flatNumber}")
                            if (isEmpty()) append("N/A")
                        }
                    }
                }

                Text(
                    text = locationInfo,
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
                            text = stringResource(R.string.contact_details_hidden_for_privacy),
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.textSecondary.copy(alpha = 0.5f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            if (isAdmin) {
                IconButton(onClick = onRemove, modifier = Modifier.padding(start = 8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove), tint = MaterialTheme.colorScheme.error)
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
            if (staff.photoUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(staff.photoUrl),
                    contentDescription = stringResource(R.string.profile_photo),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
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
                            text = stringResource(R.string.staff),
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
                            text = stringResource(R.string.contact_details_hidden_for_privacy),
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.textSecondary.copy(alpha = 0.5f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            if (isAdmin) {
                IconButton(onClick = onRemoveFromSociety, modifier = Modifier.padding(start = 8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove), tint = MaterialTheme.colorScheme.error)
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
