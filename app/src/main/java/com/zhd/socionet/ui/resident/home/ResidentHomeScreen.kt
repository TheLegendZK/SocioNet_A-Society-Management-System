package com.zhd.socionet.ui.resident.home

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
    noticeViewModel: NoticeBoardViewModel = hiltViewModel(),
    sosViewModel: SosViewModel = hiltViewModel(),
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val billingState by billingViewModel.uiState.collectAsState()
    val noticeState by noticeViewModel.uiState.collectAsState()

    val societyId = remember(flatId) {
        flatId.substringBefore("_")
    }

    LaunchedEffect(userId) { if (userId.isNotBlank()) profileViewModel.loadProfile(userId) }
    LaunchedEffect(profileState.userProfile) {
        val user = profileState.userProfile
        if (user != null) {
            val wing = user.wingId ?: "A"
            noticeViewModel.loadNotices(wing, user.roles)
        }
    }
    LaunchedEffect(flatId) {
        if (flatId.isNotBlank()) {
            billingViewModel.loadBills(flatId)
        }
    }

    val user = profileState.userProfile
    var showSosDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun launchSms() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "sms:".toUri()
            putExtra("sms_body", "HELP! Emergency at ${user?.flatId ?: flatId}. Please help!")
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
                    colors = TopAppBarDefaults.topAppBarColors(
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
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
                            text = "${stringResource(R.string.hello)} ${user?.fullName ?: stringResource(R.string.resident)}!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user?.flatNumber ?: user?.flatId?.substringAfterLast("_") ?: flatId.substringAfterLast("_"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickActionCard(
                            icon = Icons.Default.Build,
                            label = stringResource(R.string.complaints),
                            color = SoftTheme.info,
                            onClick = { if (flatId.isNotBlank() && userId.isNotBlank()) navController.navigate("${Routes.RESIDENT_COMPLAINTS}/$flatId/$userId") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            icon = Icons.Default.Warning,
                            label = stringResource(R.string.sos),
                            color = SoftAccent,
                            onClick = { showSosDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            icon = Icons.Default.VpnKey,
                            label = stringResource(R.string.gate_pass),
                            color = SoftTheme.success,
                            onClick = { navController.navigate("${Routes.RESIDENT_CREATE_PASS}/$flatId/$userId") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            icon = Icons.Default.Contacts,
                            label = stringResource(R.string.directory),
                            color = SoftTheme.purple,
                            onClick = {
                                if (societyId.isNotBlank()) {
                                    navController.navigate("${Routes.SOCIETY_DIRECTORY}/false/$societyId?flatId=$flatId&userId=$userId")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

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
                                        text = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).format(billingState.totalOutstanding),
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
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

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
                            TextButton(onClick = { navController.navigate("${Routes.RESIDENT_NOTICES}/$flatId/$userId") }) {
                                Text(
                                    text = stringResource(R.string.view_all),
                                    color = SoftTheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (noticeState.notices.isNotEmpty()) {
                    items(noticeState.notices.take(2)) { notice ->
                        PaddingWrapper {
                            NoticeSummaryCard(notice)
                        }
                    }
                } else {
                    item {
                        PaddingWrapper {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No notices yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftTheme.textSecondary,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
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
    modifier: Modifier = Modifier,
) {
    SoftCard(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        backgroundColor = SoftTheme.surface,
        padding = 12.dp
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
