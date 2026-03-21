package morse.practice

sealed class Exercise {
    data class ListenAndIdentify(val morse: String, val answer: Char) : Exercise()

    data class ReadAndTap(val character: Char, val expectedMorse: String) : Exercise()

    data class DecodeWord(val morse: String, val answer: String) : Exercise()

    data class EncodeWord(val word: String, val expectedMorse: String) : Exercise()

    data class SpeedChallenge(val text: String, val targetWpm: Int) : Exercise()
}
