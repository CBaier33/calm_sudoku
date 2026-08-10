package com.cbaier33.sudoku

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cbaier33.sudoku.game.Difficulty
import com.cbaier33.sudoku.game.ui.GameScreen
import com.cbaier33.sudoku.home.HomeScreen
import com.cbaier33.sudoku.options.OptionsScreen
import com.cbaier33.sudoku.stats.StatsScreen

object Routes {
    const val HOME = "home"
    const val OPTIONS = "options"
    const val STATS = "stats"
    const val GAME = "game/{difficulty}?resume={resume}"

    fun game(difficulty: Difficulty, resume: Boolean = false) =
        "game/${difficulty.name}?resume=$resume"

    const val ARG_DIFFICULTY = "difficulty"
    const val ARG_RESUME = "resume"
}

/**
 * Every transition is instant. The Flutter build did this with a SimplePageRoute
 * whose buildTransitions returned the child unchanged - motion ghosts on E Ink.
 */
@Composable
fun SudokuApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onPlay = { navController.navigate(Routes.OPTIONS) },
                onStatistics = { navController.navigate(Routes.STATS) },
            )
        }

        composable(Routes.OPTIONS) {
            OptionsScreen(
                onBack = { navController.popBackStack() },
                onPlay = { difficulty -> navController.navigate(Routes.game(difficulty)) },
                onResume = { difficulty ->
                    navController.navigate(Routes.game(difficulty, resume = true))
                },
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(
                navArgument(Routes.ARG_DIFFICULTY) { type = NavType.StringType },
                navArgument(Routes.ARG_RESUME) {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) {
            GameScreen(
                onBack = { navController.popBackStack() },
                // Quit and Save & Quit both drop straight back to the title,
                // matching Navigator.popUntil((route) => route.isFirst).
                onExit = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }

        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
    }
}
