package morse.android.audio

import morse.core.Signal

class FakeAudioPlayer : IAudioPlayer {
    val playedSignals = mutableListOf<List<Signal>>()
    override fun playSignals(signals: List<Signal>, frequencyHz: Float) {
        playedSignals += signals
    }
    override fun stop() {}
}
