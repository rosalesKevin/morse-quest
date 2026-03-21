package morse.android.audiodecode

import kotlinx.coroutines.flow.Flow
import morse.core.AudioToneEvent

interface IAudioCapture {
    /**
     * Start capturing audio. Returns a cold Flow that emits AudioToneEvent values until
     * [stop] is called or the coroutine collecting the flow is cancelled.
     */
    fun start(): Flow<AudioToneEvent>

    /** Stop any active capture session. Safe to call when not active. */
    fun stop()

    /** Update detection sensitivity. No-op by default; override in implementations that support it. */
    fun setSensitivity(value: Float) {}
}
