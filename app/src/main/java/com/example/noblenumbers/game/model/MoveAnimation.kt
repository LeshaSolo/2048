package com.example.noblenumbers.game.model

data class MoveAnimation(
    val direction: MoveDirection,
    val motions: List<TileMotion>,
    val merges: List<TileMerge>,
    val spawnedTileId: Long? = null,
)

data class TileMotion(
    val tileId: Long,
    val value: Int,
    val fromRow: Int,
    val fromColumn: Int,
    val toRow: Int,
    val toColumn: Int,
    val isFrozen: Boolean,
    val frozenMovesRemaining: Int,
)

data class TileMerge(
    val sourceTileId: Long,
    val targetTileId: Long,
    val resultTileId: Long,
    val resultValue: Int,
    val row: Int,
    val column: Int,
)
