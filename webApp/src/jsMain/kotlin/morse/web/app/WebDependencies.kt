package morse.web.app

import kotlinx.coroutines.MainScope
import morse.practice.Lesson
import morse.practice.LessonCatalog
import morse.practice.TimeProvider
import morse.web.audio.WebAudioPlayer
import morse.web.communication.CommunicationGateway
import morse.web.communication.SharedCommunicationGateway
import morse.web.persistence.BrowserStorage
import morse.web.persistence.WebProgressRepository
import morse.web.persistence.WebSettingsRepository

data class WebDependencies(
    val lessons: List<Lesson>,
    val settingsRepository: WebSettingsRepository,
    val progressRepository: WebProgressRepository,
    val audioPlayer: WebAudioPlayer,
    val timeProvider: TimeProvider,
    val communicationGateway: CommunicationGateway,
) {
    companion object {
        fun default(): WebDependencies {
            val lessons = LessonCatalog.defaultLessons()
            val timeProvider = TimeProvider { kotlin.js.Date.now().toLong() }
            val browserStorage = BrowserStorage()
            return WebDependencies(
                lessons = lessons,
                settingsRepository = WebSettingsRepository(browserStorage),
                progressRepository = WebProgressRepository(browserStorage, lessons, timeProvider),
                audioPlayer = WebAudioPlayer(),
                timeProvider = timeProvider,
                communicationGateway = SharedCommunicationGateway(
                    storage = browserStorage,
                    scope = MainScope(),
                    timeProvider = timeProvider,
                ),
            )
        }
    }
}
