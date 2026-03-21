package morse.practice

fun interface TimeProvider {
    fun currentEpochMillis(): Long
}
