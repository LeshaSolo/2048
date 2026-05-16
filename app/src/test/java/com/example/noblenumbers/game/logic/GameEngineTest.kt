package com.example.noblenumbers.game.logic

import com.example.noblenumbers.game.model.Board
import com.example.noblenumbers.game.model.GameState
import com.example.noblenumbers.game.model.MoveDirection
import com.example.noblenumbers.game.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {
    private val resolver = MoveResolver()
    private val gameOverChecker = GameOverChecker()

    @Test
    fun newBoardIs5x5() {
        assertEquals(5, Board().size)
    }

    @Test
    fun newGameStartsWith2To4Tiles() {
        val state = GameEngine(FixedRandom(ints = listOf(0, 0, 1))).newGame()

        assertTrue(state.board.tiles.size in 2..4)
    }

    @Test
    fun spawnedTileValueIsOnly2Or4() {
        val spawner = TileSpawner(FixedRandom(floats = listOf(0.1f, 0.9f)))

        assertEquals(2, spawner.nextTileValue())
        assertEquals(4, spawner.nextTileValue())
    }

    @Test
    fun moveLeftMergesCorrectly() {
        val result = resolver.move(boardOf(tile(1, 2, 0, 0), tile(2, 2, 0, 1)), MoveDirection.Left, 3)

        assertTile(result.board, row = 0, column = 0, value = 4)
    }

    @Test
    fun moveRightMergesCorrectly() {
        val result = resolver.move(boardOf(tile(1, 2, 0, 3), tile(2, 2, 0, 4)), MoveDirection.Right, 3)

        assertTile(result.board, row = 0, column = 4, value = 4)
    }

    @Test
    fun mergeRightKeepsRightmostTileIdentityForDirectionalAnimation() {
        val result = resolver.move(boardOf(tile(1, 2, 0, 3), tile(2, 2, 0, 4)), MoveDirection.Right, 3)

        assertEquals(2L, result.board.tileAt(0, 4)?.id)
    }

    @Test
    fun moveUpMergesCorrectly() {
        val result = resolver.move(boardOf(tile(1, 2, 0, 0), tile(2, 2, 1, 0)), MoveDirection.Up, 3)

        assertTile(result.board, row = 0, column = 0, value = 4)
    }

    @Test
    fun moveDownMergesCorrectly() {
        val result = resolver.move(boardOf(tile(1, 2, 3, 0), tile(2, 2, 4, 0)), MoveDirection.Down, 3)

        assertTile(result.board, row = 4, column = 0, value = 4)
    }

    @Test
    fun animationMotionsNeverMoveOppositeToSwipeDirection() {
        val board = boardOf(
            tile(1, 2, 1, 1),
            tile(2, 2, 1, 3),
            tile(3, 4, 3, 2),
        )

        assertNoOppositeMotion(resolver.move(board, MoveDirection.Left, 4), MoveDirection.Left)
        assertNoOppositeMotion(resolver.move(board, MoveDirection.Right, 4), MoveDirection.Right)
        assertNoOppositeMotion(resolver.move(board, MoveDirection.Up, 4), MoveDirection.Up)
        assertNoOppositeMotion(resolver.move(board, MoveDirection.Down, 4), MoveDirection.Down)
    }

    @Test
    fun mergeDownKeepsBottomTileIdentityForDirectionalAnimation() {
        val result = resolver.move(boardOf(tile(1, 2, 3, 0), tile(2, 2, 4, 0)), MoveDirection.Down, 3)

        assertEquals(2L, result.board.tileAt(4, 0)?.id)
    }

    @Test
    fun tileCannotMergeTwiceInOneMove() {
        val result = resolver.move(
            boardOf(tile(1, 2, 0, 0), tile(2, 2, 0, 1), tile(3, 4, 0, 2)),
            MoveDirection.Left,
            4,
        )

        assertTile(result.board, row = 0, column = 0, value = 4)
        assertTile(result.board, row = 0, column = 1, value = 4)
        assertEquals(2, result.board.tiles.size)
    }

    @Test
    fun threeEqualTilesMergeOnlyLeadingPairWhenMovingLeft() {
        val result = resolver.move(
            boardOf(tile(1, 2, 0, 0), tile(2, 2, 0, 1), tile(3, 2, 0, 2)),
            MoveDirection.Left,
            4,
        )

        assertTile(result.board, row = 0, column = 0, value = 4)
        assertTile(result.board, row = 0, column = 1, value = 2)
        assertEquals(2, result.board.tiles.size)
    }

    @Test
    fun threeEqualTilesMergeOnlyLeadingPairWhenMovingRight() {
        val result = resolver.move(
            boardOf(tile(1, 2, 0, 0), tile(2, 2, 0, 1), tile(3, 2, 0, 2)),
            MoveDirection.Right,
            4,
        )

        assertTile(result.board, row = 0, column = 3, value = 2)
        assertTile(result.board, row = 0, column = 4, value = 4)
        assertEquals(2, result.board.tiles.size)
    }

    @Test
    fun fourEqualTilesBecomeTwoMergedTilesInOneMove() {
        val result = resolver.move(
            boardOf(tile(1, 2, 0, 0), tile(2, 2, 0, 1), tile(3, 2, 0, 2), tile(4, 2, 0, 3)),
            MoveDirection.Left,
            5,
        )

        assertTile(result.board, row = 0, column = 0, value = 4)
        assertTile(result.board, row = 0, column = 1, value = 4)
        assertEquals(8, result.scoreDelta)
    }

    @Test
    fun equalTilesMergeAcrossEmptyCellsWhenSliding() {
        val result = resolver.move(
            boardOf(tile(1, 2, 0, 0), tile(2, 2, 0, 4)),
            MoveDirection.Left,
            3,
        )

        assertTile(result.board, row = 0, column = 0, value = 4)
        assertEquals(1, result.board.tiles.size)
    }

    @Test
    fun newlyMergedTileDoesNotMergeAgainInSameMove() {
        val result = resolver.move(
            boardOf(tile(1, 2, 0, 0), tile(2, 2, 0, 1), tile(3, 4, 0, 2), tile(4, 4, 0, 3)),
            MoveDirection.Left,
            5,
        )

        assertTile(result.board, row = 0, column = 0, value = 4)
        assertTile(result.board, row = 0, column = 1, value = 8)
        assertEquals(12, result.scoreDelta)
    }

    @Test
    fun mergeOrderFollowsMoveDirectionWhenMovingDown() {
        val result = resolver.move(
            boardOf(tile(1, 2, 0, 0), tile(2, 2, 1, 0), tile(3, 2, 2, 0)),
            MoveDirection.Down,
            4,
        )

        assertTile(result.board, row = 3, column = 0, value = 2)
        assertTile(result.board, row = 4, column = 0, value = 4)
    }

    @Test
    fun scoreIncreasesByMergedTileValue() {
        val result = resolver.move(boardOf(tile(1, 128, 0, 0), tile(2, 128, 0, 1)), MoveDirection.Left, 3)

        assertEquals(256, result.scoreDelta)
    }

    @Test
    fun creationOf4096EnablesFrozenMode() {
        val engine = GameEngine(FixedRandom(ints = listOf(0), floats = listOf(0.9f, 0.1f)))
        val state = GameState(
            board = boardOf(tile(1, 2048, 0, 0), tile(2, 2048, 0, 1)),
            nextTileId = 3,
        )

        val result = engine.move(state, MoveDirection.Left).state

        assertTrue(result.targetReached)
        assertTrue(result.frozenModeEnabled)
    }

    @Test
    fun before4096NewTilesAreNeverFrozen() {
        val spawner = TileSpawner(FixedRandom(ints = listOf(0), floats = listOf(0.0f)))
        val result = spawner.spawnTile(Board(), nextTileId = 1, frozenModeEnabled = false)

        assertFalse(result.spawnedTile!!.isFrozen)
    }

    @Test
    fun after4096FrozenTileSpawningIsPossible() {
        val spawner = TileSpawner(FixedRandom(ints = listOf(0), floats = listOf(0.1f, 0.1f)))
        val result = spawner.spawnTile(Board(), nextTileId = 1, frozenModeEnabled = true)

        assertTrue(result.spawnedTile!!.isFrozen)
    }

    @Test
    fun frozenTileDoesNotMove() {
        val result = resolver.move(
            boardOf(tile(1, 2, 0, 2, frozen = true, moves = 3)),
            MoveDirection.Left,
            2,
        )

        assertTile(result.board, row = 0, column = 2, value = 2, frozen = true)
    }

    @Test
    fun frozenTileCountdownDecreasesAfterSuccessfulMove() {
        val engine = GameEngine(FixedRandom(ints = listOf(0), floats = listOf(0.9f)))
        val state = GameState(
            board = boardOf(tile(1, 2, 0, 2, frozen = true, moves = 3), tile(2, 4, 4, 4)),
            targetReached = true,
            frozenModeEnabled = true,
            nextTileId = 3,
        )

        val result = engine.move(state, MoveDirection.Left).state

        assertEquals(2, result.board.tileAt(0, 2)!!.frozenMovesRemaining)
    }

    @Test
    fun frozenTileBecomesNormalAfter3SuccessfulMoves() {
        var state = GameState(
            board = boardOf(tile(1, 2, 0, 2, frozen = true, moves = 3), tile(2, 4, 4, 4)),
            targetReached = true,
            frozenModeEnabled = true,
            nextTileId = 3,
        )
        val engine = GameEngine(FixedRandom(ints = listOf(0, 0, 0), floats = listOf(0.9f, 0.9f, 0.9f)))

        state = engine.move(state, MoveDirection.Left).state
        state = engine.move(state, MoveDirection.Right).state
        state = engine.move(state, MoveDirection.Left).state

        assertFalse(state.board.tileAt(0, 2)!!.isFrozen)
    }

    @Test
    fun normalTileCanMergeIntoFrozenTileOfSameValue() {
        val result = resolver.move(
            boardOf(tile(1, 2, 0, 0, frozen = true, moves = 3), tile(2, 2, 0, 1)),
            MoveDirection.Left,
            3,
        )

        assertTile(result.board, row = 0, column = 0, value = 4)
    }

    @Test
    fun resultOfMergeWithFrozenTileIsNormal() {
        val result = resolver.move(
            boardOf(tile(1, 2, 0, 0, frozen = true, moves = 3), tile(2, 2, 0, 1)),
            MoveDirection.Left,
            3,
        )

        assertFalse(result.board.tileAt(0, 0)!!.isFrozen)
    }

    @Test
    fun gameOverTriggersWhenFullAndNoMergesExist() {
        assertTrue(gameOverChecker.isGameOver(fullBoardWithoutMerges()))
    }

    @Test
    fun gameOverDoesNotTriggerWhenAdjacentSameValueTilesExist() {
        val board = fullBoardWithoutMerges().copy(
            tiles = fullBoardWithoutMerges().tiles.filterNot { it.row == 0 && it.column in 0..1 } +
                tile(100, 2, 0, 0) +
                tile(101, 2, 0, 1),
        )

        assertFalse(gameOverChecker.isGameOver(board))
    }

    @Test
    fun frozenTileIsMergeCompatibleForGameOverCheck() {
        val board = fullBoardWithoutMerges().copy(
            tiles = fullBoardWithoutMerges().tiles.filterNot { it.row == 0 && it.column in 0..1 } +
                tile(100, 2, 0, 0, frozen = true, moves = 3) +
                tile(101, 2, 0, 1),
        )

        assertFalse(gameOverChecker.isGameOver(board))
    }

    @Test
    fun continueGrants5ExtraMoves() {
        val state = GameState(gameOver = true)

        val continued = GameEngine().grantContinue(state)

        assertEquals(5, continued.extraMovesRemaining)
        assertFalse(continued.gameOver)
    }

    @Test
    fun continueCanBeUsedOnlyOnce() {
        val engine = GameEngine()
        val once = engine.grantContinue(GameState(gameOver = true))
        val twice = engine.grantContinue(once.copy(gameOver = true, extraMovesRemaining = 0))

        assertEquals(0, twice.extraMovesRemaining)
        assertTrue(twice.continueUsed)
    }

    private fun assertTile(
        board: Board,
        row: Int,
        column: Int,
        value: Int,
        frozen: Boolean = false,
    ) {
        val tile = board.tileAt(row, column)
        assertEquals(value, tile?.value)
        assertEquals(frozen, tile?.isFrozen)
    }

    private fun assertNoOppositeMotion(result: MoveResult, direction: MoveDirection) {
        result.animation.motions.forEach { motion ->
            when (direction) {
                MoveDirection.Left -> assertTrue(motion.toColumn <= motion.fromColumn)
                MoveDirection.Right -> assertTrue(motion.toColumn >= motion.fromColumn)
                MoveDirection.Up -> assertTrue(motion.toRow <= motion.fromRow)
                MoveDirection.Down -> assertTrue(motion.toRow >= motion.fromRow)
            }
        }
    }

    private fun tile(
        id: Long,
        value: Int,
        row: Int,
        column: Int,
        frozen: Boolean = false,
        moves: Int = 0,
    ): Tile = Tile(id, value, row, column, frozen, moves)

    private fun boardOf(vararg tiles: Tile): Board = Board(tiles = tiles.toList())

    private fun fullBoardWithoutMerges(): Board {
        val values = listOf(
            listOf(2, 4, 2, 4, 2),
            listOf(4, 2, 4, 2, 4),
            listOf(2, 4, 2, 4, 2),
            listOf(4, 2, 4, 2, 4),
            listOf(2, 4, 2, 4, 8),
        )
        var id = 1L
        return Board(
            tiles = values.flatMapIndexed { row, line ->
                line.mapIndexed { column, value -> tile(id++, value, row, column) }
            },
        )
    }
}

private class FixedRandom(
    private val ints: List<Int> = emptyList(),
    private val floats: List<Float> = emptyList(),
) : RandomProvider {
    private var intIndex = 0
    private var floatIndex = 0

    override fun nextInt(bound: Int): Int {
        val value = ints.getOrElse(intIndex++) { 0 }
        return value.floorMod(bound)
    }

    override fun nextFloat(): Float = floats.getOrElse(floatIndex++) { 0.1f }

    private fun Int.floorMod(bound: Int): Int = ((this % bound) + bound) % bound
}
