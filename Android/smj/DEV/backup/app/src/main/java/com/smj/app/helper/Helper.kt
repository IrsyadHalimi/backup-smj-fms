package com.smj.app.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import java.text.SimpleDateFormat
import java.util.*

class Helper {

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showLoadingView(preload: View) {
        preload.let { preload.visibility = View.VISIBLE }
    }

    fun stopLoadingView(preload: View) {
        preload.let { preload.visibility = View.GONE }
    }
    fun showToast(msg: String, context: Activity) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun processError(msg: String?, context: Activity) {
        if (msg != null) {
            if(msg.isNotEmpty()) {
                this.showToast("Error:$msg", context)
            }
        }
    }

    fun processNotified(msg: String?, context: Activity) {
        if (msg != null) {
            if(msg.isNotEmpty()) {
                this.showToast("Notified:$msg", context)
            }
        }
    }

    fun changeStatusBarColor(color: Int, isLight: Boolean, activity: Activity) {
        (activity as AppCompatActivity?)!!.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        (activity as AppCompatActivity?)!!.window.statusBarColor = color

        WindowInsetsControllerCompat((activity as AppCompatActivity?)!!.window, (activity as AppCompatActivity?)!!.window.decorView).isAppearanceLightStatusBars = isLight
    }

    fun changeStatusNavColor(color: Int, isLight: Boolean, activity: Activity) {
        (activity as AppCompatActivity?)!!.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        (activity as AppCompatActivity?)!!.window.statusBarColor = color

        WindowInsetsControllerCompat((activity as AppCompatActivity?)!!.window, (activity as AppCompatActivity?)!!.window.decorView).isAppearanceLightNavigationBars = isLight
    }

    fun capitalizeWords(data: String?): String? {
        return data?.split(" ")?.joinToString(" ") { it ->
            it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
        }
    }

    fun emailMask(email: String): String {
        var maskEmail = "*****"
        val at = email.indexOf("@")
        if (at > 2) {
            val data = email.split("@")
            val maskLen = (data[0].length / 2).coerceAtLeast(2).coerceAtMost(4)
            val start = (data[0].length - maskLen) / 2
            val regex1 = """(?:\G(?!^)|(?<=^[^@]{2}))[^@](?!\.[^.]+$)""".toRegex()
            val regex2 = """(?:\G(?!^)|(?<=^[^@]{0}))[^@]{4}(?!\.[^.]+$)""".toRegex()
            val first = data[0].substring(0, start+maskLen+2).replace(regex1, "*")
            val last = data[0].substring(start+maskLen-1).replace(regex2, "*")
            maskEmail = first+last+"@"+data[1]
        }
        return maskEmail
    }

    fun phoneMask(phone: String?): String {
        var maskPhone = "XXXX XXXX XXXX"
        if (phone != null) {
            if (phone.length > 10) {
                val maskLen = (phone.length / 2).coerceAtLeast(2).coerceAtMost(4)
                val start = (phone.length - maskLen) / 2
                val regex1 = """(?:\G(?!^)|(?<=^[^@]{4}))[^@](?!\.[^.]+$)""".toRegex()
                val regex2 = """(?:\G(?!^)|(?<=^[^@]{0}))[^@]{4}(?!\.[^.]+$)""".toRegex()
                val first = phone.substring(0, start+maskLen).replace(regex1, "x")
                val last = phone.substring(start+maskLen-3).replace(regex2, "x")
                maskPhone = first+last
            }
        }
        return maskPhone
    }

    @SuppressLint("SimpleDateFormat")
    fun dateTimeNow(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val currentDate = sdf.format(Date())
        return currentDate
    }
}