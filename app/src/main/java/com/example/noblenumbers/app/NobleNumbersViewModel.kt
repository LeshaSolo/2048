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
import com.example.noblenumbers.data.SettingsRepository
import com.example.noblenumbers.game.logic.GameEngine
import com.example.noblenumbers.game.logic.KotlinRandomProvider
import com.example.noblenumbers.game.logic.MoveOutcome
import com.example.noblenumbers.game.model.GameState
import com.example.noblenumbers.game.model.MoveAnimation
import com.example.noblenumbers.game.model.MoveDirection
import com.example.noblenumbers.ui.model.BoardTileUi
import com.example.noblenumbers.ui.model.toBoardTileUi
import com.example.noblenumbers.vibration.VibrationEvent
import com.example.noblenumbers.vibration.VibrationManager
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
    private val engine = GameEngine(KotlinRandomProvider())
    private val soundManager = SoundManager(application)
    private val vibrationManager = VibrationManager(application)
    private val adsManager: RewardedAdsManager = FakeRewardedAdsManager()
    private var moveAnimationJob: Job? = null

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
            adsManager.isRewardedAdAvailable.collectLatest { available ->
                _uiState.value = _uiState.value.copy(rewardedAdAvailable = available)
            }
        }
    }

    fun playFromMenu() {
        playButtonClick()
        moveAnimationJob?.cancel()
        val best = _uiState.value.game.bestScore
        _uiState.value = _uiState.value.copy(
            game = engine.newGame(best),
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
        _uiState.value = _uiState.value.copy(
            game = engine.newGame(best),
            screen = AppScreen.Game,
            showNewGameDialog = false,
            displayTiles = null,
            isAnimatingMove = false,
        )
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
            }
        }

        if (event.state.bestScore > current.game.bestScore) {
            viewModelScope.launch { bestScoreRepository.saveBestScore(event.state.bestScore) }
        }
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

    fun continueWithAd(activity: Activity) {
        val current = _uiState.value
        if (current.game.continueUsed || !current.rewardedAdAvailable) return
        playButtonClick()
        adsManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                _uiState.value = _uiState.value.copy(
                    game = engine.grantContinue(_uiState.value.game),
                    screen = AppScreen.Game,
                )
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
        const val ANIMATION_BOOTSTRAP_DELAY_MILLIS = 16L
        const val SLIDE_PHASE_MILLIS = 125L
        const val MERGE_PHASE_MILLIS = 90L
        const val SPAWN_PHASE_MILLIS = 95L
    }
}

data class NobleNumbersUiState(
    val screen: AppScreen = AppScreen.MainMenu,
    val previousScreen: AppScreen? = null,
    val game: GameState = GameState(),
    val settings: AppSettings = AppSettings(),
    val rewardedAdAvailable: Boolean = false,
    val showNewGameDialog: Boolean = false,
    val displayTiles: List<BoardTileUi>? = null,
    val isAnimatingMove: Boolean = false,
)
