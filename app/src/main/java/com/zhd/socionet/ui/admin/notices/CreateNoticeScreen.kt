package com.zhd.socionet.ui.admin.notices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhd.socionet.R
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoticeScreen(
    viewModel: CreateNoticeViewModel = hiltViewModel(),
    adminId: String,
    societyId: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var priorityExpanded by remember { mutableStateOf(false) }
    var audienceExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(societyId) {
        viewModel.loadWings(societyId)
    }

    if (uiState.isSuccess) {
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text(stringResource(R.string.success), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.notice_success_msg)) },
            confirmButton = {
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.ok), fontWeight = FontWeight.Bold)
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
                    text = stringResource(R.string.new_announcement),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

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

                Spacer(Modifier.weight(1f))
                SocioButton(
                    text = stringResource(R.string.broadcast_notice),
                    onClick = { viewModel.postNotice(adminId) },
                    isLoading = uiState.isLoading,
                    enabled = uiState.title.isNotBlank() && uiState.content.isNotBlank()
                )
            }
        }
    }
}
