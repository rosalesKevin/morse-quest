package morse.android.haptics

import morse.core.Signal

class FakeHapticsController : IHapticsController {
    override fun vibrateSignals(signals: List<Signal>) {}
    override fun cancel() {}
}
