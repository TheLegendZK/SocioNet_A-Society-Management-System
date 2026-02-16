package com.zhd.socionet.ui.resident.complaints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhd.socionet.R
import com.zhd.socionet.data.model.ComplaintCategory
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentComplaintScreen(
    viewModel: ComplaintViewModel = hiltViewModel(),
    flatId: String,
    userId: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(flatId) { if (flatId.isNotBlank()) viewModel.loadComplaints(flatId) }

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
                    title = { Text(stringResource(R.string.help_desk), fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = SoftTheme.primary,
                    contentColor = Color.White,
                    shape = MaterialTheme.shapes.large,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Your Complaints",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    if (uiState.complaints.isEmpty()) {
                        item {
                            SoftCard {
                                Text(
                                    text = stringResource(R.string.no_complaints_history),
                                    color = SoftTheme.textSecondary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(vertical = 32.dp).align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    } else {
                        items(uiState.complaints) { complaint ->
                            SoftCard {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = complaint.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftTheme.textPrimary
                                        )
                                        Spacer(Modifier.weight(1f))
                                        val statusColor = when (complaint.status) {
                                            "RESOLVED" -> Color(0xFF4CAF50)
                                            "IN_PROGRESS" -> Color(0xFFFF9800)
                                            else -> Color(0xFFF44336)
                                        }
                                        Surface(
                                            color = statusColor.copy(alpha = 0.1f),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = complaint.status,
                                                color = statusColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = complaint.description,
                                        color = SoftTheme.textSecondary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = SoftTheme.primary.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = complaint.category.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SoftTheme.primary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                fontWeight = FontWeight.Bold
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

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var cat by remember { mutableStateOf(ComplaintCategory.PLUMBING) }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.new_complaint), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SocioTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = stringResource(R.string.title)
                    )
                    SocioTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = stringResource(R.string.details),
                        singleLine = false
                    )
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        SocioTextField(
                            value = cat.name,
                            onValueChange = {},
                            readOnly = true,
                            label = stringResource(R.string.category),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(SoftTheme.surface)
                        ) {
                            ComplaintCategory.entries.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name, color = SoftTheme.textPrimary) },
                                    onClick = { cat = c; expanded = false }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                SocioButton(
                    text = stringResource(R.string.submit),
                    onClick = { viewModel.submitComplaint(title, desc, cat, flatId, userId); showDialog = false },
                    modifier = Modifier.width(100.dp).height(44.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = SoftTheme.surface,
            titleContentColor = SoftTheme.textPrimary,
            textContentColor = SoftTheme.textSecondary,
            shape = MaterialTheme.shapes.large
        )
    }
}
