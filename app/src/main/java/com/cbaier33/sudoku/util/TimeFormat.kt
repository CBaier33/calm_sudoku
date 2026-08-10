package com.cbaier33.sudoku.util

/** MM:SS, pinned at 99:59 so the readout never outgrows its box. */
fun formatTime(seconds: Int): String {
    if (seconds >= 99 * 60 + 59) return "99:59"

    val minutes = seconds / 60
    val rest = seconds % 60

    return "%02d:%02d".format(minutes, rest)
}
