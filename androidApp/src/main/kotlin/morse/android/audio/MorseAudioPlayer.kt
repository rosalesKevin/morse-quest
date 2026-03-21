package morse.android.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import morse.core.Signal
import morse.core.SignalType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

interface IAudioPlayer {
    fun playSignals(signals: List<Signal>, frequencyHz: Float = 700f)
    fun stop()
}

@Singleton
class MorseAudioPlayer @Inject constructor() : IAudioPlayer {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var playbackJob: Job? = null

    override fun playSignals(signals: List<Signal>, frequencyHz: Float) {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            playSignalsBlocking(signals, frequencyHz)
        }
    }

    override fun stop() {
        playbackJob?.cancel()
    }

    // The coroutine scope is intentionally not cancelled here. MorseAudioPlayer is a
    // @Singleton whose scope lives for the application process lifetime. stop() cancels
    // the active playback job, which is sufficient for screen transitions. The OS
    // reclaims all resources when the process exits.

    private suspend fun playSignalsBlocking(signals: List<Signal>, frequencyHz: Float) {
        val sampleRate = 44100
        val allSamples = buildSamples(signals, frequencyHz, sampleRate)
        if (allSamples.isEmpty()) return

        val bufferBytes = allSamples.size * 4
        // Cap to 1 MB to stay within MODE_STATIC limits on all devices.
        if (bufferBytes > 1_048_576) return
        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(bufferBytes, minBuffer))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(allSamples, 0, allSamples.size, AudioTrack.WRITE_BLOCKING)
        if (!currentCoroutineContext().isActive) {
            track.release()
            return
        }
        track.play()

        val durationMs = signals.sumOf { it.durationMs }
        delay(durationMs)

        track.stop()
        track.release()
    }

    private fun buildSamples(signals: List<Signal>, frequencyHz: Float, sampleRate: Int): FloatArray {
        val result = mutableListOf<Float>()
        signals.forEach { signal ->
            val count = (sampleRate * signal.durationMs / 1000.0).toInt()
            when (signal.type) {
                SignalType.DOT, SignalType.DASH ->
                    repeat(count) { i ->
                        result += (0.7f * sin(2.0 * PI * frequencyHz * i / sampleRate)).toFloat()
                    }
                else -> repeat(count) { result += 0f }
            }
        }
        return result.toFloatArray()
    }
}
