package morse.practice

enum class LessonKind {
    Standard,
    Review,
}

data class Lesson(
    val id: String,
    val title: String,
    val characters: List<Char>,
    val exercises: List<Exercise>,
    val kind: LessonKind = LessonKind.Standard,
    val introducedCharacters: List<Char> = characters,
)
