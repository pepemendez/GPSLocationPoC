package com.example.albo_poc_gps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.albo_poc_gps.repository.ApiRepository
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.albo_poc_gps.httpRequestHelpers.NotificationBody
import com.example.albo_poc_gps.repository.MovementComponentListener
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), MovementComponentListener {

    private lateinit var tvLocation: TextView
    private val _repository = ApiRepository

    private val _requestPermissionsRequestCode = 420

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvLocation = findViewById(R.id.tv_location)

        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.notification_topic))
    }

    override fun onResume() {
        super.onResume()
        getLastLocation()

        if(_repository.registerMovementListener(this, this)){
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

    private fun sendLocationHttpRequest(params: NotificationBody){
        _repository.sendLocation(this.applicationContext, params, ::sendLocationResponse, ::connectionError)
    }

    override fun movementReached(){
        val location = _repository.getLocation()
        val params = NotificationBody(getString(R.string.app_name), "(${location.latitude}, ${location.longitude})")
        sendLocationHttpRequest(params)
    }

    override fun movementDetected() {
        val text = _repository.status()
        if(text.count() > 0)
            tvLocation.text = text
    }

    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                _repository.startLocation(this)
            } else {
                val snack = Snackbar.make(tvLocation,R.string.turn_on_location,Snackbar.LENGTH_INDEFINITE)
                snack.setAction(android.R.string.ok) {openLocationSettings()}
                snack.show()
            }
        } else {
            requestPermissions()
        }
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
        if ((requestCode == _requestPermissionsRequestCode) && (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            val snack = Snackbar.make(tvLocation,R.string.permission_rationale,Snackbar.LENGTH_INDEFINITE)
            snack.setAction(android.R.string.ok) {requestPermissions()}
            snack.show()
        }
        else{
            getLastLocation()
        }
    }
}