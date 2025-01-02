package com.sm.sm_lepu

import io.flutter.plugin.common.EventChannel
import org.json.JSONObject

class SharedStreamHandler private constructor() : EventChannel.StreamHandler {

    private var events: EventChannel.EventSink? = null

    companion object {
        private var instance: SharedStreamHandler? = null

        fun getInstance(): SharedStreamHandler {
            if (instance == null) {
                instance = SharedStreamHandler()
            }
            return instance!!
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.events = events
    }

    override fun onCancel(arguments: Any?) {
        this.events = null
    }

    fun sendEvent(data: JSONObject) {
        try {
            events?.success(data.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
