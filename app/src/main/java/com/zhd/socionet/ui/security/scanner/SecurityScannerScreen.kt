package com.zhd.socionet.ui.security.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme
import com.zhd.socionet.ui.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScannerScreen(
    viewModel: SecurityScannerViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToManualEntry: () -> Unit,
    onNavigateToStaffAttendance: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(SoftTheme.bg)) {
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
                    title = { Text("Gate Keeper", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    actions = {
                        IconButton(onClick = { profileViewModel.logout(onLogout) }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color.Red)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = onNavigateToStaffAttendance,
                        containerColor = SoftTheme.surface,
                        contentColor = SoftTheme.primary,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Badge, "Staff")
                    }
                    ExtendedFloatingActionButton(
                        onClick = onNavigateToManualEntry,
                        containerColor = SoftTheme.primary,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Edit, "Log") },
                        text = { Text("Manual Entry", fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    divider = {},
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(selectedTab),
                            color = Color.White,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Scan QR", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.QrCodeScanner, null) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Enter OTP", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Dialpad, null) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                }

                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    if (selectedTab == 0) {
                        QrScanView(onSimulateScan = { viewModel.onQrScanned(it) })
                    } else {
                        ManualOtpView(onSubmit = { viewModel.onOtpEntered(it) })
                    }
                }
            }

            uiState.scanResult?.let { result ->
                ResultOverlay(result, onDismiss = { viewModel.resetScanner() })
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
fun QrScanView(onSimulateScan: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Camera Active", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelMedium)
            }
            
            Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Box(modifier = Modifier.size(40.dp).align(Alignment.TopStart).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(topStart = 12.dp)))
                Box(modifier = Modifier.size(40.dp).align(Alignment.TopEnd).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(topEnd = 12.dp)))
                Box(modifier = Modifier.size(40.dp).align(Alignment.BottomStart).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(bottomStart = 12.dp)))
                Box(modifier = Modifier.size(40.dp).align(Alignment.BottomEnd).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(bottomEnd = 12.dp)))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Center the QR code in the frame",
            style = MaterialTheme.typography.bodyLarge,
            color = SoftTheme.textPrimary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = { onSimulateScan("SIMULATED_PASS_ID") },
            shape = CircleShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.primary)
        ) {
            Text("Simulate Scan", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ManualOtpView(onSubmit: (String) -> Unit) {
    var otp by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            "Verify Visitor OTP",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = SoftTheme.textPrimary
        )
        Text(
            "Ask visitor for the 6-digit code",
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
                        "000000",
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
            text = "VERIFY ENTRY",
            onClick = { onSubmit(otp) },
            enabled = otp.length == 6
        )
    }
}

@Composable
fun ResultOverlay(result: ScanResult, onDismiss: () -> Unit) {
    val isSuccess = result is ScanResult.Success
    val backgroundColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.GppBad
    val title = if (isSuccess) "ACCESS GRANTED" else "ACCESS DENIED"

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
                        Text("Visitor Details", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = result.entry.visitorName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Flat ${result.entry.hostFlatId.replace("_", " ")}",
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("DONE", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
        }
    }
}
