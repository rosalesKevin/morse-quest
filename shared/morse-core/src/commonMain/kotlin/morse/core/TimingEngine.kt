package morse.core

import kotlin.math.roundToLong

class TimingEngine(
    characterWpm: Int = 20,
    effectiveWpm: Int = 20,
) {
    private var characterWpmValue: Int = characterWpm.coerceAtLeast(1)
    private var effectiveWpmValue: Int = effectiveWpm.coerceAtLeast(1)

    val dotDurationMs: Long
        get() = 1200L / characterWpmValue

    val dashDurationMs: Long
        get() = dotDurationMs * 3

    val intraCharacterGapMs: Long
        get() = dotDurationMs

    val letterGapMs: Long
        get() = spacingUnitMs() * 3

    val wordGapMs: Long
        get() = spacingUnitMs() * 7

    fun textToSignals(text: String): List<Signal> = MorseEncoder(this).encodeToSignals(text)

    fun signalsToText(signals: List<Signal>): String = MorseDecoder.decodeSignals(signals)

    fun setSpeeds(characterWpm: Int, effectiveWpm: Int) {
        characterWpmValue = characterWpm.coerceAtLeast(1)
        effectiveWpmValue = effectiveWpm.coerceAtLeast(1)
    }

    private fun spacingUnitMs(): Long {
        if (effectiveWpmValue >= characterWpmValue) {
            return dotDurationMs
        }

        val characterUnitMs = dotDurationMs.toDouble()
        val effectiveUnitMs = 1200.0 / effectiveWpmValue
        val stretchedUnitMs = ((50.0 * effectiveUnitMs) - (31.0 * characterUnitMs)) / 19.0
        return stretchedUnitMs.roundToLong().coerceAtLeast(dotDurationMs)
    }
}
