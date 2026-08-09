import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sudoku/ui/game/view_models/game_viewmodel.dart';

class SmileyButton extends StatelessWidget {
  const SmileyButton({super.key});

  @override
  Widget build(BuildContext context) {
    GameViewModel currentGame = Provider.of<GameViewModel>(context);

    return GestureDetector(
      onTap: () {
        currentGame.resetGame();
      },
      child: Container(
        width: 60,
        height: 60,
        padding: EdgeInsets.all(5),
        decoration: BoxDecoration(
          border: Border.all(color: Colors.black, width: 3),
          borderRadius: BorderRadius.circular(13),
        ),
        child: Center(
          child: AnimatedSwitcher(
            duration: const Duration(milliseconds: 1),
            child: Image.asset(
              currentGame.getCurrentSmiley(),
              fit: BoxFit.contain,
            ),
          ),
        ),
      ),
    );
  }
}
