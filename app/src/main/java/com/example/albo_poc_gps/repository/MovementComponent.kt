package com.example.albo_poc_gps.repository

import android.content.Context
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
}

object AccelerationComponent : IMovementComponent {

    private var movementCounter: Movement = Movement()

    override fun registerMovement(event: FloatArray, shouldUpdateLocation: (Boolean) -> Unit) {
        movementCounter.addMovement(event, shouldUpdateLocation)
    }

    override fun status(): String {
        return movementCounter.status
    }

}