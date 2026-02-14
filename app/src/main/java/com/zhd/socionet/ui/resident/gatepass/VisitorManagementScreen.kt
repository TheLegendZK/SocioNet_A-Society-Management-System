package com.zhd.socionet.ui.resident.gatepass

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.zhd.socionet.R
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.data.model.ServiceStaff
import com.zhd.socionet.data.model.VisitorType
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftAccent
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorManagementScreen(
    viewModel: GatePassViewModel = hiltViewModel(),
    hostFlatId: String,
    hostUserId: String,
    onBackClick: () -> Unit,
) {
    val uiState = viewModel.uiState
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_create_pass),
        stringResource(R.string.tab_approvals),
        stringResource(R.string.tab_history),
        stringResource(R.string.tab_my_staff)
    )

    LaunchedEffect(Unit) {
        viewModel.loadLinkedStaffAndLogs("WbZ6qVoyNlMi1Quch8o3", hostFlatId)
    }
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            1 -> viewModel.loadPendingRequests(hostFlatId)
            2 -> viewModel.loadHistory(hostFlatId)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SoftTheme.bg)) {
        // Background Header Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
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
                        title = { Text(stringResource(R.string.visitor_hub), fontWeight = FontWeight.ExtraBold, color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        edgePadding = 24.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Color.White,
                                    height = 3.dp
                                )
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                selectedContentColor = Color.White,
                                unselectedContentColor = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (selectedTab) {
                    0 -> CreatePassContent(viewModel, hostFlatId, hostUserId)
                    1 -> RequestsList(uiState.pendingRequests) { entry, accept -> viewModel.respondToRequest(entry, accept) }
                    2 -> HistoryList(uiState.visitorHistory)
                    3 -> StaffTrackingList(uiState.linkedStaff, uiState.staffLogs)
                }
            }
        }
    }
}

@Composable
fun CreatePassContent(viewModel: GatePassViewModel, flatId: String, userId: String) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

    if (uiState.isSuccess) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SoftCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.entry_pass),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = SoftTheme.primary,
                        letterSpacing = 4.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Box(modifier = Modifier.background(Color.White, MaterialTheme.shapes.medium).padding(16.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${uiState.generatedPassId}"),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(180.dp)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.otp_code),
                        style = MaterialTheme.typography.labelMedium,
                        color = SoftTheme.textSecondary
                    )
                    Text(
                        text = uiState.generatedOtp ?: "000000",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = SoftTheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Surface(color = SoftTheme.bg, shape = CircleShape) {
                        Text(
                            text = "${stringResource(R.string.visitor)}: ${uiState.visitorName}",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.textPrimary
                        )
                    }
                }
            }
            Spacer(Modifier.height(48.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { viewModel.resetState() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.close), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Gate Pass for ${uiState.visitorName}\nOTP: ${uiState.generatedOtp}\n\nShow to Security.")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Pass"))
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.share), fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(
                text = stringResource(R.string.pre_approve_entry),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            SoftCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    SocioTextField(
                        value = uiState.visitorName,
                        onValueChange = { viewModel.onVisitorNameChanged(it) },
                        label = stringResource(R.string.visitor_name_company)
                    )

                    Column {
                        Text(
                            text = stringResource(R.string.visitor_type),
                            style = MaterialTheme.typography.labelLarge,
                            color = SoftTheme.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(VisitorType.GUEST, VisitorType.DELIVERY, VisitorType.CAB).forEach { type ->
                                FilterChip(
                                    selected = uiState.visitorType == type,
                                    onClick = { viewModel.onVisitorTypeChanged(type) },
                                    label = { Text(type.name) },
                                    leadingIcon = if (uiState.visitorType == type) {
                                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SoftTheme.primary.copy(alpha = 0.1f),
                                        selectedLabelColor = SoftTheme.primary,
                                        selectedLeadingIconColor = SoftTheme.primary,
                                        containerColor = SoftTheme.bg.copy(alpha = 0.5f)
                                    ),
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }
            }

            SocioButton(
                text = stringResource(R.string.generate_qr_pass),
                onClick = { viewModel.generatePass(flatId, userId) },
                isLoading = uiState.isLoading,
                enabled = uiState.visitorName.isNotBlank()
            )
        }
    }
}

@Composable
fun RequestsList(requests: List<GateEntry>, onRespond: (GateEntry, Boolean) -> Unit) {
    if (requests.isEmpty()) {
        EmptyStateView(Icons.Default.VerifiedUser, "No pending requests")
    } else {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(requests) { entry ->
                SoftCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = SoftAccent.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.NotificationsActive, null, tint = SoftAccent, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = stringResource(R.string.approval_needed),
                                fontWeight = FontWeight.Bold,
                                color = SoftAccent,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = entry.visitorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.textPrimary
                        )
                        Text(
                            text = "${entry.visitorType} • ${stringResource(R.string.waiting_at_gate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.textSecondary
                        )
                        Spacer(Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { onRespond(entry, false) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.deny), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onRespond(entry, true) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.allow), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryList(history: List<GateEntry>) {
    if (history.isEmpty()) {
        EmptyStateView(Icons.Default.History, "No entry history found")
    } else {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(history) { entry ->
                SoftCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isCheckIn = entry.status == EntryStatus.CHECKED_IN
                        Surface(
                            shape = CircleShape,
                            color = (if (isCheckIn) Color(0xFF4CAF50) else SoftTheme.textSecondary).copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isCheckIn) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    tint = if (isCheckIn) Color(0xFF4CAF50) else SoftTheme.textSecondary
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.visitorName,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.textPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = formatTime(entry.entryTime),
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.textSecondary
                            )
                        }
                        Surface(
                            color = SoftTheme.bg,
                            shape = CircleShape
                        ) {
                            Text(
                                text = entry.status.name.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = SoftTheme.textSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffTrackingList(myStaff: List<ServiceStaff>, logs: List<GateEntry>) {
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Text(
                text = stringResource(R.string.my_staff),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SoftTheme.textPrimary
            )
            Spacer(Modifier.height(16.dp))
            if (myStaff.isEmpty()) {
                Text(stringResource(R.string.no_staff_linked), color = SoftTheme.textSecondary, style = MaterialTheme.typography.bodyMedium)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    myStaff.forEach { staff ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = SoftTheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = staff.name.take(1),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 28.sp,
                                        color = SoftTheme.primary
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = staff.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.textPrimary
                            )
                            Text(
                                text = staff.role,
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftTheme.textSecondary
                            )
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.recent_activity),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SoftTheme.textPrimary
            )
        }
        
        if (logs.isEmpty()) {
            item {
                SoftCard(backgroundColor = SoftTheme.bg.copy(alpha = 0.5f)) {
                    Text("No recent staff activity", modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = SoftTheme.textSecondary)
                }
            }
        } else {
            items(logs) { log ->
                SoftCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isCheckIn = log.status == EntryStatus.CHECKED_IN
                        Surface(
                            shape = CircleShape,
                            color = (if (isCheckIn) Color(0xFF4CAF50) else Color(0xFFF44336)).copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isCheckIn) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout,
                                    null,
                                    tint = if (isCheckIn) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = log.visitorName,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.textPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${if (isCheckIn) "Checked In" else "Checked Out"} • ${formatTime(log.entryTime)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, modifier = Modifier.size(64.dp), tint = SoftTheme.textSecondary.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text(message, color = SoftTheme.textSecondary, style = MaterialTheme.typography.bodyLarge)
    }
}

fun formatTime(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    return SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(timestamp.toDate())
}
