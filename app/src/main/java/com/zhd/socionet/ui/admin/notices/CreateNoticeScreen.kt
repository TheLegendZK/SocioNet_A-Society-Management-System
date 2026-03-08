package com.zhd.socionet.ui.admin.notices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
fun CreateNoticeScreen(
    viewModel: CreateNoticeViewModel = hiltViewModel(),
    adminId: String,
    societyId: String?,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var priorityExpanded by remember { mutableStateOf(false) }
    var audienceExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }
    val navPadding = LocalNavPadding.current
    val layoutDirection = LocalLayoutDirection.current

    LaunchedEffect(adminId, societyId) {
        viewModel.loadInitialData(adminId, societyId)
    }

    if (uiState.isSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.resetForm() },
            title = { Text(stringResource(R.string.success), fontWeight = FontWeight.Bold) },
            text = { Text(if (uiState.editingNoticeId != null) "Notice updated successfully!" else stringResource(R.string.notice_success_msg)) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetForm() }) {
                    Text(stringResource(R.string.ok), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SoftTheme.surface,
            shape = MaterialTheme.shapes.large
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Notice", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this notice? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNotice(showDeleteConfirm!!)
                        showDeleteConfirm = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
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
                    title = { Text(stringResource(R.string.create_notice), fontWeight = FontWeight.Bold, color = Color.White) },
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp + padding.calculateStartPadding(layoutDirection) + navPadding.calculateStartPadding(layoutDirection),
                    top = padding.calculateTopPadding() + navPadding.calculateTopPadding(),
                    end = 24.dp + padding.calculateEndPadding(layoutDirection) + navPadding.calculateEndPadding(layoutDirection),
                    bottom = 32.dp + padding.calculateBottomPadding() + navPadding.calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Create/Edit Notice
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.editingNoticeId != null) "Edit Notice" else stringResource(R.string.new_announcement),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (uiState.editingNoticeId != null) {
                            TextButton(onClick = { viewModel.resetForm() }) {
                                Text("Cancel Edit", color = Color.White)
                            }
                        }
                    }
                }

                item {
                    SoftCard {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            SocioTextField(
                                value = uiState.title,
                                onValueChange = { viewModel.onFieldChange(it, uiState.content) },
                                label = stringResource(R.string.subject_headline),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            SocioTextField(
                                value = uiState.content,
                                onValueChange = { viewModel.onFieldChange(uiState.title, it) },
                                label = stringResource(R.string.notice_details),
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
                                        label = stringResource(R.string.priority),
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
                                        label = stringResource(R.string.audience),
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
                                        val audiences = listOf("ALL", "OWNERS_ONLY") + uiState.availableWings.map { "WING_$it" }
                                        audiences.forEach { audience ->
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
                }

                item {
                    SocioButton(
                        text = if (uiState.editingNoticeId != null) "Update Notice" else stringResource(R.string.broadcast_notice),
                        onClick = { viewModel.postNotice(adminId) },
                        isLoading = uiState.isLoading,
                        enabled = uiState.title.isNotBlank() && uiState.content.isNotBlank() && uiState.resolvedSocietyId.isNotBlank()
                    )
                }

                // Section 2: Existing Notices (Now below)
                item {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Existing Notices",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.textPrimary
                    )
                }

                if (uiState.isLoadingNotices) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SoftTheme.primary, modifier = Modifier.size(32.dp))
                        }
                    }
                } else if (uiState.errorMessage != null) {
                    item {
                        SoftCard(backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)) {
                            Text(
                                text = "Error: ${uiState.errorMessage}",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                        }
                    }
                } else if (uiState.notices.isEmpty()) {
                    item {
                        SoftCard(backgroundColor = SoftTheme.bg.copy(alpha = 0.5f)) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Info, null, tint = SoftTheme.textSecondary)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "No notices posted yet.",
                                    color = SoftTheme.textSecondary,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.notices) { notice ->
                        SoftCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SoftTheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            null,
                                            tint = SoftTheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        notice.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        notice.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftTheme.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { viewModel.selectNoticeForEdit(notice) }) {
                                    Icon(Icons.Default.Edit, "Edit", tint = SoftTheme.primary)
                                }
                                IconButton(onClick = { showDeleteConfirm = notice.id }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}