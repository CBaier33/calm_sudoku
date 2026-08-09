import 'package:flutter/material.dart';
import 'package:sudoku/ui/page/view_models/options_view_model.dart';

Widget difficultyButton(
  BuildContext context, {
  required String difficulty,
  required String detail,
  required DifficultyLevel value,
  required DifficultyLevel selected,
  required ValueChanged<DifficultyLevel> onSelected,
}) {
  final isSelected = value == selected;

  return Material(
    color: Colors.transparent,
    child: InkWell(
      onTap: () => onSelected(value),
      borderRadius: BorderRadius.circular(12),
      splashFactory: NoSplash.splashFactory,
      overlayColor: const WidgetStatePropertyAll(Colors.transparent),
      highlightColor: Colors.transparent,
      hoverColor: Colors.transparent,
      child: SizedBox(
        width: 100,
        height: 60,
        child: Padding(
          padding: EdgeInsets.all(isSelected ? 2 : 3),
          child: Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(
                color: Colors.black,
                width: isSelected ? 3 : 2,
              ),
            ),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(difficulty, style: Theme.of(context).textTheme.bodySmall),
                Text(detail, style: Theme.of(context).textTheme.bodySmall),
              ],
            ),
          ),
        ),
      ),
    ),
  );
}
