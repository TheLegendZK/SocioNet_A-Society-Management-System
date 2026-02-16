package com.zhd.socionet.ui.directory

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhd.socionet.R
import com.zhd.socionet.data.model.UserProfile
import com.zhd.socionet.ui.common.SocioTextField
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyDirectoryScreen(
    viewModel: SocietyDirectoryViewModel = hiltViewModel(),
    isAdmin: Boolean,
    societyId: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(societyId) {
        if (societyId.isNotBlank()) {
            viewModel.loadDirectory(societyId)
        }
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
                    title = { Text(stringResource(R.string.society_directory), fontWeight = FontWeight.Bold, color = Color.White) },
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
            ) {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SocioTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        label = "Search Residents or Flats",
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = SoftTheme.primary) }
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SoftTheme.primary)
                    }
                } else if (uiState.filteredResidents.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.People, null, tint = SoftTheme.textSecondary.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No residents found", color = SoftTheme.textSecondary)
                            if (societyId.isBlank()) {
                                Text("(Society ID Missing)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.filteredResidents) { user ->
                            DirectoryUserItem(
                                user = user,
                                showContactInfo = isAdmin,
                                onCall = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.phoneNumber}"))
                                    context.startActivity(intent)
                                },
                                onEmail = {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${user.email}"))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DirectoryUserItem(
    user: UserProfile,
    showContactInfo: Boolean,
    onCall: () -> Unit,
    onEmail: () -> Unit
) {
    SoftCard {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                shape = CircleShape,
                color = SoftTheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.fullName.take(1),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.primary
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = SoftTheme.textSecondary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = user.roles.firstOrNull()?.name ?: "Resident",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftTheme.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Flat ${user.assignedFlatId?.replace("_", " ")?.split(" ")?.last() ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(12.dp))

                if (showContactInfo) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Phone, null, tint = Color(0xFF4CAF50))
                        }
                        Text(
                            text = user.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.textSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(onClick = onEmail, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Email, null, tint = Color(0xFF2196F3))
                        }
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftTheme.textSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = SoftTheme.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Contact details hidden for privacy",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.textSecondary.copy(alpha = 0.5f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}
