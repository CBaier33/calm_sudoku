import 'package:flutter/material.dart';

class OptionsViewModel extends ChangeNotifier {
  OptionsViewModel();

  DifficultyLevel difficulty = DifficultyLevel.easy;
  bool highlightPeers = true;
  bool autoClearNotes = true;

  void setDifficulty(DifficultyLevel diff) {
    difficulty = diff;
    notifyListeners();
  }

  OptionsViewModel.clone(OptionsViewModel other) {
    difficulty = other.difficulty;
    highlightPeers = other.highlightPeers;
    autoClearNotes = other.autoClearNotes;
  }
}

enum DifficultyLevel {
  easy(givens: 40, mistakes: 5),
  medium(givens: 32, mistakes: 3),
  hard(givens: 26, mistakes: 3);

  const DifficultyLevel({required this.givens, required this.mistakes});

  // How many cells the puzzle starts with filled in. Fewer givens means more
  // of the grid has to be deduced.
  final int givens;

  // Wrong digits allowed before the game is lost.
  final int mistakes;
}
