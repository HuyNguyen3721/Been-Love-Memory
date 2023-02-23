package com.eco.beenlovememory.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*


class TimeUtil {
    fun convertTime(time: Int): String {
        val second = (time / 1000 % 60).toLong()
        val minute = (time / (1000 * 60) % 60).toLong()
        val hour = (time / (1000 * 60 * 60) % 24).toLong()
        return if (hour > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second)
        } else {
            String.format(Locale.US, "%02d:%02d", minute, second)
        }
    }

    companion object {
        fun getDurationString(seconds: Long): String {
            var seconds = seconds
            val hours = seconds / 3600
            val minutes = seconds % 3600 / 60
            seconds = seconds % 60
            return if (hours <= 0) {
                twoDigitString(minutes) + ":" + twoDigitString(seconds)
            } else twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(
                seconds
            )
        }

        @SuppressLint("SimpleDateFormat")
        fun convertDuration(duration: Long): String {
            return if (duration >= 3600000) SimpleDateFormat("hh:mm:ss").format(duration) else SimpleDateFormat(
                "mm:ss"
            ).format(duration)
        }

        private fun twoDigitString(number: Long): String {
            if (number == 0L) {
                return "00"
            }
            return if (number / 10 == 0L) {
                "0$number"
            } else number.toString()
        }

        fun formatDecimal(x: Double): String {
            var xWhole = x.toInt()
            var xFrac = (100 * (x - xWhole) + 0.5).toInt()
            if (xFrac >= 100) {
                xWhole++ //Round up
                xFrac -= 100 //Now we need the remainder after the round up
                if (xFrac < 10) {
                    xFrac *= 10 //we need a fraction that is 2 digits long
                }
            }
            return if (xFrac < 10) {
                if (xWhole < 10) "0$xWhole.0$xFrac" else "$xWhole.0$xFrac"
            } else {
                if (xWhole < 10) "0$xWhole.$xFrac" else "$xWhole.$xFrac"
            }
        }
    }

    fun checkCurrentYear(smsTimeInMilis: Long): Boolean {
        val smsTime = Calendar.getInstance()
        smsTime.timeInMillis = smsTimeInMilis
        val now = Calendar.getInstance()
        return now[Calendar.YEAR] == smsTime[Calendar.YEAR]
    }

    fun checkDateIsToday(smsTimeInMilis: Long): Boolean {
        val smsTime = Calendar.getInstance()
        smsTime.timeInMillis = smsTimeInMilis
        val now = Calendar.getInstance()
        return now[Calendar.DATE] == smsTime[Calendar.DATE]
    }

}
