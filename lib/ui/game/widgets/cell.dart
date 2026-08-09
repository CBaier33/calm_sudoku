import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sudoku/services/puzzle_service.dart';
import 'package:sudoku/ui/game/view_models/game_viewmodel.dart';

class Cell extends StatelessWidget {
  const Cell({super.key, required this.gridPoint});

  final CellPoint gridPoint;

  // KEY
  // 0 -> Empty, shows pencil marks if there are any
  // 1-9 -> a given, a solved digit, or a wrong guess

  @override
  Widget build(BuildContext context) {
    GameViewModel currentGame = Provider.of<GameViewModel>(context);

    CellItem cell = currentGame.getCell(gridPoint);

    final wrong = cell.value != 0 && !cell.correct;

    return Container(
      decoration: BoxDecoration(
        color: _cellFill(currentGame, gridPoint, wrong),
        border: _cellBorder(gridPoint),
      ),
      child: cell.value == 0
          ? Notes(notes: cell.notes)
          : Center(
              child: Text(
                cell.value.toString(),
                style:
                    (cell.given
                            ? Theme.of(context).textTheme.headlineSmall
                            : Theme.of(context).textTheme.headlineMedium)
                        ?.copyWith(color: wrong ? Colors.white : Colors.black),
              ),
            ),
    );
  }
}

Color? _cellFill(GameViewModel currentGame, CellPoint p, bool wrong) {
  // A wrong guess inverts, which stays legible on a grey scale panel.
  if (wrong) return Colors.black;

  if (currentGame.isSelected(p)) return Colors.grey[400];

  final selected = currentGame.selected;

  if (selected != null &&
      currentGame.options.highlightPeers &&
      currentGame.isPeer(selected, p)) {
    return Colors.grey[200];
  }

  return Colors.white;
}

// Only the top and left edges are drawn so that neighbours do not stack two
// borders on top of each other. The box boundaries get the heavy line.
Border _cellBorder(CellPoint p) {
  const double thin = 1.0;
  const double thick = 3.0;
  const int last = PuzzleService.size - 1;

  return Border(
    top: BorderSide(
      color: Colors.black,
      width: p.x % PuzzleService.box == 0 ? thick : thin,
    ),
    left: BorderSide(
      color: Colors.black,
      width: p.y % PuzzleService.box == 0 ? thick : thin,
    ),
    right: p.y == last
        ? const BorderSide(color: Colors.black, width: thick)
        : BorderSide.none,
    bottom: p.x == last
        ? const BorderSide(color: Colors.black, width: thick)
        : BorderSide.none,
  );
}

class Notes extends StatelessWidget {
  const Notes({super.key, required this.notes});

  final Set<int> notes;

  @override
  Widget build(BuildContext context) {
    if (notes.isEmpty) {
      return const SizedBox.shrink();
    }

    return Column(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        for (int row = 0; row < PuzzleService.box; row++)
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              for (int col = 0; col < PuzzleService.box; col++)
                Text(
                  notes.contains(row * PuzzleService.box + col + 1)
                      ? "${row * PuzzleService.box + col + 1}"
                      : " ",
                  style: Theme.of(context).textTheme.labelSmall,
                ),
            ],
          ),
      ],
    );
  }
}

class CellPoint {
  const CellPoint({required this.x, required this.y});

  final int x;
  final int y;
}
