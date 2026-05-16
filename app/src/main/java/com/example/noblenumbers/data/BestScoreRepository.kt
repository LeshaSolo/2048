package com.example.noblenumbers.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bestScoreDataStore: DataStore<Preferences> by preferencesDataStore(name = "noble_best_score")

class BestScoreRepository(
    context: Context,
) {
    private val dataStore = context.bestScoreDataStore

    val bestScore: Flow<Int> = dataStore.data.map { it[BEST_SCORE] ?: 0 }

    suspend fun saveBestScore(score: Int) {
        dataStore.edit { preferences ->
            if (score > (preferences[BEST_SCORE] ?: 0)) {
                preferences[BEST_SCORE] = score
            }
        }
    }

    private companion object {
        val BEST_SCORE = intPreferencesKey("best_score")
    }
}
