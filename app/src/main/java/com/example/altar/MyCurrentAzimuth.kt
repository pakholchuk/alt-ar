package com.example.altar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class MyCurrentAzimuth(
    private val azimuthChangedListener: OnAzimuthChangedListener,
    private val context: Context
) :
    SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private var azimuthFrom = 0f
    private var azimuthTo = 0f

    fun start() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        azimuthFrom = azimuthTo
        var orientation = FloatArray(3)
        val rMat = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rMat, p0?.values)
        azimuthTo = (((Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0].toDouble()) + 360).toInt()) % 360).toFloat()
        azimuthChangedListener.onAzimuthChanged(azimuthFrom, azimuthTo)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        TODO("Not yet implemented")
    }

}

class Pikachu(
    val poiName: String,
    val poiLatitude: Double, val poiLongitude: Double
)