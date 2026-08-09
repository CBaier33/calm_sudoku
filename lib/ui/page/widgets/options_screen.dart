import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sudoku/ui/core/widgets/difficulty_selector.dart';
import 'package:sudoku/ui/core/widgets/page_route.dart';
import 'package:sudoku/ui/core/widgets/simple_appbar.dart';
import 'package:sudoku/ui/core/widgets/simple_button.dart';
import 'package:sudoku/ui/core/widgets/simple_toggle.dart';
import 'package:sudoku/ui/game/view_models/game_viewmodel.dart';
import 'package:sudoku/ui/game/widgets/game_screen.dart';
import 'package:sudoku/ui/page/view_models/options_view_model.dart';

class OptionsScreen extends StatefulWidget {
  const OptionsScreen({super.key});

  @override
  State<OptionsScreen> createState() => _OptionsScreenState();
}

class _OptionsScreenState extends State<OptionsScreen> {
  late final OptionsViewModel optionsViewModel;

  @override
  void initState() {
    super.initState();
    optionsViewModel = OptionsViewModel();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _precacheImages();
    });
  }

  Future<void> _precacheImages() async {
  const assets = [
    'assets/pause.png',
    'assets/engaged-smile.png',
    'assets/lose-smile.png',
    'assets/normal-smile.png',
    'assets/win-smile.png',
  ];

  for (final asset in assets) {
    try {
      await precacheImage(
        AssetImage(asset),
        context,
      );
    } catch (e) {
      debugPrint('Failed loading $asset: $e');
    }
  }
}

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: SimpleAppBar(title: "Options"),
      backgroundColor: Colors.white,
      body: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.start,
                children: [
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: Text(
                      "Select Difficulty",
                      style: Theme.of(context).textTheme.bodyMedium,
                    ),
                  ),
                  ChangeNotifierProvider.value(
                    value: optionsViewModel,
                    child: DifficultySelector(),
                  ),
                  SimpleToggle(
                    label: "Highlight Peers",
                    value: optionsViewModel.highlightPeers,
                    onChanged: (v) {
                      setState(() {
                        optionsViewModel.highlightPeers = v;
                      });
                    },
                  ),
                  SimpleToggle(
                    label: "Auto Clear Notes",
                    value: optionsViewModel.autoClearNotes,
                    onChanged: (v) {
                      setState(() {
                        optionsViewModel.autoClearNotes = v;
                      });
                    },
                  ),
                ],
              ),
            ),
            Column(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              spacing: 16,
              children: [
                SimpleButton(
                  text: "Play",
                  filled: true,
                  onPressed: () {
                    Navigator.push(
                      context,
                      SimplePageRoute<void>(
                        builder: (context) => GameScreen(
                          viewModel: GameViewModel(options: optionsViewModel),
                        ),
                      ),
                    );
                  },
                  onLongPress: () {},
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
