package com.cbaier33.sudoku.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbaier33.sudoku.game.Difficulty
import com.cbaier33.sudoku.util.formatTime
import com.mudita.mmd.components.bottom_sheet.ModalBottomSheetMMD
import com.mudita.mmd.components.bottom_sheet.rememberModalBottomSheetMMDState
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import kotlinx.coroutines.launch

@Composable
fun StatsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { StatsRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()

    val stats by repository.stats.collectAsStateWithLifecycle(initialValue = GameStats.EMPTY)

    var confirmingClear by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBarMMD(
                title = { TextMMD("Statistics", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    OutlinedButtonMMD(
                        onClick = { confirmingClear = true },
                        modifier = Modifier.padding(end = 10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 4.dp,
                        ),
                    ) {
                        TextMMD("Clear all", style = MaterialTheme.typography.titleSmall)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            StatLine("Games Played", stats.gamesPlayed.toString())
            StatLine("Win Streak", stats.streak.toString())
            StatLine("Win Percentage", stats.winPercentage)

            StatLine("Easy Win / Loss Ratio", stats.winLoss(Difficulty.EASY))
            StatLine("Medium Win / Loss Ratio", stats.winLoss(Difficulty.MEDIUM))
            StatLine("Hard Win / Loss Ratio", stats.winLoss(Difficulty.HARD))

            StatLine("Best Easy Game", bestTime(stats, Difficulty.EASY))
            StatLine("Best Medium Game", bestTime(stats, Difficulty.MEDIUM))
            StatLine("Best Hard Game", bestTime(stats, Difficulty.HARD))
        }
    }

    if (confirmingClear) {
        val sheetState = rememberModalBottomSheetMMDState(skipPartiallyExpanded = true)

        ModalBottomSheetMMD(
            onDismissRequest = { confirmingClear = false },
            sheetState = sheetState,
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextMMD("Clear all statistics?", style = MaterialTheme.typography.titleLarge)
                TextMMD(
                    "This will give you a fresh start, but it can't be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                ButtonMMD(
                    onClick = {
                        confirmingClear = false
                        scope.launch { repository.clearAll() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextMMD("Clear Statistics", style = MaterialTheme.typography.titleMedium)
                }

                OutlinedButtonMMD(
                    onClick = { confirmingClear = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextMMD("Cancel", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/** 0 seconds is the "never won one" sentinel. */
private fun bestTime(stats: GameStats, difficulty: Difficulty): String {
    val seconds = stats.bestTimes[difficulty] ?: 0
    return if (seconds != 0) formatTime(seconds) else "NA"
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextMMD(label, style = MaterialTheme.typography.bodyMedium)
        TextMMD(value, style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDividerMMD(thickness = 1.dp)
}
