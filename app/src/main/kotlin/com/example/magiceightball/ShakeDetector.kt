package com.example.magiceightball

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detects a device shake gesture using the accelerometer and notifies
 * a [ShakeListener] when the threshold is exceeded.
 *
 * Typical usage:
 * ```
 * val detector = ShakeDetector(sensorManager) { onShake() }
 * detector.start()
 * // …later…
 * detector.stop()
 * ```
 */
class ShakeDetector(
    private val sensorManager: SensorManager,
    private val onShake: () -> Unit
) : SensorEventListener {

    companion object {
        /** Acceleration threshold (m/s²) required to trigger a shake. */
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
        /** Minimum time (ms) between two consecutive shake events. */
        private const val SHAKE_SLOP_TIME_MS = 500L
    }

    private var shakeTimestamp: Long = 0

    /** Registers the accelerometer listener. Returns false if no accelerometer is available. */
    fun start(): Boolean {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: return false
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        return true
    }

    /** Unregisters the accelerometer listener to conserve battery. */
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val gX = event.values[0] / SensorManager.GRAVITY_EARTH
        val gY = event.values[1] / SensorManager.GRAVITY_EARTH
        val gZ = event.values[2] / SensorManager.GRAVITY_EARTH

        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)
        if (gForce < SHAKE_THRESHOLD_GRAVITY) return

        val now = System.currentTimeMillis()
        if (now - shakeTimestamp < SHAKE_SLOP_TIME_MS) return

        shakeTimestamp = now
        onShake()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
}
