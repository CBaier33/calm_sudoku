import 'dart:math';

import 'package:flutter_test/flutter_test.dart';

import 'package:sudoku/services/puzzle_service.dart';

// A grid is valid when no digit repeats down a row, a column or a box.
void expectValidSolution(List<int> grid) {
  const size = PuzzleService.size;
  const box = PuzzleService.box;

  for (int i = 0; i < size; i++) {
    final row = <int>{};
    final col = <int>{};

    for (int j = 0; j < size; j++) {
      expect(grid[i * size + j], inInclusiveRange(1, size));
      row.add(grid[i * size + j]);
      col.add(grid[j * size + i]);
    }

    expect(row.length, size, reason: 'row $i repeats a digit');
    expect(col.length, size, reason: 'column $i repeats a digit');
  }

  for (int boxRow = 0; boxRow < size; boxRow += box) {
    for (int boxCol = 0; boxCol < size; boxCol += box) {
      final seen = <int>{};

      for (int r = boxRow; r < boxRow + box; r++) {
        for (int c = boxCol; c < boxCol + box; c++) {
          seen.add(grid[r * size + c]);
        }
      }

      expect(seen.length, size, reason: 'box $boxRow,$boxCol repeats a digit');
    }
  }
}

void main() {
  test('solutions are complete and legal', () {
    final service = PuzzleService(random: Random(7));

    for (int i = 0; i < 5; i++) {
      expectValidSolution(service.generate(40).solution);
    }
  });

  test('puzzles agree with their solution and hit the given count', () {
    final service = PuzzleService(random: Random(11));

    for (final givens in [40, 32, 26]) {
      final generated = service.generate(givens);

      int filled = 0;

      for (int i = 0; i < PuzzleService.cells; i++) {
        if (generated.puzzle[i] == 0) continue;

        expect(generated.puzzle[i], generated.solution[i]);
        filled++;
      }

      // Digging is best effort on sparse targets, so allow a little slack over
      // the ask but never under it.
      expect(filled, greaterThanOrEqualTo(givens));
      expect(filled, lessThanOrEqualTo(givens + 6));
    }
  });

  test('two puzzles in a row are not the same puzzle', () {
    final service = PuzzleService(random: Random(3));

    expect(service.generate(40).puzzle, isNot(service.generate(40).puzzle));
  });
}
