package com.smj.app.ui.main.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.databinding.ActivityMainBinding
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.ui.main.view.fragment.*
import com.smj.app.ui.offline.view.activity.OfflineDashActivity
import com.smj.app.ui.pengawas.view.activity.PengawasActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var usersRefrence: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null
    private var context: Context? = null

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.fragments[supportFragmentManager.fragments.size - 1]
        if (currentFragment.tag == "DashboardFragment")
        {
            showAppClosingDialog()
        }
        else {
            val view: View = findViewById(R.id.dashboardFragment)
            view.performClick()
        }
    }

    private val mOnNavigationItemSelectedListener = NavigationBarView.OnItemSelectedListener { menuItem->
        when (menuItem.itemId) {
            R.id.dashboardFragment -> {
                val fragment = DashboardFragment.newInstance()
                addFragment(fragment)
                return@OnItemSelectedListener true
            }
            R.id.contactFragment -> {
                val fragment = ContactFragment()
                addFragment(fragment)
                return@OnItemSelectedListener true
            }
            R.id.taksFragment -> {
                val fragment = TaskFragment()
                addFragment(fragment)
                return@OnItemSelectedListener true
            }
            R.id.productFragment -> {
                val fragment = FleetFragment()
                addFragment(fragment)
                return@OnItemSelectedListener true
            }
            R.id.settingsFragment -> {
                val fragment = SettingsFragment()
                addFragment(fragment)
                return@OnItemSelectedListener true
            }
        }
        false
    }

    @SuppressLint("ResourceType")
    private fun addFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, fragment, fragment.javaClass.simpleName)
            .commit()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        firebaseAuth = FirebaseAuth.getInstance()
        checkUserCurrent()

        if(!isNetworkAvailable(this@MainActivity)){
            val intent = Intent(this@MainActivity, OfflineDashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        if (intent.getStringExtra("fragment").equals("ContactFragment")) {
            binding.navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener)
            val fragment = ContactFragment()
            addFragment(fragment)
            val view: View = findViewById(R.id.contactFragment)
            view.performClick()
        } else {
            binding.navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener)
            val fragment = DashboardFragment.newInstance()
            addFragment(fragment)
        }

    }

    private fun showAppClosingDialog() {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setMessage(HtmlCompat.fromHtml("Do you want to close the application<br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
        dialogBuilder.setPositiveButton("Yes"){ _, _ ->
            finish()
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog,_ ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(alertDialog.window!!.attributes)
        layoutParams.width = 900
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        alertDialog.window!!.attributes = layoutParams
    }

    private fun checkUserCurrent() {
        if(firebaseAuth.currentUser?.uid == null){
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            NavigationHelper().navigateToActivityFlags(this@MainActivity, intent)
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
                                val intent = Intent(this@MainActivity, PendingActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            if (user?.getStatus() == "suspend") {
                                val intent = Intent(this@MainActivity, SuspendActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            if (user?.getStatus() == "active") {
                                if (
                                    user.getPosition() == "Foreman Produksi"
                                    || user.getPosition() == "Sr Foreman Produksi"
                                    || user.getPosition() == "Jr Foreman Produksi"
                                    || user.getPosition() == "SPV Produksi"
                                    || user.getPosition() == "Jr SPV Produksi"
                                ) {
                                    val intent = Intent(this@MainActivity, PengawasActivity::class.java)
                                    NavigationHelper().navigateToActivity(this@MainActivity, intent)
                                }
                                binding.llProgressBar.preload.visibility = View.GONE
                            }
                        }
                        else{
                            binding.llProgressBar.preload.visibility = View.GONE
                            Toast.makeText(this@MainActivity, "Something wrong..", Toast.LENGTH_LONG).show()
                        }
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Toast.makeText(this@MainActivity, "Something wrong..", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    binding.llProgressBar.preload.visibility = View.GONE
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