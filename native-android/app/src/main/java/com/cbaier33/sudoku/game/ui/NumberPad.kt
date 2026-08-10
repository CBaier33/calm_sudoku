package com.cbaier33.sudoku.game.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cbaier33.sudoku.game.GameUiState
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.SIZE
import com.cbaier33.sudoku.theme.BoardColors
import com.mudita.mmd.black
import com.mudita.mmd.components.buttons.ButtonDefaultsMMD
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.text.TextMMD

private val BUTTON_HEIGHT = 48.dp
private val GAP = 8.dp

@Composable
fun NumberPad(
    state: GameUiState,
    onDigit: (Int) -> Unit,
    onToggleNotes: () -> Unit,
    onErase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(GAP),
        verticalArrangement = Arrangement.spacedBy(GAP),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (digit in 1..SIZE) {
                // A digit with all nine placed has nothing left to give, but it
                // stays tappable: placing it again is still a mistake.
                val spent = state.digitsRemaining.getOrElse(digit) { SIZE } == 0

                PadButton(
                    label = digit.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    filled = false,
                    spent = spent,
                    onClick = { onDigit(digit) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GAP),
        ) {
            PadButton(
                label = "Notes",
                style = MaterialTheme.typography.labelLarge,
                filled = state.noteMode,
                spent = false,
                onClick = onToggleNotes,
                modifier = Modifier.weight(1f),
            )

            PadButton(
                label = "Erase",
                style = MaterialTheme.typography.labelLarge,
                filled = false,
                spent = false,
                onClick = onErase,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * `Modifier.weight` hands the button a fixed width, which overrides the
 * `defaultMinSize(minWidth = 50.dp)` inside ButtonMMD - nine of those would want
 * 450dp on a 360dp-wide screen. Content padding goes to zero for the same reason.
 */
@Composable
private fun PadButton(
    label: String,
    style: androidx.compose.ui.text.TextStyle,
    filled: Boolean,
    spent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = @Composable {
        TextMMD(
            text = label,
            style = style,
            color = when {
                filled -> BoardColors.paper
                spent -> BoardColors.spentInk
                else -> black
            },
            textAlign = TextAlign.Center,
        )
    }

    if (filled) {
        ButtonMMD(
            onClick = onClick,
            modifier = modifier.height(BUTTON_HEIGHT),
            contentPadding = PaddingValues(0.dp),
        ) { content() }
    } else {
        OutlinedButtonMMD(
            onClick = onClick,
            modifier = modifier.height(BUTTON_HEIGHT),
            border = BorderStroke(
                ButtonDefaultsMMD.borderWidth,
                if (spent) BoardColors.spentInk else black,
            ),
            contentPadding = PaddingValues(0.dp),
        ) { content() }
    }
}
