package morse.android.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.runTest
import morse.android.util.MainDispatcherRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class DispatcherDiagTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * Pattern: cold flow emitting two items in stateIn(WhileSubscribed).
     * With UnconfinedTestDispatcher (via MainDispatcherRule), both items are
     * emitted synchronously, so only the final value is visible on subscription.
     */
    private class VmColdFlow : ViewModel() {
        val state: StateFlow<String?> = flow {
            emit(null)
            emit("loaded")
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    @Test
    fun `stateIn WhileSubscribed emits final value after cold flow completes`() = runTest {
        val vm = VmColdFlow()
        vm.state.test {
            val value = awaitItem()
            // With UnconfinedTestDispatcher, the cold flow runs eagerly.
            // The final emitted value ("loaded") replaces the intermediate null.
            assertNotNull(value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class VmInitialNull : ViewModel() {
        val state: StateFlow<String?> = flow {
            emit(null) // only null, never emits a non-null value
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    @Test
    fun `stateIn WhileSubscribed with only null emission returns null`() = runTest {
        val vm = VmInitialNull()
        vm.state.test {
            val value = awaitItem()
            assertNull(value)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
