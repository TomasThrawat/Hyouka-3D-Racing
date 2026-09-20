package com.tomasthrawat.hyouka3dracing

import kotlin.math.sin

class Ai(private val skill: Float) {
    private var phase = 0f

    fun update(car: CarState, dt: Float) {
        phase += dt
        val target = 220f + skill * 65f + sin(phase * 0.7f) * 10f
        car.speedKmh += (target - car.speedKmh) * 2.2f * dt
        car.speedKmh = car.speedKmh.coerceIn(0f, 300f)
        car.progress += (car.speedKmh / 3.6f) * dt
        car.lateral += sin(phase * (0.45f + skill * 0.1f)) * 0.12f * dt
        car.lateral = car.lateral.coerceIn(-2.0f, 2.0f)
    }
}
