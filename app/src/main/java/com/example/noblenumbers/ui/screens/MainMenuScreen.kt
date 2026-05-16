package com.example.noblenumbers.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.noblenumbers.R
import com.example.noblenumbers.ui.components.NobleButton
import com.example.noblenumbers.ui.components.NobleButtonStyle
import com.example.noblenumbers.ui.components.NobleParchmentPanel
import com.example.noblenumbers.ui.components.NobleTitlePlaque
import com.example.noblenumbers.ui.localizedString
import com.example.noblenumbers.ui.theme.NoblePalette

@Composable
fun MainMenuScreen(
    bestScore: Int,
    onPlay: () -> Unit,
    onSettings: () -> Unit,
) {
    WoodScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(18.dp))
            NobleTitlePlaque(
                title = localizedString(R.string.app_name),
                modifier = Modifier.widthIn(max = 420.dp),
            )
            Spacer(Modifier.height(28.dp))
            NobleParchmentPanel(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "${localizedString(R.string.best)} $bestScore",
                    color = NoblePalette.Ink,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Spacer(Modifier.height(36.dp))
            NobleButton(
                text = localizedString(R.string.play),
                onClick = onPlay,
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth(),
                style = NobleButtonStyle.Primary,
            )
            Spacer(Modifier.height(14.dp))
            NobleButton(
                text = localizedString(R.string.settings),
                onClick = onSettings,
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth(),
                style = NobleButtonStyle.Secondary,
            )
            Spacer(Modifier.height(18.dp))
        }
    }
}
