package com.cbaier33.sudoku.stats

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cbaier33.sudoku.game.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.statsDataStore: DataStore<Preferences> by preferencesDataStore(name = "stats")

/**
 * The write side of statistics, so the game view model can be exercised without
 * an Android context.
 */
interface StatsSink {
    suspend fun recordGame(seconds: Int, difficulty: Difficulty, win: Boolean)
}

/**
 * Statistics are the only thing the app persists - never the board, the options
 * or an in-progress game.
 *
 * Key names are carried over verbatim from the Flutter build's
 * `lib/services/stats_service.dart`, which used shared_preferences. Nothing
 * reads the old store; statistics start from zero on this build.
 */
class StatsRepository(private val context: Context) : StatsSink {

    val stats: Flow<GameStats> = context.statsDataStore.data.map { it.toGameStats() }

    suspend fun load(): GameStats = stats.first()

    suspend fun clearAll() {
        context.statsDataStore.edit { prefs ->
            prefs[GAMES_PLAYED] = 0
            prefs[STREAK] = 0
            for (d in Difficulty.entries) {
                prefs[winsKey(d)] = 0
                prefs[lossesKey(d)] = 0
                prefs[bestTimeKey(d)] = 0
            }
        }
    }

    /**
     * On a win: bump the difficulty's win count and the streak, and take the
     * time as a new record when there is no record yet or it beats the old one.
     * On a loss: bump the losses and reset the streak. Either way the game
     * counts as played.
     */
    override suspend fun recordGame(seconds: Int, difficulty: Difficulty, win: Boolean) {
        context.statsDataStore.edit { prefs ->
            prefs[GAMES_PLAYED] = (prefs[GAMES_PLAYED] ?: 0) + 1

            if (win) {
                prefs[winsKey(difficulty)] = (prefs[winsKey(difficulty)] ?: 0) + 1
                prefs[STREAK] = (prefs[STREAK] ?: 0) + 1

                val currentBest = prefs[bestTimeKey(difficulty)] ?: 0
                if (currentBest == 0 || seconds < currentBest) {
                    prefs[bestTimeKey(difficulty)] = seconds
                }
            } else {
                prefs[lossesKey(difficulty)] = (prefs[lossesKey(difficulty)] ?: 0) + 1
                prefs[STREAK] = 0
            }
        }
    }

    private fun Preferences.toGameStats() = GameStats(
        gamesPlayed = this[GAMES_PLAYED] ?: 0,
        streak = this[STREAK] ?: 0,
        wins = Difficulty.entries.associateWith { this[winsKey(it)] ?: 0 },
        losses = Difficulty.entries.associateWith { this[lossesKey(it)] ?: 0 },
        bestTimes = Difficulty.entries.associateWith { this[bestTimeKey(it)] ?: 0 },
    )

    private companion object {
        val GAMES_PLAYED = intPreferencesKey("gamesPlayed")
        val STREAK = intPreferencesKey("streak")

        // easy / medium / hard, matching the Dart enum's `name`.
        fun slug(d: Difficulty) = d.name.lowercase()

        fun winsKey(d: Difficulty) = intPreferencesKey("wins_${slug(d)}")
        fun lossesKey(d: Difficulty) = intPreferencesKey("losses_${slug(d)}")
        fun bestTimeKey(d: Difficulty) = intPreferencesKey("bestTime_${slug(d)}")
    }
}
