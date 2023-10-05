package com.plcoding.spotifycloneyt.exoplayer

class FirebaseMusicSource {
    private val onReadyListener = mutableListOf<(Boolean) -> Unit>()

    private var state: State = State.STATE_CREATE
        set(value) {
            if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized(onReadyListener) {
                    field = value
                    onReadyListener.forEach { listener ->
                        listener(state == State.STATE_INITIALIZING)
                    }
                }
            } else {
                field = value

            }
        }
    fun whenReady(action: (Boolean) ->Unit):Boolean{
        if (state == State.STATE_CREATE || state == State.STATE_INITIALIZING) {
            onReadyListener ==action
            return false
        }else{
            action(state == State.STATE_INITIALIZED)
            return true
        }

    }

}

enum class State {
    STATE_CREATE,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR


}