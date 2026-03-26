package morse.android.quest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class FakeDailyQuestRepository(
    completedDate: String = "",
    progressDate: String = "",
    progressIndexValue: Int = 0,
    progressDifficultyValue: String = "",
) : IDailyQuestRepository {

    private var _completedDate = completedDate
    private var _progressDate = progressDate
    private var _progressIndex = progressIndexValue
    private var _progressDifficulty = progressDifficultyValue

    private val _completedToday = MutableStateFlow(_completedDate == LocalDate.now().toString())
    override val completedToday: Flow<Boolean> = _completedToday.asStateFlow()

    override suspend fun isCompletedToday(): Boolean =
        _completedDate == LocalDate.now().toString()

    override suspend fun progressTodayIndex(): Int =
        if (_progressDate == LocalDate.now().toString()) _progressIndex else 0

    override suspend fun progressTodayDifficulty(): DailyQuestDifficulty? {
        if (_progressDate != LocalDate.now().toString()) return null
        return DailyQuestDifficulty.entries.firstOrNull { it.name == _progressDifficulty }
    }

    override suspend fun saveProgress(index: Int, difficulty: DailyQuestDifficulty) {
        _progressDate = LocalDate.now().toString()
        _progressIndex = index
        _progressDifficulty = difficulty.name
    }

    override suspend fun markCompletedToday() {
        _completedDate = LocalDate.now().toString()
        _completedToday.value = true
    }
}
