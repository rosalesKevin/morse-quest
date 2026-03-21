package morse.android.persistence

import kotlinx.serialization.Serializable

@Serializable
data class StoredSession(
    val lessonId: String,
    val correct: Int,
    val total: Int,
    val wpm: Double,
    val mistakes: List<StoredMistake>,
    val recordedAtEpochMillis: Long,
)

@Serializable
data class StoredMistake(
    val character: String,
    val count: Int,
)
