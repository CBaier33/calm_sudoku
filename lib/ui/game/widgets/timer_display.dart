import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sudoku/ui/game/view_models/game_viewmodel.dart';

class TimerDisplay extends StatelessWidget {
  const TimerDisplay({super.key});

  @override
  Widget build(BuildContext context) {
    GameViewModel currentGame = Provider.of<GameViewModel>(context);

    return Container(
      width: 60,
      height: 35,
      decoration: BoxDecoration(
        border: Border.all(color: Colors.black, width: 2.0),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Center(
        child: Text(
          formatTime(currentGame.time),
          style: Theme.of(context).textTheme.titleSmall,
        ),
      ),
    );
  }
}

String formatTime(int seconds) {
  if (seconds >= 5999) return "99:59";
  final minutes = (seconds ~/ 60).toString().padLeft(2, '0');
  return "$minutes:${(seconds % 60).toString().padLeft(2, '0')}";
}
