package com.zhd.socionet.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhd.socionet.R
import com.zhd.socionet.ui.common.SocioButton
import com.zhd.socionet.ui.common.SoftCard
import com.zhd.socionet.ui.common.SoftTheme

@Composable
fun PendingApprovalScreen(
    onBackToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftTheme.bg)
    ) {
        AuthHeader(
            title = "Verification",
            subtitle = "Safe & Secure Community"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 232.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SoftCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = SoftTheme.primary.copy(alpha = 0.05f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = SoftTheme.primary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.account_pending),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftTheme.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.account_review_msg),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = SoftTheme.textSecondary,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    SocioButton(
                        text = stringResource(R.string.back_to_login),
                        onClick = onBackToLogin
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(onClick = { /* TODO */ }) {
                Text(
                    text = "Need help? Contact Society Office",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
