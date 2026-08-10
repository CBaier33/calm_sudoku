package com.cbaier33.sudoku.stats

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Outlives any single screen, so a statistics write started as a game ends
 * cannot be cancelled by the player immediately backing out of the game.
 */
val AppScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
