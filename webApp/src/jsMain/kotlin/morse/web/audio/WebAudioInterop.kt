package morse.web.audio

import morse.core.Signal
import morse.core.SignalType

internal fun createBrowserAudioSink(): AudioSignalSink? {
    val constructor = js("window.AudioContext || window.webkitAudioContext")
    if (constructor == null) {
        return null
    }
    return BrowserAudioSignalSink(constructor)
}

private class BrowserAudioSignalSink(
    private val audioContextConstructor: dynamic,
) : AudioSignalSink {
    private val context: dynamic = runCatching { js("new audioContextConstructor()") }.getOrNull()
    private val activeNodes = mutableListOf<dynamic>()

    override fun play(signals: List<Signal>, toneFrequencyHz: Float) {
        val audioContext = context ?: return
        runCatching { audioContext.resume() }
        stop()

        var cursorSeconds = (audioContext.currentTime as? Double) ?: 0.0
        signals.forEach { signal ->
            when (signal.type) {
                SignalType.DOT, SignalType.DASH -> {
                    val oscillator = audioContext.createOscillator()
                    val gain = audioContext.createGain()

                    oscillator.type = "sine"
                    oscillator.frequency.value = toneFrequencyHz
                    gain.gain.setValueAtTime(0.15, cursorSeconds)

                    oscillator.connect(gain)
                    gain.connect(audioContext.destination)
                    oscillator.start(cursorSeconds)
                    oscillator.stop(cursorSeconds + (signal.durationMs.toDouble() / 1000.0))

                    activeNodes.add(oscillator)
                    activeNodes.add(gain)
                    cursorSeconds += signal.durationMs.toDouble() / 1000.0
                }

                SignalType.LETTER_GAP,
                SignalType.WORD_GAP,
                SignalType.SILENCE,
                -> {
                    cursorSeconds += signal.durationMs.toDouble() / 1000.0
                }
            }
        }
    }

    override fun stop() {
        activeNodes.forEach { node ->
            runCatching { node.stop(0) }
            runCatching { node.disconnect() }
        }
        activeNodes.clear()
    }
}
