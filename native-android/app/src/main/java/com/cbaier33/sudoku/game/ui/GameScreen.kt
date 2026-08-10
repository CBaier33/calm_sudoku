package com.cbaier33.sudoku.game.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cbaier33.sudoku.R
import com.cbaier33.sudoku.game.GameViewModel
import com.cbaier33.sudoku.util.formatTime
import com.mudita.mmd.black
import com.mudita.mmd.components.bottom_sheet.ModalBottomSheetMMD
import com.mudita.mmd.components.bottom_sheet.rememberModalBottomSheetMMDState
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD

@Composable
fun GameScreen(
    onBack: () -> Unit,
    onExit: () -> Unit,
    viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // The clock is collected on its own so a tick never redraws the board.
    val elapsed by viewModel.elapsed.collectAsStateWithLifecycle()

    var menuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBarMMD(
                title = { TextMMD("Sudoku", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Readout(formatTime(elapsed))

                    Readout("${state.mistakes}/${state.mistakesAllowed}")

                    IconButton(
                        onClick = {
                            viewModel.pauseTimer()
                            menuOpen = true
                        },
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        Icon(painterResource(R.drawable.ic_pause), contentDescription = "Pause")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                SudokuBoard(
                    state = state,
                    highlightPeers = viewModel.highlightPeers,
                    onTapCell = viewModel::onTapCell,
                    onLongPressCell = viewModel::onLongPressCell,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                )
            }

            NumberPad(
                state = state,
                onDigit = viewModel::onTapDigit,
                onToggleNotes = viewModel::toggleNoteMode,
                onErase = viewModel::onTapErase,
            )
        }
    }

    if (menuOpen) {
        val sheetState = rememberModalBottomSheetMMDState(skipPartiallyExpanded = true)

        // Every way out of this sheet resumes the clock, including a scrim tap.
        val dismiss = {
            menuOpen = false
            viewModel.resumeTimer()
        }

        ModalBottomSheetMMD(
            onDismissRequest = dismiss,
            sheetState = sheetState,
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextMMD("Menu", style = MaterialTheme.typography.titleLarge)

                ButtonMMD(
                    onClick = dismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextMMD("Continue", style = MaterialTheme.typography.titleMedium)
                }

                OutlinedButtonMMD(
                    onClick = {
                        viewModel.resetGame()
                        dismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextMMD("New Puzzle", style = MaterialTheme.typography.titleMedium)
                }

                OutlinedButtonMMD(
                    onClick = {
                        menuOpen = false
                        onExit()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextMMD("Exit", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/** The boxed timer and mistake counters in the app bar. */
@Composable
private fun Readout(text: String) {
    Box(
        modifier = Modifier
            .padding(end = 6.dp)
            .border(2.dp, black, RoundedCornerShape(8.dp))
            .defaultMinSize(minWidth = 52.dp)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        TextMMD(text, style = MaterialTheme.typography.labelMedium)
    }
}
