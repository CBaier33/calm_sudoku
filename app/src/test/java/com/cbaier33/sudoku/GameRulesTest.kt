package com.cbaier33.sudoku

import com.cbaier33.sudoku.game.CellPoint
import com.cbaier33.sudoku.game.Difficulty
import com.cbaier33.sudoku.game.GameResult
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.SIZE
import com.cbaier33.sudoku.game.isPeer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the rules the Flutter build never had tests for. Boards come from the
 * real generator with a fixed seed; assertions are derived from each cell's own
 * solution rather than a hard-coded grid.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameRulesTest : GameTestSupport() {

    @Test
    fun `board loads with the difficulty's mistake budget`() = gameTest {
        val vm = newViewModel(Difficulty.MEDIUM)

        assertFalse(vm.state.value.loading)
        assertEquals(81, vm.state.value.cells.size)
        assertEquals(Difficulty.MEDIUM.mistakes, vm.state.value.mistakesAllowed)
    }

    @Test
    fun `a wrong digit costs a mistake and stays in the cell`() = gameTest {
        val vm = newViewModel()
        val p = vm.state.value.firstEmpty()
        val wrong = wrongDigitFor(vm.state.value.cellAt(p).solution)

        vm.onTapCell(p)
        vm.onTapDigit(wrong)

        assertEquals(1, vm.state.value.mistakes)
        assertEquals(wrong, vm.state.value.cellAt(p).value)
        assertFalse(vm.state.value.cellAt(p).correct)
    }

    @Test
    fun `a correct digit locks the cell`() = gameTest {
        val vm = newViewModel()
        val p = vm.state.value.firstEmpty()
        val answer = vm.state.value.cellAt(p).solution

        vm.onTapCell(p)
        vm.onTapDigit(answer)
        assertTrue(vm.state.value.cellAt(p).correct)

        // Further digits and erase both bounce off a solved cell.
        vm.onTapDigit(wrongDigitFor(answer))
        vm.onTapErase()

        assertEquals(answer, vm.state.value.cellAt(p).value)
        assertEquals(0, vm.state.value.mistakes)
    }

    @Test
    fun `givens are locked`() = gameTest {
        val vm = newViewModel()
        val p = vm.state.value.firstGiven()
        val original = vm.state.value.cellAt(p).value

        vm.onTapCell(p)
        vm.onTapDigit(wrongDigitFor(original))
        vm.onTapErase()

        assertEquals(original, vm.state.value.cellAt(p).value)
        assertEquals(0, vm.state.value.mistakes)
    }

    @Test
    fun `notes toggle and never cost a mistake`() = gameTest {
        val vm = newViewModel()
        val p = vm.state.value.firstEmpty()

        vm.onTapCell(p)
        vm.toggleNoteMode()
        assertTrue(vm.state.value.noteMode)

        vm.onTapDigit(5)
        assertEquals(setOf(5), vm.state.value.cellAt(p).notes)

        vm.onTapDigit(7)
        assertEquals(setOf(5, 7), vm.state.value.cellAt(p).notes)

        vm.onTapDigit(5)
        assertEquals(setOf(7), vm.state.value.cellAt(p).notes)

        assertEquals(0, vm.state.value.mistakes)
        assertEquals(GameResult.PLAYING, vm.state.value.result)
    }

    @Test
    fun `a correct digit clears that mark from peers only`() = gameTest {
        val vm = newViewModel()
        val target = vm.state.value.firstEmpty()
        val answer = vm.state.value.cellAt(target).solution

        // Put the answer as a pencil mark in every other empty cell.
        vm.toggleNoteMode()
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                val p = CellPoint(x, y)
                if (p == target || vm.state.value.cellAt(p).given) continue
                vm.onTapCell(p)
                vm.onTapDigit(answer)
            }
        }
        vm.toggleNoteMode()

        val marked = (0 until SIZE).flatMap { x -> (0 until SIZE).map { y -> CellPoint(x, y) } }
            .filter { answer in vm.state.value.cellAt(it).notes }
        assertTrue("expected pencil marks to place", marked.isNotEmpty())

        vm.onTapCell(target)
        vm.onTapDigit(answer)

        for (p in marked) {
            val stillMarked = answer in vm.state.value.cellAt(p).notes
            assertEquals(
                "peer ${p.x},${p.y} should have lost the mark",
                !isPeer(target, p),
                stillMarked,
            )
        }
    }

    @Test
    fun `long press clears without starting the clock`() = gameTest {
        val vm = newViewModel()
        val p = vm.state.value.firstEmpty()
        val wrong = wrongDigitFor(vm.state.value.cellAt(p).solution)

        // Place a wrong digit via a fresh view model so the clock is untouched.
        val other = newViewModel()
        other.onLongPressCell(p)
        advanceTimeBy(5_000)
        assertEquals("long press must not start the clock", 0, other.elapsed.value)

        vm.onTapCell(p)
        vm.onTapDigit(wrong)
        vm.onLongPressCell(p)

        assertEquals(0, vm.state.value.cellAt(p).value)
        assertEquals(p, vm.state.value.selected)
    }

    @Test
    fun `tapping a cell starts the clock`() = gameTest {
        val vm = newViewModel()

        assertEquals(0, vm.elapsed.value)
        vm.onTapCell(vm.state.value.firstEmpty())

        advanceTimeBy(3_500)
        assertEquals(3, vm.elapsed.value)
    }

    @Test
    fun `pausing gates the clock and resuming restarts it`() = gameTest {
        val vm = newViewModel()
        vm.onTapCell(vm.state.value.firstEmpty())

        advanceTimeBy(2_500)
        assertEquals(2, vm.elapsed.value)

        vm.pauseTimer()
        advanceTimeBy(5_000)
        assertEquals("paused clock must not advance", 2, vm.elapsed.value)

        vm.resumeTimer()
        advanceTimeBy(2_000)
        assertEquals(4, vm.elapsed.value)
    }

    @Test
    fun `running out of mistakes loses, reveals the solution and records the game`() =
        gameTest {
            val vm = newViewModel(Difficulty.MEDIUM)
            val allowed = Difficulty.MEDIUM.mistakes

            val empties = (0 until SIZE).flatMap { x -> (0 until SIZE).map { y -> CellPoint(x, y) } }
                .filter { !vm.state.value.cellAt(it).given }
                .take(allowed)

            for (p in empties) {
                vm.onTapCell(p)
                vm.onTapDigit(wrongDigitFor(vm.state.value.cellAt(p).solution))
            }

            assertEquals(GameResult.LOST, vm.state.value.result)
            assertNull("selection is dropped when the game ends", vm.state.value.selected)

            assertTrue(
                "a loss reveals the whole solution",
                vm.state.value.cells.all { it.value == it.solution && it.notes.isEmpty() },
            )

            runCurrent()
            assertEquals(1, stats.recorded.size)
            assertFalse(stats.recorded.single().win)
            assertEquals(Difficulty.MEDIUM, stats.recorded.single().difficulty)
        }

    @Test
    fun `filling the grid correctly wins and records the game`() = gameTest {
        val vm = newViewModel()

        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                val p = CellPoint(x, y)
                if (vm.state.value.cellAt(p).given) continue
                vm.onTapCell(p)
                vm.onTapDigit(vm.state.value.cellAt(p).solution)
            }
        }

        assertEquals(GameResult.WON, vm.state.value.result)
        assertEquals(0, vm.state.value.remaining)
        assertEquals(0, vm.state.value.mistakes)

        runCurrent()
        assertEquals(1, stats.recorded.size)
        assertTrue(stats.recorded.single().win)
    }

    /**
     * The Flutter build tested the loss before the win, so spending the last
     * mistake on the final cell loses. Preserved deliberately.
     */
    @Test
    fun `the last mistake loses even when it fills the final cell`() = gameTest {
        val vm = newViewModel(Difficulty.MEDIUM)
        val allowed = Difficulty.MEDIUM.mistakes

        val empties = (0 until SIZE).flatMap { x -> (0 until SIZE).map { y -> CellPoint(x, y) } }
            .filter { !vm.state.value.cellAt(it).given }

        // Spend all but one mistake, then solve everything except the last cell.
        for (p in empties.take(allowed - 1)) {
            vm.onTapCell(p)
            vm.onTapDigit(wrongDigitFor(vm.state.value.cellAt(p).solution))
        }

        val last = empties.last()
        for (p in empties.dropLast(1)) {
            vm.onTapCell(p)
            vm.onTapDigit(vm.state.value.cellAt(p).solution)
        }

        assertEquals(GameResult.PLAYING, vm.state.value.result)
        assertEquals(1, vm.state.value.remaining)
        assertEquals(allowed - 1, vm.state.value.mistakes)

        vm.onTapCell(last)
        vm.onTapDigit(wrongDigitFor(vm.state.value.cellAt(last).solution))

        assertEquals(GameResult.LOST, vm.state.value.result)
    }

    @Test
    fun `spent digits are reported as having none left`() = gameTest {
        val vm = newViewModel()
        val target = vm.state.value.firstEmpty()
        val answer = vm.state.value.cellAt(target).solution

        assertTrue(vm.state.value.digitsRemaining[answer] > 0)

        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                val p = CellPoint(x, y)
                val cell = vm.state.value.cellAt(p)
                if (cell.given || cell.solution != answer) continue
                vm.onTapCell(p)
                vm.onTapDigit(answer)
            }
        }

        assertEquals(0, vm.state.value.digitsRemaining[answer])
    }

    @Test
    fun `new puzzle resets the board, mistakes and clock`() = gameTest {
        val vm = newViewModel()
        val before = vm.state.value.cells.map { it.solution }

        val p = vm.state.value.firstEmpty()
        vm.onTapCell(p)
        vm.onTapDigit(wrongDigitFor(vm.state.value.cellAt(p).solution))
        advanceTimeBy(4_000)

        assertEquals(1, vm.state.value.mistakes)
        assertTrue(vm.elapsed.value > 0)

        vm.resetGame()
        runCurrent()

        assertEquals(0, vm.state.value.mistakes)
        assertEquals(0, vm.elapsed.value)
        assertNull(vm.state.value.selected)
        assertEquals(GameResult.PLAYING, vm.state.value.result)
        assertNotEquals(before, vm.state.value.cells.map { it.solution })
    }
}
