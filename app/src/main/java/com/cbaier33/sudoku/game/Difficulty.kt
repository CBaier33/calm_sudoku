package com.cbaier33.sudoku.game

/**
 * Difficulty affects only how much of the grid starts filled in and how many
 * wrong digits are forgiven. The techniques a puzzle requires are never
 * analysed - this matches the Flutter build exactly.
 */
enum class Difficulty(
    val label: String,
    /** How many cells the puzzle starts with filled in. */
    val givens: Int,
    /** Wrong digits allowed before the game is lost. */
    val mistakes: Int,
) {
    EASY("Easy", givens = 40, mistakes = 5),
    MEDIUM("Medium", givens = 32, mistakes = 3),
    HARD("Hard", givens = 26, mistakes = 3);

    companion object {
        fun fromName(name: String?): Difficulty =
            entries.firstOrNull { it.name == name } ?: EASY
    }
}
