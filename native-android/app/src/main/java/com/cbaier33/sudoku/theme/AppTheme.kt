package com.cbaier33.sudoku.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.mudita.mmd.ThemeMMD
import com.mudita.mmd.black
import com.mudita.mmd.eInkColorScheme
import com.mudita.mmd.eInkTypography
import com.mudita.mmd.white

/**
 * MMD's theme, with the gaps in [eInkColorScheme] filled in.
 *
 * MMD leaves six surface slots [Color.Unspecified] - `surfaceBright`,
 * `surfaceDim`, `surfaceContainer`, `surfaceContainerHigh`,
 * `surfaceContainerHighest` and `surfaceContainerLowest`. TopAppBarMMD reads
 * `surfaceContainer` for its scrolled container, so anything landing on one of
 * those renders undefined. Everything in MMD is white with black outlines, so
 * white is the right value.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    ThemeMMD(
        colorScheme = eInkColorScheme.copy(
            surfaceBright = white,
            surfaceDim = white,
            surfaceContainer = white,
            surfaceContainerHigh = white,
            surfaceContainerHighest = white,
            surfaceContainerLowest = white,
        ),
        typography = eInkTypography,
        content = content,
    )
}

/**
 * Fills used for board cells.
 *
 * MMD is strictly black and white with no grey ramp, but the board needs three
 * distinguishable non-content states. Black at low alpha is inside MMD's own
 * vocabulary - it uses `black.copy(alpha = 0.75f)` for disabled button borders -
 * and dithers to a stable grey on the panel.
 */
object BoardColors {
    val ink: Color = black
    val paper: Color = white

    val selectedFill: Color = black.copy(alpha = 0.25f)
    val peerFill: Color = black.copy(alpha = 0.08f)

    /** A wrong guess inverts, which stays legible on a greyscale panel. */
    val wrongFill: Color = black
    val wrongInk: Color = white

    /** A digit with all nine placed has nothing left to give. */
    val spentInk: Color = black.copy(alpha = 0.4f)
}

/**
 * Board text styles.
 *
 * MMD asks for [FontWeight.Medium] in every slot, but its Lato family registers
 * no W500 face - only Thin, Light, Regular, Bold and Black (plus italics), so
 * Compose's weight matching lands MMD's body text on Lato Regular. That leaves
 * no room for the given-vs-entered contrast the game depends on, so the board
 * derives its own weights: Black (W900) and Light (W300) both hit real faces
 * exactly, giving a much stronger separation than the Flutter build's w900/w500.
 *
 * Deriving with `copy(fontWeight = ...)` keeps MMD's private Lato family and its
 * 28sp size, so this stays inside the type scale.
 *
 * Only [MaterialTheme.typography] slots MMD actually overrides are used here.
 * `display*`, `headlineMedium` and `headlineSmall` are *not* overridden by MMD
 * and silently fall back to Roboto.
 */
@Immutable
data class BoardTextStyles(
    val given: TextStyle,
    val entered: TextStyle,
    val note: TextStyle,
)

@Composable
fun rememberBoardTextStyles(): BoardTextStyles {
    val headline = MaterialTheme.typography.headlineLarge // Lato 28sp
    val label = MaterialTheme.typography.labelSmall       // Lato 14sp
    return BoardTextStyles(
        given = headline.copy(fontWeight = FontWeight.Black),
        entered = headline.copy(fontWeight = FontWeight.Light),
        note = label,
    )
}
