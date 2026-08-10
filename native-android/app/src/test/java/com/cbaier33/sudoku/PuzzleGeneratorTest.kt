package com.cbaier33.sudoku

import com.cbaier33.sudoku.game.Difficulty
import com.cbaier33.sudoku.game.PuzzleGenerator
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.BOX
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.CELLS
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.SIZE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Ported from the Flutter build's `test/puzzle_service_test.dart`. Kotlin's
 * seeded Random does not produce Dart's sequence, so these stay property based
 * rather than comparing against a golden grid.
 */
class PuzzleGeneratorTest {

    @Test
    fun `solutions are complete and legal`() {
        val generator = PuzzleGenerator(Random(7))

        repeat(5) {
            expectValidSolution(generator.generate(Difficulty.EASY.givens).solution)
        }
    }

    @Test
    fun `puzzles agree with their solution and hit the given count`() {
        val generator = PuzzleGenerator(Random(11))

        for (difficulty in Difficulty.entries) {
            val givens = difficulty.givens
            val generated = generator.generate(givens)

            expectValidSolution(generated.solution)

            var filled = 0
            for (i in 0 until CELLS) {
                val value = generated.puzzle[i]
                if (value == 0) continue

                filled++
                assertEquals(
                    "puzzle cell $i disagrees with the solution",
                    generated.solution[i],
                    value,
                )
            }

            // Digging is best effort: if every remaining cell is load bearing
            // the puzzle stops with a few more givens than asked for.
            assertTrue(
                "$difficulty left $filled givens, wanted $givens..${givens + 6}",
                filled in givens..(givens + 6),
            )
        }
    }

    @Test
    fun `puzzles are uniquely solvable`() {
        val generator = PuzzleGenerator(Random(3))

        for (difficulty in Difficulty.entries) {
            val generated = generator.generate(difficulty.givens)

            assertEquals(
                "$difficulty puzzle does not have exactly one solution",
                1,
                countSolutions(generated.puzzle.copyOf(), limit = 2),
            )
        }
    }

    @Test
    fun `two puzzles in a row are not the same puzzle`() {
        val generator = PuzzleGenerator(Random(5))

        val first = generator.generate(Difficulty.EASY.givens)
        val second = generator.generate(Difficulty.EASY.givens)

        assertNotEquals(first.puzzle.toList(), second.puzzle.toList())
    }

    private fun expectValidSolution(grid: IntArray) {
        assertEquals(CELLS, grid.size)
        assertTrue("every cell holds 1..9", grid.all { it in 1..SIZE })

        for (i in 0 until SIZE) {
            val row = (0 until SIZE).map { grid[i * SIZE + it] }
            val col = (0 until SIZE).map { grid[it * SIZE + i] }

            assertEquals("row $i repeats a digit", SIZE, row.toSet().size)
            assertEquals("column $i repeats a digit", SIZE, col.toSet().size)
        }

        for (boxRow in 0 until SIZE step BOX) {
            for (boxCol in 0 until SIZE step BOX) {
                val box = mutableListOf<Int>()
                for (r in boxRow until boxRow + BOX) {
                    for (c in boxCol until boxCol + BOX) {
                        box += grid[r * SIZE + c]
                    }
                }
                assertEquals("box $boxRow,$boxCol repeats a digit", SIZE, box.toSet().size)
            }
        }
    }

    /** An independent solver, so the test does not lean on the generator's own. */
    private fun countSolutions(grid: IntArray, limit: Int): Int {
        val empty = (0 until CELLS).firstOrNull { grid[it] == 0 } ?: return 1

        var found = 0
        for (digit in 1..SIZE) {
            if (!legal(grid, empty, digit)) continue

            grid[empty] = digit
            found += countSolutions(grid, limit - found)
            grid[empty] = 0

            if (found >= limit) break
        }
        return found
    }

    private fun legal(grid: IntArray, index: Int, digit: Int): Boolean {
        val row = index / SIZE
        val col = index % SIZE

        for (i in 0 until SIZE) {
            if (grid[row * SIZE + i] == digit) return false
            if (grid[i * SIZE + col] == digit) return false
        }

        val boxRow = row - row % BOX
        val boxCol = col - col % BOX

        for (r in boxRow until boxRow + BOX) {
            for (c in boxCol until boxCol + BOX) {
                if (grid[r * SIZE + c] == digit) return false
            }
        }

        return true
    }
}
