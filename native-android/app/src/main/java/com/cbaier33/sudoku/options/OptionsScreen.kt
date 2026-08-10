package com.cbaier33.sudoku.options

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbaier33.sudoku.game.Difficulty
import com.cbaier33.sudoku.game.SavedGameRepository
import com.cbaier33.sudoku.theme.CTA_HEIGHT
import com.cbaier33.sudoku.util.formatTime
import com.mudita.mmd.black
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.cards.CardMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD

@Composable
fun OptionsScreen(
    onBack: () -> Unit,
    onPlay: (Difficulty) -> Unit,
    onResume: (Difficulty) -> Unit,
) {
    // Not persisted - the Flutter build built a fresh OptionsViewModel per visit.
    var difficulty by rememberSaveable { mutableStateOf(Difficulty.EASY) }

    val context = LocalContext.current
    val saves = remember(context) { SavedGameRepository(context.applicationContext) }

    // Re-read on every visit, so a game saved and quit shows up here at once.
    val saved by saves.summaries.collectAsStateWithLifecycle(initialValue = emptyMap())

    Scaffold(
        topBar = {
            TopAppBarMMD(
                title = { TextMMD("Options", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextMMD("Select Difficulty", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (level in Difficulty.entries) {
                    DifficultyCard(
                        difficulty = level,
                        selected = level == difficulty,
                        saved = level in saved,
                        onClick = { difficulty = level },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom),
            ) {
                val resumable = saved[difficulty]

                if (resumable == null) {
                    ButtonMMD(
                        onClick = { onPlay(difficulty) },
                        modifier = Modifier.fillMaxWidth().height(CTA_HEIGHT),
                    ) {
                        TextMMD("Play", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    ButtonMMD(
                        onClick = { onResume(difficulty) },
                        modifier = Modifier.fillMaxWidth().height(CTA_HEIGHT),
                    ) {
                        TextMMD(
                            "Resume · ${formatTime(resumable.elapsed)}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    // The saved game survives this: only the next Save & Quit at
                    // this difficulty replaces it.
                    OutlinedButtonMMD(
                        onClick = { onPlay(difficulty) },
                        modifier = Modifier.fillMaxWidth().height(CTA_HEIGHT),
                    ) {
                        TextMMD("New Game", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

/**
 * Selection is carried by border weight rather than a fill, so the card reads the
 * same whether or not the panel has finished refreshing. The 3dp/2dp pair keeps
 * the card's outer size identical between states, so nothing shifts on tap.
 */
@Composable
private fun DifficultyCard(
    difficulty: Difficulty,
    selected: Boolean,
    saved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CardMMD(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(if (selected) 3.dp else 2.dp, black),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TextMMD(difficulty.label, style = MaterialTheme.typography.bodyLarge)
            TextMMD("${difficulty.givens} Given", style = MaterialTheme.typography.labelSmall)

            // Every card is sized for three lines whether or not it has a save,
            // so marking one never shifts the row.
            if (saved) TextMMD("Saved", style = MaterialTheme.typography.labelSmall)
        }
    }
}
