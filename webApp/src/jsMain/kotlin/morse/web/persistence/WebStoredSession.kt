package morse.web.persistence

import kotlinx.serialization.Serializable
import morse.practice.MistakeRecord
import morse.practice.SessionScore

@Serializable
data class WebStoredSession(
    val lessonId: String,
    val correct: Int,
    val total: Int,
    val wpm: Double,
    val mistakes: List<WebStoredMistake>,
    val recordedAtEpochMillis: Long,
) {
    companion object {
        fun from(
            lessonId: String,
            score: SessionScore,
            mistakes: List<MistakeRecord>,
            recordedAtEpochMillis: Long,
        ): WebStoredSession = WebStoredSession(
            lessonId = lessonId,
            correct = score.correct,
            total = score.total,
            wpm = score.wpm,
            mistakes = mistakes.map { WebStoredMistake(it.character.toString(), it.count) },
            recordedAtEpochMillis = recordedAtEpochMillis,
        )
    }
}

@Serializable
data class WebStoredMistake(
    val character: String,
    val count: Int,
)
