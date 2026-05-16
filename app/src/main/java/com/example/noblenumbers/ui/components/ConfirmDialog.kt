package com.example.noblenumbers.ui.components

import androidx.compose.runtime.Composable

@Composable
fun ConfirmNewGameDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    NobleConfirmDialog(onDismiss = onDismiss, onConfirm = onConfirm)
}
