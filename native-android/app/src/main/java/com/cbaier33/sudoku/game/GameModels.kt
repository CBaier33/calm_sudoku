package com.cbaier33.sudoku.game

import androidx.compose.runtime.Immutable
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.CELLS
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.SIZE

/** [x] is the row, [y] the column - the same convention the Flutter build used. */
@Immutable
data class CellPoint(val x: Int, val y: Int) {
    val index: Int get() = x * SIZE + y
}

/**
 * [value] is 0 for empty, 1..9 for a given, a solved digit, or a wrong guess.
 *
 * Immutable, unlike the Dart CellItem, so StateFlow can tell states apart.
 */
@Immutable
data class CellItem(
    val value: Int,
    val solution: Int,
    val given: Boolean,
    val notes: Set<Int> = emptySet(),
) {
    /**
     * Validation is answer comparison, not rule checking: a digit that is legal
     * against the current grid but is not the solution still counts wrong.
     */
    val correct: Boolean get() = value == solution
}

enum class GameResult { PLAYING, LOST, WON }

@Immutable
data class GameUiState(
    val cells: List<CellItem> = emptyList(),
    val selected: CellPoint? = null,
    val noteMode: Boolean = false,
    val mistakes: Int = 0,
    val mistakesAllowed: Int = 0,
    /** Counts down to zero as the grid gets filled in correctly. */
    val remaining: Int = CELLS,
    val result: GameResult = GameResult.PLAYING,
    /** `digitsRemaining[d]` is how many of digit `d` are still missing. */
    val digitsRemaining: List<Int> = List(SIZE + 1) { SIZE },
    val loading: Boolean = true,
    /**
     * Whether this board is the one sitting in its difficulty's save slot -
     * either resumed from it or written to it. Quitting or finishing such a
     * board empties the slot; any other board leaves it alone.
     */
    val inSaveSlot: Boolean = false,
) {
    fun cellAt(p: CellPoint): CellItem = cells[p.index]

    fun isSelected(p: CellPoint): Boolean = selected == p
}

/**
 * Two cells are peers when one of them being a digit rules that digit out of the
 * other: same row, same column or same box.
 */
fun isPeer(a: CellPoint, b: CellPoint): Boolean {
    if (a == b) return false
    if (a.x == b.x || a.y == b.y) return true

    return a.x / PuzzleGenerator.BOX == b.x / PuzzleGenerator.BOX &&
        a.y / PuzzleGenerator.BOX == b.y / PuzzleGenerator.BOX
}
