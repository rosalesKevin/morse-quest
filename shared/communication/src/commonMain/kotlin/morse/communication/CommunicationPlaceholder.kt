package morse.communication

import kotlinx.serialization.Serializable

@Serializable
data class CommunicationPlaceholder(
    val ready: Boolean = false
)
