package com.example.albo_poc_gps.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.albo_poc_gps.data.UserPreferences
import org.json.JSONException
import org.json.JSONObject
import android.media.RingtoneManager
import android.media.Ringtone
import com.example.albo_poc_gps.R
import com.example.albo_poc_gps.data.Movement

interface IMovementComponent {
    fun registerMovement(event: FloatArray, shouldUpdateLocation: (Boolean) -> Unit)
    fun status(): String
    fun registerMovementListener(sensorManager: SensorManager, movementComponentListener: MovementComponentListener): Boolean
    fun unregisterMovementListener()
}

interface MovementComponentListener{
    fun movementReached()
    fun movementDetected()
}

object AccelerationComponent : IMovementComponent, SensorEventListener {
    private var mMovementCounter: Movement = Movement()
    private lateinit var mSensorManager: SensorManager
    private lateinit var mListener: MovementComponentListener

    override fun registerMovementListener(sensorManager: SensorManager, onMovementComponentListener: MovementComponentListener): Boolean {
        mSensorManager = sensorManager
        mListener = onMovementComponentListener
        var stepsSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        return if(stepsSensor != null){
            mSensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else{
            false
        }
    }

    override fun unregisterMovementListener() {
        mSensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // required method
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type === Sensor.TYPE_ACCELEROMETER) {
            // Shake detection
            mListener.movementDetected()

            registerMovement(event.values.clone()){
                shouldSendLocation ->
                if(shouldSendLocation) {
                    mListener.movementReached()
                }
            }
        }
    }

    override fun registerMovement(event: FloatArray, shouldUpdateLocation: (Boolean) -> Unit) {
        mMovementCounter.addMovement(event, shouldUpdateLocation)
    }

    override fun status(): String {
        return mMovementCounter.status
    }

}