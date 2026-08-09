import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sudoku/services/puzzle_service.dart';
import 'package:sudoku/ui/game/view_models/game_viewmodel.dart';

class NumberPad extends StatelessWidget {
  const NumberPad({super.key});

  @override
  Widget build(BuildContext context) {
    GameViewModel currentGame = Provider.of<GameViewModel>(context);

    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Column(
        spacing: 8,
        children: [
          Row(
            spacing: 4,
            children: [
              for (int digit = 1; digit <= PuzzleService.size; digit++)
                Expanded(
                  child: padButton(
                    context,
                    label: digit.toString(),
                    style: Theme.of(context).textTheme.bodyMedium,
                    // a digit that is fully placed has nothing left to give
                    spent: currentGame.remainingFor(digit) == 0,
                    filled: false,
                    onTap: () => currentGame.onTapDigit(digit),
                  ),
                ),
            ],
          ),
          Row(
            spacing: 8,
            children: [
              Expanded(
                child: padButton(
                  context,
                  label: "Notes",
                  style: Theme.of(context).textTheme.bodySmall,
                  spent: false,
                  filled: currentGame.noteMode,
                  onTap: () => currentGame.toggleNoteMode(),
                ),
              ),
              Expanded(
                child: padButton(
                  context,
                  label: "Erase",
                  style: Theme.of(context).textTheme.bodySmall,
                  spent: false,
                  filled: false,
                  onTap: () => currentGame.onTapErase(),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

Widget padButton(
  BuildContext context, {
  required String label,
  required TextStyle? style,
  required bool spent,
  required bool filled,
  required VoidCallback onTap,
}) {
  final ink = spent ? Colors.grey[400]! : Colors.black;

  return Material(
    color: Colors.transparent,
    child: InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      splashFactory: NoSplash.splashFactory,
      overlayColor: const WidgetStatePropertyAll(Colors.transparent),
      highlightColor: Colors.transparent,
      hoverColor: Colors.transparent,
      child: Container(
        height: 48,
        decoration: BoxDecoration(
          color: filled ? Colors.black : Colors.white,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: ink, width: filled ? 3 : 2),
        ),
        child: Center(
          child: Text(
            label,
            style: style?.copyWith(color: filled ? Colors.white : ink),
          ),
        ),
      ),
    ),
  );
}
