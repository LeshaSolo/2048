package com.example.noblenumbers.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.noblenumbers.R
import com.example.noblenumbers.game.model.GameState
import com.example.noblenumbers.game.model.MoveDirection
import com.example.noblenumbers.game.model.ScorePopup
import com.example.noblenumbers.ui.components.NobleButton
import com.example.noblenumbers.ui.components.NobleButtonStyle
import com.example.noblenumbers.ui.components.NobleGameBoard
import com.example.noblenumbers.ui.components.NobleHudPanel
import com.example.noblenumbers.ui.components.nobleSwipeInput
import com.example.noblenumbers.ui.localizedString
import com.example.noblenumbers.ui.model.BoardTileUi

@Composable
fun GameScreen(
    game: GameState,
    displayTiles: List<BoardTileUi>?,
    scorePopups: List<ScorePopup> = emptyList(),
    onSwipe: (MoveDirection) -> Unit,
    onNewGame: () -> Unit,
    onUndo: () -> Unit,
    onSettings: () -> Unit,
    onPause: () -> Unit,
) {
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 48.dp.toPx() }

    WoodScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nobleSwipeInput(onSwipe = onSwipe, swipeThresholdPx = swipeThresholdPx),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NobleHudPanel(game = game, modifier = Modifier.fillMaxWidth())
            NobleGameBoard(
                board = game.board,
                displayTiles = displayTiles,
                onSwipe = onSwipe,
                scorePopups = scorePopups,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                NobleButton(
                    text = localizedString(R.string.undo),
                    onClick = onUndo,
                    enabled = game.undoSnapshot != null,
                    modifier = Modifier.weight(1f),
                    style = NobleButtonStyle.Secondary,
                )
                NobleButton(
                    text = localizedString(R.string.new_game),
                    onClick = onNewGame,
                    modifier = Modifier.weight(1f),
                    style = NobleButtonStyle.Secondary,
                )
                NobleButton(
                    text = localizedString(R.string.pause),
                    onClick = onPause,
                    modifier = Modifier.weight(1f),
                    style = NobleButtonStyle.Primary,
                )
            }
        }
    }
}
