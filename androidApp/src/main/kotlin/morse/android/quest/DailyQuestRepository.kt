package morse.android.quest

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyQuestRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : IDailyQuestRepository {

    private object Keys {
        val COMPLETED_DATE      = stringPreferencesKey("quest_completed_date")
        val PROGRESS_DATE       = stringPreferencesKey("quest_progress_date")
        val PROGRESS_INDEX      = intPreferencesKey("quest_progress_index")
        val PROGRESS_DIFFICULTY = stringPreferencesKey("quest_progress_difficulty")
    }

    override val completedToday: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.COMPLETED_DATE] == LocalDate.now().toString()
    }

    override suspend fun isCompletedToday(): Boolean =
        dataStore.data.first()[Keys.COMPLETED_DATE] == LocalDate.now().toString()

    override suspend fun progressTodayIndex(): Int {
        val prefs = dataStore.data.first()
        return if (prefs[Keys.PROGRESS_DATE] == LocalDate.now().toString())
            prefs[Keys.PROGRESS_INDEX] ?: 0
        else 0
    }

    override suspend fun progressTodayDifficulty(): DailyQuestDifficulty? {
        val prefs = dataStore.data.first()
        if (prefs[Keys.PROGRESS_DATE] != LocalDate.now().toString()) return null
        val stored = prefs[Keys.PROGRESS_DIFFICULTY] ?: return null
        return DailyQuestDifficulty.entries.firstOrNull { it.name == stored }
    }

    override suspend fun saveProgress(index: Int, difficulty: DailyQuestDifficulty) {
        dataStore.edit { prefs ->
            prefs[Keys.PROGRESS_DATE]       = LocalDate.now().toString()
            prefs[Keys.PROGRESS_INDEX]      = index
            prefs[Keys.PROGRESS_DIFFICULTY] = difficulty.name
        }
    }

    override suspend fun markCompletedToday() {
        dataStore.edit { prefs ->
            prefs[Keys.COMPLETED_DATE] = LocalDate.now().toString()
        }
    }
}
