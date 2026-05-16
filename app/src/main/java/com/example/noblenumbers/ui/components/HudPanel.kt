package com.example.noblenumbers.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.noblenumbers.game.model.GameState

@Composable
fun HudPanel(
    game: GameState,
    modifier: Modifier = Modifier,
) {
    NobleHudPanel(game = game, modifier = modifier)
}
