package com.example.albo_poc_gps.data

import android.app.Activity
import android.content.Context
import com.example.albo_poc_gps.R
import java.util.*

class UserPreferences{
    fun getUUID(c: Context): String{
        val sharedPreferences = c.getSharedPreferences(c.getString(R.string.app_name), Activity.MODE_PRIVATE)
        var value = sharedPreferences.getString(c.getString(R.string.resource_device_id), "")
        if (value.isEmpty()) {
            value = UUID.randomUUID().toString()
            val editor = sharedPreferences.edit()
            editor.putString(c.getString(R.string.resource_device_id), value)
            editor.commit()
        }
        return value
    }
}