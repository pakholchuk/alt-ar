package com.example.altar

import android.app.Activity
import android.content.Context
import android.graphics.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import androidx.lifecycle.LifecycleOwner
import com.example.altar.databinding.ActivityMainBinding
import com.netguru.arlocalizerview.ARLocalizerDependencyProvider
import com.netguru.arlocalizerview.location.LocationData
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.max

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, OnLocationChangedListener, OnAzimuthChangedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var camera: Camera
    private lateinit var mSurfaceHolder: SurfaceHolder
    private var isCameraOn = false
    private lateinit var mPoi: Pikachu

    private var mAzimuthReal = 0.toDouble()
    private var mAzimuthTheoretical = 0.toDouble()

    private val DISTANCE_ACCURACY = 500
    private val AZIMUTH_ACCURACY = 10

    val TARGET_LATTITUDE = 55.696655
    val TARGET_LONGITUDE = 37.324245

    private var mMyLatitude = 0.0
    private var mMyLongitude = 0.0

    private lateinit var myCurrentAzimuth: MyCurrentAzimuth
    private lateinit var myCurrentLocation: MyCurrentLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val list = listOf<LocationData>(
            LocationData(55.696286, 37.326230),
            LocationData(55.695150, 37.325458),
            LocationData(55.695391, 37.323891),
            LocationData(55.696226, 37.323054),
            LocationData(55.696655, 37.324245),
            LocationData(55.696928, 37.325866),
            )
        setupListeners()
        setupLayout()
        setARPoint()
    }

    private fun setARPoint() {
        mPoi = Pikachu("PIKA", TARGET_LATTITUDE, TARGET_LONGITUDE)
    }

    fun calculateDistance(): Double {
        var dX = mPoi.poiLatitude - mMyLatitude
        var dY = mPoi.poiLongitude - mMyLongitude

        var tanPhi = abs(dY/dX)
        var phiAngle = atan(tanPhi)
        phiAngle = Math.toDegrees(phiAngle)

        when  {
            dX > 0 && dY > 0 -> return phiAngle
            dX < 0 && dY > 0 -> return 180 - phiAngle
            dX < 0 && dY < 0 -> return 180 + phiAngle
            dX > 0 && dY < 0 -> return 360 - phiAngle
        }
        return phiAngle
    }

    private fun calculateAzimuthAccuracy(azimuth: Double): List<Double> {
        var minAngle = azimuth - AZIMUTH_ACCURACY
        var maxAngle = azimuth + AZIMUTH_ACCURACY
        var list = mutableListOf<Double>()
        if (minAngle < 0) minAngle += 360
        if (maxAngle > 360) maxAngle -= 360

        list.apply {
            clear()
            add(minAngle)
            add(maxAngle)
        }
        return list
    }

    private fun isBetween(minAngle: Double, maxAngle: Double, azimuth: Double): Boolean {
        if (minAngle > maxAngle) {
            if (isBetween(0.0, maxAngle, azimuth) && isBetween(minAngle, 360.0, azimuth))
                return true
        } else if (azimuth > minAngle && azimuth < maxAngle) return true
        return false
    }

    private fun updateDescription() {
        var distance = calculateDistance().toLong()
        var tAzimuth = mAzimuthTheoretical.toInt()
        var rAzimuth = mAzimuthReal.toInt()

        var text: String = mPoi.poiName + " location:" +
                "\n latitude: $TARGET_LATTITUDE" +
                "  longitude: $TARGET_LONGITUDE" +
                "\n Current location:"
    }
}