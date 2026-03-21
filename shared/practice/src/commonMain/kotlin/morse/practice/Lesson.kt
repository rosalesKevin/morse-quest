package morse.practice

data class Lesson(
    val id: String,
    val title: String,
    val characters: List<Char>,
    val exercises: List<Exercise>,
)
