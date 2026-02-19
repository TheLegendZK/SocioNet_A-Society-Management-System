package com.zhd.socionet.ui.admin.masterdata

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSetupScreen(
    viewModel: AdminSetupViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showWingDialog by remember { mutableStateOf(false) }

    if (uiState.isSuccess) {
        AlertDialog(
            onDismissRequest = onSetupComplete,
            title = { Text("Success", fontWeight = FontWeight.Bold) },
            text = { Text("Society structure generated successfully.") },
            confirmButton = {
                TextButton(onClick = onSetupComplete) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SoftTheme.surface,
            shape = MaterialTheme.shapes.large
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
                CenterAlignedTopAppBar(
                    title = { Text("Setup Society", fontWeight = FontWeight.Bold, color = Color.White) },
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
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text(
                        text = "Society Configuration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    SoftCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Apartment, null, tint = SoftTheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Basic Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.textPrimary
                                )
                            }
                            
                            SocioTextField(
                                value = uiState.societyName,
                                onValueChange = { viewModel.onSocietyDetailsChanged(it, uiState.societyAddress, uiState.registrationNo) },
                                label = "Society Name"
                            )
                            SocioTextField(
                                value = uiState.societyAddress,
                                onValueChange = { viewModel.onSocietyDetailsChanged(uiState.societyName, it, uiState.registrationNo) },
                                label = "Full Address"
                            )
                            SocioTextField(
                                value = uiState.registrationNo,
                                onValueChange = { viewModel.onSocietyDetailsChanged(uiState.societyName, uiState.societyAddress, it) },
                                label = "Society Registration No"
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Wings & Blocks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.textPrimary
                        )
                        Button(
                            onClick = { showWingDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.primary),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Wing", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (uiState.wingConfigurations.isEmpty()) {
                    item {
                        SoftCard(backgroundColor = SoftTheme.bg.copy(alpha = 0.5f)) {
                            Text(
                                "No wings configured. Add at least one to generate flats.",
                                color = SoftTheme.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                            )
                        }
                    }
                }

                items(uiState.wingConfigurations) { config ->
                    SoftCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = SoftTheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = config.wingName,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.primary
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Wing ${config.wingName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.textPrimary
                                )
                                Text(
                                    "${config.floors} Floors • ${config.flatsPerFloor} Units per floor",
                                    color = SoftTheme.textSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    SocioButton(
                        text = "Generate Society Masterdata",
                        onClick = { viewModel.submitSocietySetup() },
                        isLoading = uiState.isLoading,
                        enabled = uiState.societyName.isNotEmpty() && uiState.wingConfigurations.isNotEmpty()
                    )
                }
            }
        }
    }

    if (showWingDialog) {
        var name by remember { mutableStateOf("") }
        var floors by remember { mutableStateOf("") }
        var flats by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showWingDialog = false }) {
            SoftCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(
                        "Add Wing Configuration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.textPrimary
                    )
                    
                    SocioTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Wing Name (e.g. A, B, Block-1)"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SocioTextField(
                            value = floors,
                            onValueChange = { floors = it },
                            label = "Floors",
                            modifier = Modifier.weight(1f)
                        )
                        SocioTextField(
                            value = flats,
                            onValueChange = { flats = it },
                            label = "Units/Floor",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showWingDialog = false }) {
                            Text("Cancel", color = SoftTheme.textSecondary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.addWingConfiguration(
                                    name,
                                    floors.toIntOrNull() ?: 0,
                                    flats.toIntOrNull() ?: 0
                                )
                                showWingDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Add Wing", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
