package com.eco.beenlovememory.utils

import android.annotation.SuppressLint
import android.provider.MediaStore
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    @SuppressLint("SimpleDateFormat")
    fun getDateWithString(date: String): Triple<Int, Int, Int>? {
        return runCatching {
            val split = date.split("-")
            val year = split[0].toInt()
            val month = split[1].toInt()
            val day = split[2].toInt()
            Triple(year, month, day)
        }.getOrElse { null }
    }

    @SuppressLint("SimpleDateFormat")
    fun getTimeCurrent(): Triple<String, String, String> {
        val simpleDateFormat: DateFormat = SimpleDateFormat("HH-mm-ss")
        return runCatching {
            val format = simpleDateFormat.format(Date(Calendar.getInstance().timeInMillis))
            val split = format.split("-")
            Triple(split[0], split[1], split[2])
        }.getOrElse { Triple("0", "0", "0") }
    }

    @SuppressLint("SimpleDateFormat")
    fun getDiaryDate(date: String): String {
        val simpleDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val simpleDateFormat2: DateFormat = SimpleDateFormat("E, dd-MM-yyyy")
        return runCatching {
            return simpleDateFormat2.format(simpleDateFormat.parse(date)!!)
        }.getOrElse { MediaStore.UNKNOWN_STRING }
    }
//
//    @SuppressLint("SimpleDateFormat")
//    fun getLoveDay(date: String): String {
//        val simpleDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
//        var date1: Date
//        var date2: Date
//        return runCatching {
//            val currentDate = simpleDateFormat.format(Calendar.getInstance().timeInMillis)
//            date1 = simpleDateFormat.parse(date)!!
//            date2 = simpleDateFormat.parse(currentDate)!!
//            val getDiff = date2.time - date1.time
//            var getDaysDiff = getDiff / (24 * 60 * 60 * 1000L)
//            if (Pref.isCalculateOne) {
//                getDaysDiff += 1
//            }
//            getDaysDiff.toString()
//        }.getOrElse { "0" }
//    }
//
//    @SuppressLint("SimpleDateFormat")
//    fun getDateLoveDetails(date: String): ClockModel {
//        val simpleDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
//        if (date == "0") {
//            return ClockModel("0", "0", "0", "0")
//        }
//        val result = runCatching {
//            val createDate = simpleDateFormat.parse(date)!!
//            val calendar = Calendar.getInstance()
//            val time = if (Pref.isCalculateOne) {
//                createDate.time - (24 * 60 * 60 * 1000)
//            } else {
//                createDate.time
//            }
//            calendar.timeInMillis = time
//            val year = calendar[Calendar.YEAR]
//            val month = calendar[Calendar.MONTH] + 1
//            val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
//            val localDateLoveDay: LocalDate = LocalDate.of(year, month, dayOfMonth)
//            val localDateCurrent: LocalDate = LocalDate.now()
//            val period = Period.between(localDateLoveDay, localDateCurrent)
//            val valueYear = period.years
//            val valueMonth = period.months
//            val valueWeek = period.days / 7
//            val valueDay = period.days - (valueWeek * 7)
//            ClockModel(
//                valueYear.toString(),
//                valueMonth.toString(),
//                valueWeek.toString(),
//                valueDay.toString()
//            )
//        }
//        return result.getOrElse { ClockModel("?", "?", "?", "?") }
//    }
//
//    fun getInfoWithDate(date: String): Triple<String, String, Int> {
//        val split = date.split("-")
//        val year = split[0].toInt()
//        val month = split[1].toInt()
//        val day = split[2].toInt()
//        val age = getAge(year)
//        val bow = getBow(day, month)
//        return Triple(age, bow.first, bow.second)
//    }

    private fun getAge(year: Int): String {
        return runCatching {
            val yearCurrent = Calendar.getInstance()[Calendar.YEAR]
            (yearCurrent - year).toString()
        }.getOrElse { "0" }
    }

//    private fun getBow(day: Int, month: Int): Pair<String, Int> {
//        return if (day in 21..31 && month == 3) {
//            Pair(getStringCompat(R.string.bachduong), R.drawable.ic_aries)
//        } else if (day in 1..20 && month == 4) {
//            Pair(getStringCompat(R.string.bachduong), R.drawable.ic_aries)
//        } else if (day in 21..31 && month == 4) {
//            Pair(getStringCompat(R.string.kimnguu), R.drawable.ic_taurus)
//        } else if (day in 1..20 && month == 5) {
//            Pair(getStringCompat(R.string.kimnguu), R.drawable.ic_taurus)
//        } else if (day in 21..31 && month == 5) {
//            Pair(getStringCompat(R.string.songtu), R.drawable.ic_gemini)
//        } else if (day in 1..21 && month == 6) {
//            Pair(getStringCompat(R.string.songtu), R.drawable.ic_gemini)
//        } else if (day in 22..31 && month == 6) {
//            Pair(getStringCompat(R.string.cugiai), R.drawable.ic_cancer)
//        } else if (day in 1..22 && month == 7) {
//            Pair(getStringCompat(R.string.cugiai), R.drawable.ic_cancer)
//        } else if (day in 23..31 && month == 7) {
//            Pair(getStringCompat(R.string.sutu), R.drawable.ic_leo)
//        } else if (day in 1..22 && month == 8) {
//            Pair(getStringCompat(R.string.sutu), R.drawable.ic_leo)
//        } else if (day in 23..31 && month == 8) {
//            Pair(getStringCompat(R.string.xunu), R.drawable.ic_virgo)
//        } else if (day in 1..22 && month == 9) {
//            Pair(getStringCompat(R.string.xunu), R.drawable.ic_virgo)
//        } else if (day in 23..31 && month == 9) {
//            Pair(getStringCompat(R.string.thienbinh), R.drawable.ic_libra)
//        } else if (day in 1..23 && month == 10) {
//            Pair(getStringCompat(R.string.thienbinh), R.drawable.ic_libra)
//        } else if (day in 24..31 && month == 10) {
//            Pair(getStringCompat(R.string.bocap), R.drawable.ic_scorpio)
//        } else if (day in 1..22 && month == 11) {
//            Pair(getStringCompat(R.string.bocap), R.drawable.ic_scorpio)
//        } else if (day in 23..31 && month == 11) {
//            Pair(getStringCompat(R.string.nhanma), R.drawable.ic_sagittarius)
//        } else if (day in 1..21 && month == 12) {
//            Pair(getStringCompat(R.string.nhanma), R.drawable.ic_sagittarius)
//        } else if (day in 22..31 && month == 12) {
//            Pair(getStringCompat(R.string.maket), R.drawable.ic_capricornus)
//        } else if (day in 1..19 && month == 1) {
//            Pair(getStringCompat(R.string.maket), R.drawable.ic_capricornus)
//        } else if (day in 20..31 && month == 1) {
//            Pair(getStringCompat(R.string.baobinh), R.drawable.ic_aquarius)
//        } else if (day in 1..18 && month == 2) {
//            Pair(getStringCompat(R.string.baobinh), R.drawable.ic_aquarius)
//        } else if (day in 19..31 && month == 2) {
//            Pair(getStringCompat(R.string.songngu), R.drawable.ic_pisces)
//        } else if (day in 1..20 && month == 3) {
//            Pair(getStringCompat(R.string.songngu), R.drawable.ic_pisces)
//        } else {
//            Pair("0", -1)
//        }
//    }
}