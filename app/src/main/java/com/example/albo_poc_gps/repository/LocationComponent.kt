package com.example.albo_poc_gps.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.example.albo_poc_gps.data.Coordinates
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

interface ILocationComponent{
    fun startLocation(context: Context)
    fun getLocation(): Coordinates
}

object LocationComponent: ILocationComponent, OnCompleteListener<Location> {

    private const val locationInterval: Long = 1000
    private const val locationFastestInterval: Long = 500
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mCurrentLocation = Coordinates()

    override fun getLocation(): Coordinates {
        return mCurrentLocation
    }

    @SuppressLint("MissingPermission")
    override fun startLocation(context: Context) {
        Log.wtf("TAG", "getLastLocation $context")
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        mFusedLocationClient.lastLocation.addOnCompleteListener(this)
    }

    @SuppressLint("MissingPermission")
    fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = locationInterval
        mLocationRequest.fastestInterval = locationFastestInterval

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult) {
            Log.wtf("TAG", "mLocationCallback ${location.lastLocation.latitude} ${location.lastLocation.longitude}")
            mCurrentLocation.update(location.lastLocation.latitude, location.lastLocation.longitude)
        }
    }

    override fun onComplete(task: Task<Location>) {
        val location: Location? = task.result
        if (location == null) {
            Log.wtf("TAG", "onComplete location null")
            requestNewLocationData()
        } else {
            Log.wtf("TAG", "onComplete ${location.latitude} ${location.longitude}")
            mCurrentLocation.update(location.latitude, location.longitude)
            requestNewLocationData()
        }
    }

}