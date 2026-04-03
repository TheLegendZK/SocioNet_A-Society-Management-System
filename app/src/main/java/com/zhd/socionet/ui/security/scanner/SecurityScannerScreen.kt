package com.zhd.socionet.ui.security.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zhd.socionet.R
import com.zhd.socionet.data.model.EntryStatus
import com.zhd.socionet.data.model.GateEntry
import com.zhd.socionet.ui.common.LocalNavPadding
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme
import com.zhd.socionet.ui.profile.ProfileViewModel
import com.zhd.socionet.ui.security.log.SecurityLogViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScannerScreen(
    viewModel: SecurityScannerViewModel = hiltViewModel(),
    logViewModel: SecurityLogViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToManualEntry: () -> Unit,
    onNavigateToStaffAttendance: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }
    val context = LocalContext.current
    val navPadding = LocalNavPadding.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showLogoutDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.societyId) {
        if (uiState.societyId.isNotBlank()) {
            logViewModel.loadLogs(uiState.societyId)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout_confirmation), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.are_you_sure_you_want_to_logout)) },
            confirmButton = {
                TextButton(onClick = { 
                    showLogoutDialog = false
                    profileViewModel.logout(onLogout) 
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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(SoftTheme.bg)) {
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
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.gate_keeper), fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    actions = {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.Logout,
                                stringResource(R.string.logout), tint = Color.Red)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End, 
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = navPadding.calculateBottomPadding())
                ) {
                    FloatingActionButton(
                        onClick = { onNavigateToStaffAttendance(uiState.societyId) },
                        containerColor = SoftTheme.surface,
                        contentColor = SoftTheme.primary,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Badge, stringResource(R.string.staff))
                    }
                    ExtendedFloatingActionButton(
                        onClick = onNavigateToManualEntry,
                        containerColor = SoftTheme.primary,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Edit, stringResource(R.string.manual_entry)) },
                        text = { Text(stringResource(R.string.manual_entry), fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier
                .padding(padding)
                .fillMaxSize()) {
                SecondaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    divider = {},
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(pagerState.currentPage),
                            color = Color.White,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { 
                            scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        text = { Text(stringResource(R.string.scan_qr), fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.QrCodeScanner, null) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { 
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        text = { Text(stringResource(R.string.enter_otp), fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Dialpad, null) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                    Tab(
                        selected = pagerState.currentPage == 2,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(2) }
                        },
                        text = { Text(stringResource(R.string.log), fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.History, null) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top
                ) { pageIndex ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(if (pageIndex == 2) 0.dp else 24.dp), contentAlignment = Alignment.TopCenter) {
                        when (pageIndex) {
                            0 -> QrScanView(
                                hasPermission = hasCameraPermission,
                                onRequestPermission = { launcher.launch(Manifest.permission.CAMERA) },
                                onQrScanned = { viewModel.onQrScanned(it) }
                            )
                            1 -> ManualOtpView(onSubmit = { viewModel.onOtpEntered(it) })
                            2 -> SecurityLogView(viewModel = logViewModel)
                        }
                    }
                }
            }

            uiState.scanResult?.let { result ->
                ResultOverlay(result, onDismiss = { viewModel.resetScanner() })
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
fun QrScanView(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onQrScanned: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 48.dp)) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasPermission) {
                QrCodeScanner(
                    modifier = Modifier.fillMaxSize(),
                    onQrCodeScanned = onQrScanned
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.camera_permission_is_required_to_scan_qr_codes),
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRequestPermission) {
                        Text(stringResource(R.string.grant_permission))
                    }
                }
            }
            
            // Scanner Overlay
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)) {
                Box(modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopStart)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(topStart = 12.dp)
                    ))
                Box(modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(topEnd = 12.dp)))
                Box(modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomStart)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(bottomStart = 12.dp)
                    ))
                Box(modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(bottomEnd = 12.dp)
                    ))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            stringResource(R.string.center_the_qr_code_in_the_frame),
            style = MaterialTheme.typography.bodyLarge,
            color = SoftTheme.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ManualOtpView(onSubmit: (String) -> Unit) {
    var otp by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 48.dp)) {
        Text(
            stringResource(R.string.verify_visitor_otp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = SoftTheme.textPrimary
        )
        Text(
            stringResource(R.string.ask_visitor_for_the_6_digit_code),
            style = MaterialTheme.typography.bodyMedium,
            color = SoftTheme.textSecondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        SoftCard {
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color = SoftTheme.primary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = SoftTheme.primary
                ),
                placeholder = {
                    Text(
                        "------",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = LocalTextStyle.current.copy(
                            fontSize = 36.sp, 
                            letterSpacing = 8.sp, 
                            color = SoftTheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        SocioButton(
            text = stringResource(R.string.verify_entry),
            onClick = { onSubmit(otp) },
            enabled = otp.length == 6
        )
    }
}

@Composable
fun SecurityLogView(viewModel: SecurityLogViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val navPadding = LocalNavPadding.current

    if (uiState.isLoading && uiState.logs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    } else if (uiState.logs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_entry_history), color = Color.White.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = 24.dp,
                end = 24.dp,
                bottom = 100.dp + navPadding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.logs.filter { !it.isStaffEntry }) { entry ->
                LogEntryItem(entry)
            }
        }
    }
}

@Composable
fun LogEntryItem(entry: GateEntry) {
    SoftCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.visitorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.textPrimary
                )
                val subtitle = buildString {
                    if (entry.isStaffEntry) append("Staff") else append(entry.visitorType)
                    if (entry.hostFlatNumber != null) {
                        append(" • Flat ")
                        append(entry.hostFlatNumber)
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.textSecondary
                )
                entry.entryTime?.let {
                    val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(it.toDate())
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftTheme.primary.copy(alpha = 0.7f)
                    )
                }
            }
            
            StatusBadge(entry.status)
        }
    }
}

@Composable
fun StatusBadge(status: EntryStatus) {
    val (color, text) = when (status) {
        EntryStatus.CHECKED_IN -> Color(0xFF4CAF50) to "IN"
        EntryStatus.CHECKED_OUT -> Color(0xFFF44336) to "OUT"
        EntryStatus.APPROVED -> SoftTheme.primary to "APPROVED"
        EntryStatus.PENDING_APPROVAL -> Color(0xFFFF9800) to "PENDING"
        EntryStatus.DENIED -> Color.Gray to "DENIED"
        EntryStatus.EXPIRED -> Color.LightGray to "EXPIRED"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ResultOverlay(result: ScanResult, onDismiss: () -> Unit) {
    val isSuccess = result is ScanResult.Success
    val backgroundColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.GppBad
    val title = if (isSuccess) stringResource(R.string.access_granted) else stringResource(R.string.access_denied)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor.copy(alpha = 0.95f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(80.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (result is ScanResult.Success) {
                SoftCard(backgroundColor = Color.White.copy(alpha = 0.15f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.visitor_details), color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = result.entry.visitorName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        
                        val displayFlat = result.entry.hostFlatNumber ?: result.entry.hostFlatId.substringAfterLast("_")
                        Text(
                            text = stringResource(R.string.flat_name, displayFlat),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            } else if (result is ScanResult.Error) {
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = backgroundColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.done), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
        }
    }
}
