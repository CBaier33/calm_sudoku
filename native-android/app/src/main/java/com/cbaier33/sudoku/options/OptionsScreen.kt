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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cbaier33.sudoku.game.Difficulty
import com.mudita.mmd.black
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.cards.CardMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD

@Composable
fun OptionsScreen(
    onBack: () -> Unit,
    onPlay: (Difficulty) -> Unit,
) {
    // Not persisted - the Flutter build built a fresh OptionsViewModel per visit.
    var difficulty by rememberSaveable { mutableStateOf(Difficulty.EASY) }

    Scaffold(
        topBar = {
            TopAppBarMMD(
                title = { TextMMD("Options", style = MaterialTheme.typography.titleMedium) },
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
            TextMMD("Select Difficulty", style = MaterialTheme.typography.bodyLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (level in Difficulty.entries) {
                    DifficultyCard(
                        difficulty = level,
                        selected = level == difficulty,
                        onClick = { difficulty = level },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Bottom,
            ) {
                ButtonMMD(
                    onClick = { onPlay(difficulty) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextMMD("Play", style = MaterialTheme.typography.titleMedium)
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CardMMD(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(if (selected) 3.dp else 2.dp, black),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TextMMD(difficulty.label, style = MaterialTheme.typography.bodyMedium)
            TextMMD("${difficulty.givens} Given", style = MaterialTheme.typography.labelSmall)
        }
    }
}
