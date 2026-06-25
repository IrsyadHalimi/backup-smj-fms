package com.smj.app.helper

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DateHelper {
    private var calendar: Calendar? = null
    private var year = 0
    private var month = 0
    private var day = 0
    private var date: String? = null

    fun indonesianToday(): String {
        calendar = Calendar.getInstance()
        year = calendar!!.get(Calendar.YEAR)
        month = calendar!!.get(Calendar.MONTH)
        day = calendar!!.get(Calendar.DAY_OF_MONTH)
        val c = Calendar.getInstance()
        c[year, month] = day
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("dd MMM yyyy")
        val date = sdf.format(c.time)
        val Day = String.format("%tA", c)
        return "$Day, $date"
    }

    fun timeStimeToDateEn(time: Int): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time * 1000L
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(cal.time)
    }

    fun timeStimeToDateId(time: Int): String? {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time * 1000L
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("dd MMM yyyy")
        date = if (time > 0) {
            sdf.format(cal.time)
        } else {
            "-"
        }
        return date
    }

    fun timeStimeToDateTimeId(time: Int): String? {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time * 1000L
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("dd MMM yyyy HH:mm")
        date = if (time > 0) {
            sdf.format(cal.time)
        } else {
            "-"
        }
        return date
    }

    fun timeStimeToFullDateEn(time: Int): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time * 1000L
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("yyyy MMM dd")
        val date = sdf.format(cal.time)
        val Day = String.format("%tA", cal)
        return "$Day, $date"
    }

    fun timeStimeToFullDate(time: Int): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time * 1000L
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = sdf.format(cal.time)
        val Day = String.format("%tA", cal)
        return "$Day, $date"
    }
    fun timeStimeToDateTime(time: Int): String {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = time * 1000L
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = sdf.format(cal.time)
        val Day = String.format("%tA", cal)
        return "$date"
    }

    fun timeToDateTime(time: Int): String {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = time * 1000L
        @SuppressLint("SimpleDateFormat")
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
        val date = sdf.format(cal.time)
        return "$date"
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun timeStimeToDateStringId(date: String): String {
        val getDate = date
        val fmt = SimpleDateFormat("yyyy-MM-dd")
        val fmt2 = SimpleDateFormat("dd MMMM yyyy")
        return try {
            val dayDate = fmt.parse(getDate)
            fmt2.format(dayDate!!)
        } catch (pe: ParseException) {
            "Date"
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun timeStimeToTimeStringId(date: String): String {
        val getDate = date
//        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val fmt = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val fmt2 = SimpleDateFormat("HH:mm:ss")
        return try {
            val dayTime = fmt.parse(getDate)
            fmt2.format(dayTime!!)
        } catch (pe: ParseException) {
            "Time"
        }
    }

    fun dateToTimeStime(dataDate: String): Long {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val pasTime = dateFormat.parse(dataDate)
        return pasTime?.time ?: 0
    }

    fun timeStimeToDate(dataDate1: String, dataDate2: String): String {
        var convTime: String? = null

        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val pasTime1 = dateFormat.parse(dataDate1)
        val pasTime2 = dateFormat.parse(dataDate2)
        val dateDiff = pasTime1?.time?.let { pasTime2?.time?.minus(it) }!!.toLong()

        val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
        val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
        if (minute < 60) {
            val sdf = SimpleDateFormat("mm")
            convTime = "00:"+sdf.format(dateDiff)
        } else if (hour < 24) {
            val sdf = SimpleDateFormat("mm")
            convTime = "$hour:"+sdf.format(dateDiff)
        }
        return convTime.toString()
    }

    fun timeStimeToDate2(timeStime: Long): String {
        var convTime: String? = null

        val minute: Long = TimeUnit.MILLISECONDS.toMinutes(timeStime)
        val hour: Long = TimeUnit.MILLISECONDS.toHours(timeStime)
        if (minute < 60) {
            val sdf = SimpleDateFormat("mm")
            convTime = "00:" + sdf.format(timeStime)
        } else if (hour < 24) {
            val sdf = SimpleDateFormat("HH")
            convTime = "$hour:" + sdf.format(timeStime)
        }
        return convTime.toString()
    }

    fun dateToTimeStime(dataDate1: String, dataDate2: String): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val pasTime1 = dateFormat.parse(dataDate1)
        val pasTime2 = dateFormat.parse(dataDate2)
        val dateDiff = pasTime1?.time?.let { pasTime2?.time?.minus(it) }!!.toLong()

        return dateDiff.toString()
    }

    fun covertTimeToText2(dataDate1: String?, dataDate2: String?): String? {
        var convTime: String? = null
        val prefix = ""
        val suffix = "Ago"
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val pasTime = dateFormat.parse(dataDate1)
            val nowTime = dateFormat.parse(dataDate2)
            val dateDiff = nowTime.time - pasTime.time
            val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
            val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
            val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
            val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
            if (second < 60) {
                convTime = "$second Seconds $suffix"
            } else if (minute < 60) {
                convTime = "$minute Minutes $suffix"
            } else if (hour < 24) {
                convTime = "$hour Hours $suffix"
            } else if (day >= 7) {
                convTime = if (day > 360) {
                    (day / 360).toString() + " Years " + suffix
                } else if (day > 30) {
                    (day / 30).toString() + " Months " + suffix
                } else {
                    (day / 7).toString() + " Week " + suffix
                }
            } else if (day < 7) {
                convTime = "$day Days $suffix"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.e("ConvTimeE", e.message!!)
        }
        return convTime
    }

    @SuppressLint("SimpleDateFormat", "NewApi")
    @RequiresApi(Build.VERSION_CODES.N)
    fun convertToCustomFormat(dateStr: String?): String {
        val utc = TimeZone.getTimeZone("UTC")
        val sourceFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
        val destFormat = SimpleDateFormat("dd-MMM-YYYY HH:mm aa")
        sourceFormat.timeZone = utc
        val convertedDate = sourceFormat.parse(dateStr)
        return destFormat.format(convertedDate)
    }

    @SuppressLint("SimpleDateFormat")
    fun today(): String{
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val cal = Calendar.getInstance(Locale.ENGLISH)
        val date = sdf.format(cal.time)
        val day = String.format("%tA", cal)
        return "$day $date"
    }
    fun today2(): String{
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val cal = Calendar.getInstance(Locale.ENGLISH)
        val date = sdf.format(cal.time)
        val day = String.format("%tA", cal)
        return "$date"
    }

    fun todayTime(): Int {
        return (Date().time / 1000).toInt()
    }

    fun covertTimeToText(dataDate: String?): String? {
        var convTime: String? = null
        val prefix = ""
        val suffix = "Ago"
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val pasTime = dateFormat.parse(dataDate)
            val nowTime = Date()
            val dateDiff = nowTime.time - pasTime.time
            val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
            val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
            val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
            val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
            if (second < 60) {
                convTime = "$second Seconds $suffix"
            } else if (minute < 60) {
                convTime = "$minute Minutes $suffix"
            } else if (hour < 24) {
                convTime = "$hour Hours $suffix"
            } else if (day >= 7) {
                convTime = if (day > 360) {
                    (day / 360).toString() + " Years " + suffix
                } else if (day > 30) {
                    (day / 30).toString() + " Months " + suffix
                } else {
                    (day / 7).toString() + " Week " + suffix
                }
            } else if (day < 7) {
                convTime = "$day Days $suffix"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.e("ConvTimeE", e.message!!)
        }
        return convTime
    }

    fun covertTimeToTextCustome(dataDate: String?): String? {
        var convTime: String? = null
        val suffix = "Ago"
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val pasTime = dateFormat.parse(dataDate)
            val nowTime = Date()
            val dateDiff = nowTime.time - pasTime.time
            val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
            val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)

            if (hour < 24) {
                convTime = "Today"
            }
            else if (day < 7) {
                convTime = "This Week"
            }
            else if (day < 30) {
                convTime = "This Months"
            }
            else if (day > 360) {
                convTime = "This Years"
            }

        } catch (e: ParseException) {
            e.printStackTrace()
            Log.e("ConvTimeE", e.message!!)
        }
        return convTime
    }

    fun covertTimeToDateDiff(dataDate: String?): Long? {
        var convTime: Long? = null
        val suffix = "Ago"
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val pasTime = dateFormat.parse(dataDate)
            val nowTime = Date()
            val dateDiff = nowTime.time - pasTime.time
            convTime = dateDiff

        } catch (e: ParseException) {
            e.printStackTrace()
            Log.e("ConvTimeE", e.message!!)
        }
        return convTime
    }

    fun differenceResult(time: Long): String {
        var x: Long = time / 1000

        var seconds = x % 60
        x /= 60
        var minutes = x % 60
        x /= 60
        var hours = (x % 24).toInt()
        x /= 24
        var days = x
//        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        return String.format("%02d:%02d", hours, minutes)
    }

    fun getDateTime(s: String): String? {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.ENGLISH)
            val netDate = Date(s.toLong() * 1000L)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    fun tes(time: Long): String? {
//        val dateDiff = time
//        val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
//        val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
//        val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
//        val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)

        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val netDate = Date(time)
        return sdf.format(netDate)
    }
}