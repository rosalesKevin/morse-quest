package morse.practice

expect class PlatformStorage() {
    suspend fun savePlaceholder(value: String)
}
