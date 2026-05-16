package com.example.noblenumbers.game.model

data class Tile(
    val id: Long,
    val value: Int,
    val row: Int,
    val column: Int,
    val isFrozen: Boolean = false,
    val frozenMovesRemaining: Int = 0,
)
