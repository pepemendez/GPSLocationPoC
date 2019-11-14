package com.example.albo_poc_gps.repository

import android.content.Context
import android.hardware.SensorManager
import org.json.JSONObject


interface ApiComponent: INotificationComponent, IMovementComponent

object ApiRepository: ApiComponent {
    private val notificationRepository: INotificationComponent = NotificationComponent
    private val movementRepository: IMovementComponent = AccelerationComponent

    override fun sendLocation(applicationContext: Context, latitude: Double, longitude: Double, response: (JSONObject) -> Unit, error: () -> Unit) {
        notificationRepository.sendLocation(applicationContext, latitude, longitude, response, error)
    }

    override fun registerMovement(event: FloatArray, shouldUpdateLocation: (Boolean) -> Unit) {
        movementRepository.registerMovement(event, shouldUpdateLocation)
    }

    override fun status(): String {
        return movementRepository.status()
    }

    override fun registerMovementListener(sensorManager: SensorManager, movementComponentListener: MovementComponentListener): Boolean {
        return movementRepository.registerMovementListener(sensorManager, movementComponentListener)
    }

    override fun unregisterMovementListener() {
        movementRepository.unregisterMovementListener()
    }
}
