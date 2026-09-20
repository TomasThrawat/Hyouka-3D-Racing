package com.tomasthrawat.hyouka3dracing

class Race(private val map: MapId, private val lapsToFinish: Int = 3) {
    fun lapFor(progress: Float): Int {
        return (progress / Track.lapMeters(map)).toInt().plus(1).coerceAtMost(lapsToFinish)
    }

    fun finished(progress: Float): Boolean {
        return progress >= Track.lapMeters(map) * lapsToFinish
    }
}
