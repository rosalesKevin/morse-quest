package morse.practice

object PracticeScaffold {
    const val moduleName: String = "practice"
}

expect class PlatformStorage() {
    suspend fun savePlaceholder(value: String)
}
