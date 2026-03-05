package com.zhd.socionet.ui.security.entries

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zhd.socionet.R
import com.zhd.socionet.ui.common.LocalNavPadding
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    viewModel: ManualEntryViewModel = hiltViewModel(),
    onEntryLogged: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var visitorName by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }

    var wingExpanded by remember { mutableStateOf(false) }
    var flatExpanded by remember { mutableStateOf(false) }
    val navPadding = LocalNavPadding.current
    val layoutDirection = LocalLayoutDirection.current

    if (uiState.successMessage != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.clearSuccessMessage()
                onEntryLogged()
            },
            title = { Text(stringResource(R.string.request_sent), fontWeight = FontWeight.Bold) },
            text = { Text(uiState.successMessage ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearSuccessMessage()
                    onEntryLogged()
                }) {
                    Text(stringResource(R.string.ok), fontWeight = FontWeight.Bold)
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
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.manual_visitor_entry), fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack,
                                stringResource(R.string.back), tint = Color.White)
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.visitor_information),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                SoftCard {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SocioTextField(
                            value = visitorName,
                            onValueChange = { visitorName = it },
                            label = stringResource(R.string.visitor_company_name)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Wing Selection
                            ExposedDropdownMenuBox(
                                expanded = wingExpanded,
                                onExpandedChange = { wingExpanded = !wingExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                SocioTextField(
                                    value = uiState.selectedWing,
                                    onValueChange = {},
                                    label = stringResource(R.string.wing),
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wingExpanded) },
                                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                )
                                ExposedDropdownMenu(
                                    expanded = wingExpanded,
                                    onDismissRequest = { wingExpanded = false }
                                ) {
                                    uiState.wings.forEach { wing ->
                                        DropdownMenuItem(
                                            text = { Text(wing) },
                                            onClick = {
                                                viewModel.onWingSelected(wing)
                                                wingExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Flat Selection
                            ExposedDropdownMenuBox(
                                expanded = flatExpanded,
                                onExpandedChange = { if (uiState.selectedWing.isNotBlank()) flatExpanded = !flatExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                SocioTextField(
                                    value = uiState.selectedFlat,
                                    onValueChange = {},
                                    label = stringResource(R.string.flat),
                                    readOnly = true,
                                    enabled = uiState.selectedWing.isNotBlank(),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = flatExpanded) },
                                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                )
                                ExposedDropdownMenu(
                                    expanded = flatExpanded,
                                    onDismissRequest = { flatExpanded = false }
                                ) {
                                    uiState.flatsInWing.forEach { flat ->
                                        DropdownMenuItem(
                                            text = { Text(flat) },
                                            onClick = {
                                                viewModel.onFlatSelected(flat)
                                                flatExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        SocioTextField(
                            value = vehicleNumber,
                            onValueChange = { vehicleNumber = it },
                            label = stringResource(R.string.vehicle_number_optional)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                SocioButton(
                    text = stringResource(R.string.log_entry_request),
                    onClick = { viewModel.logEntry(visitorName, vehicleNumber) },
                    isLoading = uiState.isLoading,
                    enabled = visitorName.isNotBlank() && uiState.selectedFlat.isNotBlank()
                )
            }
        }
    }
}