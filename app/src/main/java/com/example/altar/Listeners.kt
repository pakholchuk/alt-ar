package com.example.altar

import android.location.Location

interface OnLocationChangedListener {
    fun onLocationChanged(location: Location)
}

interface OnAzimuthChangedListener {
    fun onAzimuthChanged(azimuthFrom: Float, azimuthTo: Float)
}