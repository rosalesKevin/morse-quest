package morse.android.haptics

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import dagger.hilt.android.qualifiers.ApplicationContext
import morse.core.Signal
import morse.core.SignalType
import javax.inject.Inject
import javax.inject.Singleton

interface IHapticsController {
    fun vibrateSignals(signals: List<Signal>)
    fun cancel()
}

@Singleton
class HapticsController @Inject constructor(
    @ApplicationContext private val context: Context,
) : IHapticsController {

    private val vibrator: Vibrator = context.getSystemService(Vibrator::class.java)

    override fun vibrateSignals(signals: List<Signal>) {
        val timings = signals.map { it.durationMs }.toLongArray()
        val amplitudes = signals.map { signal ->
            when (signal.type) {
                SignalType.DOT, SignalType.DASH -> 180
                else -> 0
            }
        }.toIntArray()
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator.vibrate(effect)
    }

    override fun cancel() {
        vibrator.cancel()
    }
}
