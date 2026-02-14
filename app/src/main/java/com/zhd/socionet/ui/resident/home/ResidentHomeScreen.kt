package com.zhd.socionet.ui.resident.home

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zhd.socionet.R
import com.zhd.socionet.core.utils.DateUtils
import com.zhd.socionet.data.model.Notice
import com.zhd.socionet.ui.billing.BillingViewModel
import com.zhd.socionet.ui.common.SoftAccent
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme
import com.zhd.socionet.ui.navigation.Routes
import com.zhd.socionet.ui.profile.ProfileViewModel
import com.zhd.socionet.ui.resident.notices.NoticeBoardViewModel
import com.zhd.socionet.ui.resident.staff.ResidentStaffViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentHomeScreen(
    navController: NavController,
    flatId: String,
    userId: String,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
    staffViewModel: ResidentStaffViewModel = hiltViewModel(),
    noticeViewModel: NoticeBoardViewModel = hiltViewModel(),
    sosViewModel: SosViewModel = hiltViewModel()
) {
    val profileState = profileViewModel.uiState
    val billingState = billingViewModel.uiState
    val staffState = staffViewModel.uiState
    val noticeState = noticeViewModel.uiState

    LaunchedEffect(userId) { if (userId.isNotBlank()) profileViewModel.loadProfile(userId) }
    LaunchedEffect(profileState.userProfile) {
        val user = profileState.userProfile
        if (user != null) {
            val wing = user.assignedFlatId?.split("_")?.getOrNull(1) ?: "A"
            noticeViewModel.loadNotices(wing, user.roles)
        }
    }
    LaunchedEffect(flatId) {
        if (flatId.isNotBlank()) {
            billingViewModel.loadBills(flatId)
            staffViewModel.loadStaff("WbZ6qVoyNlMi1Quch8o3")
        }
    }

    val user = profileState.userProfile
    var showSosDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun launchSms() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:")
            putExtra("sms_body", "HELP! Emergency at ${user?.assignedFlatId ?: flatId}. Please help!")
        }
        context.startActivity(intent)
    }

    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            title = { Text(stringResource(R.string.confirm_sos), color = SoftAccent, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.sos_confirmation_msg)) },
            confirmButton = {
                Button(
                    onClick = {
                        sosViewModel.triggerEmergency(flatId, userId)
                        launchSms()
                        showSosDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftAccent)
                ) {
                    Text(stringResource(R.string.yes_send_help))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = SoftTheme.surface,
            titleContentColor = SoftTheme.textPrimary,
            textContentColor = SoftTheme.textSecondary
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(SoftTheme.bg)) {
        // Background Header Gradient
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
                    title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.ExtraBold, color = Color.White) },
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
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. WELCOME SECTION
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user?.fullName?.take(1) ?: "U",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Hello, ${user?.fullName ?: stringResource(R.string.resident)}!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user?.assignedFlatId?.replace("_", " ") ?: flatId.replace("_", " "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // QUICK ACTIONS
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickActionCard(
                            icon = Icons.Default.Build,
                            label = "Complaints",
                            color = Color(0xFF42A5F5),
                            onClick = { if (flatId.isNotBlank() && userId.isNotBlank()) navController.navigate("${Routes.RESIDENT_COMPLAINTS}/$flatId/$userId") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            icon = Icons.Default.Warning,
                            label = "SOS",
                            color = SoftAccent,
                            onClick = { showSosDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            icon = Icons.Default.VpnKey,
                            label = "Gatepass",
                            color = Color(0xFF66BB6A),
                            onClick = { navController.navigate("${Routes.RESIDENT_CREATE_PASS}/$flatId/$userId") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. DUES CARD
                item {
                    PaddingWrapper {
                        SoftCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.outstanding_dues),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = SoftTheme.textSecondary
                                    )
                                    Text(
                                        text = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(billingState.totalOutstanding),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (billingState.totalOutstanding > 0) SoftAccent else SoftTheme.primary
                                    )
                                }
                                Button(
                                    onClick = { if (flatId.isNotBlank() && userId.isNotBlank()) navController.navigate("${Routes.RESIDENT_PAYMENTS}/$flatId/$userId") },
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.ArrowForwardIos, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // 3. MY STAFF CARD
                item {
                    PaddingWrapper {
                        SoftCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Badge, null, tint = SoftTheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.my_household_staff),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.textPrimary
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            val myStaff = staffState.allStaff.filter { it.employerFlatIds.contains(flatId) }
                            if (myStaff.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_staff_linked),
                                    color = SoftTheme.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    myStaff.take(3).forEach { staff ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SoftTheme.bg, MaterialTheme.shapes.small)
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(8.dp).background(if (staff.isActive) Color.Green else Color.Red, CircleShape))
                                            Spacer(Modifier.width(12.dp))
                                            Text(staff.name, fontWeight = FontWeight.SemiBold, color = SoftTheme.textPrimary)
                                            Spacer(Modifier.weight(1f))
                                            Text(staff.role, style = MaterialTheme.typography.bodySmall, color = SoftTheme.textSecondary)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            TextButton(
                                onClick = { if (flatId.isNotBlank()) navController.navigate("${Routes.RESIDENT_STAFF}/$flatId") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.manage_staff), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 4. LATEST NOTICES
                if (noticeState.notices.isNotEmpty()) {
                    item {
                        PaddingWrapper {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.latest_notices),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.textPrimary
                                )
                                TextButton(onClick = { navController.navigate(Routes.RESIDENT_NOTICES) }) {
                                    Text(
                                        text = stringResource(R.string.view_all),
                                        color = SoftTheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    items(noticeState.notices.take(2)) { notice ->
                        PaddingWrapper {
                            NoticeSummaryCard(notice)
                        }
                    }
                }

                item { Spacer(Modifier.height(64.dp)) }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = SoftTheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PaddingWrapper(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        content()
    }
}

@Composable
fun NoticeSummaryCard(notice: Notice) {
    SoftCard {
        Row {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Campaign, null, tint = SoftTheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notice.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = DateUtils.getRelativeTime(notice.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftTheme.textSecondary
                )
            }
        }
    }
}
