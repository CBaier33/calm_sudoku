import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sudoku/ui/core/widgets/action_modal.dart';
import 'package:sudoku/ui/core/widgets/simple_button.dart';
import 'package:sudoku/ui/game/view_models/game_viewmodel.dart';
import 'package:sudoku/ui/game/widgets/board.dart';
import 'package:sudoku/ui/game/widgets/game_bar.dart';
import 'package:sudoku/ui/game/widgets/mistake_display.dart';
import 'package:sudoku/ui/game/widgets/number_pad.dart';

class GameScreen extends StatefulWidget {
  const GameScreen({super.key, required this.viewModel});

  final GameViewModel viewModel;

  @override
  State<GameScreen> createState() => _GameScreenState();
}

class _GameScreenState extends State<GameScreen> {
  // The view model is handed to us per game, so its timer dies with the screen.
  @override
  void dispose() {
    widget.viewModel.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider.value(
      value: widget.viewModel,
      child: Scaffold(
        appBar: AppBar(
          leadingWidth: 38,
          leading: SizedBox(
            width: 36,
            height: 36,
            child: IconButton(
              iconSize: 36,
              icon: const Icon(Icons.arrow_back),
              onPressed: () => Navigator.pop(context),
              style: IconButton.styleFrom(overlayColor: Colors.transparent),
              hoverColor: Colors.transparent,
              splashColor: Colors.transparent,
              focusColor: Colors.transparent,
            ),
          ),
          title: Text(
            "Sudoku",
            style: Theme.of(context).textTheme.titleMedium,
          ),
          backgroundColor: Colors.white,
          shape: const Border(top: BorderSide(color: Colors.black, width: 3.0)),
          actionsPadding: EdgeInsets.only(right: 10),
          actions: [
            MistakeDisplay(),
            const SizedBox(width: 8),
            Container(
              width: 50,
              height: 35,
              decoration: BoxDecoration(
                border: Border.all(color: Colors.black, width: 2.0),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Center(
                child: IconButton(
                  icon: Image.asset(
                    'assets/pause.png',
                    fit: BoxFit.contain,
                  ),
                  iconSize: 70,
                  onPressed: () {
                    widget.viewModel.pauseTimer();
                    // Also covers dismissing the sheet by tapping the barrier,
                    // which would otherwise leave the clock stopped.
                    actionModal(
                      context,
                      Column(
                        spacing: 10,
                        crossAxisAlignment: CrossAxisAlignment.center,
                        children: [
                          Text(
                            "Menu",
                            style: Theme.of(context).textTheme.titleLarge,
                          ),
                        ],
                      ),
                      [
                        SimpleButton(
                          text: "Continue",
                          filled: true,
                          onPressed: () {
                            widget.viewModel.resumeTimer();
                            Navigator.pop(context);
                          },
                          onLongPress: () => {},
                        ),

                        SimpleButton(
                          text: "New Puzzle",
                          filled: false,
                          onPressed: () {
                            widget.viewModel.resetGame();
                            Navigator.pop(context);
                          },
                          onLongPress: () => {},
                        ),

                        SimpleButton(
                          text: "Exit",
                          filled: false,
                          onPressed: () => {
                            Navigator.of(
                              context,
                            ).popUntil((route) => route.isFirst),
                          },
                          onLongPress: () => {},
                        ),
                      ],
                    ).then((_) => widget.viewModel.resumeTimer());
                  },
                  style: IconButton.styleFrom(overlayColor: Colors.transparent),
                ),
              ),
            ),
          ],
        ),
        backgroundColor: Colors.white,
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            GameBar(),
            Expanded(
              child: Center(
                child: AspectRatio(
                  aspectRatio: 1,
                  child: Board(),
                ),
              ),
            ),
            NumberPad(),
          ],
        ),
      ),
    );
  }
}
