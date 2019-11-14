package com.example.albo_poc_gps.data

class Coordinates{
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    val latitude: Double get() = mLatitude
    val longitude: Double get() = mLongitude

    fun update(Latitude: Double, Longitude: Double){
        mLatitude = Latitude
        mLongitude = Longitude
    }
}