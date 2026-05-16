package com.example.noblenumbers.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WoodButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    NobleButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    )
}
