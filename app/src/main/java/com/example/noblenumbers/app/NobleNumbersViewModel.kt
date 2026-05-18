package com.example.noblenumbers.app

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noblenumbers.ads.FakeRewardedAdsManager
import com.example.noblenumbers.ads.RewardedAdsManager
import com.example.noblenumbers.app.navigation.AppScreen
import com.example.noblenumbers.audio.SoundEvent
import com.example.noblenumbers.audio.SoundManager
import com.example.noblenumbers.data.AppSettings
import com.example.noblenumbers.data.BestScoreRepository
import com.example.noblenumbers.data.GameProgressRepository
import com.example.noblenumbers.data.SettingsRepository
import com.example.noblenumbers.game.logic.GameEngine
import com.example.noblenumbers.game.logic.KotlinRandomProvider
import com.example.noblenumbers.game.logic.MoveOutcome
import com.example.noblenumbers.game.model.GameState
import com.example.noblenumbers.game.model.MoveAnimation
import com.example.noblenumbers.game.model.MoveDirection
import com.example.noblenumbers.game.model.ScorePopup
import com.example.noblenumbers.ui.model.BoardTileUi
import com.example.noblenumbers.ui.model.toBoardTileUi
import com.example.noblenumbers.updates.AppUpdateInstaller
import com.example.noblenumbers.updates.AppUpdateRepository
import com.example.noblenumbers.updates.AvailableUpdate
import com.example.noblenumbers.updates.InstallLaunchResult
import com.example.noblenumbers.updates.UpdateCheckResult
import com.example.noblenumbers.updates.UpdateError
import com.example.noblenumbers.vibration.VibrationEvent
import com.example.noblenumbers.vibration.VibrationManager
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NobleNumbersViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    private val bestScoreRepository = BestScoreRepository(application)
    private val gameProgressRepository = GameProgressRepository(application)
    private val engine = GameEngine(KotlinRandomProvider())
    private val soundManager = SoundManager(application)
    private val vibrationManager = VibrationManager(application)
    private val adsManager: RewardedAdsManager = FakeRewardedAdsManager()
    private val updateRepository = AppUpdateRepository(application)
    private val updateInstaller = AppUpdateInstaller(application)
    private var moveAnimationJob: Job? = null
    private var downloadedUpdateApk: File? = null

    private val _uiState = MutableStateFlow(NobleNumbersUiState())
    val uiState: StateFlow<NobleNumbersUiState> = _uiState.asStateFlow()

    init {
        adsManager.loadRewardedAd()
        viewModelScope.launch {
            settingsRepository.settings.collectLatest { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
        viewModelScope.launch {
            bestScoreRepository.bestScore.collectLatest { best ->
                _uiState.value = _uiState.value.copy(game = _uiState.value.game.copy(bestScore = best))
            }
        }
        viewModelScope.launch {
            gameProgressRepository.savedGame.collectLatest { savedGame ->
                val current = _uiState.value
                _uiState.value = if (savedGame != null && current.screen == AppScreen.MainMenu) {
                    current.copy(
                        game = savedGame.copy(bestScore = maxOf(savedGame.bestScore, current.game.bestScore)),
                        hasSavedGame = true,
                    )
                } else {
                    current.copy(hasSavedGame = savedGame != null)
                }
            }
        }
        viewModelScope.launch {
            gameProgressRepository.recentScores.collectLatest { recentScores ->
                _uiState.value = _uiState.value.copy(recentScores = recentScores)
            }
        }
        viewModelScope.launch {
            adsManager.isRewardedAdAvailable.collectLatest { available ->
                _uiState.value = _uiState.value.copy(rewardedAdAvailable = available)
            }
        }
    }

    fun playFromMenu() {
        playButtonClick()
        moveAnimationJob?.cancel()
        val best = _uiState.value.game.bestScore
        val newGame = engine.newGame(best)
        _uiState.value = _uiState.value.copy(
            game = newGame,
            screen = AppScreen.Game,
            displayTiles = null,
            isAnimatingMove = false,
            hasSavedGame = true,
        )
        viewModelScope.launch { gameProgressRepository.saveGame(newGame) }
    }

    fun continueFromMenu() {
        if (!_uiState.value.hasSavedGame) return
        playButtonClick()
        moveAnimationJob?.cancel()
        _uiState.value = _uiState.value.copy(
            screen = AppScreen.Game,
            displayTiles = null,
            isAnimatingMove = false,
        )
    }

    fun requestNewGame() {
        playButtonClick()
        _uiState.value = _uiState.value.copy(showNewGameDialog = true)
    }

    fun dismissNewGameDialog() {
        _uiState.value = _uiState.value.copy(showNewGameDialog = false)
    }

    fun confirmNewGame() {
        playButtonClick()
        moveAnimationJob?.cancel()
        val best = _uiState.value.game.bestScore
        val newGame = engine.newGame(best)
        _uiState.value = _uiState.value.copy(
            game = newGame,
            screen = AppScreen.Game,
            showNewGameDialog = false,
            displayTiles = null,
            isAnimatingMove = false,
            hasSavedGame = true,
        )
        viewModelScope.launch { gameProgressRepository.saveGame(newGame) }
    }

    fun move(direction: MoveDirection) {
        val current = _uiState.value
        if (current.screen != AppScreen.Game || current.isAnimatingMove) return

        val event = engine.move(current.game, direction)
        when (val outcome = event.outcome) {
            MoveOutcome.Blocked -> {
                _uiState.value = current.copy(
                    game = event.state,
                    screen = if (event.state.gameOver) AppScreen.GameOver else current.screen,
                )
                persistGameProgress(event.state, current.game.gameOver)
            }
            is MoveOutcome.Success -> {
                startMoveAnimation(
                    previous = current,
                    finalState = event.state,
                    outcome = outcome,
                )
                if (outcome.merged) {
                    soundManager.play(SoundEvent.Merge, current.settings.soundEnabled)
                    vibrationManager.vibrate(VibrationEvent.Merge, current.settings.vibrationEnabled)
                    if (outcome.comboLevel >= 2) {
                        vibrationManager.vibrate(VibrationEvent.Combo, current.settings.vibrationEnabled)
                    }
                } else {
                    soundManager.play(SoundEvent.Swipe, current.settings.soundEnabled)
                    vibrationManager.vibrate(VibrationEvent.Swipe, current.settings.vibrationEnabled)
                }
                if (outcome.targetReachedNow) {
                    soundManager.play(SoundEvent.TargetReached, current.settings.soundEnabled)
                    soundManager.play(SoundEvent.FrozenMode, current.settings.soundEnabled)
                    vibrationManager.vibrate(VibrationEvent.TargetReached, current.settings.vibrationEnabled)
                    vibrationManager.vibrate(VibrationEvent.FrozenModeStarted, current.settings.vibrationEnabled)
                }
                if (outcome.frozenTileSpawned) {
                    soundManager.play(SoundEvent.FrozenTile, current.settings.soundEnabled)
                    vibrationManager.vibrate(VibrationEvent.FrozenTileAppeared, current.settings.vibrationEnabled)
                }
                if (outcome.gameOverNow) {
                    soundManager.play(SoundEvent.GameOver, current.settings.soundEnabled)
                    vibrationManager.vibrate(VibrationEvent.GameOver, current.settings.vibrationEnabled)
                }
                persistGameProgress(event.state, current.game.gameOver)
            }
        }

        if (event.state.bestScore > current.game.bestScore) {
            viewModelScope.launch { bestScoreRepository.saveBestScore(event.state.bestScore) }
        }
    }

    fun undo() {
        val current = _uiState.value
        if (current.game.undoSnapshot == null || current.isAnimatingMove) return
        playButtonClick()
        val undone = engine.undo(current.game)
        _uiState.value = current.copy(
            game = undone,
            displayTiles = null,
        )
        viewModelScope.launch { gameProgressRepository.saveGame(undone) }
    }

    fun pause() {
        playButtonClick()
        _uiState.value = _uiState.value.copy(screen = AppScreen.Pause)
    }

    fun resume() {
        playButtonClick()
        _uiState.value = _uiState.value.copy(screen = AppScreen.Game)
    }

    fun openSettings() {
        playButtonClick()
        _uiState.value = _uiState.value.copy(
            previousScreen = _uiState.value.screen,
            screen = AppScreen.Settings,
        )
    }

    fun openRecords() {
        playButtonClick()
        _uiState.value = _uiState.value.copy(
            previousScreen = _uiState.value.screen,
            screen = AppScreen.Records,
        )
    }

    fun closeSettings() {
        playButtonClick()
        val previous = _uiState.value.previousScreen ?: AppScreen.MainMenu
        _uiState.value = _uiState.value.copy(screen = previous, previousScreen = null)
    }

    fun goToMainMenu() {
        playButtonClick()
        _uiState.value = _uiState.value.copy(screen = AppScreen.MainMenu, previousScreen = null)
    }

    fun onBack(): Boolean {
        return when (_uiState.value.screen) {
            AppScreen.MainMenu -> false
            AppScreen.Game -> {
                pause()
                true
            }
            AppScreen.Pause -> {
                resume()
                true
            }
            AppScreen.Settings -> {
                closeSettings()
                true
            }
            AppScreen.Records -> {
                goToMainMenu()
                true
            }
            AppScreen.GameOver -> {
                goToMainMenu()
                true
            }
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setVibrationEnabled(enabled) }
    }

    fun setLanguage(languageTag: String) {
        viewModelScope.launch { settingsRepository.setLanguage(languageTag) }
    }

    fun checkForUpdates() {
        playButtonClick()
        _uiState.value = _uiState.value.copy(update = AppUpdateUiState(status = UpdateStatus.Checking))
        viewModelScope.launch {
            val result = runCatching { updateRepository.checkLatestRelease() }
                .getOrElse { UpdateCheckResult.Error(UpdateError.Network) }
            _uiState.value = when (result) {
                UpdateCheckResult.NoUpdate -> _uiState.value.copy(
                    update = AppUpdateUiState(status = UpdateStatus.NoUpdate),
                )
                is UpdateCheckResult.UpdateAvailable -> _uiState.value.copy(
                    update = AppUpdateUiState(
                        status = UpdateStatus.Available,
                        availableUpdate = result.update,
                    ),
                )
                is UpdateCheckResult.Error -> _uiState.value.copy(
                    update = AppUpdateUiState(
                        status = UpdateStatus.Error,
                        error = result.error,
                    ),
                )
            }
        }
    }

    fun downloadAndInstallUpdate() {
        playButtonClick()
        val update = _uiState.value.update.availableUpdate ?: return
        _uiState.value = _uiState.value.copy(
            update = _uiState.value.update.copy(status = UpdateStatus.Downloading, error = null),
        )
        viewModelScope.launch {
            val apkFile = runCatching { updateRepository.download(update) }
                .getOrElse {
                    _uiState.value = _uiState.value.copy(
                        update = _uiState.value.update.copy(
                            status = UpdateStatus.Error,
                            error = UpdateError.DownloadFailed,
                        ),
                    )
                    return@launch
                }
            downloadedUpdateApk = apkFile
            launchDownloadedUpdate(apkFile)
        }
    }

    fun installDownloadedUpdate() {
        playButtonClick()
        val apkFile = downloadedUpdateApk ?: return
        launchDownloadedUpdate(apkFile)
    }

    fun continueWithAd(activity: Activity) {
        val current = _uiState.value
        if (current.game.continueUsed || !current.rewardedAdAvailable) return
        playButtonClick()
        adsManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                val continuedGame = engine.grantContinue(_uiState.value.game)
                _uiState.value = _uiState.value.copy(
                    game = continuedGame,
                    hasSavedGame = true,
                    screen = AppScreen.Game,
                )
                viewModelScope.launch { gameProgressRepository.saveGame(continuedGame) }
            },
            onUnavailable = {
                _uiState.value = _uiState.value.copy(rewardedAdAvailable = false)
            },
        )
    }

    private fun playButtonClick() {
        val current = _uiState.value
        soundManager.play(SoundEvent.ButtonClick, current.settings.soundEnabled)
        vibrationManager.vibrate(VibrationEvent.ButtonClick, current.settings.vibrationEnabled)
    }

    private fun persistGameProgress(game: GameState, wasGameOver: Boolean) {
        viewModelScope.launch {
            if (game.gameOver) {
                gameProgressRepository.clearSavedGame()
                if (!wasGameOver) {
                    gameProgressRepository.addRecentScore(game.score)
                }
            } else {
                gameProgressRepository.saveGame(game)
            }
        }
    }

    private fun launchDownloadedUpdate(apkFile: File) {
        val result = updateInstaller.launchInstall(apkFile)
        val status = when (result) {
            InstallLaunchResult.Started -> UpdateStatus.InstallerStarted
            InstallLaunchResult.PermissionRequired -> UpdateStatus.PermissionRequired
            InstallLaunchResult.Failed -> UpdateStatus.Error
        }
        _uiState.value = _uiState.value.copy(
            update = _uiState.value.update.copy(
                status = status,
                error = if (result == InstallLaunchResult.Failed) {
                    UpdateError.InstallerLaunchFailed
                } else {
                    null
                },
            ),
        )
    }

    private fun startMoveAnimation(
        previous: NobleNumbersUiState,
        finalState: GameState,
        outcome: MoveOutcome.Success,
    ) {
        moveAnimationJob?.cancel()
        val animation = outcome.animation
        _uiState.value = previous.copy(
            game = finalState,
            screen = AppScreen.Game,
            displayTiles = animation.startTiles(),
            isAnimatingMove = true,
        )
        moveAnimationJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(scorePopups = animation.scorePopups)
            delay(ANIMATION_BOOTSTRAP_DELAY_MILLIS)
            _uiState.value = _uiState.value.copy(displayTiles = animation.slideTargetTiles())
            delay(SLIDE_PHASE_MILLIS)

            val mergeIds = animation.merges.map { it.resultTileId }.toSet()
            val spawnedTileId = outcome.spawnedTileId
            val beforeSpawn = finalState.board.tiles
                .filterNot { it.id == spawnedTileId }
                .map { it.toBoardTileUi(isMerging = it.id in mergeIds) }
            _uiState.value = _uiState.value.copy(displayTiles = beforeSpawn)
            delay(MERGE_PHASE_MILLIS)

            val withSpawn = finalState.board.tiles.map {
                it.toBoardTileUi(isSpawning = it.id == spawnedTileId)
            }
            _uiState.value = _uiState.value.copy(displayTiles = withSpawn)
            delay(SPAWN_PHASE_MILLIS)

            _uiState.value = _uiState.value.copy(
                displayTiles = null,
                scorePopups = emptyList(),
                isAnimatingMove = false,
                screen = if (finalState.gameOver) AppScreen.GameOver else AppScreen.Game,
            )
        }
    }

    private fun MoveAnimation.startTiles(): List<BoardTileUi> = motions.map {
        BoardTileUi(
            id = it.tileId,
            value = it.value,
            row = it.fromRow,
            column = it.fromColumn,
            isFrozen = it.isFrozen,
            frozenMovesRemaining = it.frozenMovesRemaining,
            isMoving = it.fromRow != it.toRow || it.fromColumn != it.toColumn,
        )
    }

    private fun MoveAnimation.slideTargetTiles(): List<BoardTileUi> = motions.map {
        BoardTileUi(
            id = it.tileId,
            value = it.value,
            row = it.toRow,
            column = it.toColumn,
            isFrozen = it.isFrozen,
            frozenMovesRemaining = it.frozenMovesRemaining,
            isMoving = it.fromRow != it.toRow || it.fromColumn != it.toColumn,
        )
    }

    override fun onCleared() {
        moveAnimationJob?.cancel()
        soundManager.release()
        super.onCleared()
    }

    private companion object {
        const val ANIMATION_BOOTSTRAP_DELAY_MILLIS = 12L
        const val SLIDE_PHASE_MILLIS = 100L
        const val MERGE_PHASE_MILLIS = 80L
        const val SPAWN_PHASE_MILLIS = 80L
    }
}

data class NobleNumbersUiState(
    val screen: AppScreen = AppScreen.MainMenu,
    val previousScreen: AppScreen? = null,
    val game: GameState = GameState(),
    val settings: AppSettings = AppSettings(),
    val rewardedAdAvailable: Boolean = false,
    val hasSavedGame: Boolean = false,
    val recentScores: List<Int> = emptyList(),
    val showNewGameDialog: Boolean = false,
    val displayTiles: List<BoardTileUi>? = null,
    val isAnimatingMove: Boolean = false,
    val scorePopups: List<ScorePopup> = emptyList(),
    val update: AppUpdateUiState = AppUpdateUiState(),
)

data class AppUpdateUiState(
    val status: UpdateStatus = UpdateStatus.Idle,
    val availableUpdate: AvailableUpdate? = null,
    val error: UpdateError? = null,
)

enum class UpdateStatus {
    Idle,
    Checking,
    NoUpdate,
    Available,
    Downloading,
    PermissionRequired,
    InstallerStarted,
    Error,
}
