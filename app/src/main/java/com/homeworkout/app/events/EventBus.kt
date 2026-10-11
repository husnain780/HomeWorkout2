package com.homeworkout.app.events

class EventBus {

    private val listeners = mutableListOf<(Any) -> Unit>()

    fun subscribe(listener: (Any) -> Unit) {
        listeners.add(listener)
    }

    fun publish(event: Any) {
        listeners.forEach { listener ->
            listener(event)
        }
    }
}
