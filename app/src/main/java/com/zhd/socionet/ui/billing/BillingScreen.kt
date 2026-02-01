package com.zhd.socionet.ui.billing

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhd.socionet.R
import com.zhd.socionet.data.model.Bill
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: BillingViewModel = hiltViewModel(),
    flatId: String?,
    userId: String,
    isAdmin: Boolean,
    societyId: String = "",
    onBack: () -> Unit
) {
    val uiState = viewModel.uiState

    LaunchedEffect(Unit) {
        if (!isAdmin && flatId != null) viewModel.loadBills(flatId)
    }

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
                    title = {
                        Text(
                            text = if (isAdmin) stringResource(R.string.accounts_admin) else stringResource(R.string.my_dues),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
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
            Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp)) {
                if (isAdmin) {
                    Spacer(Modifier.height(16.dp))
                    SoftCard {
                        Column {
                            Text(
                                text = stringResource(R.string.monthly_billing_cycle),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.billing_cycle_msg),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftTheme.textSecondary
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            SocioButton(
                                text = stringResource(R.string.generate_bills_month),
                                onClick = { viewModel.generateBillsForCurrentMonth(societyId) },
                                isLoading = uiState.isLoading
                            )
                            if (uiState.adminMessage != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = uiState.adminMessage,
                                    color = SoftTheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.height(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.total_outstanding),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = formatCurrency(uiState.totalOutstanding),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        if (uiState.totalOutstanding <= 0.0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp).background(Color.White.copy(alpha = 0.2f), CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.all_caught_up),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.payment_history),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.textPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SoftTheme.primary)
                        }
                    } else if (uiState.bills.isEmpty()) {
                        SoftCard {
                            Text(
                                text = "No billing history found",
                                modifier = Modifier.padding(vertical = 32.dp).align(Alignment.CenterHorizontally),
                                color = SoftTheme.textSecondary
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                            items(uiState.bills) { bill ->
                                BillItem(bill, onPay = { viewModel.payBill(bill, userId) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillItem(bill: Bill, onPay: () -> Unit) {
    val isPaid = bill.isPaid
    val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())

    SoftCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = bill.month,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.textPrimary
                )
                Text(
                    text = "Due: ${formatter.format(bill.dueDate.toDate())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (!isPaid) MaterialTheme.colorScheme.error else SoftTheme.textSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(bill.amountDue),
                    fontWeight = FontWeight.ExtraBold,
                    color = SoftTheme.textPrimary,
                    style = MaterialTheme.typography.titleLarge
                )

                if (isPaid) {
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.paid),
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onPay,
                        modifier = Modifier.height(36.dp).padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.pay_now),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount)
}
