package com.example.noblenumbers.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.noblenumbers.ui.components.NobleScreenFrame

@Composable
fun WoodScreen(content: @Composable () -> Unit) {
    NobleScreenFrame(
        modifier = Modifier,
        contentPadding = PaddingValues(16.dp),
    ) {
        content()
    }
}
