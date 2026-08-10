package com.cbaier33.sudoku.game

import kotlin.random.Random

/**
 * Builds 9x9 puzzles that have exactly one solution.
 *
 * Grids are held row major as a flat array of 81 digits, where 0 is an empty
 * cell. Candidate sets are carried around as bitmasks: bit `n` set means the
 * digit `n` is still allowed, which keeps the solution counting fast enough to
 * run on every dug hole.
 *
 * Port of the Flutter build's `lib/services/puzzle_service.dart`.
 */
class PuzzleGenerator(private val random: Random = Random.Default) {

    /**
     * Builds a solved grid, then removes cells one at a time - keeping only the
     * removals that leave the puzzle solvable a single way - until [givens] are
     * left. Sparse targets are best effort: if every remaining cell is load
     * bearing the puzzle stops there with a few more givens than asked for.
     */
    fun generate(givens: Int): Puzzle {
        val solution = IntArray(CELLS)
        fill(solution, 0)
        return Puzzle(puzzle = digHoles(solution, givens), solution = solution)
    }

    /**
     * Fills in reading order, trying digits in a random order so that every call
     * lands on a different grid.
     */
    private fun fill(grid: IntArray, index: Int): Boolean {
        if (index == CELLS) return true

        val mask = candidates(grid, index)
        val digits = (1..SIZE).shuffled(random)

        for (digit in digits) {
            if (mask and (1 shl digit) == 0) continue

            grid[index] = digit
            if (fill(grid, index + 1)) return true
            grid[index] = 0
        }

        return false
    }

    private fun digHoles(solution: IntArray, givens: Int): IntArray {
        val puzzle = solution.copyOf()
        val order = (0 until CELLS).shuffled(random)

        var remaining = CELLS

        for (index in order) {
            if (remaining <= givens) break

            val removed = puzzle[index]
            puzzle[index] = 0

            if (countSolutions(puzzle.copyOf(), 2) == 1) {
                remaining--
            } else {
                puzzle[index] = removed
            }
        }

        return puzzle
    }

    /**
     * Counts solutions, giving up as soon as [limit] have been found. Solving
     * the most constrained cell first collapses most of the search tree, which
     * is what makes the uniqueness check cheap.
     */
    private fun countSolutions(grid: IntArray, limit: Int): Int {
        var target = -1
        var targetMask = 0
        var fewest = SIZE + 1

        for (index in 0 until CELLS) {
            if (grid[index] != 0) continue

            val mask = candidates(grid, index)
            val count = Integer.bitCount(mask)

            if (count < fewest) {
                target = index
                targetMask = mask
                fewest = count

                if (count <= 1) break
            }
        }

        // Nothing left to place, so the grid itself is a solution.
        if (target == -1) return 1

        var found = 0

        for (digit in 1..SIZE) {
            if (targetMask and (1 shl digit) == 0) continue

            grid[target] = digit
            found += countSolutions(grid, limit - found)
            grid[target] = 0

            if (found >= limit) break
        }

        return found
    }

    private fun candidates(grid: IntArray, index: Int): Int {
        val row = index / SIZE
        val col = index % SIZE

        var used = 0

        for (i in 0 until SIZE) {
            used = used or (1 shl grid[row * SIZE + i])
            used = used or (1 shl grid[i * SIZE + col])
        }

        val boxRow = row - row % BOX
        val boxCol = col - col % BOX

        for (r in boxRow until boxRow + BOX) {
            for (c in boxCol until boxCol + BOX) {
                used = used or (1 shl grid[r * SIZE + c])
            }
        }

        return ALL_DIGITS and used.inv()
    }

    companion object {
        const val SIZE = 9
        const val BOX = 3
        const val CELLS = SIZE * SIZE

        /** bits 1..9 */
        private const val ALL_DIGITS = 0x3FE
    }
}

class Puzzle(val puzzle: IntArray, val solution: IntArray)
