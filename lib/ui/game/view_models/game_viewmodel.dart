import 'dart:async';
import 'package:flutter/material.dart';
import 'package:sudoku/services/puzzle_service.dart';
import 'package:sudoku/services/stats_service.dart';
import 'package:sudoku/ui/game/widgets/cell.dart';
import 'package:sudoku/ui/page/view_models/options_view_model.dart';

class GameViewModel extends ChangeNotifier {
  GameViewModel({required this.options}) {
    _generateBoard();
  }

  final OptionsViewModel options;
  final StatsService _stats = StatsService();
  final PuzzleService _puzzles = PuzzleService();

  int gameState = 0;
  late BoardState boardState;

  // The cell the number pad writes to, if any.
  CellPoint? selected;

  // When set, the number pad toggles pencil marks instead of placing digits.
  bool noteMode = false;

  int mistakes = 0;
  late int remaining;

  Timer? _timer;
  int time = 0;
  bool _running = false;
  bool _paused = false;

  int get mistakesAllowed => options.difficulty.mistakes;

  void _checkGame() {
    _countRemaining();

    // Lose Case
    if (mistakes >= mistakesAllowed) {
      _endGame(1);
      return;
    }

    // Win Case
    if (remaining == 0) {
      _endGame(2);
      return;
    }
  }

  void _endGame(int result) {
    pauseTimer();
    gameState = result;
    bool win = (result == 2);
    _stats.recordGame(time, options.difficulty, win);

    if (!win) _revealSolution();

    selected = null;

    notifyListeners();
  }

  void _revealSolution() {
    for (final row in boardState) {
      for (final cell in row) {
        cell.value = cell.solution;
        cell.notes.clear();
      }
    }
  }

  //
  // BOARD ACTIONS
  //

  void _generateBoard() {
    final generated = _puzzles.generate(options.difficulty.givens);

    boardState = List.generate(
      PuzzleService.size,
      (x) => List.generate(PuzzleService.size, (y) {
        final index = x * PuzzleService.size + y;

        return CellItem(
          value: generated.puzzle[index],
          solution: generated.solution[index],
          given: generated.puzzle[index] != 0,
        );
      }),
    );

    _countRemaining();
  }

  // deals a new puzzle without resetting the current options
  void resetGame() {
    // Reset Board
    _generateBoard();

    // Reset Mistakes
    mistakes = 0;

    // Clear the selection and drop back out of pencil mark mode
    selected = null;
    noteMode = false;

    // reset game state
    gameState = 0;

    // Reset Timer
    resetTimer();

    notifyListeners();
  }

  void _countRemaining() {
    int solved = 0;

    for (final row in boardState) {
      for (final cell in row) {
        if (cell.correct) solved++;
      }
    }

    // counts down to zero as the grid gets filled in correctly
    remaining = PuzzleService.cells - solved;
  }

  // How many of a digit are still missing from the grid.
  int remainingFor(int digit) {
    int placed = 0;

    for (final row in boardState) {
      for (final cell in row) {
        if (cell.correct && cell.value == digit) placed++;
      }
    }

    return PuzzleService.size - placed;
  }

  // Two cells are peers when one of them being a digit rules that digit out of
  // the other: same row, same column or same box.
  bool isPeer(CellPoint a, CellPoint b) {
    if (a.x == b.x && a.y == b.y) return false;

    if (a.x == b.x || a.y == b.y) return true;

    return a.x ~/ PuzzleService.box == b.x ~/ PuzzleService.box &&
        a.y ~/ PuzzleService.box == b.y ~/ PuzzleService.box;
  }

  //
  // CELL ACTIONS
  //

  CellItem getCell(CellPoint p) {
    return boardState[p.x][p.y];
  }

  bool isSelected(CellPoint p) {
    final s = selected;

    return s != null && s.x == p.x && s.y == p.y;
  }

  void onTapCell(CellPoint p) {
    if (gameState != 0) {
      return;
    }

    startTimer();

    selected = p;

    notifyListeners();
  }

  void onLongPressCell(CellPoint p) {
    if (gameState != 0) {
      return;
    }

    selected = p;
    _clearCell(p);

    notifyListeners();
  }

  void onTapDigit(int digit) {
    if (gameState != 0) {
      return;
    }

    CellPoint? p = selected;

    if (p == null) {
      return;
    }

    CellItem cell = getCell(p);

    // givens and solved cells are locked
    if (cell.given || cell.correct) {
      return;
    }

    startTimer();

    if (noteMode) {
      if (!cell.notes.remove(digit)) cell.notes.add(digit);

      notifyListeners();
      return;
    }

    cell.value = digit;
    cell.notes.clear();

    if (cell.correct) {
      if (options.autoClearNotes) _clearNotesFromPeers(p, digit);
    } else {
      mistakes++;
    }

    _checkGame();

    notifyListeners();
  }

  void onTapErase() {
    if (gameState != 0) {
      return;
    }

    CellPoint? p = selected;

    if (p == null) {
      return;
    }

    _clearCell(p);

    notifyListeners();
  }

  void toggleNoteMode() {
    noteMode = !noteMode;
    notifyListeners();
  }

  void _clearCell(CellPoint p) {
    CellItem cell = getCell(p);

    if (cell.given || cell.correct) {
      return;
    }

    cell.value = 0;
    cell.notes.clear();
  }

  void _clearNotesFromPeers(CellPoint p, int digit) {
    for (int x = 0; x < PuzzleService.size; x++) {
      for (int y = 0; y < PuzzleService.size; y++) {
        final peer = CellPoint(x: x, y: y);

        if (isPeer(p, peer)) getCell(peer).notes.remove(digit);
      }
    }
  }

  //
  // TIMER
  //

  void startTimer() {
    if (_running) return;

    _running = true;
    _timer = Timer.periodic(const Duration(seconds: 1), (_) {
      if (_paused) return;
      time++;
      notifyListeners();
    });
  }

  void pauseTimer() {
    _paused = true;
    notifyListeners();
  }

  void resumeTimer() {
    _paused = false;
  }

  void stopTimer() {
    _timer?.cancel();
    _timer = null;
    _running = false;
    notifyListeners();
  }

  void resetTimer() {
    stopTimer();
    time = 0;
    _running = false;
    _paused = false;
    notifyListeners();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}

typedef BoardState = List<List<CellItem>>;

class CellItem {
  CellItem({required this.value, required this.solution, required this.given});

  int value = 0;
  final int solution;
  final bool given;
  final Set<int> notes = {};

  bool get correct => value == solution;
}
