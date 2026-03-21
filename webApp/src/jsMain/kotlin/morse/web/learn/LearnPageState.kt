package morse.web.learn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import morse.core.MorseAlphabet
import morse.practice.Lesson
import morse.web.audio.WebAudioPlayer
import morse.web.persistence.WebProgressRepository
import morse.web.persistence.WebSettingsRepository

data class LearnLessonItem(
    val lesson: Lesson,
    val isUnlocked: Boolean,
)

data class ReferenceEntry(
    val character: Char,
    val morse: String,
)

class LearnPageState(
    private val lessons: List<Lesson>,
    private val progressRepository: WebProgressRepository,
    private val settingsRepository: WebSettingsRepository,
    private val audioPlayer: WebAudioPlayer,
) {
    var lessonItems by mutableStateOf(emptyList<LearnLessonItem>())
        private set

    var selectedLessonId by mutableStateOf<String?>(null)
        private set

    val referenceEntries: List<ReferenceEntry> = MorseAlphabet.characters.entries
        .sortedBy { it.key }
        .map { ReferenceEntry(character = it.key, morse = it.value) }

    val selectedLesson: Lesson?
        get() = lessonItems.firstOrNull { it.lesson.id == selectedLessonId }?.lesson

    init {
        refresh()
    }

    fun refresh() {
        val unlockedIds = progressRepository.buildTracker().getUnlockedLessons().map { it.id }.toSet()
        lessonItems = lessons.mapIndexed { index, lesson ->
            LearnLessonItem(
                lesson = lesson,
                isUnlocked = index == 0 || lesson.id in unlockedIds,
            )
        }
        if (selectedLessonId == null || lessonItems.none { it.lesson.id == selectedLessonId }) {
            selectedLessonId = lessonItems.firstOrNull { it.isUnlocked }?.lesson?.id ?: lessonItems.firstOrNull()?.lesson?.id
        }
    }

    fun selectLesson(lessonId: String) {
        val item = lessonItems.firstOrNull { it.lesson.id == lessonId } ?: return
        if (item.isUnlocked) {
            selectedLessonId = lessonId
        }
    }

    fun canStartSelectedPractice(): Boolean {
        return lessonItems.firstOrNull { it.lesson.id == selectedLessonId }?.isUnlocked == true
    }

    fun playCharacter(character: Char) {
        audioPlayer.playText(character.toString(), settingsRepository.settings)
    }
}
