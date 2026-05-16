package com.example.noblenumbers.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.noblenumbers.app.navigation.AppScreen
import com.example.noblenumbers.ui.ProvideAppLanguage
import com.example.noblenumbers.ui.components.ConfirmNewGameDialog
import com.example.noblenumbers.ui.screens.GameOverScreen
import com.example.noblenumbers.ui.screens.GameScreen
import com.example.noblenumbers.ui.screens.MainMenuScreen
import com.example.noblenumbers.ui.screens.PauseScreen
import com.example.noblenumbers.ui.screens.SettingsScreen
import com.example.noblenumbers.ui.theme.NobleNumbersTheme

@Composable
fun NobleNumbersApp(
    viewModel: NobleNumbersViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()

    NobleNumbersTheme {
        ProvideAppLanguage(state.settings.languageTag) {
            BackHandler {
                if (!viewModel.onBack()) activity?.finish()
            }
            when (state.screen) {
                AppScreen.MainMenu -> MainMenuScreen(
                    bestScore = state.game.bestScore,
                    onPlay = viewModel::playFromMenu,
                    onSettings = viewModel::openSettings,
                )

                AppScreen.Game -> GameScreen(
                    game = state.game,
                    displayTiles = state.displayTiles,
                    onSwipe = viewModel::move,
                    onNewGame = viewModel::requestNewGame,
                    onSettings = viewModel::openSettings,
                    onPause = viewModel::pause,
                )

                AppScreen.Pause -> PauseScreen(
                    onResume = viewModel::resume,
                    onNewGame = viewModel::requestNewGame,
                    onSettings = viewModel::openSettings,
                    onMainMenu = viewModel::goToMainMenu,
                )

                AppScreen.GameOver -> GameOverScreen(
                    game = state.game,
                    rewardedAdAvailable = state.rewardedAdAvailable,
                    activity = activity,
                    onNewGame = viewModel::requestNewGame,
                    onContinue = viewModel::continueWithAd,
                )

                AppScreen.Settings -> SettingsScreen(
                    settings = state.settings,
                    onSoundChanged = viewModel::setSoundEnabled,
                    onVibrationChanged = viewModel::setVibrationEnabled,
                    onLanguageChanged = viewModel::setLanguage,
                    onClose = viewModel::closeSettings,
                )
            }

            if (state.showNewGameDialog) {
                ConfirmNewGameDialog(
                    onDismiss = viewModel::dismissNewGameDialog,
                    onConfirm = viewModel::confirmNewGame,
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
