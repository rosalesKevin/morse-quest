package morse.android.practice

class MorseSymbolComposer(
    initialAnswer: String = "",
) {
    var answer: String = canonicalize(initialAnswer)
        private set

    fun appendDot() {
        answer += "."
    }

    fun appendDash() {
        answer += "-"
    }

    fun appendLetterGap() {
        if (answer.isBlank() || answer.endsWith(" / ")) {
            return
        }
        if (!answer.endsWith(" ")) {
            answer += " "
        }
    }

    fun appendWordGap() {
        if (answer.isBlank()) {
            return
        }
        answer = answer.trimEnd() + " / "
    }

    fun deleteLast() {
        answer = when {
            answer.endsWith(" / ") -> answer.dropLast(3)
            answer.endsWith(" ") -> answer.dropLast(1)
            answer.isNotEmpty() -> answer.dropLast(1)
            else -> answer
        }
    }

    fun clear() {
        answer = ""
    }

    private fun canonicalize(value: String): String {
        return value
            .replace(Regex("\\s*/\\s*"), " / ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }
}
