package morse.android.audiodecode

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import morse.core.AudioToneEvent
import kotlin.math.sqrt

/**
 * Captures PCM audio from the device microphone and emits [AudioToneEvent] values.
 *
 * **Requires** `android.permission.RECORD_AUDIO` permission granted at runtime. The caller
 * must verify permission before calling [start]; unchecked calls on Android 6+ will throw
 * [SecurityException].
 *
 * Detection: amplitude thresholding — RMS of each audio buffer is compared against
 * [amplitudeThreshold] (normalised 0..1 over the 16-bit range). Transitions between
 * tone and silence states produce an [AudioToneEvent] carrying the elapsed duration of
 * the previous state.
 *
 * Minimum event duration [minEventDurationMs] suppresses noise spikes shorter than
 * a useful Morse dot. Events shorter than this threshold are swallowed without emission.
 */
class MicrophoneCapture(
    private val sampleRateHz: Int = 16_000,
    @field:Volatile
    var amplitudeThreshold: Float = 0.05f,
    private val minEventDurationMs: Long = 20L,
) : IAudioCapture {

    @Volatile private var audioRecord: AudioRecord? = null

    @SuppressLint("MissingPermission") // Permission checked by caller (AudioDecodeViewModel) before start() is invoked
    override fun start(): Flow<AudioToneEvent> = flow {
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRateHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(3200) // at least ~100ms of audio

        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRateHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
        )
        audioRecord = record

        try {
            record.startRecording()
            val buffer = ShortArray(bufferSize / 2)
            var currentIsTone = false
            var stateStartMs = SystemClock.elapsedRealtime()

            while (currentCoroutineContext().isActive) {
                val read = record.read(buffer, 0, buffer.size)
                if (read <= 0) continue

                val rms = calculateRms(buffer, read)
                val nowTone = rms > amplitudeThreshold

                if (nowTone != currentIsTone) {
                    val nowMs = SystemClock.elapsedRealtime()
                    val durationMs = nowMs - stateStartMs
                    if (durationMs >= minEventDurationMs) {
                        emit(AudioToneEvent(isTone = currentIsTone, durationMs = durationMs))
                    }
                    currentIsTone = nowTone
                    stateStartMs = nowMs
                }
            }

            // Emit trailing event on cancellation so decoder can flush the last state
            val durationMs = SystemClock.elapsedRealtime() - stateStartMs
            if (durationMs >= minEventDurationMs) {
                emit(AudioToneEvent(isTone = currentIsTone, durationMs = durationMs))
            }
        } finally {
            // stop() may have already been called externally to unblock record.read().
            // Wrap in try/catch: calling stop() twice throws IllegalStateException.
            try { record.stop() } catch (_: IllegalStateException) { }
            record.release()
            audioRecord = null
        }
    }.flowOn(Dispatchers.IO)

    override fun stop() {
        try {
            audioRecord?.stop()
        } catch (_: IllegalStateException) {
            // AudioRecord not in recording state — already stopped or not started
        }
    }

    override fun setSensitivity(value: Float) {
        amplitudeThreshold = value
    }

    private fun calculateRms(buffer: ShortArray, count: Int): Float {
        var sumSquares = 0.0
        for (i in 0 until count) sumSquares += (buffer[i].toDouble() / Short.MAX_VALUE).let { it * it }
        return sqrt(sumSquares / count).toFloat()
    }
}
