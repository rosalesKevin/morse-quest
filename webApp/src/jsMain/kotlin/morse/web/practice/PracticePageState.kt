package morse.web.practice

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import morse.practice.Exercise
import morse.practice.ExerciseResult
import morse.practice.Lesson
import morse.practice.MistakeRecord
import morse.practice.PracticeSession
import morse.practice.SessionScore
import morse.practice.TimeProvider
import morse.web.audio.WebAudioPlayer
import morse.web.persistence.WebProgressRepository
import morse.web.persistence.WebSettingsRepository

data class PracticeSummary(
    val lesson: Lesson,
    val score: SessionScore,
    val mistakes: List<MistakeRecord>,
)

class PracticePageState(
    private val lessons: List<Lesson>,
    private val progressRepository: WebProgressRepository,
    private val settingsRepository: WebSettingsRepository,
    private val audioPlayer: WebAudioPlayer,
    private val timeProvider: TimeProvider,
    private val onProgressChanged: () -> Unit = {},
) {
    var selectedLessonId by mutableStateOf<String?>(null)
        private set

    var currentExercise by mutableStateOf<Exercise?>(null)
        private set

    var answer by mutableStateOf("")
        private set

    var result by mutableStateOf<ExerciseResult?>(null)
        private set

    var summary by mutableStateOf<PracticeSummary?>(null)
        private set

    var lastTapFeedback by mutableStateOf<HoldFeedback?>(null)
        private set

    private var session: PracticeSession? = null
    private var currentIndex: Int = 0
    private val tapKeyer = TapKeyer(morse.core.TimingEngine())

    fun setLesson(lessonId: String) {
        val lesson = lessons.firstOrNull { it.id == lessonId } ?: return
        selectedLessonId = lessonId
        session = PracticeSession(lesson)
        currentIndex = 0
        answer = ""
        result = null
        summary = null
        tapKeyer.reset()
        lastTapFeedback = null
        currentExercise = session?.exercises?.getOrNull(currentIndex)
    }

    fun updateAnswer(value: String) {
        answer = value
    }

    fun pressKey(timestampMs: Long) {
        tapKeyer.press(timestampMs)
        syncTapState()
    }

    fun releaseKey(timestampMs: Long) {
        tapKeyer.release(timestampMs)
        syncTapState()
    }

    fun idleKey(timestampMs: Long): String? {
        val decoded = tapKeyer.idle(timestampMs)
        syncTapState()
        return decoded
    }

    fun submitAnswer() {
        val current = currentExercise ?: return
        val activeSession = session ?: return
        result = activeSession.submitAnswer(current, answer)
    }

    fun nextExercise() {
        val activeSession = session ?: return
        val lesson = lessons.firstOrNull { it.id == selectedLessonId } ?: return
        if (result == null) {
            return
        }

        currentIndex += 1
        if (currentIndex >= activeSession.exercises.size) {
            val score = activeSession.getScore()
            val mistakes = activeSession.getMistakes()
            progressRepository.recordSession(
                lesson = lesson,
                score = score,
                mistakes = mistakes,
                recordedAtEpochMillis = timeProvider.currentEpochMillis(),
            )
            summary = PracticeSummary(lesson = lesson, score = score, mistakes = mistakes)
            currentExercise = null
            onProgressChanged()
            return
        }

        answer = ""
        result = null
        tapKeyer.reset()
        lastTapFeedback = null
        currentExercise = activeSession.exercises[currentIndex]
    }

    fun playPrompt() {
        val exercise = currentExercise ?: return
        when (exercise) {
            is Exercise.ListenAndIdentify -> audioPlayer.playMorse(exercise.morse, settingsRepository.settings)
            is Exercise.DecodeWord -> audioPlayer.playMorse(exercise.morse, settingsRepository.settings)
            is Exercise.SpeedChallenge -> audioPlayer.playText(exercise.text, settingsRepository.settings)
            is Exercise.ReadAndTap,
            is Exercise.EncodeWord,
            -> Unit
        }
    }

    private fun syncTapState() {
        answer = tapKeyer.currentAnswer
        lastTapFeedback = tapKeyer.lastFeedback
    }
}
