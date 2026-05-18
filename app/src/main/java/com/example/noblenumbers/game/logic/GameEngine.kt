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
        val preview = tileSpawner.nextTileValue()
        return GameState(
            board = board,
            bestScore = bestScore,
            nextTileId = nextId,
            continueAvailable = false,
            nextTilePreview = preview,
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

        val undoSnapshot = if (!state.undoUsed && state.undoSnapshot == null) state else state.undoSnapshot

        val newComboCount = if (move.merged) state.comboCount + 1 else 0
        val comboMultiplier = if (newComboCount > 1) newComboCount - 1 else 0
        val comboBonus = if (comboMultiplier > 0) (move.scoreDelta * comboMultiplier) / 4 else 0
        val totalScoreDelta = move.scoreDelta + comboBonus

        val score = state.score + totalScoreDelta
        val targetReached = state.targetReached || move.createdTarget
        val countdownBoard = decrementFrozenTiles(move.board)
        val spawn = tileSpawner.spawnTile(
            board = countdownBoard,
            nextTileId = move.nextTileId,
            frozenModeEnabled = targetReached,
        )
        val nextPreview = tileSpawner.nextTileValue()
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
            undoSnapshot = undoSnapshot,
            comboCount = newComboCount,
            nextTilePreview = nextPreview,
        )

        return EngineEvent(
            state = updatedState,
            outcome = MoveOutcome.Success(
                merged = move.merged,
                targetReachedNow = targetReached && !state.targetReached,
                frozenTileSpawned = spawn.spawnedTile?.isFrozen == true,
                gameOverNow = gameOver && !state.gameOver,
                spawnedTileId = spawn.spawnedTile?.id,
                animation = move.animation.copy(
                    spawnedTileId = spawn.spawnedTile?.id,
                    scorePopups = move.animation.merges.map { merge ->
                        com.example.noblenumbers.game.model.ScorePopup(
                            value = merge.resultValue,
                            row = merge.row,
                            column = merge.column,
                        )
                    },
                ),
                comboLevel = newComboCount,
            ),
        )
    }

    fun undo(state: GameState): GameState {
        val snapshot = state.undoSnapshot ?: return state
        return snapshot.copy(
            bestScore = state.bestScore,
            undoSnapshot = null,
            undoUsed = true,
            comboCount = 0,
            nextTilePreview = state.nextTilePreview,
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
        val comboLevel: Int = 0,
    ) : MoveOutcome
}
