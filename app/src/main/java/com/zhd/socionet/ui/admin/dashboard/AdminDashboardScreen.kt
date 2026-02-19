package com.zhd.socionet.ui.admin.dashboard

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.zhd.socionet.R
import com.zhd.socionet.ui.admin.approval.AdminApprovalViewModel
import com.zhd.socionet.ui.common.SoftAccent
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme
import com.zhd.socionet.ui.navigation.NavArgs
import com.zhd.socionet.ui.navigation.Routes
import com.zhd.socionet.ui.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    approvalViewModel: AdminApprovalViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val approvalState by approvalViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val user = profileState.userProfile

    val societyId = user?.assignedFlatId?.substringBefore("_") ?: ""

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            profileViewModel.loadProfile(currentUserId)
        }
    }

    LaunchedEffect(societyId) {
        if (societyId.isNotEmpty()) {
            approvalViewModel.loadPendingRequests(societyId)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SoftTheme.bg)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
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
                    title = { Text(stringResource(R.string.admin_console), fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.SETTINGS_SCREEN) }) {
                            Icon(Icons.Default.Settings, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user?.fullName?.take(1) ?: "A",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.welcome_admin, user?.fullName ?: "Admin"),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.society_administrator),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                if (user != null && !user.assignedFlatId.isNullOrBlank()) {
                    item(span = { GridItemSpan(2) }) {
                        SoftCard(
                            onClick = {
                                navController.navigate("${Routes.RESIDENT_HOME}/${user.assignedFlatId}/${user.id}")
                            },
                            backgroundColor = SoftTheme.surface
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SoftTheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Home,
                                            null,
                                            tint = SoftTheme.primary
                                        )
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.my_residence),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.textPrimary
                                    )
                                    Text(
                                        text = stringResource(R.string.switch_to_resident_view, profileState.displayFlatId ?: ""),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    SoftCard(
                        onClick = {
                            if (societyId.isNotEmpty()) {
                                navController.navigate("${Routes.ADMIN_APPROVALS}/$societyId")
                            }
                        },
                        backgroundColor = if(approvalState.pendingUsers.isNotEmpty()) SoftAccent.copy(alpha = 0.1f) else SoftTheme.surface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if(approvalState.pendingUsers.isNotEmpty()) SoftAccent.copy(alpha = 0.2f) else SoftTheme.bg,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.VerifiedUser, 
                                            null, 
                                            tint = if(approvalState.pendingUsers.isNotEmpty()) SoftAccent else SoftTheme.primary
                                        )
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.pending_approvals),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.textPrimary
                                    )
                                    Text(
                                        text = stringResource(R.string.new_user_requests),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.textSecondary
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if(approvalState.pendingUsers.isNotEmpty()) SoftAccent else SoftTheme.bg,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "${approvalState.pendingUsers.size}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = if(approvalState.pendingUsers.isNotEmpty()) Color.White else SoftTheme.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    AdminActionCard(
                        title = stringResource(R.string.setup_society),
                        icon = Icons.Default.Apartment,
                        color = SoftTheme.info
                    ) {
                        navController.navigate(Routes.ADMIN_MANAGE_FLATS)
                    }
                }
                item {
                    AdminActionCard(
                        title = stringResource(R.string.accounts),
                        icon = Icons.Default.AttachMoney,
                        color = SoftTheme.success
                    ) {
                        if (societyId.isNotEmpty()) {
                            navController.navigate("${Routes.ADMIN_BILLING}/$societyId")
                        }
                    }
                }
                item {
                    AdminActionCard(
                        title = stringResource(R.string.staff_registry),
                        icon = Icons.Default.Badge,
                        color = SoftTheme.warning
                    ) {
                        if (societyId.isNotEmpty()) {
                            navController.navigate("${Routes.ADMIN_STAFF_REGISTRY}/$societyId")
                        }
                    }
                }
                item {
                    AdminActionCard(
                        title = stringResource(R.string.post_notice),
                        icon = Icons.Default.Campaign,
                        color = SoftAccent
                    ) {
                        if (societyId.isNotEmpty()) {
                            navController.navigate("${Routes.ADMIN_POST_NOTICE}?${NavArgs.SOCIETY_ID}=$societyId")
                        } else {
                            navController.navigate(Routes.ADMIN_POST_NOTICE)
                        }
                    }
                }
                item {
                    AdminActionCard(
                        title = stringResource(R.string.directory),
                        icon = Icons.Default.Contacts,
                        color = SoftTheme.purple
                    ) {
                        if (societyId.isNotEmpty()) {
                            navController.navigate("${Routes.SOCIETY_DIRECTORY}/true/$societyId")
                        }
                    }
                }
                
                item(span = { GridItemSpan(2) }) {
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun AdminActionCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    SoftCard(onClick = onClick) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = SoftTheme.textPrimary,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}