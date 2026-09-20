package com.tomasthrawat.hyouka3dracing

class Physics {
    fun update(car: CarState, input: CarInput, dt: Float) {
        val throttle = input.throttle.coerceIn(0f, 1f)
        val brake = input.brake.coerceIn(0f, 1f)
        val steer = input.steer.coerceIn(-1f, 1f)

        val target = when {
            brake > 0.1f -> 0f
            throttle > 0.1f -> 310f
            else -> 115f
        }
        val response = when {
            brake > 0.1f -> 8.0f
            throttle > 0.1f -> 4.5f
            else -> 1.35f
        }
        car.speedKmh += (target - car.speedKmh) * response * dt
        car.speedKmh = car.speedKmh.coerceIn(0f, 310f)

        val speedFactor = (car.speedKmh / 310f).coerceIn(0f, 1f)
        val steeringAuthority = 3.0f + 5.5f * speedFactor
        car.lateral += steer * steeringAuthority * dt
        car.lateral *= (1f - 1.8f * dt).coerceAtLeast(0.7f)
        car.lateral = car.lateral.coerceIn(-2.4f, 2.4f)

        car.progress += (car.speedKmh / 3.6f) * dt
    }
}
