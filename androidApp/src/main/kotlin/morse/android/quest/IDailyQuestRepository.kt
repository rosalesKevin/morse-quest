package morse.android.quest

import kotlinx.coroutines.flow.Flow

interface IDailyQuestRepository {
    val completedToday: Flow<Boolean>
    suspend fun isCompletedToday(): Boolean
    suspend fun progressTodayIndex(): Int
    suspend fun progressTodayDifficulty(): DailyQuestDifficulty?
    suspend fun saveProgress(index: Int, difficulty: DailyQuestDifficulty)
    suspend fun markCompletedToday()
}
