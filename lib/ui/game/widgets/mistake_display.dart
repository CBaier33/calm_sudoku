import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sudoku/ui/game/view_models/game_viewmodel.dart';

class MistakeDisplay extends StatelessWidget {
  const MistakeDisplay({super.key});

  @override
  Widget build(BuildContext context) {
    GameViewModel currentGame = Provider.of<GameViewModel>(context);

    return Container(
      width: 54,
      height: 35,
      decoration: BoxDecoration(
        border: Border.all(color: Colors.black, width: 2.0),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Center(
        child: Text(
          "${currentGame.mistakes}/${currentGame.mistakesAllowed}",
          style: Theme.of(context).textTheme.titleSmall,
        ),
      ),
    );
  }
}
