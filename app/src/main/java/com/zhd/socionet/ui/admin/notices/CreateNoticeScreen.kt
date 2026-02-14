package com.zhd.socionet.ui.admin.notices

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoticeScreen(
    viewModel: CreateNoticeViewModel = hiltViewModel(),
    adminId: String,
    onBack: () -> Unit
) {
    val uiState = viewModel.uiState
    val focusManager = LocalFocusManager.current
    var priorityExpanded by remember { mutableStateOf(false) }
    var audienceExpanded by remember { mutableStateOf(false) }

    if (uiState.isSuccess) {
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text("Success", fontWeight = FontWeight.Bold) },
            text = { Text("Community notice has been broadcasted successfully.") },
            confirmButton = {
                TextButton(onClick = onBack) {
                    Text("OK", fontWeight = FontWeight.Bold)
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
                    title = { Text("Create Notice", fontWeight = FontWeight.Bold, color = Color.White) },
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
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "New Announcement",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                SoftCard {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SocioTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.onFieldChange(it, uiState.content) },
                            label = "Subject / Headline",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        SocioTextField(
                            value = uiState.content,
                            onValueChange = { viewModel.onFieldChange(uiState.title, it) },
                            label = "Notice Details",
                            singleLine = false,
                            modifier = Modifier.height(150.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SocioTextField(
                                    value = uiState.priority,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = "Priority",
                                    trailingIcon = {
                                        IconButton(onClick = { priorityExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = priorityExpanded,
                                    onDismissRequest = { priorityExpanded = false },
                                    modifier = Modifier.background(SoftTheme.surface)
                                ) {
                                    listOf("NORMAL", "HIGH", "CRITICAL").forEach { level ->
                                        DropdownMenuItem(
                                            text = { Text(level, color = SoftTheme.textPrimary) },
                                            onClick = { viewModel.onPrioritySelected(level); priorityExpanded = false }
                                        )
                                    }
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SocioTextField(
                                    value = uiState.targetAudience,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = "Audience",
                                    trailingIcon = {
                                        IconButton(onClick = { audienceExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = audienceExpanded,
                                    onDismissRequest = { audienceExpanded = false },
                                    modifier = Modifier.background(SoftTheme.surface)
                                ) {
                                    listOf("ALL", "OWNERS_ONLY", "WING_A").forEach { audience ->
                                        DropdownMenuItem(
                                            text = { Text(audience, color = SoftTheme.textPrimary) },
                                            onClick = { viewModel.onAudienceSelected(audience); audienceExpanded = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                SocioButton(
                    text = "Broadcast Notice",
                    onClick = { viewModel.postNotice(adminId) },
                    isLoading = uiState.isLoading,
                    enabled = uiState.title.isNotBlank() && uiState.content.isNotBlank()
                )
            }
        }
    }
}
