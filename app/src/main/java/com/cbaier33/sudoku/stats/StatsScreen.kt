package com.cbaier33.sudoku.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbaier33.sudoku.game.Difficulty
import com.cbaier33.sudoku.theme.CTA_HEIGHT
import com.cbaier33.sudoku.util.formatTime
import com.mudita.mmd.black
import com.mudita.mmd.components.bottom_sheet.ModalBottomSheetMMD
import com.mudita.mmd.components.bottom_sheet.rememberModalBottomSheetMMDState
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
                title = { TextMMD("Statistics", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    OutlinedButtonMMD(
                        onClick = { confirmingClear = true },
                        modifier = Modifier.padding(end = 10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
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
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            StatLine("Games Played", stats.gamesPlayed.toString())
            StatLine("Win Streak", stats.streak.toString())
            StatLine("Percentage of wins", stats.winPercentage)

            StatLine("Easy Win / Loss", stats.winLoss(Difficulty.EASY))
            StatLine("Medium Win / Loss", stats.winLoss(Difficulty.MEDIUM))
            StatLine("Hard Win / Loss", stats.winLoss(Difficulty.HARD))

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
                    modifier = Modifier.fillMaxWidth().height(CTA_HEIGHT),
                ) {
                    TextMMD("Clear Statistics", style = MaterialTheme.typography.titleMedium)
                }

                OutlinedButtonMMD(
                    onClick = { confirmingClear = false },
                    modifier = Modifier.fillMaxWidth().height(CTA_HEIGHT),
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

/**
 * Value first and large, label second - the arrangement Mudita's own MMD apps
 * use for a results list, which reads far better at arm's length than a
 * label-left/value-right row.
 */
@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextMMD(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.widthIn(min = 86.dp),
        )
        TextMMD(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }

    DottedRule()
}

/**
 * Drawn rather than dashed with a stroke so the dashes land on whole pixels -
 * a half-pixel dash dithers to grey on the panel.
 */
@Composable
private fun DottedRule() {
    Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
        val on = 3.dp.toPx().roundToInt().coerceAtLeast(1).toFloat()
        val off = 5.dp.toPx().roundToInt().coerceAtLeast(1).toFloat()

        drawLine(
            color = black,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(on, off), 0f),
        )
    }
}
