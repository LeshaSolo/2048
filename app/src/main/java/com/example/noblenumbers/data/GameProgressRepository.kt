package com.example.noblenumbers.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.noblenumbers.game.model.Board
import com.example.noblenumbers.game.model.GameState
import com.example.noblenumbers.game.model.Tile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.gameProgressDataStore: DataStore<Preferences> by preferencesDataStore(name = "noble_game_progress")

class GameProgressRepository(
    context: Context,
) {
    private val dataStore = context.gameProgressDataStore

    val savedGame: Flow<GameState?> = dataStore.data.map { preferences ->
        preferences[SAVED_GAME]?.let { encoded ->
            runCatching { decodeGame(encoded) }.getOrNull()
        }
    }

    val recentScores: Flow<List<Int>> = dataStore.data.map { preferences ->
        preferences[RECENT_SCORES]
            ?.let { encoded -> runCatching { decodeScores(encoded) }.getOrNull() }
            ?: emptyList()
    }

    suspend fun saveGame(game: GameState) {
        if (game.gameOver) {
            clearSavedGame()
            return
        }
        dataStore.edit { preferences ->
            preferences[SAVED_GAME] = encodeGame(game)
        }
    }

    suspend fun clearSavedGame() {
        dataStore.edit { preferences ->
            preferences.remove(SAVED_GAME)
        }
    }

    suspend fun addRecentScore(score: Int) {
        dataStore.edit { preferences ->
            val current = preferences[RECENT_SCORES]
                ?.let { encoded -> runCatching { decodeScores(encoded) }.getOrNull() }
                ?: emptyList()
            preferences[RECENT_SCORES] = encodeScores((listOf(score) + current).take(MAX_RECENT_SCORES))
        }
    }

    private fun encodeGame(game: GameState): String {
        val tiles = JSONArray()
        game.board.tiles.forEach { tile ->
            tiles.put(
                JSONObject()
                    .put("id", tile.id)
                    .put("value", tile.value)
                    .put("row", tile.row)
                    .put("column", tile.column)
                    .put("isFrozen", tile.isFrozen)
                    .put("frozenMovesRemaining", tile.frozenMovesRemaining),
            )
        }
        return JSONObject()
            .put("score", game.score)
            .put("bestScore", game.bestScore)
            .put("targetReached", game.targetReached)
            .put("frozenModeEnabled", game.frozenModeEnabled)
            .put("gameOver", game.gameOver)
            .put("continueAvailable", game.continueAvailable)
            .put("continueUsed", game.continueUsed)
            .put("extraMovesRemaining", game.extraMovesRemaining)
            .put("nextTileId", game.nextTileId)
            .put("tiles", tiles)
            .toString()
    }

    private fun decodeGame(encoded: String): GameState {
        val json = JSONObject(encoded)
        val tilesJson = json.getJSONArray("tiles")
        val tiles = buildList {
            for (index in 0 until tilesJson.length()) {
                val tile = tilesJson.getJSONObject(index)
                add(
                    Tile(
                        id = tile.getLong("id"),
                        value = tile.getInt("value"),
                        row = tile.getInt("row"),
                        column = tile.getInt("column"),
                        isFrozen = tile.optBoolean("isFrozen", false),
                        frozenMovesRemaining = tile.optInt("frozenMovesRemaining", 0),
                    ),
                )
            }
        }
        return GameState(
            board = Board(tiles = tiles),
            score = json.getInt("score"),
            bestScore = json.getInt("bestScore"),
            targetReached = json.optBoolean("targetReached", false),
            frozenModeEnabled = json.optBoolean("frozenModeEnabled", false),
            gameOver = json.optBoolean("gameOver", false),
            continueAvailable = json.optBoolean("continueAvailable", false),
            continueUsed = json.optBoolean("continueUsed", false),
            extraMovesRemaining = json.optInt("extraMovesRemaining", 0),
            nextTileId = json.getLong("nextTileId"),
        )
    }

    private fun encodeScores(scores: List<Int>): String {
        val array = JSONArray()
        scores.forEach(array::put)
        return array.toString()
    }

    private fun decodeScores(encoded: String): List<Int> {
        val array = JSONArray(encoded)
        return buildList {
            for (index in 0 until array.length()) {
                add(array.getInt(index))
            }
        }
    }

    private companion object {
        const val MAX_RECENT_SCORES = 10
        val SAVED_GAME = stringPreferencesKey("saved_game")
        val RECENT_SCORES = stringPreferencesKey("recent_scores")
    }
}
