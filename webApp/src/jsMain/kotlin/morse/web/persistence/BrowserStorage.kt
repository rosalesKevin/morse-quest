package morse.web.persistence

import kotlinx.browser.window

interface StorageDriver {
    fun get(key: String): String?
    fun set(key: String, value: String)
    fun remove(key: String)
}

class BrowserStorage(
    private val driver: StorageDriver = LocalStorageDriver(),
) {
    fun getString(key: String): String? = driver.get(key)

    fun setString(key: String, value: String) {
        driver.set(key, value)
    }

    fun remove(key: String) {
        driver.remove(key)
    }
}

class LocalStorageDriver : StorageDriver {
    override fun get(key: String): String? = runCatching { window.localStorage.getItem(key) }.getOrNull()

    override fun set(key: String, value: String) {
        runCatching { window.localStorage.setItem(key, value) }
    }

    override fun remove(key: String) {
        runCatching { window.localStorage.removeItem(key) }
    }
}
