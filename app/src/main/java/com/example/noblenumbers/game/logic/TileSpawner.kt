package com.example.noblenumbers.game.logic

import com.example.noblenumbers.game.model.Board
import com.example.noblenumbers.game.model.Tile

class TileSpawner(
    private val randomProvider: RandomProvider,
) {
    fun startingTileCount(): Int = 2 + randomProvider.nextInt(3)

    fun nextTileValue(): Int = if (randomProvider.nextFloat() < TWO_TILE_CHANCE) 2 else 4

    fun spawnTile(
        board: Board,
        nextTileId: Long,
        frozenModeEnabled: Boolean,
    ): SpawnResult {
        val emptyCells = board.emptyCells()
        if (emptyCells.isEmpty()) return SpawnResult(board, nextTileId, null)

        val cell = emptyCells[randomProvider.nextInt(emptyCells.size)]
        val frozen = frozenModeEnabled && randomProvider.nextFloat() < FROZEN_TILE_CHANCE
        val tile = Tile(
            id = nextTileId,
            value = nextTileValue(),
            row = cell.row,
            column = cell.column,
            isFrozen = frozen,
            frozenMovesRemaining = if (frozen) FROZEN_MOVE_COUNT else 0,
        )
        return SpawnResult(
            board = board.copy(tiles = board.tiles + tile),
            nextTileId = nextTileId + 1,
            spawnedTile = tile,
        )
    }

    companion object {
        const val FROZEN_MOVE_COUNT = 3
        private const val TWO_TILE_CHANCE = 0.8f
        private const val FROZEN_TILE_CHANCE = 0.2f
    }
}

data class SpawnResult(
    val board: Board,
    val nextTileId: Long,
    val spawnedTile: Tile?,
)
