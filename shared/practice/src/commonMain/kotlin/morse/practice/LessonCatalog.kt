package morse.practice

import morse.core.MorseEncoder
import morse.core.TimingEngine

object LessonCatalog {
    private val encoder = MorseEncoder(TimingEngine())

    fun defaultLessons(): List<Lesson> {
        val standardPairs = KochOrder.characters.chunked(2)
        val lessons = mutableListOf<Lesson>()

        standardPairs.forEachIndexed { index, introducedCharacters ->
            lessons += Lesson(
                id = "lesson-${index + 1}",
                title = "Lesson ${index + 1}: ${introducedCharacters.joinToString(" ")}",
                characters = introducedCharacters,
                exercises = buildExercises(introducedCharacters),
                kind = LessonKind.Standard,
                introducedCharacters = introducedCharacters,
            )

            val standardLessonNumber = index + 1
            if (standardLessonNumber % 2 == 0) {
                val reviewCharacters = KochOrder.characters.take(standardLessonNumber * 2)
                val reviewNumber = standardLessonNumber / 2
                lessons += Lesson(
                    id = "review-$reviewNumber",
                    title = "Review $reviewNumber",
                    characters = reviewCharacters,
                    exercises = buildExercises(reviewCharacters),
                    kind = LessonKind.Review,
                    introducedCharacters = emptyList(),
                )
            }
        }

        return lessons
    }

    private fun buildExercises(unlockedCharacters: List<Char>): List<Exercise> {
        val cycle = buildCharacterCycle(unlockedCharacters)
        val pairWords = unlockedCharacters.windowed(size = 2, step = 1, partialWindows = true)
            .map { window -> window.joinToString(separator = "") }
            .ifEmpty { listOf(unlockedCharacters.first().toString()) }

        return listOf(
            Exercise.ListenAndIdentify(
                morse = encoder.encode(cycle[0].toString()),
                answer = cycle[0],
            ),
            Exercise.ReadAndTap(
                character = cycle[1],
                expectedMorse = encoder.encode(cycle[1].toString()),
            ),
            Exercise.DecodeWord(
                morse = encoder.encode(pairWords[0]),
                answer = pairWords[0],
            ),
            Exercise.EncodeWord(
                word = pairWords.getOrElse(1) { pairWords[0] },
                expectedMorse = encoder.encode(pairWords.getOrElse(1) { pairWords[0] }),
            ),
            Exercise.SpeedChallenge(
                text = pairWords.getOrElse(2) { pairWords.last() },
                targetWpm = 15,
            ),
            Exercise.ListenAndIdentify(
                morse = encoder.encode(cycle[2].toString()),
                answer = cycle[2],
            ),
            Exercise.ReadAndTap(
                character = cycle[3],
                expectedMorse = encoder.encode(cycle[3].toString()),
            ),
            Exercise.DecodeWord(
                morse = encoder.encode(pairWords.getOrElse(3) { pairWords.last() }),
                answer = pairWords.getOrElse(3) { pairWords.last() },
            ),
            Exercise.EncodeWord(
                word = pairWords.getOrElse(4) { pairWords.first() },
                expectedMorse = encoder.encode(pairWords.getOrElse(4) { pairWords.first() }),
            ),
            Exercise.SpeedChallenge(
                text = pairWords.getOrElse(5) { pairWords.last() },
                targetWpm = 20,
            ),
        )
    }

    private fun buildCharacterCycle(unlockedCharacters: List<Char>): List<Char> {
        if (unlockedCharacters.isEmpty()) {
            return emptyList()
        }

        return List(4) { index -> unlockedCharacters[index % unlockedCharacters.size] }
    }
}
