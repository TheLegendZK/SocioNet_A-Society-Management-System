package com.zhd.socionet.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SocioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        isError = isError,
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SoftTheme.primary,
            unfocusedBorderColor = SoftTheme.textSecondary.copy(alpha = 0.3f),
            focusedLabelColor = SoftTheme.primary,
            unfocusedLabelColor = SoftTheme.textSecondary,
            cursorColor = SoftTheme.primary,
            errorBorderColor = MaterialTheme.colorScheme.error
        )
    )
}
