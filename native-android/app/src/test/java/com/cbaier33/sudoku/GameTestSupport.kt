package com.cbaier33.sudoku

import com.cbaier33.sudoku.game.CellPoint
import com.cbaier33.sudoku.game.Difficulty
import com.cbaier33.sudoku.game.GameUiState
import com.cbaier33.sudoku.game.GameViewModel
import com.cbaier33.sudoku.game.PuzzleGenerator
import com.cbaier33.sudoku.game.PuzzleGenerator.Companion.SIZE
import com.cbaier33.sudoku.game.SavedGame
import com.cbaier33.sudoku.game.SavedGameStore
import com.cbaier33.sudoku.stats.StatsSink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class RecordedGame(val seconds: Int, val difficulty: Difficulty, val win: Boolean)

class FakeStats : StatsSink {
    val recorded = mutableListOf<RecordedGame>()

    override suspend fun recordGame(seconds: Int, difficulty: Difficulty, win: Boolean) {
        recorded += RecordedGame(seconds, difficulty, win)
    }
}

class FakeSaves : SavedGameStore {
    val slots = mutableMapOf<Difficulty, SavedGame>()

    override suspend fun load(difficulty: Difficulty): SavedGame? = slots[difficulty]

    override suspend fun save(difficulty: Difficulty, game: SavedGame) {
        slots[difficulty] = game
    }

    override suspend fun clear(difficulty: Difficulty) {
        slots.remove(difficulty)
    }
}

/**
 * Scaffolding shared by the view model tests: a test dispatcher on Main, fakes
 * for both things the app persists, and a builder that hands back a view model
 * whose puzzle has already been generated.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class GameTestSupport {

    protected val dispatcher = StandardTestDispatcher()

    protected lateinit var stats: FakeStats
    protected lateinit var saves: FakeSaves

    private val built = mutableListOf<GameViewModel>()

    @Before
    fun installTestDispatcher() {
        Dispatchers.setMain(dispatcher)
        stats = FakeStats()
        saves = FakeSaves()
    }

    @After
    fun removeTestDispatcher() {
        Dispatchers.resetMain()
    }

    /**
     * The tick loop schedules another delay forever, and pauseTimer only gates
     * the increment, so runTest would never see the scheduler go idle. Every
     * view model built here gets its timer stopped when the body finishes.
     */
    protected fun gameTest(body: suspend TestScope.() -> Unit) = runTest(
        dispatcher,
        timeout = 30.seconds,
    ) {
        built.clear()
        try {
            body()
        } finally {
            built.forEach { it.stopTimer() }
        }
    }

    protected fun TestScope.newViewModel(
        difficulty: Difficulty = Difficulty.EASY,
        seed: Int = 42,
        resume: Boolean = false,
    ): GameViewModel {
        val vm = GameViewModel(
            difficulty = difficulty,
            stats = stats,
            saves = saves,
            resume = resume,
            generator = PuzzleGenerator(Random(seed)),
            externalScope = this,
            computeDispatcher = dispatcher,
        )
        built += vm
        runCurrent()
        return vm
    }

    protected fun GameUiState.firstEmpty(): CellPoint {
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                val p = CellPoint(x, y)
                if (!cellAt(p).given) return p
            }
        }
        error("puzzle has no empty cell")
    }

    protected fun GameUiState.firstGiven(): CellPoint {
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                val p = CellPoint(x, y)
                if (cellAt(p).given) return p
            }
        }
        error("puzzle has no given")
    }

    protected fun GameUiState.empties(): List<CellPoint> =
        (0 until SIZE).flatMap { x -> (0 until SIZE).map { y -> CellPoint(x, y) } }
            .filter { !cellAt(it).given }

    protected fun wrongDigitFor(solution: Int) = if (solution == 1) 2 else 1
}
