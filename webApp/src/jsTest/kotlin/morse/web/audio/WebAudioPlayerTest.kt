package morse.web.audio

import morse.core.SignalType
import morse.web.TestDependencies
import morse.web.createTestDependencies
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebAudioPlayerTest {

    @Test
    fun decodesCanonicalMorseBeforePlayingSignals() {
        val dependencies: TestDependencies = createTestDependencies()

        dependencies.audioPlayer.playMorse(".-", dependencies.settingsRepository.settings)

        assertTrue(dependencies.sink.lastSignals.isNotEmpty())
        assertEquals(SignalType.DOT, dependencies.sink.lastSignals.first().type)
        assertEquals(700f, dependencies.sink.lastToneFrequencyHz)
    }

    @Test
    fun stopDelegatesToSink() {
        val dependencies: TestDependencies = createTestDependencies()

        dependencies.audioPlayer.stop()

        assertEquals(1, dependencies.sink.stopCalls)
    }
}
