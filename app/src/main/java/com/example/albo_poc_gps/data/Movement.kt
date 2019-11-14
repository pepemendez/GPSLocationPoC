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
    private var timeElapsed: Double = 0.toDouble()

    private val SAMPLESIZE = 50.0 // change this sample size as you want, higher is more precise but slow measure.
    private val THRESHOLD = 0.9 // change this threshold as you want, higher is more spike movement
    private val COEFICIENT = 0.9f
    private val MAXTIMEELAPSED = 2.0
    private var hitCount = 0
    private var hitSum = 0.0
    private var hitResult = 0.0

    val status: String get(){
        return if(started) {
            "Walking\n$timeElapsed"
        } else{
            "Stop Walking \nLasted: $timeElapsed"
        }
    }

    init {
        mAccel = 0.00
        mAccelCurrent = SensorManager.GRAVITY_EARTH.toDouble()
        mAccelLast = SensorManager.GRAVITY_EARTH.toDouble()
    }

    fun addMovement(event: FloatArray, shouldUpdateLocation: (Boolean) -> Unit){
        mGravity = event

        // Shake detection
        val x = mGravity[0].toDouble()
        val y = mGravity[1].toDouble()
        val z = mGravity[2].toDouble()

        mAccelLast = mAccelCurrent
        mAccelCurrent = Math.sqrt(x * x + y * y + z * z)

        val delta = mAccelCurrent - mAccelLast
        mAccel = mAccel * COEFICIENT + delta

        currentTimeStamp = System.currentTimeMillis()

        if (hitCount <= SAMPLESIZE) {
            hitCount++
            hitSum += Math.abs(mAccel)
        } else {
            hitResult = hitSum / SAMPLESIZE

            if (hitResult > THRESHOLD) {
                if (!started) {
                    started = true
                } else {
                    timeElapsed += (currentTimeStamp.toDouble() - lastTimeStamp.toDouble()) / 1000.toDouble()
                }
                lastTimeStamp = currentTimeStamp
            } else {
                if (started) {
                    started = false
                    timeElapsed += (currentTimeStamp.toDouble() - lastTimeStamp.toDouble()) / 1000.toDouble()
                }
            }

            if (timeElapsed >= MAXTIMEELAPSED) {
                shouldUpdateLocation(true)
                val integerPart = timeElapsed.roundToInt()
                timeElapsed -= integerPart
            }

            hitCount = 0
            hitSum = 0.0
            hitResult = 0.0
        }
    }
}