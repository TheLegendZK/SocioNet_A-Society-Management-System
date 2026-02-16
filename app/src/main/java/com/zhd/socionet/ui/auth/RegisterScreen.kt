package com.zhd.socionet.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhd.socionet.R
import com.zhd.socionet.data.model.UserRole
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SocioPasswordField
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var socExpanded by remember { mutableStateOf(false) }
    var wingExpanded by remember { mutableStateOf(false) }
    var flatExpanded by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }

    if (uiState.isSuccess) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.registration_pending)) },
            text = { Text(stringResource(R.string.registration_success_msg)) },
            confirmButton = {
                Button(onClick = onNavigateToLogin) {
                    Text(stringResource(R.string.back_to_login))
                }
            },
            containerColor = SoftTheme.surface,
            titleContentColor = SoftTheme.textPrimary,
            textContentColor = SoftTheme.textSecondary
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftTheme.bg)
    ) {
        AuthHeader(
            title = stringResource(R.string.join_society),
            subtitle = "Create your account to get started"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 232.dp, start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SoftCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.personal_details),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.primary
                    )

                    SocioTextField(
                        value = name, onValueChange = { name = it },
                        label = stringResource(R.string.full_name),
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = SoftTheme.primary) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    SocioTextField(
                        value = email, onValueChange = { email = it },
                        label = stringResource(R.string.email_address),
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = SoftTheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    SocioPasswordField(
                        value = password, onValueChange = { password = it },
                        label = stringResource(R.string.password),
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = SoftTheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    SocioTextField(
                        value = phone, onValueChange = { phone = it },
                        label = stringResource(R.string.phone_number),
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = SoftTheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
            }

            SoftCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.residence_details),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.primary
                    )

                    ExposedDropdownMenuBox(expanded = socExpanded, onExpandedChange = { socExpanded = it }) {
                        SocioTextField(
                            value = uiState.societies.find { it.first == uiState.selectedSocietyId }?.second ?: "",
                            onValueChange = {}, readOnly = true,
                            label = stringResource(R.string.select_society),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = socExpanded) },
                            modifier = Modifier.menuAnchor(),
                            leadingIcon = { Icon(Icons.Default.Business, null, tint = SoftTheme.primary) }
                        )
                        ExposedDropdownMenu(
                            expanded = socExpanded,
                            onDismissRequest = { socExpanded = false },
                            modifier = Modifier.background(SoftTheme.surface)
                        ) {
                            uiState.societies.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name, color = SoftTheme.textPrimary) },
                                    onClick = { viewModel.onSocietySelected(id); socExpanded = false }
                                )
                            }
                        }
                    }

                    if (uiState.selectedSocietyId.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ExposedDropdownMenuBox(expanded = wingExpanded, onExpandedChange = { wingExpanded = it }, modifier = Modifier.weight(1f)) {
                                SocioTextField(
                                    value = uiState.selectedWing, onValueChange = {}, readOnly = true,
                                    label = stringResource(R.string.wing),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wingExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = wingExpanded,
                                    onDismissRequest = { wingExpanded = false },
                                    modifier = Modifier.background(SoftTheme.surface)
                                ) {
                                    uiState.wings.forEach { wing ->
                                        DropdownMenuItem(
                                            text = { Text(wing, color = SoftTheme.textPrimary) },
                                            onClick = { viewModel.onWingSelected(wing); wingExpanded = false }
                                        )
                                    }
                                }
                            }
                            ExposedDropdownMenuBox(expanded = flatExpanded, onExpandedChange = { flatExpanded = it }, modifier = Modifier.weight(1f)) {
                                SocioTextField(
                                    value = uiState.flats.find { it.first == uiState.selectedFlatId }?.second ?: "",
                                    onValueChange = {}, readOnly = true,
                                    label = stringResource(R.string.flat),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = flatExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = flatExpanded,
                                    onDismissRequest = { flatExpanded = false },
                                    modifier = Modifier.background(SoftTheme.surface)
                                ) {
                                    uiState.flats.forEach { (id, num) ->
                                        DropdownMenuItem(
                                            text = { Text(num, color = SoftTheme.textPrimary) },
                                            onClick = { viewModel.onFlatSelected(id); flatExpanded = false }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                        SocioTextField(
                            value = when(uiState.selectedRole) {
                                UserRole.OWNER -> stringResource(R.string.primary_owner)
                                UserRole.TENANT -> stringResource(R.string.tenant)
                                UserRole.FAMILY_MEMBER -> stringResource(R.string.family_member)
                                else -> uiState.selectedRole.name
                            },
                            onValueChange = {}, readOnly = true,
                            label = stringResource(R.string.i_am_a),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false },
                            modifier = Modifier.background(SoftTheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.primary_owner), color = SoftTheme.textPrimary) },
                                onClick = { viewModel.onRoleSelected(UserRole.OWNER); roleExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.tenant), color = SoftTheme.textPrimary) },
                                onClick = { viewModel.onRoleSelected(UserRole.TENANT); roleExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.family_member), color = SoftTheme.textPrimary) },
                                onClick = { viewModel.onRoleSelected(UserRole.FAMILY_MEMBER); roleExpanded = false }
                            )
                        }
                    }
                }
            }

            if (uiState.selectedRole == UserRole.OWNER) {
                Text(
                    text = stringResource(R.string.owner_approval_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.textSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            SocioButton(
                text = stringResource(R.string.submit_registration),
                onClick = { viewModel.registerUser(email, password, name, phone) },
                isLoading = uiState.isLoading,
                enabled = uiState.selectedFlatId.isNotEmpty()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Already have an account?",
                    color = SoftTheme.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Login",
                        color = SoftTheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
