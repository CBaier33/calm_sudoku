package com.cbaier33.sudoku.game

import androidx.compose.runtime.Immutable
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.CELLS
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.SIZE

/**
 * A game put down mid-play. Everything needed to carry on: the grid (with its
 * solution, so resuming needs no solver), the mistakes spent so far and the
 * clock.
 *
 * The selection is deliberately not saved - resuming with a highlighted cell
 * the player chose ten minutes ago is noise, and on E Ink it costs a redraw.
 */
@Immutable
data class SavedGame(
    val elapsed: Int,
    val mistakes: Int,
    val cells: List<CellItem>,
)

/** Just enough of a save to label the options screen's Resume button. */
@Immutable
data class SavedGameSummary(val elapsed: Int)

/**
 * One save slot per difficulty. Saving overwrites the slot for that difficulty;
 * nothing else touches it.
 *
 * An interface so the game view model can be exercised without an Android
 * context, exactly as [com.cbaier33.sudoku.stats.StatsSink] is.
 */
interface SavedGameStore {
    suspend fun load(difficulty: Difficulty): SavedGame?
    suspend fun save(difficulty: Difficulty, game: SavedGame)
    suspend fun clear(difficulty: Difficulty)
}

/**
 * Saves are stored as one string per slot.
 *
 * `version|elapsed|mistakes|cell,cell,...` with 81 cells, each packed into a
 * single int. Hand-rolled rather than pulling in kotlinx.serialization for one
 * flat record: the whole board is a fixed-size grid of small integers, and this
 * keeps a slot around 400 bytes.
 *
 * Anything that does not decode cleanly - a truncated write, a slot left by a
 * future [VERSION] - reads back as no save at all, and the next Save & Quit at
 * that difficulty replaces it.
 */
internal object SavedGameCodec {

    const val VERSION = 1

    private const val FIELD = "|"
    private const val CELL = ","

    private const val VALUE_MASK = 0xF
    private const val SOLUTION_SHIFT = 4
    private const val GIVEN_BIT = 1 shl 8
    private const val NOTES_SHIFT = 9

    fun encode(game: SavedGame): String = buildString {
        append(VERSION).append(FIELD)
        append(game.elapsed).append(FIELD)
        append(game.mistakes).append(FIELD)
        game.cells.joinTo(this, CELL) { pack(it).toString() }
    }

    fun decode(text: String): SavedGame? {
        val fields = fields(text) ?: return null

        val elapsed = fields[1].toIntOrNull()?.takeIf { it >= 0 } ?: return null
        val mistakes = fields[2].toIntOrNull()?.takeIf { it >= 0 } ?: return null

        val packed = fields[3].split(CELL)
        if (packed.size != CELLS) return null

        val cells = packed.map { field ->
            val bits = field.toIntOrNull() ?: return null
            unpack(bits) ?: return null
        }

        return SavedGame(elapsed = elapsed, mistakes = mistakes, cells = cells)
    }

    /** Reads the header only - the options screen never needs the grid. */
    fun decodeSummary(text: String): SavedGameSummary? {
        val fields = fields(text) ?: return null
        val elapsed = fields[1].toIntOrNull()?.takeIf { it >= 0 } ?: return null

        return SavedGameSummary(elapsed = elapsed)
    }

    private fun fields(text: String): List<String>? {
        val fields = text.split(FIELD)
        if (fields.size != 4) return null
        if (fields[0].toIntOrNull() != VERSION) return null

        return fields
    }

    /**
     * `value` in bits 0-3, `solution` in bits 4-7, `given` in bit 8, and the
     * pencil marks as a nine-bit set in bits 9-17.
     */
    private fun pack(cell: CellItem): Int {
        var bits = cell.value or (cell.solution shl SOLUTION_SHIFT)
        if (cell.given) bits = bits or GIVEN_BIT

        for (note in cell.notes) bits = bits or (1 shl (NOTES_SHIFT + note - 1))

        return bits
    }

    private fun unpack(bits: Int): CellItem? {
        val value = bits and VALUE_MASK
        val solution = (bits shr SOLUTION_SHIFT) and VALUE_MASK

        // 0 is a legal value - an empty cell - but never a legal solution.
        if (value > SIZE || solution !in 1..SIZE) return null

        val notes = (1..SIZE).filterTo(mutableSetOf()) {
            bits and (1 shl (NOTES_SHIFT + it - 1)) != 0
        }

        return CellItem(
            value = value,
            solution = solution,
            given = bits and GIVEN_BIT != 0,
            notes = notes,
        )
    }
}
