package com.smj.app.utils.session

import android.content.Context
import android.content.SharedPreferences
import com.smj.app.R

object SessionManager {
    //Define Data
    private const val FirebaseId = "firebaseid"
    private const val Address = "address"
    private const val Feature = "featureName"
    private const val Admin = "adminArea"
    private const val SubAdmin = "subAdminArea"
    private const val Locality = "locality"
    private const val PostalCode = "postalCode"
    private const val CountryCode = "countryCode"
    private const val CountryName = "countryName"
    private const val Latitude = "latitude"
    private const val Longitude = "longitude"
    private const val SignIn = "signIn"
    private const val Rejected = "rejected"

    //Save Data
    fun saveFirebaseId(context: Context, data: String?) {
        dataFirebaseId(context, data)
    }
    fun saveAddress(context: Context, data: String?) {
        dataAddress(context, data)
    }
    fun saveFeature(context: Context, data: String?) {
        dataFeature(context, data)
    }
    fun saveAdmin(context: Context, data: String?) {
        dataAdmin(context, data)
    }
    fun saveSubAdmin(context: Context, data: String?) {
        dataSubAdmin(context, data)
    }
    fun saveLocality(context: Context, data: String?) {
        dataLocality(context, data)
    }
    fun savePostalCode(context: Context, data: String?) {
        dataPostalCode(context, data)
    }
    fun saveCountryCode(context: Context, data: String?) {
        dataCountryCode(context, data)
    }
    fun saveCountryName(context: Context, data: String?) {
        dataCountryName(context, data)
    }
    fun saveLatitude(context: Context, data: String?) {
        dataLatitude(context, data)
    }
    fun saveLongitude(context: Context, data: String?) {
        dataLongitude(context, data)
    }
    fun saveSignInBy(context: Context, data: String?) {
        dataSignInBy(context, data)
    }

    fun saveRejected(context: Context, data: Int) {
        dataRejected(context, data)
    }

    //Data Value
    private fun dataFirebaseId(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(FirebaseId, value)
        editor.apply()
    }
    private fun dataAddress(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(Address, value)
        editor.apply()
    }
    private fun dataFeature(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(Feature, value)
        editor.apply()
    }
    private fun dataAdmin(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(Admin, value)
        editor.apply()
    }
    private fun dataSubAdmin(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(SubAdmin, value)
        editor.apply()
    }
    private fun dataLocality(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(Locality, value)
        editor.apply()
    }
    private fun dataPostalCode(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(PostalCode, value)
        editor.apply()
    }
    private fun dataCountryCode(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(CountryCode, value)
        editor.apply()
    }
    private fun dataCountryName(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(CountryName, value)
        editor.apply()
    }
    private fun dataLatitude(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(Latitude, value)
        editor.apply()
    }
    private fun dataLongitude(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(Longitude, value)
        editor.apply()
    }
    private fun dataSignInBy(context: Context, value: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(SignIn, value)
        editor.apply()
    }
    private fun dataRejected(context: Context, value: Int) {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(Rejected, value.toString())
        editor.apply()
    }

    //Get Data
    private fun getDataStringPrivate(context: Context, key: String): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }

    fun getDataString(context: Context, data: String): String? {
        return getDataStringPrivate(context, data)
    }

    // Clear Session
    fun clearData(context: Context){
        val editor = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE).edit()
        editor.clear()
        editor.apply()
    }
}