package morse.web.audio

import morse.core.MorseDecoder
import morse.core.Signal
import morse.core.TimingEngine
import morse.web.persistence.WebSettings

interface AudioSignalSink {
    fun play(signals: List<Signal>, toneFrequencyHz: Float)
    fun stop()
}

class WebAudioPlayer(
    private val timingEngine: TimingEngine = TimingEngine(),
    private val sinkFactory: () -> AudioSignalSink? = { createBrowserAudioSink() },
) {
    private val sink: AudioSignalSink? by lazy(sinkFactory)

    fun playText(text: String, settings: WebSettings) {
        timingEngine.setSpeeds(settings.wpm, settings.wpm)
        sink?.play(timingEngine.textToSignals(text), settings.toneFrequencyHz)
    }

    fun playMorse(morse: String, settings: WebSettings) {
        val decoded = MorseDecoder.decode(morse)
        if (decoded.isBlank()) {
            return
        }
        playText(decoded, settings)
    }

    fun stop() {
        sink?.stop()
    }
}
