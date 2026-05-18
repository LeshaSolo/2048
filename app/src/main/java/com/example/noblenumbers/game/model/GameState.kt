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
    val undoSnapshot: GameState? = null,
    val undoUsed: Boolean = false,
    val comboCount: Int = 0,
    val nextTilePreview: Int? = null,
    val mergeCount: Int = 0,
    val maxTileReached: Int = 0,
    val longestCombo: Int = 0,
) {
    val maxTile: Int = board.maxTileValue()

    val gameOverRating: String
        get() = when {
            maxTileReached >= 4096 -> "Noble Legend"
            maxTileReached >= 2048 -> "Noble Master"
            maxTileReached >= 1024 -> "Royal Strategist"
            maxTileReached >= 512 -> "Grand Tactician"
            maxTileReached >= 256 -> "Knight of Order"
            maxTileReached >= 128 -> "Squire"
            else -> "Apprentice"
        }
}
