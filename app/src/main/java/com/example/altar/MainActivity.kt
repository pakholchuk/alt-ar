package com.example.altar

import android.app.Activity
import android.content.Context
import android.hardware.Camera
import android.graphics.PixelFormat
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsMessage
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.example.altar.databinding.ActivityMainBinding
import com.netguru.arlocalizerview.ARLocalizerDependencyProvider
import com.netguru.arlocalizerview.location.LocationData
import java.lang.Exception
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.max
import kotlin.math.sqrt

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
        return sqrt(Math.pow(dX, 2.0) + Math.pow(dY, 2.0)) * 100000
    }

    fun calculateTheoreticalAzimuth(): Double {
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
                " location:" +
                 "\n latitude: " + TARGET_LATTITUDE + "  longitude: " + TARGET_LONGITUDE +
                 "\n Current location:" +
                 "\n Latitude: " + mMyLatitude       + "  Longitude: " + mMyLongitude +
                 "\n " +
                 "\n Target azimuth: " + tAzimuth +
                " \n Current azimuth: " + rAzimuth +
                " \n Distance: " + distance;
    }

    override fun onAzimuthChanged(azimuthFrom: Float, azimuthTo: Float) {
        mAzimuthReal = azimuthTo.toDouble()
        mAzimuthTheoretical = calculateTheoreticalAzimuth()
        var distance = calculateDistance().toInt()

        var minAngle = calculateAzimuthAccuracy(mAzimuthTheoretical)[0]
        var maxAngle = calculateAzimuthAccuracy(mAzimuthTheoretical)[1]

        if (isBetween(minAngle, maxAngle, mAzimuthReal) && distance <= DISTANCE_ACCURACY)
            binding.icon.visibility = View.VISIBLE
        else
            binding.icon.visibility = View.INVISIBLE
        updateDescription()
    }

    override fun onLocationChanged(location: Location) {
        mMyLatitude = location.latitude
        mMyLongitude = location.longitude
        mAzimuthTheoretical = calculateTheoreticalAzimuth()

        Toast.makeText(this, "lat $mMyLatitude   lon $mMyLongitude", Toast.LENGTH_SHORT).show()
        if (mAzimuthReal == 0.0) {
            if (calculateDistance() <= DISTANCE_ACCURACY)
                binding.icon.visibility = View.VISIBLE
            else binding.icon.visibility = View.INVISIBLE
        }
        updateDescription()
    }

    override fun onStop() {
        myCurrentAzimuth.stop()
        myCurrentLocation.stop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        myCurrentLocation.start()
        myCurrentAzimuth.start()
    }

    private fun setupListeners() {
        myCurrentLocation = MyCurrentLocation(this)
        myCurrentLocation.buildGoogleApiClient(this)
        myCurrentLocation.start()

        myCurrentAzimuth = MyCurrentAzimuth(this, this)
        myCurrentAzimuth.start()
    }

    private fun setupLayout() {
        window.setFormat(PixelFormat.UNKNOWN)
        mSurfaceHolder = binding.surfaceView.holder
        mSurfaceHolder.addCallback(this)
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        TODO("Not yet implemented")
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        if (isCameraOn) {
            camera.stopPreview()
            isCameraOn = false
        }
        if (this::camera.isInitialized) {
            try {
                camera.setPreviewDisplay(mSurfaceHolder)
                camera.startPreview()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        TODO("Not yet implemented")
    }
}