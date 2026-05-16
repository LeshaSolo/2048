package com.example.noblenumbers.game.logic

import com.example.noblenumbers.game.model.Board
import com.example.noblenumbers.game.model.GameState
import com.example.noblenumbers.game.model.MoveDirection
import com.example.noblenumbers.game.model.Tile

class GameEngine(
    randomProvider: RandomProvider = KotlinRandomProvider(),
    private val moveResolver: MoveResolver = MoveResolver(),
    private val gameOverChecker: GameOverChecker = GameOverChecker(),
) {
    private val tileSpawner = TileSpawner(randomProvider)

    fun newGame(bestScore: Int = 0): GameState {
        var board = Board()
        var nextId = 1L
        repeat(tileSpawner.startingTileCount()) {
            val spawn = tileSpawner.spawnTile(board, nextId, frozenModeEnabled = false)
            board = spawn.board
            nextId = spawn.nextTileId
        }
        return GameState(
            board = board,
            bestScore = bestScore,
            nextTileId = nextId,
            continueAvailable = false,
        )
    }

    fun move(state: GameState, direction: MoveDirection): EngineEvent {
        if (state.gameOver && state.extraMovesRemaining <= 0) return EngineEvent(state, MoveOutcome.Blocked)

        val move = moveResolver.move(state.board, direction, state.nextTileId)
        if (!move.moved) {
            val continuedState = if (state.extraMovesRemaining > 0) {
                val remaining = state.extraMovesRemaining - 1
                state.copy(
                    extraMovesRemaining = remaining,
                    gameOver = remaining == 0 && gameOverChecker.isGameOver(state.board),
                    continueAvailable = false,
                )
            } else {
                state
            }
            return EngineEvent(continuedState, MoveOutcome.Blocked)
        }

        val score = state.score + move.scoreDelta
        val targetReached = state.targetReached || move.createdTarget
        val countdownBoard = decrementFrozenTiles(move.board)
        val spawn = tileSpawner.spawnTile(
            board = countdownBoard,
            nextTileId = move.nextTileId,
            frozenModeEnabled = targetReached,
        )
        val gameOver = gameOverChecker.isGameOver(spawn.board)
        val extraMovesRemaining = if (move.moved && state.extraMovesRemaining > 0) 0 else state.extraMovesRemaining
        val updatedState = state.copy(
            board = spawn.board,
            score = score,
            bestScore = maxOf(state.bestScore, score),
            targetReached = targetReached,
            frozenModeEnabled = targetReached,
            gameOver = gameOver,
            continueAvailable = gameOver && !state.continueUsed,
            extraMovesRemaining = extraMovesRemaining,
            nextTileId = spawn.nextTileId,
        )

        return EngineEvent(
            state = updatedState,
            outcome = MoveOutcome.Success(
                merged = move.merged,
                targetReachedNow = targetReached && !state.targetReached,
                frozenTileSpawned = spawn.spawnedTile?.isFrozen == true,
                gameOverNow = gameOver && !state.gameOver,
                spawnedTileId = spawn.spawnedTile?.id,
                animation = move.animation.copy(spawnedTileId = spawn.spawnedTile?.id),
            ),
        )
    }

    fun grantContinue(state: GameState): GameState {
        if (state.continueUsed) return state
        return state.copy(
            gameOver = false,
            continueAvailable = false,
            continueUsed = true,
            extraMovesRemaining = CONTINUE_EXTRA_MOVES,
        )
    }

    private fun decrementFrozenTiles(board: Board): Board = board.copy(
        tiles = board.tiles.map { tile ->
            if (!tile.isFrozen) {
                tile
            } else {
                val remaining = tile.frozenMovesRemaining - 1
                if (remaining <= 0) {
                    tile.copy(isFrozen = false, frozenMovesRemaining = 0)
                } else {
                    tile.copy(frozenMovesRemaining = remaining)
                }
            }
        },
    )

    companion object {
        const val TARGET_TILE = 4096
        const val CONTINUE_EXTRA_MOVES = 5
    }
}

data class EngineEvent(
    val state: GameState,
    val outcome: MoveOutcome,
)

sealed interface MoveOutcome {
    data object Blocked : MoveOutcome

    data class Success(
        val merged: Boolean,
        val targetReachedNow: Boolean,
        val frozenTileSpawned: Boolean,
        val gameOverNow: Boolean,
        val spawnedTileId: Long?,
        val animation: com.example.noblenumbers.game.model.MoveAnimation,
    ) : MoveOutcome
}
