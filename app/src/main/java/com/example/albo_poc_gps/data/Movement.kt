package com.example.albo_poc_gps.data

import android.hardware.SensorManager
import kotlin.math.roundToInt

class Movement{
    private lateinit var mGravity: FloatArray
    private var mAccel: Double = 0.toDouble()
    private var mAccelCurrent: Double = 0.toDouble()
    private var mAccelLast: Double = 0.toDouble()

    private var lastTimeStamp: Long = 0.toLong()
    private var currentTimeStamp: Long = 0.toLong()

    private var started = false
    private var timeelapsed: Double = 0.toDouble()

    private var SAMPLE_SIZE = 50.0 // change this sample size as you want, higher is more precise but slow measure.
    private var THRESHOLD = 0.9 // change this threshold as you want, higher is more spike movement

    private var hitCount = 0
    private var hitSum = 0.0
    private var hitResult = 0.0

    init {
        mAccel = 0.00
        mAccelCurrent = SensorManager.GRAVITY_EARTH.toDouble()
        mAccelLast = SensorManager.GRAVITY_EARTH.toDouble()
    }

    fun addMovement(event: FloatArray, sendLocation: () -> Unit){
        mGravity = event

        // Shake detection
        val x = mGravity[0].toDouble()
        val y = mGravity[1].toDouble()
        val z = mGravity[2].toDouble()
        mAccelLast = mAccelCurrent
        mAccelCurrent = Math.sqrt(x * x + y * y + z * z)
        val delta = mAccelCurrent - mAccelLast
        mAccel = mAccel * 0.9f + delta

        currentTimeStamp = System.currentTimeMillis()

        if (hitCount <= SAMPLE_SIZE) {
            hitCount++
            hitSum += Math.abs(mAccel)
        } else {
            hitResult = hitSum / SAMPLE_SIZE

            if (hitResult > THRESHOLD) {
                if (!started) {
                    started = true
                } else {
                    timeelapsed += (currentTimeStamp.toDouble() - lastTimeStamp.toDouble()) / 1000.toDouble()
                }
                lastTimeStamp = currentTimeStamp
            } else {
                if (started) {
                    started = false
                    timeelapsed += (currentTimeStamp.toDouble() - lastTimeStamp.toDouble()) / 1000.toDouble()
                }
            }

            if (timeelapsed > 2) {
                sendLocation()
                val integerPart = timeelapsed.roundToInt()
                timeelapsed -= integerPart
            }

            hitCount = 0
            hitSum = 0.0
            hitResult = 0.0
        }
    }

    fun print(): String{
        if(started) {
            return "Walking\n$timeelapsed"
        }
        else{
            return "Stop Walking \nLasted: $timeelapsed"
        }

    }
}