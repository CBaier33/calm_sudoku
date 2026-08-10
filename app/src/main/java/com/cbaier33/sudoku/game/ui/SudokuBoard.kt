package com.cbaier33.sudoku.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.cbaier33.sudoku.game.CellPoint
import com.cbaier33.sudoku.game.GameUiState
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.BOX
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.SIZE
import com.cbaier33.sudoku.game.isPeer
import com.cbaier33.sudoku.theme.BoardColors
import com.cbaier33.sudoku.theme.rememberBoardTextStyles
import kotlin.math.roundToInt

/**
 * The whole grid is one [Canvas] rather than 81 composables.
 *
 * That buys three things that matter on an E Ink panel: a single draw pass per
 * state change instead of a recomposition per cell, exact control of the grid
 * lines (they are snapped to whole pixels, so they stay crisp black instead of
 * dithering to grey the way antialiased half-pixel borders do), and glyphs
 * measured once per layout instead of per frame.
 *
 * No MMD component covers a grid, so this is hand-drawn - but it stays inside
 * MMD's palette and type scale.
 */
@Composable
fun SudokuBoard(
    state: GameUiState,
    highlightPeers: Boolean,
    onTapCell: (CellPoint) -> Unit,
    onLongPressCell: (CellPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val styles = rememberBoardTextStyles()

        val cellDp = maxWidth / SIZE

        // Digits and pencil marks shrink to fit a small cell but never grow past
        // the type scale - the same intent as the Flutter build's
        // FittedBox(fit: BoxFit.scaleDown).
        val digitSize = with(density) { (cellDp * 0.78f).toSp() }
            .let { fitted -> if (fitted.value < styles.given.fontSize.value) fitted else styles.given.fontSize }
        val noteSize = with(density) { (cellDp / BOX * 0.92f).toSp() }
            .let { fitted -> if (fitted.value < styles.note.fontSize.value) fitted else styles.note.fontSize }

        val measurer = rememberTextMeasurer()

        // Colour is applied at draw time, so three layout sets cover every cell.
        val givenGlyphs = rememberGlyphs(measurer, styles.given.copy(fontSize = digitSize))
        val enteredGlyphs = rememberGlyphs(measurer, styles.entered.copy(fontSize = digitSize))
        val noteGlyphs = rememberGlyphs(measurer, styles.note.copy(fontSize = noteSize))

        val insetPx = with(density) { 2.dp.toPx() }

        val thickPx = with(density) { 3.dp.toPx() }.roundToInt().coerceAtLeast(1)
        val thinPx = with(density) { 1.dp.toPx() }.roundToInt().coerceAtLeast(1)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.loading, state.result) {
                    val cell = size.width / SIZE.toFloat()

                    detectTapGestures(
                        onTap = { offset -> onTapCell(offset.toCellPoint(cell)) },
                        onLongPress = { offset -> onLongPressCell(offset.toCellPoint(cell)) },
                    )
                },
        ) {
            val cell = size.width / SIZE.toFloat()

            // Whole-pixel cell edges, so fills meet exactly and lines stay hard.
            val edge = IntArray(SIZE + 1) { i -> (i * cell).roundToInt() }

            drawFills(state, highlightPeers, edge)
            drawGridLines(edge, thickPx, thinPx)

            if (!state.loading && state.cells.isNotEmpty()) {
                drawContents(state, edge, insetPx, givenGlyphs, enteredGlyphs, noteGlyphs)
            }
        }
    }
}

private fun Offset.toCellPoint(cell: Float): CellPoint = CellPoint(
    x = (y / cell).toInt().coerceIn(0, SIZE - 1),
    y = (x / cell).toInt().coerceIn(0, SIZE - 1),
)

@Composable
private fun rememberGlyphs(measurer: TextMeasurer, style: TextStyle): List<TextLayoutResult> =
    remember(measurer, style) {
        List(SIZE) { i -> measurer.measure(text = (i + 1).toString(), style = style) }
    }

private fun DrawScope.drawFills(
    state: GameUiState,
    highlightPeers: Boolean,
    edge: IntArray,
) {
    drawRect(color = BoardColors.paper)

    if (state.cells.isEmpty()) return

    val selected = state.selected

    for (x in 0 until SIZE) {
        for (y in 0 until SIZE) {
            val p = CellPoint(x, y)
            val cell = state.cellAt(p)
            val wrong = cell.value != 0 && !cell.correct

            val fill = when {
                wrong -> BoardColors.wrongFill
                selected != null && selected == p -> BoardColors.selectedFill
                selected != null && highlightPeers && isPeer(selected, p) -> BoardColors.peerFill
                else -> null
            } ?: continue

            drawRect(
                color = fill,
                topLeft = Offset(edge[y].toFloat(), edge[x].toFloat()),
                size = Size(
                    (edge[y + 1] - edge[y]).toFloat(),
                    (edge[x + 1] - edge[x]).toFloat(),
                ),
            )
        }
    }
}

/**
 * Lines are filled rectangles of an exact pixel width rather than strokes, so
 * nothing lands on a half pixel. Box boundaries and the outer frame get the
 * heavy line; the outermost lines are pulled inside the canvas so they are not
 * clipped in half.
 */
private fun DrawScope.drawGridLines(
    edge: IntArray,
    thickPx: Int,
    thinPx: Int,
) {
    for (i in 0..SIZE) {
        val width = if (i % BOX == 0) thickPx else thinPx
        val centre = edge[i]

        val left = (centre - width / 2)
            .coerceIn(0, (size.width - width).toInt().coerceAtLeast(0))
        val top = (centre - width / 2)
            .coerceIn(0, (size.height - width).toInt().coerceAtLeast(0))

        drawRect(
            color = BoardColors.ink,
            topLeft = Offset(left.toFloat(), 0f),
            size = Size(width.toFloat(), size.height),
        )

        drawRect(
            color = BoardColors.ink,
            topLeft = Offset(0f, top.toFloat()),
            size = Size(size.width, width.toFloat()),
        )
    }
}

private fun DrawScope.drawContents(
    state: GameUiState,
    edge: IntArray,
    inset: Float,
    givenGlyphs: List<TextLayoutResult>,
    enteredGlyphs: List<TextLayoutResult>,
    noteGlyphs: List<TextLayoutResult>,
) {
    for (x in 0 until SIZE) {
        for (y in 0 until SIZE) {
            val p = CellPoint(x, y)
            val cell = state.cellAt(p)

            // Keep glyphs off the grid lines, which are drawn underneath them.
            val left = edge[y] + inset
            val top = edge[x] + inset
            val width = (edge[y + 1] - edge[y]) - inset * 2f
            val height = (edge[x + 1] - edge[x]) - inset * 2f

            if (cell.value == 0) {
                drawNotes(cell.notes, left, top, width, height, noteGlyphs)
                continue
            }

            val wrong = !cell.correct
            val layout = if (cell.given) givenGlyphs[cell.value - 1] else enteredGlyphs[cell.value - 1]

            drawCentred(
                layout = layout,
                colour = if (wrong) BoardColors.wrongInk else BoardColors.ink,
                left = left,
                top = top,
                width = width,
                height = height,
            )
        }
    }
}

/** Each mark keeps a fixed ninth of the cell, so marks stay on their own grid. */
private fun DrawScope.drawNotes(
    notes: Set<Int>,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    noteGlyphs: List<TextLayoutResult>,
) {
    if (notes.isEmpty()) return

    val markWidth = width / BOX
    val markHeight = height / BOX

    for (digit in notes) {
        val row = (digit - 1) / BOX
        val col = (digit - 1) % BOX

        drawCentred(
            layout = noteGlyphs[digit - 1],
            colour = BoardColors.ink,
            left = left + col * markWidth,
            top = top + row * markHeight,
            width = markWidth,
            height = markHeight,
        )
    }
}

private fun DrawScope.drawCentred(
    layout: TextLayoutResult,
    colour: Color,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
) {
    drawText(
        textLayoutResult = layout,
        color = colour,
        topLeft = Offset(
            left + (width - layout.size.width) / 2f,
            top + (height - layout.size.height) / 2f,
        ),
    )
}
