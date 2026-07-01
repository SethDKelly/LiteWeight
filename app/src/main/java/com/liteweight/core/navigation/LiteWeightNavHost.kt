package com.liteweight.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.liteweight.exercise.ui.create.CreateExerciseScreen
import com.liteweight.exercise.ui.detail.ExerciseDetailScreen
import com.liteweight.exercise.ui.list.ExerciseListScreen
import com.liteweight.home.HomeScreen
import com.liteweight.generator.ui.RoutineGeneratorScreen
import com.liteweight.program.ui.CreateProgramScreen
import com.liteweight.program.ui.PresetListScreen
import com.liteweight.program.ui.ProgramDetailScreen
import com.liteweight.program.ui.ProgramListScreen
import com.liteweight.progression.ui.ProgressionSchemeDetailScreen
import com.liteweight.progression.ui.ProgressionSchemeListScreen
import com.liteweight.rotation.ui.CreateRotationScreen
import com.liteweight.rotation.ui.RotationDetailScreen
import com.liteweight.rotation.ui.RotationListScreen
import com.liteweight.substitution.ui.SubstitutionGroupDetailScreen
import com.liteweight.substitution.ui.SubstitutionGroupListScreen
import com.liteweight.session.ui.ActiveWorkoutScreen
import com.liteweight.session.ui.SessionHistoryScreen
import com.liteweight.settings.SettingsScreen

@Composable
fun LiteWeightNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LiteWeightDestinations.HOME,
    ) {
        composable(LiteWeightDestinations.HOME) {
            HomeScreen(
                onOpenExercises = { navController.navigate(LiteWeightDestinations.EXERCISES) },
                onOpenPrograms = { navController.navigate(LiteWeightDestinations.PROGRAMS) },
                onStartWorkout = { sessionId ->
                    navController.navigate(LiteWeightDestinations.activeWorkout(sessionId))
                },
                onOpenHistory = { navController.navigate(LiteWeightDestinations.SESSION_HISTORY) },
                onOpenSettings = { navController.navigate(LiteWeightDestinations.SETTINGS) },
            )
        }
        composable(LiteWeightDestinations.EXERCISES) {
            ExerciseListScreen(
                onBack = { navController.popBackStack() },
                onCreateExercise = { navController.navigate(LiteWeightDestinations.CREATE_EXERCISE) },
                onOpenExercise = { id -> navController.navigate(LiteWeightDestinations.exerciseDetail(id)) },
            )
        }
        composable(
            route = LiteWeightDestinations.EXERCISE_DETAIL,
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType }),
        ) {
            ExerciseDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(LiteWeightDestinations.CREATE_EXERCISE) {
            CreateExerciseScreen(
                onBack = { navController.popBackStack() },
                onCreated = { id ->
                    navController.popBackStack()
                    navController.navigate(LiteWeightDestinations.exerciseDetail(id))
                },
            )
        }
        composable(
            route = LiteWeightDestinations.ACTIVE_WORKOUT,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
        ) {
            ActiveWorkoutScreen(
                onBack = { navController.popBackStack() },
                onFinished = {
                    navController.popBackStack(LiteWeightDestinations.HOME, false)
                },
            )
        }
        composable(LiteWeightDestinations.SESSION_HISTORY) {
            SessionHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(LiteWeightDestinations.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(LiteWeightDestinations.PROGRAMS) {
            ProgramListScreen(
                onBack = { navController.popBackStack() },
                onCreateProgram = { navController.navigate(LiteWeightDestinations.CREATE_PROGRAM) },
                onOpenPresets = { navController.navigate(LiteWeightDestinations.PRESETS) },
                onOpenProgressionSchemes = { navController.navigate(LiteWeightDestinations.PROGRESSION_SCHEMES) },
                onOpenRotations = { navController.navigate(LiteWeightDestinations.ROTATIONS) },
                onOpenGenerator = { navController.navigate(LiteWeightDestinations.ROUTINE_GENERATOR) },
                onOpenSubstitutionGroups = { navController.navigate(LiteWeightDestinations.SUBSTITUTION_GROUPS) },
                onOpenProgram = { id -> navController.navigate(LiteWeightDestinations.programDetail(id)) },
            )
        }
        composable(LiteWeightDestinations.CREATE_PROGRAM) {
            CreateProgramScreen(
                onBack = { navController.popBackStack() },
                onCreated = { id ->
                    navController.popBackStack()
                    navController.navigate(LiteWeightDestinations.programDetail(id))
                },
            )
        }
        composable(
            route = LiteWeightDestinations.PROGRAM_DETAIL,
            arguments = listOf(navArgument("programId") { type = NavType.LongType }),
        ) {
            ProgramDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenScheme = { schemeId ->
                    navController.navigate(LiteWeightDestinations.progressionSchemeDetail(schemeId))
                },
            )
        }
        composable(LiteWeightDestinations.PRESETS) {
            PresetListScreen(
                onBack = { navController.popBackStack() },
                onInstalled = { id ->
                    navController.popBackStack()
                    navController.navigate(LiteWeightDestinations.programDetail(id))
                },
            )
        }
        composable(LiteWeightDestinations.PROGRESSION_SCHEMES) {
            ProgressionSchemeListScreen(
                onBack = { navController.popBackStack() },
                onOpenScheme = { id ->
                    navController.navigate(LiteWeightDestinations.progressionSchemeDetail(id))
                },
            )
        }
        composable(
            route = LiteWeightDestinations.PROGRESSION_SCHEME_DETAIL,
            arguments = listOf(navArgument("schemeId") { type = NavType.LongType }),
        ) {
            ProgressionSchemeDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(LiteWeightDestinations.ROTATIONS) {
            RotationListScreen(
                onBack = { navController.popBackStack() },
                onCreateRotation = { navController.navigate(LiteWeightDestinations.CREATE_ROTATION) },
                onOpenRotation = { id -> navController.navigate(LiteWeightDestinations.rotationDetail(id)) },
            )
        }
        composable(LiteWeightDestinations.CREATE_ROTATION) {
            CreateRotationScreen(
                onBack = { navController.popBackStack() },
                onCreated = { id ->
                    navController.popBackStack()
                    navController.navigate(LiteWeightDestinations.rotationDetail(id))
                },
            )
        }
        composable(
            route = LiteWeightDestinations.ROTATION_DETAIL,
            arguments = listOf(navArgument("planId") { type = NavType.LongType }),
        ) {
            RotationDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(LiteWeightDestinations.ROUTINE_GENERATOR) {
            RoutineGeneratorScreen(
                onBack = { navController.popBackStack() },
                onSaved = { id ->
                    navController.popBackStack()
                    navController.navigate(LiteWeightDestinations.programDetail(id))
                },
            )
        }
        composable(LiteWeightDestinations.SUBSTITUTION_GROUPS) {
            SubstitutionGroupListScreen(
                onBack = { navController.popBackStack() },
                onOpenGroup = { id -> navController.navigate(LiteWeightDestinations.substitutionGroupDetail(id)) },
            )
        }
        composable(
            route = LiteWeightDestinations.SUBSTITUTION_GROUP_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType }),
        ) {
            SubstitutionGroupDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
