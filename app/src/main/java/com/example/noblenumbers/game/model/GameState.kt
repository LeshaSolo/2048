package com.example.noblenumbers.game.model

data class GameState(
    val board: Board = Board(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val targetReached: Boolean = false,
    val frozenModeEnabled: Boolean = false,
    val gameOver: Boolean = false,
    val continueAvailable: Boolean = false,
    val continueUsed: Boolean = false,
    val extraMovesRemaining: Int = 0,
    val nextTileId: Long = 1L,
) {
    val maxTile: Int = board.maxTileValue()
}
