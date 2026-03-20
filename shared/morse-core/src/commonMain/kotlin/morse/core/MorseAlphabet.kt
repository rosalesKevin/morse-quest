package morse.core

object MorseAlphabet {
    val characters: Map<Char, String> = mapOf(
        'A' to ".-",
        'B' to "-...",
        'C' to "-.-.",
        'D' to "-..",
        'E' to ".",
        'F' to "..-.",
        'G' to "--.",
        'H' to "....",
        'I' to "..",
        'J' to ".---",
        'K' to "-.-",
        'L' to ".-..",
        'M' to "--",
        'N' to "-.",
        'O' to "---",
        'P' to ".--.",
        'Q' to "--.-",
        'R' to ".-.",
        'S' to "...",
        'T' to "-",
        'U' to "..-",
        'V' to "...-",
        'W' to ".--",
        'X' to "-..-",
        'Y' to "-.--",
        'Z' to "--..",
        '0' to "-----",
        '1' to ".----",
        '2' to "..---",
        '3' to "...--",
        '4' to "....-",
        '5' to ".....",
        '6' to "-....",
        '7' to "--...",
        '8' to "---..",
        '9' to "----.",
        '.' to ".-.-.-",
        ',' to "--..--",
        '?' to "..--..",
        '!' to "-.-.--",
        '/' to "-..-.",
        '(' to "-.--.",
        ')' to "-.--.-",
        '&' to ".-...",
        ':' to "---...",
        ';' to "-.-.-.",
        '=' to "-...-",
        '+' to ".-.-.",
        '-' to "-....-",
        '_' to "..--.-",
        '"' to ".-..-.",
        '$' to "...-..-",
        '@' to ".--.-.",
    )

    val prosigns: Map<String, String> = mapOf(
        "AR" to ".-.-.",
        "SK" to "...-.-",
        "BT" to "-...-",
    )

    val reverseCharacters: Map<String, Char> = characters.entries.associate { (character, pattern) ->
        pattern to character
    }

    val reverseTokens: Map<String, String> = buildMap {
        reverseCharacters.forEach { (pattern, character) -> put(pattern, character.toString()) }
        prosigns.forEach { (token, pattern) -> put(pattern, token) }
    }

    fun encodeToken(token: String): String? {
        val normalized = token.uppercase()
        return prosigns[normalized] ?: normalized.singleOrNull()?.let(characters::get)
    }

    fun decodeToken(pattern: String): String = reverseTokens[pattern] ?: "?"
}
