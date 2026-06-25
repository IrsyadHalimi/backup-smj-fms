package com.smj.app.ui.auth.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.smj.app.R
import com.smj.app.databinding.ActivityForgotBinding
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper

class ForgotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotBinding

    override fun onBackPressed() {
        val intent = Intent(this, LoginActivity::class.java)
        NavigationHelper().navigateToActivity(this, intent)
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.let {
            ContextCompat.getColor(
                it,
                com.google.android.material.R.color.mtrl_btn_transparent_bg_color
            )
        }.let {
            Helper().changeStatusBarColor(
                it, true,
                this
            )
        }

        this.let {
            ContextCompat.getColor(
                it,
                R.color.white
            )
        }.let {
            Helper().changeStatusNavColor(
                it, true,
                this
            )
        }

        binding.etEmail.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }

        binding.reset.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            FirebaseAuth.getInstance().sendPasswordResetEmail(binding.etEmail.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.llProgressBar.preload.visibility = View.GONE
                        binding.emailHint.visibility = View.GONE
                        binding.reset.visibility = View.GONE
                        binding.tvNotification.visibility = View.VISIBLE
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        val intent = Intent(this, SignupActivity::class.java)
                        intent.putExtra("putEmail", binding.etEmail.text.toString())
                        NavigationHelper().navigateToActivity(this, intent)
                    }
                }
        }
    }
}