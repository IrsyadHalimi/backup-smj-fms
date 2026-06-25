package com.smj.app.ui.main.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.databinding.ActivityPendingBinding
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.ui.pengawas.view.activity.PengawasActivity

class PendingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPendingBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var usersRefrence: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null
    private var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        firebaseAuth = FirebaseAuth.getInstance()
//        binding.tvUser.text = firebaseAuth.currentUser?.uid.toString()

        binding.tvHelp.setOnClickListener {
            sendMessage()
        }

        binding.swiperefresh.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                checkUserCurrent()
                binding.swiperefresh.isRefreshing = false
                binding.llProgressBar.preload.visibility = View.VISIBLE
            }, 3000)
        }
    }

    private fun sendMessage() {
        val uri = Uri.parse("smsto:6285183134278")
        val sendIntent = Intent(Intent.ACTION_SENDTO, uri)

        val shareIntent = Intent.createChooser(sendIntent, null)
        NavigationHelper().navigateToActivityCallback(this, shareIntent)
    }

    private fun checkUserCurrent() {
        if(firebaseAuth.currentUser?.uid == null){
            val intent = Intent(this@PendingActivity, LoginActivity::class.java)
            NavigationHelper().navigateToActivityFlags(this@PendingActivity, intent)
        }
        else{
            firebaseUser = firebaseAuth.currentUser
            usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseAuth.currentUser?.uid.toString())

            usersRefrence!!.addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists())
                    {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (context!=null)
                        {
                            if (user?.getStatus() == "pending") {
                                val intent = Intent(this@PendingActivity, PendingActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            if (user?.getStatus() == "suspend") {
                                val intent = Intent(this@PendingActivity, SuspendActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            if (user?.getStatus() == "active") {
                                if (
                                    user.getPosition() == "SPI Produksi"
                                    || user.getPosition() == "Manager HRGA"
                                    || user.getPosition() == "DIREKTUR"
                                    || user.getPosition() == "Manager Produksi"
                                    || user.getPosition() == "Jr.SPI Produksi"
                                    || user.getPosition().equals("MT MCC")
                                    || user.getPosition().equals("MCC Field")
                                    || user.getPosition().equals("Jr.Foreman MCC")
                                    ) {
                                    val intent = Intent(this@PendingActivity, MainActivity::class.java)
                                    NavigationHelper().navigateToActivity(this@PendingActivity, intent)
                                }
                                if (
                                    user.getPosition() == "Foreman Produksi"
                                    || user.getPosition() == "Sr Foreman Produksi"
                                    || user.getPosition() == "Jr Foreman Produksi"
                                    || user.getPosition() == "SPV Produksi"
                                    || user.getPosition() == "Jr SPV Produksi"
                                    ) {
                                    val intent = Intent(this@PendingActivity, PengawasActivity::class.java)
                                    NavigationHelper().navigateToActivity(this@PendingActivity, intent)
                                }
                                binding.llProgressBar.preload.visibility = View.GONE
                            }
                        }
                        else{
                            Toast.makeText(this@PendingActivity, "Something wrong..", Toast.LENGTH_LONG).show()
                        }
                    }
                    else{
                        Toast.makeText(this@PendingActivity, "Something wrong..", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }
}