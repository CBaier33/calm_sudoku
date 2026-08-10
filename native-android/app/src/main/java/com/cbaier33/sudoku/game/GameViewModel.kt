package com.cbaier33.sudoku.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.CELLS
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.SIZE
import com.cbaier33.sudoku.stats.AppScope
import com.cbaier33.sudoku.stats.StatsRepository
import com.cbaier33.sudoku.stats.StatsSink
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * All game state and rules, ported from the Flutter build's
 * `lib/ui/game/view_models/game_viewmodel.dart`.
 *
 * The board and the clock are exposed as two separate flows on purpose. In the
 * Flutter build every `notifyListeners()` - including the once-a-second tick -
 * rebuilt all 81 cells and the whole number pad. Here only the timer readout
 * collects [elapsed], so a tick never redraws the board. That matters a great
 * deal on E Ink.
 */
class GameViewModel(
    val difficulty: Difficulty,
    private val stats: StatsSink,
    private val generator: PuzzleGenerator = PuzzleGenerator(),
    /**
     * Statistics are written here rather than in [viewModelScope] so popping the
     * game screen the instant a game ends cannot cancel the write. The Flutter
     * build fired `recordGame` without awaiting it and had exactly that race.
     */
    private val externalScope: CoroutineScope = AppScope,
    /** Overridden in tests so generation is deterministic and instant. */
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState(mistakesAllowed = difficulty.mistakes))
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private val _elapsed = MutableStateFlow(0)
    val elapsed: StateFlow<Int> = _elapsed.asStateFlow()

    private var timerJob: Job? = null
    private var paused = false

    init {
        newGame()
    }

    // ---------------------------------------------------------------- board

    /**
     * Generation runs off the main thread. The Flutter build did this
     * synchronously in the view model constructor; on Android, digging ~55 holes
     * with a uniqueness solve each is an ANR risk on a hard puzzle.
     */
    private fun newGame() {
        _state.update { it.copy(loading = true) }

        viewModelScope.launch {
            val generated = withContext(computeDispatcher) {
                generator.generate(difficulty.givens)
            }

            val cells = List(CELLS) { index ->
                CellItem(
                    value = generated.puzzle[index],
                    solution = generated.solution[index],
                    given = generated.puzzle[index] != 0,
                )
            }

            _state.value = GameUiState(
                cells = cells,
                mistakesAllowed = difficulty.mistakes,
                loading = false,
            ).recount()
        }
    }

    /** Deals a new puzzle without changing the difficulty. */
    fun resetGame() {
        resetTimer()
        newGame()
    }

    // ---------------------------------------------------------- cell actions

    fun onTapCell(p: CellPoint) {
        val current = _state.value
        if (current.result != GameResult.PLAYING || current.loading) return

        startTimer()

        _state.update { it.copy(selected = p) }
    }

    /** Selects *and* clears. Deliberately does not start the clock. */
    fun onLongPressCell(p: CellPoint) {
        val current = _state.value
        if (current.result != GameResult.PLAYING || current.loading) return

        _state.update { it.copy(selected = p).clearCell(p) }
    }

    fun onTapDigit(digit: Int) {
        val current = _state.value
        if (current.result != GameResult.PLAYING || current.loading) return

        val p = current.selected ?: return
        val cell = current.cellAt(p)

        // Givens and solved cells are locked.
        if (cell.given || cell.correct) return

        startTimer()

        if (current.noteMode) {
            // Pencil marks never cost a mistake and never trigger a game check.
            val notes = if (digit in cell.notes) cell.notes - digit else cell.notes + digit
            _state.update { it.withCell(p, cell.copy(notes = notes)) }
            return
        }

        val placed = cell.copy(value = digit, notes = emptySet())
        var next = current.withCell(p, placed)

        if (placed.correct) {
            if (AUTO_CLEAR_NOTES) next = next.clearNotesFromPeers(p, digit)
        } else {
            // A wrong guess stays in the cell, drawn inverted, until it is
            // overwritten or erased.
            next = next.copy(mistakes = next.mistakes + 1)
        }

        _state.value = next.recount()

        checkGame()
    }

    fun onTapErase() {
        val current = _state.value
        if (current.result != GameResult.PLAYING || current.loading) return

        val p = current.selected ?: return

        _state.update { it.clearCell(p) }
    }

    fun toggleNoteMode() {
        _state.update { it.copy(noteMode = !it.noteMode) }
    }

    // ------------------------------------------------------------ game state

    private fun checkGame() {
        val current = _state.value

        // Loss is tested first, so filling the last cell wrongly on the final
        // mistake is a loss rather than a win. Preserved from the Flutter build.
        if (current.mistakes >= current.mistakesAllowed) {
            endGame(GameResult.LOST)
            return
        }

        if (current.remaining == 0) {
            endGame(GameResult.WON)
        }
    }

    private fun endGame(result: GameResult) {
        pauseTimer()

        val won = result == GameResult.WON
        val seconds = _elapsed.value

        externalScope.launch { stats.recordGame(seconds, difficulty, won) }

        _state.update { current ->
            val cells = if (won) current.cells else current.cells.map { cell ->
                cell.copy(value = cell.solution, notes = emptySet())
            }

            current.copy(cells = cells, selected = null, result = result).recount()
        }
    }

    // ----------------------------------------------------------------- timer

    /**
     * Lazily started by the first cell or digit tap. Pausing gates the
     * increment rather than stopping the coroutine, and the clock keeps running
     * while the app is backgrounded - both match the Flutter build.
     */
    fun startTimer() {
        if (timerJob != null) return

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                if (!paused) _elapsed.value += 1
            }
        }
    }

    fun pauseTimer() {
        paused = true
    }

    fun resumeTimer() {
        paused = false
    }

    /**
     * Ends the tick loop outright. Note that [pauseTimer] deliberately does not
     * do this - it only gates the increment, matching the Flutter build - so
     * anything that needs the coroutine actually gone must call this.
     */
    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun resetTimer() {
        stopTimer()
        _elapsed.value = 0
        paused = false
    }

    /** Peers highlight unconditionally in this build - see [HIGHLIGHT_PEERS]. */
    val highlightPeers: Boolean get() = HIGHLIGHT_PEERS

    companion object {
        /**
         * The Flutter build kept these on OptionsViewModel but removed the
         * toggles that drove them, leaving both permanently true.
         */
        const val HIGHLIGHT_PEERS = true
        const val AUTO_CLEAR_NOTES = true

        /** Reads the difficulty straight off the navigation argument. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                val handle = createSavedStateHandle()

                GameViewModel(
                    difficulty = Difficulty.fromName(handle["difficulty"]),
                    stats = StatsRepository(application),
                )
            }
        }
    }
}

// ------------------------------------------------------------ state helpers

private fun GameUiState.withCell(p: CellPoint, cell: CellItem): GameUiState =
    copy(cells = cells.toMutableList().also { it[p.index] = cell })

/** Clearing leaves givens and already-correct cells alone. */
private fun GameUiState.clearCell(p: CellPoint): GameUiState {
    val cell = cellAt(p)
    if (cell.given || cell.correct) return this

    return withCell(p, cell.copy(value = 0, notes = emptySet()))
}

private fun GameUiState.clearNotesFromPeers(p: CellPoint, digit: Int): GameUiState {
    val updated = cells.toMutableList()

    for (x in 0 until SIZE) {
        for (y in 0 until SIZE) {
            val peer = CellPoint(x, y)
            if (!isPeer(p, peer)) continue

            val cell = updated[peer.index]
            if (digit in cell.notes) {
                updated[peer.index] = cell.copy(notes = cell.notes - digit)
            }
        }
    }

    return copy(cells = updated)
}

/**
 * `remaining` counts down to zero as the grid gets filled in correctly - givens
 * already count as solved, so an easy board starts around 41.
 */
private fun GameUiState.recount(): GameUiState {
    var solved = 0
    val perDigit = IntArray(SIZE + 1)

    for (cell in cells) {
        if (cell.correct) {
            solved++
            perDigit[cell.value]++
        }
    }

    return copy(
        remaining = CELLS - solved,
        digitsRemaining = List(SIZE + 1) { digit -> if (digit == 0) 0 else SIZE - perDigit[digit] },
    )
}
