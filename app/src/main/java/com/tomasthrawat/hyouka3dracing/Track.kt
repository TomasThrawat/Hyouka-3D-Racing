package com.tomasthrawat.hyouka3dracing

data class TrackSegment(
    val model: String,
    val x: Float,
    val z: Float,
    val yaw: Float,
    val scale: Float
)

data class TrackPose(
    val x: Float,
    val z: Float,
    val yaw: Float
)

object Track {
    private fun half(map: MapId) = when (map) {
        MapId.OCEAN -> 34f
        MapId.DESERT -> 42f
        MapId.NIGHT -> 30f
    }

    private fun depth(map: MapId) = when (map) {
        MapId.OCEAN -> 72f
        MapId.DESERT -> 92f
        MapId.NIGHT -> 64f
    }

    fun lapMeters(map: MapId): Float = 2f * (half(map) + depth(map))

    fun segments(map: MapId): List<TrackSegment> {
        val s = when (map) {
            MapId.OCEAN -> 8.5f
            MapId.DESERT -> 9.5f
            MapId.NIGHT -> 8.0f
        }
        val w = half(map)
        val d = depth(map)
        return listOf(
            TrackSegment("start", -w * 0.55f, 0f, 90f, s),
            TrackSegment("straight", 0f, 0f, 90f, s),
            TrackSegment("straight", w * 0.55f, 0f, 90f, s),
            TrackSegment("corner90", w, -d * 0.42f, 0f, s),
            TrackSegment("straight", w, -d, 0f, s),
            TrackSegment("corner45", w * 0.65f, -d * 1.03f, 45f, s),
            TrackSegment("hairpin", 0f, -d * 1.08f, 90f, s),
            TrackSegment("straight", -w * 0.58f, -d, 0f, s),
            TrackSegment("corner90", -w, -d * 0.45f, 180f, s),
            TrackSegment("banked", -w, -d * 0.05f, 180f, s),
            TrackSegment("chicane", -w * 0.45f, -d * 0.02f, 90f, s),
            TrackSegment("s_bend", 0f, -d * 0.02f, 90f, s)
        )
    }

    fun pose(map: MapId, meters: Float, lateral: Float): TrackPose {
        val lap = lapMeters(map)
        val t = ((meters % lap) + lap) % lap
        val w = half(map)
        val d = depth(map)
        val distance = (t / lap) * (2f * (w + d))

        return when {
            distance < 2f * w -> {
                val x = -w + distance
                TrackPose(x + lateral, 0f, 90f)
            }
            distance < 2f * w + d -> {
                val z = -(distance - 2f * w)
                TrackPose(w + lateral, z, 0f)
            }
            distance < 4f * w + d -> {
                val x = w - (distance - (2f * w + d))
                TrackPose(x + lateral, -d, -90f)
            }
            else -> {
                val z = -d + (distance - (4f * w + d))
                TrackPose(-w + lateral, z, 180f)
            }
        }
    }
}
