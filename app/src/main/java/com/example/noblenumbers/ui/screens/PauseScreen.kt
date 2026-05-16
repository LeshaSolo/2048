package com.example.noblenumbers.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.noblenumbers.R
import com.example.noblenumbers.ui.components.NobleButton
import com.example.noblenumbers.ui.components.NobleButtonStyle
import com.example.noblenumbers.ui.components.NobleTitlePlaque
import com.example.noblenumbers.ui.components.NobleWoodPanel
import com.example.noblenumbers.ui.localizedString

@Composable
fun PauseScreen(
    onResume: () -> Unit,
    onNewGame: () -> Unit,
    onSettings: () -> Unit,
    onMainMenu: () -> Unit,
) {
    WoodScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            NobleTitlePlaque(
                title = localizedString(R.string.pause),
                modifier = Modifier.widthIn(max = 390.dp),
            )
            Spacer(Modifier.height(24.dp))
            NobleWoodPanel(
                modifier = Modifier
                    .widthIn(max = 390.dp)
                    .fillMaxWidth(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NobleButton(localizedString(R.string.resume), onResume, Modifier.fillMaxWidth())
                    NobleButton(
                        localizedString(R.string.new_game),
                        onNewGame,
                        Modifier.fillMaxWidth(),
                        style = NobleButtonStyle.Secondary,
                    )
                    NobleButton(
                        localizedString(R.string.settings),
                        onSettings,
                        Modifier.fillMaxWidth(),
                        style = NobleButtonStyle.Secondary,
                    )
                    NobleButton(
                        localizedString(R.string.main_menu),
                        onMainMenu,
                        Modifier.fillMaxWidth(),
                        style = NobleButtonStyle.Danger,
                    )
                }
            }
        }
    }
}
