package com.example.albo_poc_gps.repository

import android.content.Context
import org.json.JSONObject


interface ApiComponent: INotificationComponent

object ApiRepository: ApiComponent {
    private val repository: INotificationComponent = NotificationComponent

    override fun sendLocation(applicationContext: Context, latitude: Double, longitude: Double, response: (JSONObject) -> Unit, error: () -> Unit) {
        repository.sendLocation(applicationContext, latitude, longitude, response, error)
    }
}
