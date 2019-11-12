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
import com.example.albo_poc_gps.data.Movement
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var movementCounter: Movement
    private lateinit var tvLocation: TextView
    private val _repository = ApiRepository
    var sensorManager: SensorManager? = null
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var mCurrentLocation: Location


    private val REQUEST_PERMISSIONS_REQUEST_CODE = 420

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvLocation = findViewById(R.id.tv_location)

        movementCounter = Movement()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.notification_topic))
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    override fun onResume() {
        super.onResume()
        var stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (stepsSensor == null) {
            Toast.makeText(this, R.string.no_sensor_found, Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    fun printmovement(){
        val text = movementCounter.print()
        if(text.count() > 0)
            tvLocation.text = text
    }

    private fun sendLocation(){
        if(::mCurrentLocation.isInitialized) {
            _repository.sendLocation(
                this.applicationContext,
                mCurrentLocation.latitude,
                mCurrentLocation.longitude,
                ::sendLocationResponse,
                ::connectionError
            )
        }
    }

    private fun sendLocationResponse(response: JSONObject) {
        Log.wtf("TAG", "onResponse: $response")
    }

    private fun connectionError() {
        Toast.makeText(this@MainActivity, R.string.network_error, Toast.LENGTH_LONG).show()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type === Sensor.TYPE_ACCELEROMETER) {
            // Shake detection
            movementCounter.addMovement(event.values.clone()){
                sendLocation()
            }

            printmovement()
        }
        if (event.sensor.type === Sensor.TYPE_STEP_COUNTER) {
            tvLocation.text = "${event.values[0]}"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // required method
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.wtf("TAG", "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {
                Toast.makeText(this@MainActivity, getString(R.string.permission_denied_explanation), Toast.LENGTH_LONG).show()
                requestPermissions()
            }
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.wtf("TAG", "mLocationCallback")
            mCurrentLocation = locationResult.lastLocation
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 500

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        Log.wtf("TAG", "getLastLocation")

        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(this@MainActivity, getString(R.string.couldnt_retreive_gps_coordinates), Toast.LENGTH_LONG).show()
                        requestNewLocationData()

                    } else {
                        mCurrentLocation = location
                        requestNewLocationData()
                    }
                }
            } else {
                Toast.makeText(this, R.string.turn_on_location, Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }
}