package morse.web

import morse.core.Signal
import morse.practice.Exercise
import morse.practice.Lesson
import morse.practice.LessonCatalog
import morse.practice.TimeProvider
import morse.web.audio.AudioSignalSink
import morse.web.audio.WebAudioPlayer
import morse.web.communication.CommunicationGateway
import morse.web.communication.CommunicationHistoryEntry
import morse.web.communication.UnavailableCommunicationGateway
import morse.web.persistence.BrowserStorage
import morse.web.persistence.StorageDriver
import morse.web.persistence.WebProgressRepository
import morse.web.persistence.WebSettingsRepository

internal class InMemoryStorageDriver : StorageDriver {
    private val values = mutableMapOf<String, String>()

    override fun get(key: String): String? = values[key]

    override fun set(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }
}

internal class RecordingAudioSignalSink : AudioSignalSink {
    var lastSignals: List<Signal> = emptyList()
        private set

    var lastToneFrequencyHz: Float? = null
        private set

    var stopCalls: Int = 0
        private set

    override fun play(signals: List<Signal>, toneFrequencyHz: Float) {
        lastSignals = signals
        lastToneFrequencyHz = toneFrequencyHz
    }

    override fun stop() {
        stopCalls += 1
    }
}

internal class FakeCommunicationGateway : CommunicationGateway {
    override val isAvailable: Boolean = false
    override val isConnected: Boolean = false
    override val statusMessage: String = "Communication unavailable on this branch"
    override val history: List<CommunicationHistoryEntry> = emptyList()
    override val localUserId: String = "fake-user"
}

internal data class TestDependencies(
    val lessons: List<Lesson>,
    val storageDriver: InMemoryStorageDriver,
    val timeProvider: TimeProvider,
    val sink: RecordingAudioSignalSink,
    val settingsRepository: WebSettingsRepository,
    val progressRepository: WebProgressRepository,
    val audioPlayer: WebAudioPlayer,
    val communicationGateway: CommunicationGateway,
)

internal fun createTestDependencies(
    lessons: List<Lesson> = LessonCatalog.defaultLessons(),
    timeProvider: TimeProvider = TimeProvider { 86_400_000L },
    communicationGateway: CommunicationGateway = UnavailableCommunicationGateway(),
): TestDependencies {
    val storageDriver = InMemoryStorageDriver()
    val settingsRepository = WebSettingsRepository(BrowserStorage(storageDriver))
    val progressRepository = WebProgressRepository(BrowserStorage(storageDriver), lessons, timeProvider)
    val sink = RecordingAudioSignalSink()
    val audioPlayer = WebAudioPlayer(sinkFactory = { sink })

    return TestDependencies(
        lessons = lessons,
        storageDriver = storageDriver,
        timeProvider = timeProvider,
        sink = sink,
        settingsRepository = settingsRepository,
        progressRepository = progressRepository,
        audioPlayer = audioPlayer,
        communicationGateway = communicationGateway,
    )
}

internal fun singleExerciseLesson(): Lesson = Lesson(
    id = "lesson-test",
    title = "Lesson Test",
    characters = listOf('E'),
    exercises = listOf(Exercise.ReadAndTap(character = 'E', expectedMorse = ".")),
)
