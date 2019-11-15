package com.example.albo_poc_gps.repository

import android.content.Context
import com.example.albo_poc_gps.data.Coordinates
import com.example.albo_poc_gps.httpRequestHelpers.NotificationBody
import org.json.JSONObject


interface ApiComponent: INotificationHttpRequestComponent, IMovementComponent, ILocationComponent

object ApiRepository: ApiComponent {
    private val notificationRepository: INotificationHttpRequestComponent = NotificationHttpRequestComponent
    private val movementRepository: IMovementComponent = AccelerationComponent
    private val locationRepository: ILocationComponent = LocationComponent

    override fun sendLocation(applicationContext: Context, content: NotificationBody, response: (JSONObject) -> Unit, error: () -> Unit) {
        notificationRepository.sendLocation(applicationContext, content, response, error)
    }

    override fun registerMovement(event: FloatArray, shouldUpdateLocation: (Boolean) -> Unit) {
        movementRepository.registerMovement(event, shouldUpdateLocation)
    }

    override fun status(): String {
        return movementRepository.status()
    }

    override fun registerMovementListener(context: Context, movementComponentListener: MovementComponentListener): Boolean {
        return movementRepository.registerMovementListener(context, movementComponentListener)
    }

    override fun unregisterMovementListener() {
        movementRepository.unregisterMovementListener()
    }

    override fun startLocation(context: Context) {
        locationRepository.startLocation(context)
    }

    override fun getLocation(): Coordinates {
        return locationRepository.getLocation()
    }
}
