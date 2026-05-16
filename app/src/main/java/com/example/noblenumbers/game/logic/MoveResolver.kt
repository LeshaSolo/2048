package com.example.noblenumbers.game.logic

import com.example.noblenumbers.game.model.Board
import com.example.noblenumbers.game.model.MoveAnimation
import com.example.noblenumbers.game.model.MoveDirection
import com.example.noblenumbers.game.model.Tile
import com.example.noblenumbers.game.model.TileMerge
import com.example.noblenumbers.game.model.TileMotion

class MoveResolver {
    fun move(board: Board, direction: MoveDirection, nextTileId: Long): MoveResult {
        val resolvedLines = when (direction) {
            MoveDirection.Left -> (0 until board.size).map { row ->
                resolveLine(
                    source = (0 until board.size).map { column -> board.tileAt(row, column) },
                    toRowColumn = { index -> row to index },
                    nextTileId = nextTileId,
                )
            }

            MoveDirection.Right -> (0 until board.size).map { row ->
                resolveLine(
                    source = (board.size - 1 downTo 0).map { column -> board.tileAt(row, column) },
                    toRowColumn = { index -> row to (board.size - 1 - index) },
                    nextTileId = nextTileId,
                )
            }

            MoveDirection.Up -> (0 until board.size).map { column ->
                resolveLine(
                    source = (0 until board.size).map { row -> board.tileAt(row, column) },
                    toRowColumn = { index -> index to column },
                    nextTileId = nextTileId,
                )
            }

            MoveDirection.Down -> (0 until board.size).map { column ->
                resolveLine(
                    source = (board.size - 1 downTo 0).map { row -> board.tileAt(row, column) },
                    toRowColumn = { index -> (board.size - 1 - index) to column },
                    nextTileId = nextTileId,
                )
            }
        }

        val tiles = resolvedLines.flatMap { it.tiles }.sortedWith(compareBy<Tile> { it.row }.thenBy { it.column })
        val scoreDelta = resolvedLines.sumOf { it.scoreDelta }
        val moved = tiles.toComparableSet() != board.tiles.toComparableSet()
        val animation = MoveAnimation(
            direction = direction,
            motions = resolvedLines.flatMap { it.motions },
            merges = resolvedLines.flatMap { it.merges },
        )
        return MoveResult(
            board = board.copy(tiles = tiles),
            scoreDelta = scoreDelta,
            moved = moved,
            nextTileId = nextTileId,
            createdTarget = tiles.any { it.value >= GameEngine.TARGET_TILE } &&
                board.tiles.none { it.value >= GameEngine.TARGET_TILE },
            merged = scoreDelta > 0,
            animation = animation,
        )
    }

    private fun resolveLine(
        source: List<Tile?>,
        toRowColumn: (Int) -> Pair<Int, Int>,
        nextTileId: Long,
    ): LineResult {
        val output = MutableList<TileSlot?>(source.size) { null }
        var cursor = 0
        var scoreDelta = 0
        val motions = mutableListOf<TileMotion>()
        val merges = mutableListOf<TileMerge>()

        source.forEachIndexed { sourceIndex, tile ->
            if (tile == null) return@forEachIndexed

            if (tile.isFrozen) {
                val (row, column) = toRowColumn(sourceIndex)
                motions += tile.toMotion(row = row, column = column)
                output[sourceIndex] = TileSlot(
                    tile = tile.copy(row = row, column = column),
                    canMerge = true,
                )
                cursor = maxOf(cursor, sourceIndex + 1)
                return@forEachIndexed
            }

            val mergeIndex = output.indexOfLast { it != null }
            val mergeTarget = if (mergeIndex >= 0) output[mergeIndex] else null
            if (mergeTarget != null && mergeTarget.canMerge && mergeTarget.tile.value == tile.value) {
                val target = mergeTarget.tile
                val mergedValue = target.value * 2
                motions += tile.toMotion(row = target.row, column = target.column)
                merges += TileMerge(
                    sourceTileId = tile.id,
                    targetTileId = target.id,
                    resultTileId = target.id,
                    resultValue = mergedValue,
                    row = target.row,
                    column = target.column,
                )
                output[mergeIndex] = TileSlot(
                    tile = Tile(
                        id = target.id,
                        value = mergedValue,
                        row = target.row,
                        column = target.column,
                        isFrozen = false,
                        frozenMovesRemaining = 0,
                    ),
                    canMerge = false,
                )
                scoreDelta += mergedValue
            } else {
                while (cursor < output.size && output[cursor] != null) cursor++
                if (cursor < output.size) {
                    val (row, column) = toRowColumn(cursor)
                    motions += tile.toMotion(row = row, column = column)
                    output[cursor] = TileSlot(
                        tile = tile.copy(row = row, column = column, isFrozen = false, frozenMovesRemaining = 0),
                        canMerge = true,
                    )
                    cursor++
                }
            }
        }

        return LineResult(
            tiles = output.mapNotNull { it?.tile },
            scoreDelta = scoreDelta,
            nextTileId = nextTileId,
            motions = motions,
            merges = merges,
        )
    }

    private fun Tile.toMotion(row: Int, column: Int): TileMotion = TileMotion(
        tileId = id,
        value = value,
        fromRow = this.row,
        fromColumn = this.column,
        toRow = row,
        toColumn = column,
        isFrozen = isFrozen,
        frozenMovesRemaining = frozenMovesRemaining,
    )

    private fun List<Tile>.toComparableSet(): Set<ComparableTile> = map {
        ComparableTile(
            value = it.value,
            row = it.row,
            column = it.column,
            isFrozen = it.isFrozen,
            frozenMovesRemaining = it.frozenMovesRemaining,
        )
    }.toSet()
}

private data class TileSlot(
    val tile: Tile,
    val canMerge: Boolean,
)

private data class LineResult(
    val tiles: List<Tile>,
    val scoreDelta: Int,
    val nextTileId: Long,
    val motions: List<TileMotion>,
    val merges: List<TileMerge>,
)

private data class ComparableTile(
    val value: Int,
    val row: Int,
    val column: Int,
    val isFrozen: Boolean,
    val frozenMovesRemaining: Int,
)

data class MoveResult(
    val board: Board,
    val scoreDelta: Int,
    val moved: Boolean,
    val nextTileId: Long,
    val createdTarget: Boolean,
    val merged: Boolean,
    val animation: MoveAnimation,
)
