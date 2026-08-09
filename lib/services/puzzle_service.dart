import 'dart:math';

/// Builds 9x9 puzzles that have exactly one solution.
///
/// Grids are held row major as a flat list of 81 digits, where 0 is an empty
/// cell. Candidate sets are carried around as bitmasks: bit `n` set means the
/// digit `n` is still allowed, which keeps the solution counting fast enough
/// to run on every dug hole.
class PuzzleService {
  PuzzleService({Random? random}) : _random = random ?? Random();

  static const int size = 9;
  static const int box = 3;
  static const int cells = size * size;

  static const int _allDigits = 0x3FE; // bits 1..9

  final Random _random;

  /// Builds a solved grid, then removes cells one at a time - keeping only the
  /// removals that leave the puzzle solvable a single way - until [givens] are
  /// left. Sparse targets are best effort: if every remaining cell is load
  /// bearing the puzzle stops there with a few more givens than asked for.
  Puzzle generate(int givens) {
    final solution = List<int>.filled(cells, 0);
    _fill(solution, 0);

    return Puzzle(puzzle: _digHoles(solution, givens), solution: solution);
  }

  // Fills in reading order, trying digits in a random order so that every call
  // lands on a different grid.
  bool _fill(List<int> grid, int index) {
    if (index == cells) return true;

    final mask = _candidates(grid, index);
    final digits = List<int>.generate(size, (i) => i + 1)..shuffle(_random);

    for (final digit in digits) {
      if (mask & (1 << digit) == 0) continue;

      grid[index] = digit;
      if (_fill(grid, index + 1)) return true;
      grid[index] = 0;
    }

    return false;
  }

  List<int> _digHoles(List<int> solution, int givens) {
    final puzzle = List<int>.from(solution);
    final order = List<int>.generate(cells, (i) => i)..shuffle(_random);

    int remaining = cells;

    for (final index in order) {
      if (remaining <= givens) break;

      final removed = puzzle[index];
      puzzle[index] = 0;

      if (_countSolutions(List<int>.from(puzzle), 2) == 1) {
        remaining--;
      } else {
        puzzle[index] = removed;
      }
    }

    return puzzle;
  }

  // Counts solutions, giving up as soon as [limit] have been found. Solving the
  // most constrained cell first collapses most of the search tree, which is
  // what makes the uniqueness check cheap.
  int _countSolutions(List<int> grid, int limit) {
    int target = -1;
    int targetMask = 0;
    int fewest = size + 1;

    for (int index = 0; index < cells; index++) {
      if (grid[index] != 0) continue;

      final mask = _candidates(grid, index);
      final count = _countBits(mask);

      if (count < fewest) {
        target = index;
        targetMask = mask;
        fewest = count;

        if (count <= 1) break;
      }
    }

    // Nothing left to place, so the grid itself is a solution.
    if (target == -1) return 1;

    int found = 0;

    for (int digit = 1; digit <= size; digit++) {
      if (targetMask & (1 << digit) == 0) continue;

      grid[target] = digit;
      found += _countSolutions(grid, limit - found);
      grid[target] = 0;

      if (found >= limit) break;
    }

    return found;
  }

  int _candidates(List<int> grid, int index) {
    final row = index ~/ size;
    final col = index % size;

    int used = 0;

    for (int i = 0; i < size; i++) {
      used |= 1 << grid[row * size + i];
      used |= 1 << grid[i * size + col];
    }

    final boxRow = row - row % box;
    final boxCol = col - col % box;

    for (int r = boxRow; r < boxRow + box; r++) {
      for (int c = boxCol; c < boxCol + box; c++) {
        used |= 1 << grid[r * size + c];
      }
    }

    return _allDigits & ~used;
  }

  int _countBits(int mask) {
    int count = 0;

    while (mask != 0) {
      mask &= mask - 1;
      count++;
    }

    return count;
  }
}

class Puzzle {
  const Puzzle({required this.puzzle, required this.solution});

  final List<int> puzzle;
  final List<int> solution;
}
