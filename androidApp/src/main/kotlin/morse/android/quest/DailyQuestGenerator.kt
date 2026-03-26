package morse.android.quest

import morse.core.MorseEncoder
import morse.core.TimingEngine
import morse.practice.Exercise
import morse.practice.KochOrder
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyQuestGenerator @Inject constructor() {

    private val encoder = MorseEncoder(TimingEngine())

    fun generate(date: LocalDate, difficulty: DailyQuestDifficulty): DailyQuest {
        val seed = date.toEpochDay() * 31L + difficulty.ordinal
        val rng = kotlin.random.Random(seed)

        val (limit, questionCount, estimatedMinutes) = when (difficulty) {
            DailyQuestDifficulty.EASY   -> Triple(10, 5, 2)
            DailyQuestDifficulty.MEDIUM -> Triple(20, 7, 4)
            DailyQuestDifficulty.HARD   -> Triple(40, 10, 6)
        }

        val pool = KochOrder.characters.take(limit)
        val questions = buildQuestions(rng, pool, difficulty, questionCount)
        val summary = questions.groupingBy { exerciseKindOf(it) }.eachCount()

        return DailyQuest(
            date = date,
            difficulty = difficulty,
            questions = questions,
            estimatedMinutes = estimatedMinutes,
            exerciseSummary = summary,
            characters = pool,
        )
    }

    private fun buildQuestions(
        rng: kotlin.random.Random,
        pool: List<Char>,
        difficulty: DailyQuestDifficulty,
        count: Int,
    ): List<Exercise> {
        val result = mutableListOf<Exercise>()
        var i = 0
        while (result.size < count) {
            val char = pool[rng.nextInt(pool.size)]
            val exercise: Exercise = when (difficulty) {
                DailyQuestDifficulty.EASY -> {
                    if (i % 2 == 0) Exercise.ListenAndIdentify(encoder.encode(char.toString()), char)
                    else Exercise.ReadAndTap(char, encoder.encode(char.toString()))
                }
                DailyQuestDifficulty.MEDIUM, DailyQuestDifficulty.HARD -> {
                    when (i % 4) {
                        0 -> Exercise.ListenAndIdentify(encoder.encode(char.toString()), char)
                        1 -> Exercise.ReadAndTap(char, encoder.encode(char.toString()))
                        2 -> {
                            val word = buildWord(rng, pool)
                            Exercise.DecodeWord(encoder.encode(word), word)
                        }
                        else -> {
                            val word = buildWord(rng, pool)
                            Exercise.EncodeWord(word, encoder.encode(word))
                        }
                    }
                }
            }
            result.add(exercise)
            i++
        }
        return result
    }

    private fun buildWord(rng: kotlin.random.Random, pool: List<Char>): String {
        val length = 2 + rng.nextInt(3) // 2–4 characters
        return (1..length).map { pool[rng.nextInt(pool.size)] }.joinToString("")
    }

    private fun exerciseKindOf(exercise: Exercise): ExerciseKind = when (exercise) {
        is Exercise.ListenAndIdentify -> ExerciseKind.LISTEN
        is Exercise.ReadAndTap        -> ExerciseKind.TAP
        is Exercise.DecodeWord        -> ExerciseKind.DECODE
        is Exercise.EncodeWord        -> ExerciseKind.ENCODE
        is Exercise.SpeedChallenge    -> error("SpeedChallenge must not be generated in DailyQuest")
    }
}
