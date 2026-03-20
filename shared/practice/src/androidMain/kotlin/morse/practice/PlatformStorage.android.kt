package morse.practice

actual class PlatformStorage actual constructor() {
    actual suspend fun savePlaceholder(value: String) = Unit
}
