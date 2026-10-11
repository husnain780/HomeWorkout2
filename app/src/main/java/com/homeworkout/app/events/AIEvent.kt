package com.homeworkout.app.events

sealed class AIEvent {

    data class MessageSent(
        val text: String
    ) : AIEvent()

    data class ResponseReceived(
        val text: String
    ) : AIEvent()

    data class Error(
        val message: String
    ) : AIEvent()
}
