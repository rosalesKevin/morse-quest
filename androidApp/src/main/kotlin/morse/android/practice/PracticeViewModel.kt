package morse.android.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import morse.android.audio.IAudioPlayer
import morse.android.haptics.IHapticsController
import morse.android.persistence.IProgressRepository
import morse.android.persistence.ISettingsRepository
import morse.core.MorseDecoder
import morse.core.MorseEncoder
import morse.core.TimingEngine
import morse.practice.Exercise
import morse.practice.ExerciseResult
import morse.practice.Lesson
import morse.practice.MistakeRecord
import morse.practice.PracticeSession
import morse.practice.SessionScore
import morse.practice.TimeProvider
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val progressRepository: IProgressRepository,
    private val settingsRepository: ISettingsRepository,
    private val timingEngine: TimingEngine,
    private val audioPlayer: IAudioPlayer,
    private val hapticsController: IHapticsController,
    private val lessons: @JvmSuppressWildcards List<Lesson>,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val launchConfig = PracticeLaunchConfig.fromSavedState(savedStateHandle)
    private val lesson: Lesson = resolveLesson()
    private val session = PracticeSession(lesson)
    private val sessionWpm: Int = (launchConfig as? PracticeLaunchConfig.QuickStart)?.wpmOverride
        ?: runBlocking { settingsRepository.settings.first().wpm }
    private var currentIndex = 0
    private var pendingAnswer = ""

    sealed class UiState {
        data class Exercise(
            val exercise: morse.practice.Exercise,
            val index: Int,
            val total: Int,
            val answer: String = "",
            val result: ExerciseResult? = null,
        ) : UiState()

        data class Summary(
            val lesson: Lesson,
            val score: SessionScore,
            val mistakes: List<MistakeRecord>,
        ) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(exerciseState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        autoPlayIfAudioExercise()
    }

    fun updateAnswer(answer: String) {
        pendingAnswer = answer
    }

    fun submitAnswer() {
        val current = _uiState.value as? UiState.Exercise ?: return
        val answer = pendingAnswer
        val result = session.submitAnswer(current.exercise, answer)
        _uiState.value = current.copy(answer = answer, result = result)
    }

    fun nextExercise() {
        pendingAnswer = ""
        currentIndex++
        if (currentIndex >= session.exercises.size) {
            val score = session.getScore()
            val mistakes = session.getMistakes()
            viewModelScope.launch {
                progressRepository.recordSession(lesson, score, mistakes, timeProvider.currentEpochMillis())
            }
            _uiState.value = UiState.Summary(lesson, score, mistakes)
        } else {
            _uiState.value = exerciseState()
            autoPlayIfAudioExercise()
        }
    }

    fun playCurrentExercise() {
        val current = _uiState.value as? UiState.Exercise ?: return
        val text = audioTextFor(current.exercise) ?: return
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            timingEngine.setSpeeds(sessionWpm, sessionWpm)
            val signals = timingEngine.textToSignals(text)
            audioPlayer.playSignals(signals, settings.toneFrequencyHz)
            if (settings.hapticsEnabled) hapticsController.vibrateSignals(signals)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }

    private fun exerciseState(): UiState.Exercise = UiState.Exercise(
        exercise = session.exercises[currentIndex],
        index = currentIndex,
        total = session.exercises.size,
    )

    private fun autoPlayIfAudioExercise() {
        val current = _uiState.value as? UiState.Exercise ?: return
        if (current.exercise is Exercise.ListenAndIdentify || current.exercise is Exercise.DecodeWord) {
            playCurrentExercise()
        }
    }

    private fun audioTextFor(exercise: Exercise): String? = when (exercise) {
        is Exercise.ListenAndIdentify -> MorseDecoder.decode(exercise.morse)
        is Exercise.DecodeWord -> MorseDecoder.decode(exercise.morse)
        is Exercise.SpeedChallenge -> exercise.text
        is Exercise.ReadAndTap, is Exercise.EncodeWord -> null
    }

    private fun resolveLesson(): Lesson = when (val config = launchConfig) {
        is PracticeLaunchConfig.Lesson -> lessons.first { it.id == config.lessonId }
        is PracticeLaunchConfig.QuickStart -> createQuickStartLesson(config)
    }

    private fun createQuickStartLesson(config: PracticeLaunchConfig.QuickStart): Lesson {
        val unlockedLessons = runBlocking {
            val sessions = progressRepository.sessionHistory.first()
            progressRepository.buildTracker(sessions, lessons, timeProvider).getUnlockedLessons()
        }.ifEmpty { listOf(lessons.first()) }
        val unlockedCharacters = unlockedLessons
            .flatMap { it.characters }
            .distinct()
        val sessionCharacters = when (config.difficulty) {
            QuickStartDifficulty.EASY -> unlockedCharacters.take(2).ifEmpty { unlockedCharacters }
            QuickStartDifficulty.MEDIUM -> unlockedCharacters
            QuickStartDifficulty.HARD -> unlockedCharacters
        }
        val quickStartWpm = config.wpmOverride ?: runBlocking { settingsRepository.settings.first().wpm }
        val exercises = buildQuickStartExercises(
            characters = sessionCharacters,
            difficulty = config.difficulty,
            targetWpm = quickStartWpm,
        )

        return Lesson(
            id = "quick-start-${config.difficulty.routeValue}",
            title = "Quick Start ${config.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}",
            characters = sessionCharacters.takeLast(2).ifEmpty { unlockedLessons.first().characters },
            exercises = exercises,
        )
    }

    private fun buildQuickStartExercises(
        characters: List<Char>,
        difficulty: QuickStartDifficulty,
        targetWpm: Int,
    ): List<Exercise> {
        val encoder = MorseEncoder(TimingEngine())
        val cycle = List(4) { index -> characters.getOrElse(index % characters.size) { characters.first() } }
        val pairWords = characters
            .windowed(size = 2, step = 1, partialWindows = true)
            .map { it.joinToString(separator = "") }
            .ifEmpty { listOf(characters.first().toString()) }
        val easyExercises = listOf(
            Exercise.ListenAndIdentify(encoder.encode(cycle[0].toString()), cycle[0]),
            Exercise.ReadAndTap(cycle[1], encoder.encode(cycle[1].toString())),
            Exercise.DecodeWord(encoder.encode(pairWords.first()), pairWords.first()),
            Exercise.EncodeWord(pairWords.first(), encoder.encode(pairWords.first())),
            Exercise.ListenAndIdentify(encoder.encode(cycle[2].toString()), cycle[2]),
            Exercise.ReadAndTap(cycle[3], encoder.encode(cycle[3].toString())),
            Exercise.DecodeWord(encoder.encode(pairWords.last()), pairWords.last()),
            Exercise.EncodeWord(pairWords.last(), encoder.encode(pairWords.last())),
            Exercise.ListenAndIdentify(encoder.encode(cycle[0].toString()), cycle[0]),
            Exercise.SpeedChallenge(pairWords.first(), targetWpm),
        )
        val mediumExercises = listOf(
            Exercise.ListenAndIdentify(encoder.encode(cycle[0].toString()), cycle[0]),
            Exercise.ReadAndTap(cycle[1], encoder.encode(cycle[1].toString())),
            Exercise.DecodeWord(encoder.encode(pairWords.first()), pairWords.first()),
            Exercise.EncodeWord(pairWords.getOrElse(1) { pairWords.first() }, encoder.encode(pairWords.getOrElse(1) { pairWords.first() })),
            Exercise.SpeedChallenge(pairWords.first(), targetWpm),
            Exercise.ListenAndIdentify(encoder.encode(cycle[2].toString()), cycle[2]),
            Exercise.ReadAndTap(cycle[3], encoder.encode(cycle[3].toString())),
            Exercise.DecodeWord(encoder.encode(pairWords.last()), pairWords.last()),
            Exercise.EncodeWord(pairWords.last(), encoder.encode(pairWords.last())),
            Exercise.SpeedChallenge(pairWords.last(), targetWpm),
        )
        val hardWord = (pairWords + pairWords.map { it.reversed() }).distinct()
        val hardExercises = listOf(
            Exercise.ListenAndIdentify(encoder.encode(cycle[0].toString()), cycle[0]),
            Exercise.DecodeWord(encoder.encode(hardWord[0]), hardWord[0]),
            Exercise.EncodeWord(hardWord.getOrElse(1) { hardWord[0] }, encoder.encode(hardWord.getOrElse(1) { hardWord[0] })),
            Exercise.SpeedChallenge(hardWord[0], targetWpm),
            Exercise.ReadAndTap(cycle[1], encoder.encode(cycle[1].toString())),
            Exercise.DecodeWord(encoder.encode(hardWord.getOrElse(2) { hardWord.last() }), hardWord.getOrElse(2) { hardWord.last() }),
            Exercise.EncodeWord(hardWord.last(), encoder.encode(hardWord.last())),
            Exercise.SpeedChallenge(hardWord.last(), targetWpm),
            Exercise.ReadAndTap(cycle[2], encoder.encode(cycle[2].toString())),
            Exercise.DecodeWord(encoder.encode(hardWord.first()), hardWord.first()),
        )

        return when (difficulty) {
            QuickStartDifficulty.EASY -> easyExercises
            QuickStartDifficulty.MEDIUM -> mediumExercises
            QuickStartDifficulty.HARD -> hardExercises
        }
    }
}
