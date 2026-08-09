import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sudoku/ui/core/widgets/difficulty_button.dart';
import 'package:sudoku/ui/page/view_models/options_view_model.dart';

class DifficultySelector extends StatelessWidget {
  const DifficultySelector({super.key});

  @override
  Widget build(BuildContext context) {
    final currentOptions = Provider.of<OptionsViewModel>(context);

    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: [
        difficultyButton(
          context,
          difficulty: "Easy",
          detail: "40 Given",
          value: DifficultyLevel.easy,
          selected: currentOptions.difficulty,
          onSelected: (v) => currentOptions.setDifficulty(v),
        ),
        difficultyButton(
          context,
          difficulty: "Medium",
          detail: "32 Given",
          value: DifficultyLevel.medium,
          selected: currentOptions.difficulty,
          onSelected: (v) => currentOptions.setDifficulty(v),
        ),
        difficultyButton(
          context,
          difficulty: "Hard",
          detail: "26 Given",
          value: DifficultyLevel.hard,
          selected: currentOptions.difficulty,
          onSelected: (v) => currentOptions.setDifficulty(v),
        ),
      ],
    );
  }
}
