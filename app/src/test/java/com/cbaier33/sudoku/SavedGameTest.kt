package com.cbaier33.sudoku

import com.cbaier33.sudoku.game.CellItem
import com.cbaier33.sudoku.game.Difficulty
import com.cbaier33.sudoku.game.GameResult
import com.cbaier33.sudoku.game.GameUiState
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.CELLS
import com.cbaier33.sudoku.game.SavedGame
import com.cbaier33.sudoku.game.SavedGameCodec
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Save & Quit, and resuming from the options screen: one slot per difficulty,
 * written only by an explicit save and cleared only when the game that occupies
 * it is finished.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SavedGameTest : GameTestSupport() {

    // ----------------------------------------------------------------- codec

    /** A board with a given, an empty cell, a wrong guess and pencil marks. */
    private fun sampleGame(elapsed: Int = 754, mistakes: Int = 2) = SavedGame(
        elapsed = elapsed,
        mistakes = mistakes,
        cells = List(CELLS) { index ->
            val solution = index % 9 + 1
            when (index % 4) {
                0 -> CellItem(value = solution, solution = solution, given = true)
                1 -> CellItem(value = 0, solution = solution, given = false)
                2 -> CellItem(value = wrongDigitFor(solution), solution = solution, given = false)
                else -> CellItem(
                    value = 0,
                    solution = solution,
                    given = false,
                    notes = setOf(1, solution, 9),
                )
            }
        },
    )

    @Test
    fun `a save round-trips through the codec`() {
        val game = sampleGame()

        assertEquals(game, SavedGameCodec.decode(SavedGameCodec.encode(game)))
    }

    @Test
    fun `the summary reads the clock without decoding the grid`() {
        val summary = SavedGameCodec.decodeSummary(SavedGameCodec.encode(sampleGame(elapsed = 91)))

        assertEquals(91, summary?.elapsed)
    }

    @Test
    fun `a slot that does not decode reads back as no save at all`() {
        val encoded = SavedGameCodec.encode(sampleGame())

        val corrupt = listOf(
            "",
            "not a save",
            // A future version.
            encoded.replaceFirst("${SavedGameCodec.VERSION}|", "${SavedGameCodec.VERSION + 1}|"),
            // Truncated: one cell short.
            encoded.substringBeforeLast(","),
            // A cell whose solution is 0, which no cell can have.
            "${SavedGameCodec.VERSION}|0|0|${List(CELLS) { 0 }.joinToString(",")}",
        )

        for (text in corrupt) {
            assertNull("`$text` should not decode", SavedGameCodec.decode(text))
        }

        assertNull(SavedGameCodec.decodeSummary("not a save"))
    }

    // ------------------------------------------------------------ save & quit

    private fun GameUiState.asSave(elapsed: Int) =
        SavedGame(elapsed = elapsed, mistakes = mistakes, cells = cells)

    @Test
    fun `save and quit stores the board, the mistakes and the clock`() = gameTest {
        val vm = newViewModel(Difficulty.MEDIUM)
        val p = vm.state.value.firstEmpty()

        vm.onTapCell(p)
        vm.onTapDigit(wrongDigitFor(vm.state.value.cellAt(p).solution))
        advanceTimeBy(12_500)

        vm.saveGame()
        runCurrent()

        val saved = saves.slots[Difficulty.MEDIUM]
        assertNotNull("Save & Quit should fill the Medium slot", saved)
        assertEquals(vm.state.value.cells, saved!!.cells)
        assertEquals(1, saved.mistakes)
        assertEquals(12, saved.elapsed)

        // One slot per difficulty, and only the one played.
        assertNull(saves.slots[Difficulty.EASY])
        assertNull(saves.slots[Difficulty.HARD])
    }

    @Test
    fun `resuming restores the board, the mistakes and the clock`() = gameTest {
        val first = newViewModel(Difficulty.MEDIUM)
        val p = first.state.value.firstEmpty()

        first.onTapCell(p)
        first.onTapDigit(first.state.value.cellAt(p).solution)

        val other = first.state.value.empties().last()
        first.onTapCell(other)
        first.toggleNoteMode()
        first.onTapDigit(4)
        first.toggleNoteMode()

        advanceTimeBy(30_500)
        first.saveGame()
        runCurrent()

        val resumed = newViewModel(Difficulty.MEDIUM, seed = 7, resume = true)

        assertFalse(resumed.state.value.loading)
        assertEquals(first.state.value.cells, resumed.state.value.cells)
        assertEquals(30, resumed.elapsed.value)
        assertEquals(setOf(4), resumed.state.value.cellAt(other).notes)
        assertEquals(GameResult.PLAYING, resumed.state.value.result)

        // Progress is recounted from the restored grid rather than trusted.
        assertEquals(first.state.value.remaining, resumed.state.value.remaining)
        assertNull("a resumed board starts with nothing selected", resumed.state.value.selected)
    }

    @Test
    fun `resuming a slot that is gone deals a fresh puzzle`() = gameTest {
        val vm = newViewModel(Difficulty.HARD, resume = true)

        assertFalse(vm.state.value.loading)
        assertEquals(CELLS, vm.state.value.cells.size)
        assertEquals(0, vm.elapsed.value)
        assertEquals(0, vm.state.value.mistakes)
    }

    @Test
    fun `a resumed game clears its slot when it is finished`() = gameTest {
        val first = newViewModel()
        first.saveGame()
        runCurrent()
        assertNotNull(saves.slots[Difficulty.EASY])

        val resumed = newViewModel(resume = true)
        for (p in resumed.state.value.empties()) {
            resumed.onTapCell(p)
            resumed.onTapDigit(resumed.state.value.cellAt(p).solution)
        }
        runCurrent()

        assertEquals(GameResult.WON, resumed.state.value.result)
        assertNull("a finished game must not stay resumable", saves.slots[Difficulty.EASY])
    }

    /**
     * Finishing a puzzle that was never the saved one has to leave the save
     * alone - the slot is only ever emptied by its own game or an explicit
     * overwrite.
     */
    @Test
    fun `finishing a different game at the same difficulty leaves the save alone`() = gameTest {
        val stashed = newViewModel(seed = 3).state.value.asSave(elapsed = 60)
        saves.slots[Difficulty.EASY] = stashed

        val fresh = newViewModel(seed = 99)
        assertNotEquals(stashed.cells, fresh.state.value.cells)

        for (p in fresh.state.value.empties()) {
            fresh.onTapCell(p)
            fresh.onTapDigit(fresh.state.value.cellAt(p).solution)
        }
        runCurrent()

        assertEquals(GameResult.WON, fresh.state.value.result)
        assertEquals(stashed, saves.slots[Difficulty.EASY])
    }

    // ------------------------------------------------------------------ quit

    @Test
    fun `quitting a resumed game deletes the save it came from`() = gameTest {
        newViewModel().saveGame()
        runCurrent()

        val resumed = newViewModel(resume = true)
        assertTrue("a resumed board is the one in the slot", resumed.state.value.inSaveSlot)

        val p = resumed.state.value.firstEmpty()
        resumed.onTapCell(p)
        resumed.onTapDigit(resumed.state.value.cellAt(p).solution)

        resumed.quitWithoutSaving()
        runCurrent()

        assertNull(
            "quitting is the way to be rid of a save without solving it",
            saves.slots[Difficulty.EASY],
        )
        assertFalse(resumed.state.value.inSaveSlot)
    }

    @Test
    fun `quitting a game that was saved this session deletes the save`() = gameTest {
        val vm = newViewModel(Difficulty.HARD)
        vm.saveGame()
        runCurrent()
        assertNotNull(saves.slots[Difficulty.HARD])

        vm.quitWithoutSaving()
        runCurrent()

        assertNull(saves.slots[Difficulty.HARD])
    }

    @Test
    fun `quitting an unrelated game leaves the save alone`() = gameTest {
        val stashed = newViewModel(seed = 3).state.value.asSave(elapsed = 60)
        saves.slots[Difficulty.EASY] = stashed

        val fresh = newViewModel(seed = 99)
        assertFalse(fresh.state.value.inSaveSlot)

        fresh.quitWithoutSaving()
        runCurrent()

        assertEquals(stashed, saves.slots[Difficulty.EASY])
    }

    @Test
    fun `a new puzzle gives up the slot, so quitting keeps the save`() = gameTest {
        newViewModel().saveGame()
        runCurrent()

        val resumed = newViewModel(resume = true)
        assertTrue(resumed.state.value.inSaveSlot)

        resumed.resetGame()
        runCurrent()
        assertFalse("the dealt puzzle is not the saved one", resumed.state.value.inSaveSlot)

        resumed.quitWithoutSaving()
        runCurrent()

        assertNotNull(saves.slots[Difficulty.EASY])
    }

    @Test
    fun `a finished game is not saved`() = gameTest {
        val vm = newViewModel(Difficulty.MEDIUM)

        for (p in vm.state.value.empties().take(Difficulty.MEDIUM.mistakes)) {
            vm.onTapCell(p)
            vm.onTapDigit(wrongDigitFor(vm.state.value.cellAt(p).solution))
        }
        assertEquals(GameResult.LOST, vm.state.value.result)

        vm.saveGame()
        runCurrent()

        assertNull(saves.slots[Difficulty.MEDIUM])
    }

    @Test
    fun `a new puzzle leaves the save alone and no longer owns the slot`() = gameTest {
        val vm = newViewModel()
        vm.saveGame()
        runCurrent()

        val saved = saves.slots.getValue(Difficulty.EASY)

        vm.resetGame()
        runCurrent()
        assertEquals("New Puzzle must not throw a save away", saved, saves.slots[Difficulty.EASY])

        // ...and finishing the replacement puzzle must not take it either.
        for (p in vm.state.value.empties()) {
            vm.onTapCell(p)
            vm.onTapDigit(vm.state.value.cellAt(p).solution)
        }
        runCurrent()

        assertTrue(saves.slots.containsKey(Difficulty.EASY))
    }

    @Test
    fun `saving again replaces the slot`() = gameTest {
        val vm = newViewModel()
        vm.saveGame()
        runCurrent()
        val before = saves.slots.getValue(Difficulty.EASY)

        val p = vm.state.value.firstEmpty()
        vm.onTapCell(p)
        vm.onTapDigit(vm.state.value.cellAt(p).solution)
        advanceTimeBy(5_500)

        vm.saveGame()
        runCurrent()

        val after = saves.slots.getValue(Difficulty.EASY)
        assertNotEquals(before.cells, after.cells)
        assertEquals(5, after.elapsed)
    }
}
