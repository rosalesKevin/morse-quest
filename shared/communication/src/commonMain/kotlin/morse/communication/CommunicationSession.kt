package morse.communication

/**
 * Snapshot of a communication session.
 *
 * Message ordering is receive-order based. The MVP does not correct clock skew or
 * server/network reordering beyond the order in which frames arrive at the client.
 */
data class CommunicationSession(
    val sessionId: String,
    val localUserId: String,
    val participants: List<String> = emptyList(),
    val messages: List<MorseMessage> = emptyList(),
) {
    fun withMessage(message: MorseMessage): CommunicationSession =
        copy(messages = messages + message)

    fun withParticipant(userId: String): CommunicationSession =
        if (userId in participants) this else copy(participants = participants + userId)
}
