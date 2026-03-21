package morse.android.audiodecode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import morse.core.AudioMorseDecoder
import morse.core.TimingEngine
import javax.inject.Inject

@HiltViewModel
class AudioDecodeViewModel @Inject constructor(
    private val timingEngine: TimingEngine,
    private val audioCapture: IAudioCapture,
) : ViewModel() {

    data class UiState(
        val isListening: Boolean = false,
        val morseText: String = "",
        val decodedText: String = "",
        val permissionDenied: Boolean = false,
        val error: String? = null,
        val sensitivity: Float = 0.05f,
        /** Incremented on every [reset] call so StateFlow always emits after reset. */
        internal val resetGeneration: Int = 0,
    )

    private val decoder = AudioMorseDecoder(timingEngine)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var captureJob: Job? = null

    /**
     * Call when RECORD_AUDIO permission is granted. Clears the permission-denied flag.
     * The UI layer must call [startListening] separately if it wants to start capture.
     */
    fun onPermissionGranted() {
        _uiState.update { it.copy(permissionDenied = false) }
    }

    fun onPermissionDenied() {
        _uiState.update { it.copy(permissionDenied = true, isListening = false) }
    }

    fun startListening() {
        if (captureJob?.isActive == true) return
        if (_uiState.value.permissionDenied) return
        decoder.reset()
        _uiState.update { it.copy(isListening = true, error = null) }
        audioCapture.setSensitivity(_uiState.value.sensitivity)

        captureJob = viewModelScope.launch {
            try {
                audioCapture.start().collect { event ->
                    val state = decoder.consume(event)
                    _uiState.update { it.copy(morseText = state.morseText, decodedText = state.decodedText) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isListening = false, error = e.message ?: "Audio capture failed") }
            } finally {
                val finalState = decoder.flush()
                _uiState.update { current ->
                    // Only update if we're still marked as listening (error path already cleared it)
                    if (current.isListening) {
                        current.copy(
                            isListening = false,
                            morseText = finalState.morseText,
                            decodedText = finalState.decodedText,
                        )
                    } else current
                }
            }
        }
    }

    fun stopListening() {
        captureJob?.cancel()
        audioCapture.stop()
        // decoder.flush() is called in the coroutine's finally block when the job completes
    }

    fun reset() {
        captureJob?.cancel()
        audioCapture.stop()
        decoder.reset()
        _uiState.update {
            UiState(sensitivity = it.sensitivity, resetGeneration = it.resetGeneration + 1)
        }
    }

    fun updateSensitivity(value: Float) {
        _uiState.update { it.copy(sensitivity = value) }
        audioCapture.setSensitivity(value)
    }

    override fun onCleared() {
        super.onCleared()
        audioCapture.stop()
    }
}
