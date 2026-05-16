package com.example.noblenumbers.ui.model

import com.example.noblenumbers.game.model.Tile

data class BoardTileUi(
    val id: Long,
    val value: Int,
    val row: Int,
    val column: Int,
    val isFrozen: Boolean = false,
    val frozenMovesRemaining: Int = 0,
    val isMoving: Boolean = false,
    val isMerging: Boolean = false,
    val isSpawning: Boolean = false,
)

fun Tile.toBoardTileUi(
    isMerging: Boolean = false,
    isSpawning: Boolean = false,
): BoardTileUi = BoardTileUi(
    id = id,
    value = value,
    row = row,
    column = column,
    isFrozen = isFrozen,
    frozenMovesRemaining = frozenMovesRemaining,
    isMerging = isMerging,
    isSpawning = isSpawning,
)
