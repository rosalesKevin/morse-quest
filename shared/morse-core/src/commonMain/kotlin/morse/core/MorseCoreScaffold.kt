package morse.core

object MorseCoreScaffold {
    const val moduleName: String = "morse-core"
}

expect class PlatformAudio() {
    fun playPlaceholder()
}
