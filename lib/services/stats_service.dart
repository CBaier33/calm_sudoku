import 'package:shared_preferences/shared_preferences.dart';
import 'package:sudoku/models/game_stats.dart';
import 'package:sudoku/ui/page/view_models/options_view_model.dart';

class StatsService {
  static const _gamesPlayed = 'gamesPlayed';
  static const _streak = 'streak';

  String _winsKey(DifficultyLevel d) => 'wins_${d.name}';
  String _lossesKey(DifficultyLevel d) => 'losses_${d.name}';
  String _bestTimeKey(DifficultyLevel d) => 'bestTime_${d.name}';

  static const _difficulties = [
    DifficultyLevel.easy,
    DifficultyLevel.medium,
    DifficultyLevel.hard,
  ];

  Future<GameStats> load() async {
    final prefs = await SharedPreferences.getInstance();

    return GameStats(
      gamesPlayed: prefs.getInt(_gamesPlayed) ?? 0,
      wins: {
        for (final d in _difficulties) d: prefs.getInt(_winsKey(d)) ?? 0,
      },
      losses: {
        for (final d in _difficulties) d: prefs.getInt(_lossesKey(d)) ?? 0,
      },
      streak: prefs.getInt(_streak) ?? 0,
      bestTimes: {
        for (final d in _difficulties) d: prefs.getInt(_bestTimeKey(d)) ?? 0,
      },
    );
  }

  Future<void> save(GameStats stats) async {
    final prefs = await SharedPreferences.getInstance();

    await prefs.setInt(_gamesPlayed, stats.gamesPlayed);
    await prefs.setInt(_streak, stats.streak);

    for (final d in _difficulties) {
      await prefs.setInt(_winsKey(d), stats.wins[d] ?? 0);
      await prefs.setInt(_lossesKey(d), stats.losses[d] ?? 0);
      await prefs.setInt(_bestTimeKey(d), stats.bestTimes[d] ?? 0);
    }
  }

  Future<void> clearAll() async {
    await save(const GameStats.empty());
  }

  Future<void> recordGame(
    int seconds,
    DifficultyLevel diff,
    bool win,
  ) async {
    final stats = await load();

    final wins = Map<DifficultyLevel, int>.from(stats.wins);
    final losses = Map<DifficultyLevel, int>.from(stats.losses);
    final bestTimes = Map<DifficultyLevel, int>.from(stats.bestTimes);

    int streak = stats.streak;

    if (win) {
      wins[diff] = (wins[diff] ?? 0) + 1;
      streak++;

      final currentBest = bestTimes[diff] ?? 0;
      if (currentBest == 0 || seconds < currentBest) {
        bestTimes[diff] = seconds;
      }
    } else {
      losses[diff] = (losses[diff] ?? 0) + 1;
      streak = 0;
    }

    final updated = GameStats(
      gamesPlayed: stats.gamesPlayed + 1,
      wins: wins,
      losses: losses,
      streak: streak,
      bestTimes: bestTimes,
    );

    await save(updated);
  }
}
