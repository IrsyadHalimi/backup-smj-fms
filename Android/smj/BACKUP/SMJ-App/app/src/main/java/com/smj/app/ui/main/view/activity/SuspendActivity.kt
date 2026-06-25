package com.smj.app.ui.main.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smj.app.databinding.ActivitySuspendBinding
import com.smj.app.helper.NavigationHelper

class SuspendActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySuspendBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuspendBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.tvHelp.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val uri = Uri.parse("smsto:6285183134278")
        val sendIntent = Intent(Intent.ACTION_SENDTO, uri)

        val shareIntent = Intent.createChooser(sendIntent, null)
        NavigationHelper().navigateToActivityCallback(this, shareIntent)
    }
}