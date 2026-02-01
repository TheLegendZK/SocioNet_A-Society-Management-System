package com.zhd.socionet.ui.resident.gatepass

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import coil.compose.rememberAsyncImagePainter
import com.zhd.socionet.R
import com.zhd.socionet.data.model.VisitorType
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGatePassScreen(
    viewModel: GatePassViewModel = hiltViewModel(),
    hostFlatId: String,
    hostUserId: String,
    onBackClick: () -> Unit
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

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
                    title = { Text(stringResource(R.string.new_gate_pass), fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
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
            if (uiState.isSuccess) {
                SuccessPassCard(
                    visitorName = uiState.visitorName,
                    otp = uiState.generatedOtp ?: "000000",
                    passId = uiState.generatedPassId ?: "",
                    modifier = Modifier.padding(padding),
                    onShare = {
                        val shareText = "Hello! Here is your Gate Pass for SocioNet.\n\nVisitor: ${uiState.visitorName}\nOTP: ${uiState.generatedOtp}\n\nShow this code to the security guard."
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Pass via"))
                    },
                    onClose = {
                        viewModel.resetState()
                        onBackClick()
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(Modifier.height(16.dp))
                    
                    Column {
                        Text(
                            text = stringResource(R.string.pre_approve_entry),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.gate_pass_msg),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

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
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val types = listOf(VisitorType.GUEST, VisitorType.DELIVERY, VisitorType.CAB)
                                    types.forEach { type ->
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
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = uiState.visitorType == type,
                                                borderColor = SoftTheme.textSecondary.copy(alpha = 0.2f),
                                                selectedBorderColor = SoftTheme.primary
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
                        onClick = { viewModel.generatePass(hostFlatId, hostUserId) },
                        isLoading = uiState.isLoading,
                        enabled = uiState.visitorName.isNotBlank()
                    )

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessPassCard(
    visitorName: String,
    otp: String,
    passId: String,
    modifier: Modifier = Modifier,
    onShare: () -> Unit,
    onClose: () -> Unit
) {
    val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=$passId"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SoftCard {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.entry_pass),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = SoftTheme.textPrimary
                )
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .background(Color.White, MaterialTheme.shapes.medium)
                        .padding(16.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(qrUrl),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.otp_code),
                    style = MaterialTheme.typography.labelMedium,
                    color = SoftTheme.textSecondary
                )
                Text(
                    text = otp,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SoftTheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = SoftTheme.bg,
                    shape = CircleShape
                ) {
                    Text(
                        text = "${stringResource(R.string.visitor)}: $visitorName",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SoftTheme.textPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.close), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onShare,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.share), fontWeight = FontWeight.Bold)
            }
        }
    }
}
