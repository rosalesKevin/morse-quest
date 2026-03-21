package morse.android.settings

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import morse.android.persistence.FakeProgressRepository
import morse.android.persistence.FakeSettingsRepository
import morse.android.persistence.UserSettings
import morse.android.util.MainDispatcherRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(initial: UserSettings = UserSettings()) = SettingsViewModel(
        settingsRepository = FakeSettingsRepository(initial),
        progressRepository = FakeProgressRepository(),
    )

    @Test
    fun `initial settings reflect defaults`() = runTest {
        viewModel().settings.test {
            val s = awaitItem()
            assertEquals(20, s.wpm)
            assertEquals(700f, s.toneFrequencyHz)
            assertTrue(s.hapticsEnabled)
        }
    }

    @Test
    fun `updateWpm reflects in settings flow`() = runTest {
        val vm = viewModel()
        vm.updateWpm(30)
        vm.settings.test {
            assertEquals(30, awaitItem().wpm)
        }
    }

    @Test
    fun `updateHapticsEnabled reflects in settings flow`() = runTest {
        val vm = viewModel(UserSettings(hapticsEnabled = true))
        vm.updateHapticsEnabled(false)
        vm.settings.test {
            assertFalse(awaitItem().hapticsEnabled)
        }
    }

    @Test
    fun `resetProgress clears session history`() = runTest {
        val fakeProgress = FakeProgressRepository()
        val vm = SettingsViewModel(FakeSettingsRepository(), fakeProgress)
        vm.resetProgress()
        fakeProgress.sessionHistory.test {
            assertTrue(awaitItem().isEmpty())
        }
    }
}
