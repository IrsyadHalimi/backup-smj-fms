package com.smj.app.ui.task.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.smj.app.R
import com.smj.app.databinding.ActivityDetailTaksBinding
import com.smj.app.helper.Helper

class DetailTaksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTaksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTaksBinding.inflate(layoutInflater)
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
    }
}