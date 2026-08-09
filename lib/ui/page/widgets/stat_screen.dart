import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:sudoku/models/game_stats.dart';
import 'package:sudoku/services/stats_service.dart';
import 'package:sudoku/ui/core/widgets/action_modal.dart';
import 'package:sudoku/ui/core/widgets/simple_button.dart';
import 'package:sudoku/ui/game/widgets/digit_display.dart';
import 'package:sudoku/ui/page/view_models/options_view_model.dart';

class StatScreen extends StatefulWidget {
  const StatScreen({super.key});

  @override
  State<StatScreen> createState() => _StatScreenState();
}

class _StatScreenState extends State<StatScreen> {
  final _statsService = StatsService();
  GameStats? stats;

  @override
  void initState() {
    super.initState();
    _loadStats();
  }

  Future<void> clearAndRefresh() async {
    await _statsService.clearAll();
    await _loadStats();
  }

  Future<void> _loadStats() async {
    final loaded = await _statsService.load();

    setState(() {
      stats = loaded;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (stats == null) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        leadingWidth: 38,
        leading: SizedBox(
          width: 36,
          height: 36,
          child: IconButton(
            iconSize: 36,
            icon: const Icon(Icons.arrow_back),
            onPressed: () => Navigator.pop(context),
            hoverColor: Colors.transparent,
            splashColor: Colors.transparent,
            style: IconButton.styleFrom(overlayColor: Colors.transparent),
          ),
        ),
        title: Text(
          "Statistics",
          style: Theme.of(context).textTheme.titleMedium,
        ),
        backgroundColor: Colors.white,
        shape: const Border(
          bottom: BorderSide(color: Colors.black, width: 3.0),
        ),
        actionsPadding: EdgeInsets.only(right: 10),
        actions: [
          OutlinedButton(
            onPressed: () => actionModal(
              context,
              Column(
                mainAxisAlignment: MainAxisAlignment.start,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    "Clear all statistics?",
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  Text(
                    "This will give you a fresh start, but it can't be undone.",
                    style: GoogleFonts.lato(fontSize: 20),
                  ),
                ],
              ),
              [
                SimpleButton(
                  text: "Clear Statistics",
                  filled: true,
                  onPressed: () async {
                    Navigator.pop(context);
                    await clearAndRefresh();
                  },
                  onLongPress: () => {},
                ),

                SimpleButton(
                  text: "Cancel",
                  filled: false,
                  onPressed: () => {Navigator.pop(context)},
                  onLongPress: () => {},
                ),
              ],
            ),

            style: OutlinedButton.styleFrom(
              backgroundColor: Colors.white,
              foregroundColor: Colors.black,
              overlayColor: Colors.transparent,
              elevation: 0,
              side: const BorderSide(color: Colors.black, width: 2),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10),
              ),
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
              minimumSize: Size.zero,
              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
            ),
            child: const Text(
              'Clear all',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.w900),
            ),
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(12),
          child: Column(
            children: [
              StatLine(
                label: "Games Played",
                value: stats!.gamesPlayed.toString(),
              ),

              StatLine(
                label: "Win Streak",
                value: stats!.streak.toString(),
              ),

              StatLine(
                label: "Win Percentage",
                value: stats!.gamesPlayed == 0
                  ? "0"
                  : ((stats!.wins.values.fold(0, (a, b) => a + b) / stats!.gamesPlayed) * 100).toStringAsFixed(2),
                ),

              StatLine(
                label: "Easy Win / Loss Ratio",
                value:
                    "${stats!.wins[DifficultyLevel.easy]}/${stats!.losses[DifficultyLevel.easy]}",
              ),

              StatLine(
                label: "Medium Win / Loss Ratio",
                value:
                    "${stats!.wins[DifficultyLevel.medium]}/${stats!.losses[DifficultyLevel.medium]}",
              ),

              StatLine(
                label: "Hard Win / Loss Ratio",
                value:
                    "${stats!.wins[DifficultyLevel.hard]}/${stats!.losses[DifficultyLevel.hard]}",
              ),

              StatLine(
                label: "Best Easy Game",
                value: stats!.bestTimes[DifficultyLevel.easy] != 0
                    ? formatTime(stats!.bestTimes[DifficultyLevel.easy]!)
                    : "NA",
              ),

              StatLine(
                label: "Best Medium Game",
                value: stats!.bestTimes[DifficultyLevel.medium] != 0
                    ? formatTime(stats!.bestTimes[DifficultyLevel.medium]!)
                    : "NA",
              ),

              StatLine(
                label: "Best Hard Game",
                value: stats!.bestTimes[DifficultyLevel.hard] != 0
                    ? formatTime(stats!.bestTimes[DifficultyLevel.hard]!)
                    : "NA",
              ),
          ],
        ),
      ),
    );
  }
}

class StatHeader extends StatelessWidget {
  const StatHeader({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          decoration: BoxDecoration(
            border: Border(bottom: BorderSide(color: Colors.black, width: 2.0)),
          ),
          child: Text(title, style: Theme.of(context).textTheme.titleLarge),
        ),
      ],
    );
  }
}

class StatLine extends StatelessWidget {
  const StatLine({super.key, required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(5),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: Theme.of(context).textTheme.bodyMedium),
          Text(value, style: Theme.of(context).textTheme.bodyMedium),
        ],
      ),
    );
  }
}
