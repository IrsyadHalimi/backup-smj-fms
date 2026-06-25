package com.smj.app.helper

import android.app.Activity
import android.content.Intent

class NavigationHelper {
    fun navigateToActivity(context: Activity, intent: Intent) {
        context.startActivity(intent)
        context.finish()
    }
    fun navigateToActivityFlags(context: Activity, intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        context.startActivity(intent)
        context.finish()
    }
    fun navigateToActivityCallback(context: Activity, intent: Intent) {
        context.startActivity(intent)
    }
}