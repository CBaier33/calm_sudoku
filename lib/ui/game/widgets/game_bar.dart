import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sudoku/ui/game/view_models/game_viewmodel.dart';
import 'package:sudoku/ui/game/widgets/digit_display.dart';
import 'package:sudoku/ui/game/widgets/smiley_button.dart';

class GameBar extends StatelessWidget {
  const GameBar({super.key});

  @override
  Widget build(BuildContext context) {
    GameViewModel currentGame = Provider.of<GameViewModel>(context);

    return Container(
      decoration: BoxDecoration(
        border: Border(
          top: BorderSide(color: Colors.black, width: 2.0),
          bottom: BorderSide(color: Colors.black, width: 2.0),
          left: BorderSide(color: Colors.black, width: 2.0),
          right: BorderSide(color: Colors.black, width: 2.0),
        ),
      ),
      padding: const EdgeInsets.all(10),
      child: Center(
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.center,
          spacing: 25,
          children: [
            DigitDisplay(data: padInt(currentGame.remaining)),
            SmileyButton(),
            DigitDisplay(data: formatTime(currentGame.time)),
          ],
        ),
      ),
    );
  }
}
