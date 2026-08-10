package com.cbaier33.sudoku.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.ThemeMMD
import com.mudita.mmd.black
import com.mudita.mmd.eInkColorScheme
import com.mudita.mmd.eInkTypography
import com.mudita.mmd.white

/**
 * MMD's theme, with the gaps in [eInkColorScheme] filled in and the type scale
 * rebuilt - see [appTypography].
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
        typography = appTypography,
        content = content,
    )
}

/**
 * Everything is Bold, and a step or two larger than MMD's own defaults.
 *
 * MMD's [eInkTypography] asks for [FontWeight.Medium] in every slot, but the
 * Lato family it bundles registers no W500 face - only Thin, Light, Regular,
 * Bold and Black - so Compose's weight matching quietly drops its text to Lato
 * Regular. On the panel that reads thin and small. Mudita's own MMD apps are
 * set in Bold throughout, so this asks for W700 explicitly, which is an exact
 * face match rather than a synthesised weight.
 *
 * Sizes are lifted to match: 24sp screen titles, 20sp section headers and
 * button labels, 18sp body.
 *
 * The `display*`, `headlineMedium` and `headlineSmall` slots are set here too.
 * MMD does not override them, so left alone they fall back to Roboto - nothing
 * in the app uses them today, but a stray reference would silently change font.
 */
private val lato = eInkTypography.headlineLarge.fontFamily

private fun bold(size: TextUnit) = TextStyle(
    fontFamily = lato,
    fontWeight = FontWeight.Bold,
    fontSize = size,
)

val appTypography = Typography(
    displayLarge = bold(40.sp),
    displayMedium = bold(34.sp),
    displaySmall = bold(30.sp),

    headlineLarge = bold(30.sp),
    headlineMedium = bold(26.sp),
    headlineSmall = bold(24.sp),

    // Screen titles.
    titleLarge = bold(24.sp),
    // Section headers and primary button labels.
    titleMedium = bold(20.sp),
    // Compact controls: the app bar's Clear all, the timer and mistake boxes.
    titleSmall = bold(16.sp),

    bodyLarge = bold(20.sp),
    bodyMedium = bold(18.sp),
    bodySmall = bold(16.sp),

    labelLarge = bold(18.sp),
    labelMedium = bold(16.sp),
    labelSmall = bold(14.sp),
)

/** Full-width primary actions, sized to match Mudita's own MMD apps. */
val CTA_HEIGHT = 52.dp

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
 * Givens are Black (W900) and entered digits Regular (W400). Both are exact
 * faces in MMD's Lato family, and the two-step gap keeps the distinction
 * readable without making the player's own digits look faint.
 */
@Immutable
data class BoardTextStyles(
    val given: TextStyle,
    val entered: TextStyle,
    val note: TextStyle,
)

@Composable
fun rememberBoardTextStyles(): BoardTextStyles {
    val headline = MaterialTheme.typography.headlineLarge
    val label = MaterialTheme.typography.labelSmall

    return BoardTextStyles(
        given = headline.copy(fontWeight = FontWeight.Black),
        entered = headline.copy(fontWeight = FontWeight.Normal),
        note = label.copy(fontWeight = FontWeight.Bold),
    )
}
