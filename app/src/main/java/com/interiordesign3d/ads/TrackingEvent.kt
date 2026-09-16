package com.interiordesign3d.ads

import android.util.Log

/**
 * Every analytics call the first-open funnel makes. A no-op today (just logs) — wiring a real
 * SDK in later means filling these two functions in, not touching the screens that call them.
 */
object TrackingEvent {
    private const val TAG = "TrackingEvent"

    fun logEvent(name: String) {
        Log.d(TAG, "event: $name")
    }

    fun logScreenShow(screen: String) {
        Log.d(TAG, "screen_show: $screen")
    }
}
