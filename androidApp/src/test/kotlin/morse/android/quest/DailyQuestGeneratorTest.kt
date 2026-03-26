package morse.android.quest

import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyQuestGeneratorTest {

    private val generator = DailyQuestGenerator()
    private val today = LocalDate.of(2026, 3, 24)

    // ── Determinism ───────────────────────────────────────────────────────────

    @Test
    fun `same date and difficulty produces identical questions list`() {
        val a = generator.generate(today, DailyQuestDifficulty.MEDIUM)
        val b = generator.generate(today, DailyQuestDifficulty.MEDIUM)
        assertEquals(a.questions, b.questions)
    }

    @Test
    fun `different difficulties produce different questions`() {
        val easy = generator.generate(today, DailyQuestDifficulty.EASY)
        val hard = generator.generate(today, DailyQuestDifficulty.HARD)
        assertFalse(easy.questions == hard.questions)
    }

    @Test
    fun `different dates produce different questions`() {
        val day1 = generator.generate(today, DailyQuestDifficulty.MEDIUM)
        val day2 = generator.generate(today.plusDays(1), DailyQuestDifficulty.MEDIUM)
        assertFalse(day1.questions == day2.questions)
    }

    // ── Question counts ───────────────────────────────────────────────────────

    @Test
    fun `EASY generates exactly 5 questions`() {
        assertEquals(5, generator.generate(today, DailyQuestDifficulty.EASY).questions.size)
    }

    @Test
    fun `MEDIUM generates exactly 7 questions`() {
        assertEquals(7, generator.generate(today, DailyQuestDifficulty.MEDIUM).questions.size)
    }

    @Test
    fun `HARD generates exactly 10 questions`() {
        assertEquals(10, generator.generate(today, DailyQuestDifficulty.HARD).questions.size)
    }

    // ── Character pool sizes ──────────────────────────────────────────────────

    @Test
    fun `EASY uses first 10 Koch characters`() {
        assertEquals(10, generator.generate(today, DailyQuestDifficulty.EASY).characters.size)
    }

    @Test
    fun `MEDIUM uses first 20 Koch characters`() {
        assertEquals(20, generator.generate(today, DailyQuestDifficulty.MEDIUM).characters.size)
    }

    @Test
    fun `HARD uses all 40 Koch characters`() {
        assertEquals(40, generator.generate(today, DailyQuestDifficulty.HARD).characters.size)
    }

    // ── Exercise type constraints ─────────────────────────────────────────────

    @Test
    fun `EASY only generates ListenAndIdentify and ReadAndTap`() {
        val quest = generator.generate(today, DailyQuestDifficulty.EASY)
        quest.questions.forEach { exercise ->
            assertTrue(
                exercise is morse.practice.Exercise.ListenAndIdentify ||
                exercise is morse.practice.Exercise.ReadAndTap,
                "EASY should not contain $exercise",
            )
        }
    }

    @Test
    fun `HARD includes all four exercise types`() {
        val quest = generator.generate(today, DailyQuestDifficulty.HARD)
        val kinds = quest.exerciseSummary.keys
        assertTrue(ExerciseKind.LISTEN in kinds)
        assertTrue(ExerciseKind.TAP in kinds)
        assertTrue(ExerciseKind.DECODE in kinds)
        assertTrue(ExerciseKind.ENCODE in kinds)
    }

    @Test
    fun `no SpeedChallenge is ever generated at any difficulty`() {
        DailyQuestDifficulty.entries.forEach { diff ->
            val quest = generator.generate(today, diff)
            quest.questions.forEach { exercise ->
                assertFalse(
                    exercise is morse.practice.Exercise.SpeedChallenge,
                    "SpeedChallenge must not appear in DailyQuest at $diff",
                )
            }
        }
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    @Test
    fun `exerciseSummary counts match questions list`() {
        val quest = generator.generate(today, DailyQuestDifficulty.MEDIUM)
        assertEquals(quest.questions.size, quest.exerciseSummary.values.sum())
    }

    // ── Estimated time ────────────────────────────────────────────────────────

    @Test
    fun `estimated minutes are 2, 4, 6 for EASY, MEDIUM, HARD`() {
        assertEquals(2, generator.generate(today, DailyQuestDifficulty.EASY).estimatedMinutes)
        assertEquals(4, generator.generate(today, DailyQuestDifficulty.MEDIUM).estimatedMinutes)
        assertEquals(6, generator.generate(today, DailyQuestDifficulty.HARD).estimatedMinutes)
    }
}
