package com.smj.app.ui.offline.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import com.smj.app.R
import com.smj.app.databinding.ActivityOfflineDashBinding
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.ui.main.view.activity.MainActivity
import com.smj.app.ui.main.view.activity.PendingActivity
import com.smj.app.ui.main.view.activity.SuspendActivity
import com.smj.app.ui.pengawas.view.activity.PengawasActivity

class OfflineDashActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOfflineDashBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var usersRefrence: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null

    private var context: Context? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineDashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        firebaseAuth = FirebaseAuth.getInstance()

        binding.swiperefresh.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                if(isNetworkAvailable(this@OfflineDashActivity)) {
                    checkUserCurrent()
                    binding.swiperefresh.isRefreshing = false
                    binding.llProgressBar.preload.visibility = View.VISIBLE
                }
                else{
                    Helper().showToast(context!!.getString(R.string.connection_status), this)
                    binding.swiperefresh.isRefreshing = false
                }
            }, 3000)
        }
    }

    private fun checkUserCurrent() {
        if(firebaseAuth.currentUser?.uid == null){
            val intent = Intent(this@OfflineDashActivity, LoginActivity::class.java)
            NavigationHelper().navigateToActivityFlags(this@OfflineDashActivity, intent)
        }
        else{
            firebaseUser = firebaseAuth.currentUser
            usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

            usersRefrence!!.addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists())
                    {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (context!=null)
                        {
                            if (user?.getStatus() == "pending") {
                                val intent = Intent(this@OfflineDashActivity, PendingActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            if (user?.getStatus() == "suspend") {
                                val intent = Intent(this@OfflineDashActivity, SuspendActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            if (user?.getStatus() == "active") {
                                if (
                                    user.getPosition() == "SPI Produksi"
                                    || user.getPosition() == "ADMIN"
                                    || user.getPosition() == "DIREKTUR"
                                    || user.getPosition() == "Manager Produksi"
                                    || user.getPosition() == "Jr.SPI Produksi"
                                ) {
                                    val intent = Intent(this@OfflineDashActivity, MainActivity::class.java)
                                    NavigationHelper().navigateToActivity(this@OfflineDashActivity, intent)
                                }
                                if (
                                    user.getPosition() == "Foreman Produksi"
                                    || user.getPosition() == "Sr Foreman Produksi"
                                    || user.getPosition() == "Jr Foreman Produksi"
                                    || user.getPosition() == "SPV Produksi"
                                    || user.getPosition() == "Jr SPV Produksi"
                                ) {
                                    val intent = Intent(this@OfflineDashActivity, PengawasActivity::class.java)
                                    NavigationHelper().navigateToActivity(this@OfflineDashActivity, intent)
                                }
                                binding.llProgressBar.preload.visibility = View.GONE
                            }
                        }
                        else{
                            Toast.makeText(this@OfflineDashActivity, "Something wrong..", Toast.LENGTH_LONG).show()
                        }
                    }
                    else{
                        Toast.makeText(this@OfflineDashActivity, "Something wrong..", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }

    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(context: Context) =
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            getNetworkCapabilities(activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false
        }
}