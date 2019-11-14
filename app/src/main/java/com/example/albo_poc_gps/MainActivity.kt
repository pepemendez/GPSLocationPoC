package com.example.albo_poc_gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.albo_poc_gps.repository.ApiRepository
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.albo_poc_gps.data.Coordinates
import com.example.albo_poc_gps.data.Movement
import com.example.albo_poc_gps.repository.MovementComponentListener
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity(), MovementComponentListener, OnCompleteListener<Location> {

    private lateinit var tvLocation: TextView
    private val _repository = ApiRepository
    private var sensorManager: SensorManager? = null
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var mCurrentLocation: Coordinates

    private val _locationInterval: Long = 1000
    private val _locationFastetsInterval: Long = 500
    private val _requestPermissionsRequestCode = 420

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvLocation = findViewById(R.id.tv_location)

        mCurrentLocation = Coordinates()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.notification_topic))
        getLastLocation()
    }

    override fun onResume() {
        super.onResume()

        if(_repository.registerMovementListener(sensorManager!!, this)){
            showUserMessage(R.string.sensor_started)
        }
        else{
            showUserMessage(R.string.no_sensor_found)
        }
    }

    override fun onPause() {
        super.onPause()
        _repository.unregisterMovementListener()
    }

    private fun showUserMessage(message: Int){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun sendLocation(){
        if(::mCurrentLocation.isInitialized) {
            _repository.sendLocation(this.applicationContext,
                mCurrentLocation.latitude,
                mCurrentLocation.longitude,
                ::sendLocationResponse,
                ::connectionError
            )
        }
    }

    override fun movementDetected() {
        val text = _repository.status()
        if(text.count() > 0)
            tvLocation.text = text
    }

    private fun sendLocationResponse(response: JSONObject) {
        Log.wtf("TAG", "onResponse: $response")
    }

    private fun connectionError() {
        showUserMessage(R.string.network_error)
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            _requestPermissionsRequestCode
        )
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun openLocationSettings(){
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray) {
        Log.wtf("TAG", "onRequestPermissionResult")
        if ((requestCode == _requestPermissionsRequestCode) && (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            showUserMessage(R.string.permission_denied_explanation)
            requestPermissions()
        }
        else{
            getLastLocation()
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult) {
            Log.wtf("TAG", "mLocationCallback ${location.lastLocation.latitude} ${location.lastLocation.longitude}")
            mCurrentLocation.update(location.lastLocation.latitude, location.lastLocation.longitude)
        }
    }

    override fun onComplete(task: Task<Location>) {
        Log.wtf("TAG", "onComplete")

        val location: Location? = task.result
        if (location == null) {
            showUserMessage(R.string.couldnt_retreive_gps_coordinates)
            requestNewLocationData()
        } else {
            mCurrentLocation.update(location.latitude, location.longitude)
            requestNewLocationData()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = _locationInterval
        mLocationRequest.fastestInterval = _locationFastetsInterval

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        Log.wtf("TAG", "getLastLocation")

        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this)
            } else {
                showUserMessage(R.string.turn_on_location)
                openLocationSettings()
            }
        } else {
            requestPermissions()
        }
    }
}