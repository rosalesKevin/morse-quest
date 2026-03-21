package morse.android.reference

data class ReferenceSection(
    val title: String,
    val entries: List<ReferenceViewModel.ReferenceEntry>,
)

fun buildReferenceSections(
    entries: List<ReferenceViewModel.ReferenceEntry>,
    query: String,
): List<ReferenceSection> {
    if (query.isNotBlank()) {
        val matches = entries.filter {
            it.character.toString().contains(query, ignoreCase = true) ||
                it.morse.contains(query)
        }
        return listOf(ReferenceSection(title = "Matches", entries = matches))
    }

    val letters = entries.filter { it.character.isLetter() }
    val numbers = entries.filter { it.character.isDigit() }
    return buildList {
        if (letters.isNotEmpty()) add(ReferenceSection(title = "Letters", entries = letters))
        if (numbers.isNotEmpty()) add(ReferenceSection(title = "Numbers", entries = numbers))
    }
}
