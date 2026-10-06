package com.kunjika.app.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

/**
 * Device Angle Manager for Angle-Locked Privacy.
 *
 * Uses hardware Rotation Vector / Gravity sensors (0 permissions) to track device pitch & roll.
 * Evaluates whether the device is currently held within a natural reading cone (Pitch 25°–80°, Roll ±22°).
 * If the device is laid flat on a desk, held up high, or tilted sideways towards an onlooker,
 * passwords automatically mask to protect user privacy.
 */
class DeviceAngleManager(
    context: Context,
    private val onAngleChanged: (pitch: Float, roll: Float, isWithinReadingCone: Boolean) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var lastReadingConeState = true

    fun start() {
        rotationSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
        } else if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            orientationAngles[1] = Math.atan2(y.toDouble(), z.toDouble()).toFloat()
            orientationAngles[2] = Math.atan2(x.toDouble(), z.toDouble()).toFloat()
        } else {
            return
        }

        val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

        val isWithinReadingCone = evaluatePrivacyCone(pitch, roll)
        onAngleChanged(pitch, roll, isWithinReadingCone)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun evaluatePrivacyCone(pitch: Float, roll: Float): Boolean {
        // Natural reading posture: Pitch 25° to 80° (held in hand), Roll -22° to +22°
        val absPitch = abs(pitch)
        val absRoll = abs(roll)

        val rawState = absPitch in 25f..80f && absRoll <= 22f

        // 3-degree hysteresis filter to avoid hand tremor jitter
        if (rawState != lastReadingConeState) {
            val isWellOutside = (absPitch !in 22f..83f) || (absRoll > 25f)
            if (isWellOutside || rawState) {
                lastReadingConeState = rawState
            }
        }

        return lastReadingConeState
    }
}
