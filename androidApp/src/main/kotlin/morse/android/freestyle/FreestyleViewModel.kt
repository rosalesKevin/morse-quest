package morse.android.freestyle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import morse.android.audio.IAudioPlayer
import morse.android.haptics.IHapticsController
import morse.android.persistence.ISettingsRepository
import morse.android.persistence.UserSettings
import morse.android.practice.GapType
import morse.android.practice.TouchpadState
import morse.core.Signal
import morse.core.SignalType
import morse.core.TimingEngine
import javax.inject.Inject

@HiltViewModel
class FreestyleViewModel @Inject constructor(
    settingsRepository: ISettingsRepository,
    private val timingEngine: TimingEngine,
    private val audioPlayer: IAudioPlayer,
    private val hapticsController: IHapticsController,
) : ViewModel() {

    val touchpadState = TouchpadState(timingEngine)

    private val _decodedText = MutableStateFlow("")
    val decodedText: StateFlow<String> = _decodedText.asStateFlow()

    private var pendingWordSpace = false
    private var wordGapJob: Job? = null

    // Collect settings reactively; default to UserSettings() so init is non-blocking on main thread.
    private val settingsFlow = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    init {
        // Reactively keep TimingEngine in sync with the user's WPM setting (AC14).
        // Using launch so we receive the persisted value from DataStore, not just the stateIn default.
        viewModelScope.launch {
            settingsFlow.collect { settings ->
                timingEngine.setSpeeds(settings.wpm, settings.wpm)
            }
        }
    }

    // Called by MorseTouchpad's onTap callback
    fun onTap(durationMs: Long) {
        wordGapJob?.cancel()
        val settings = settingsFlow.value
        val signalType = if (durationMs < touchpadState.dotDashThresholdMs) SignalType.DOT else SignalType.DASH
        val signal = Signal(signalType, durationMs)
        audioPlayer.playSignals(listOf(signal), settings.toneFrequencyHz)
        if (settings.hapticsEnabled) {
            hapticsController.vibrateSignals(listOf(signal))
        }
    }

    // Called by MorseTouchpad's onGapElapsed callback
    fun onGapElapsed(type: GapType) {
        if (type != GapType.LETTER) return
        val group = touchpadState.letterGroups.lastOrNull() ?: return
        if (pendingWordSpace) {
            _decodedText.value += " "
            pendingWordSpace = false
        }
        _decodedText.value += group.decoded
        touchpadState.onGapElapsed(GapType.LETTER)
        touchpadState.clear()
        // Start word-gap timer: if user goes quiet for the extra word-gap interval,
        // mark that the next letter should be preceded by a space.
        wordGapJob?.cancel()
        wordGapJob = viewModelScope.launch {
            // Total word-gap silence = 10×dash. Letter gap already consumed 5×dash, so
            // we only need to wait the remaining 5×dash before inserting a word space.
            delay(timingEngine.dashDurationMs * 5L)
            pendingWordSpace = true
        }
    }

    // Internal helper exposed for testing (simulates the word-gap timer firing)
    internal fun onWordGapElapsed() {
        pendingWordSpace = true
        wordGapJob?.cancel()
    }

    // Called by MorseTouchpad's onDelete callback
    fun onDeleteLast() {
        when {
            touchpadState.letterGroups.isNotEmpty() -> touchpadState.deleteLast()
            _decodedText.value.isNotEmpty() -> _decodedText.value = _decodedText.value.dropLast(1)
        }
    }

    fun onClearAll() {
        wordGapJob?.cancel()
        pendingWordSpace = false
        touchpadState.clear()
        _decodedText.value = ""
    }

    fun restoreText(text: String) {
        wordGapJob?.cancel()
        pendingWordSpace = false
        _decodedText.value = text
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        wordGapJob?.cancel()
    }
}
