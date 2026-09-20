package com.tomasthrawat.hyouka3dracing

enum class MapId { OCEAN, DESERT, NIGHT }

data class CarInput(
    val throttle: Float = 0f,
    val brake: Float = 0f,
    val steer: Float = 0f
)

data class CarState(
    var speedKmh: Float = 0f,
    var lateral: Float = 0f,
    var progress: Float = 0f
)

data class RaceSnapshot(
    val speedKmh: Float,
    val progress: Float,
    val lap: Int,
    val finished: Boolean,
    val ai: List<Pair<Float, Float>>
)
