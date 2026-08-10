package com.cbaier33.sudoku.stats

import androidx.compose.runtime.Immutable
import com.cbaier33.sudoku.game.Difficulty

@Immutable
data class GameStats(
    val gamesPlayed: Int = 0,
    val wins: Map<Difficulty, Int> = ZEROED,
    val losses: Map<Difficulty, Int> = ZEROED,
    val streak: Int = 0,
    /** Seconds. 0 is the "no record yet" sentinel, shown as NA. */
    val bestTimes: Map<Difficulty, Int> = ZEROED,
) {
    val totalWins: Int get() = wins.values.sum()

    /** Two decimals, or "0" before any game has been played. */
    val winPercentage: String
        get() = if (gamesPlayed == 0) "0" else "%.2f".format(totalWins.toDouble() / gamesPlayed * 100)

    fun winLoss(difficulty: Difficulty): String =
        "${wins[difficulty] ?: 0}/${losses[difficulty] ?: 0}"

    companion object {
        private val ZEROED: Map<Difficulty, Int> = Difficulty.entries.associateWith { 0 }

        val EMPTY = GameStats()
    }
}
