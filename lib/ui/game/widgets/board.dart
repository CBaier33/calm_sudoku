import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sudoku/services/puzzle_service.dart';
import 'package:sudoku/ui/game/view_models/game_viewmodel.dart';
import 'package:sudoku/ui/game/widgets/cell.dart';

class Board extends StatelessWidget {
  const Board({super.key});

  @override
  Widget build(BuildContext context) {
    return GridView.builder(
      padding: EdgeInsets.zero,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: PuzzleService.size,
      ),
      itemBuilder: (context, index) => _buildCells(context, index),
      itemCount: PuzzleService.cells,
    );
  }
}

Widget _buildCells(BuildContext context, int index) {
  GameViewModel currentGame = Provider.of<GameViewModel>(context);

  int x = (index ~/ PuzzleService.size);
  int y = (index % PuzzleService.size);
  CellPoint cell = CellPoint(x: x, y: y);

  return GestureDetector(
    onLongPressStart: (_) => currentGame.onLongPressCell(cell),
    onTap: () => currentGame.onTapCell(cell),
    child: GridTile(
      child: Cell(gridPoint: cell),
    ),
  );
}
