package com.cbaier33.sudoku.game

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.savedGamesDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "saved_games")

/**
 * Three save slots, one per difficulty, in their own store so a corrupt or
 * cleared board can never take the statistics with it.
 *
 * A slot is written by Save & Quit, replaced by the next Save & Quit at that
 * difficulty, and removed when the game that occupied it is won or lost.
 * Starting a new game deliberately leaves the slot alone: nothing but an
 * explicit save should ever overwrite one.
 */
class SavedGameRepository(private val context: Context) : SavedGameStore {

    /** Which difficulties can be resumed, and how long each has been played. */
    val summaries: Flow<Map<Difficulty, SavedGameSummary>> =
        context.savedGamesDataStore.data.map { prefs ->
            Difficulty.entries.mapNotNull { difficulty ->
                val text = prefs[key(difficulty)] ?: return@mapNotNull null
                val summary = SavedGameCodec.decodeSummary(text) ?: return@mapNotNull null

                difficulty to summary
            }.toMap()
        }

    override suspend fun load(difficulty: Difficulty): SavedGame? {
        val text = context.savedGamesDataStore.data.first()[key(difficulty)] ?: return null

        return SavedGameCodec.decode(text)
    }

    override suspend fun save(difficulty: Difficulty, game: SavedGame) {
        context.savedGamesDataStore.edit { prefs ->
            prefs[key(difficulty)] = SavedGameCodec.encode(game)
        }
    }

    override suspend fun clear(difficulty: Difficulty) {
        context.savedGamesDataStore.edit { prefs -> prefs.remove(key(difficulty)) }
    }

    private companion object {
        // game_easy / game_medium / game_hard.
        fun key(d: Difficulty) = stringPreferencesKey("game_${d.name.lowercase()}")
    }
}
