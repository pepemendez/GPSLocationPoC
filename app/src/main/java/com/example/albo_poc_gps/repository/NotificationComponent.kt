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


private const val FCM_API = "https://fcm.googleapis.com/fcm/send"
private const val serverKey = "key=" + "firebase_key"
private const val contentType = "application/json"

interface INotificationComponent {
    fun sendLocation(applicationContext: Context, latitude: Double, longitude: Double, response: (JSONObject) -> Unit, error: () -> Unit)
}

object NotificationComponent : INotificationComponent {

    private fun playSound(applicationContext: Context){
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendNotification(applicationContext: Context, notification: JSONObject, f: (JSONObject) -> Unit, error: () -> Unit){
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                f(response)
            },
            Response.ErrorListener {
                error()
            }) {
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Authorization"] = serverKey
                    params["Content-Type"] = contentType
                    return params
            }
        }

        val requestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(jsonObjectRequest)
    }

    override fun sendLocation(applicationContext: Context, latitude: Double, longitude: Double, f: (JSONObject) -> Unit, error: () -> Unit){
        playSound(applicationContext)

        val topic = applicationContext.getString(R.string.notification_topic) //topic has to match what the receiver subscribed to
        val notification = JSONObject()
        val notificationBody = JSONObject()

        try {
            notificationBody.put("title", "Albo poc")
            notificationBody.put("message", "($latitude, $longitude)")   //Enter your notification message
            notificationBody.put("uuid", UserPreferences().getUUID(applicationContext))
            notification.put("to", topic)
            notification.put("data", notificationBody)
            Log.e("TAG", "try")
            sendNotification(applicationContext, notification, f, error)
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
            error()
        }
    }
}