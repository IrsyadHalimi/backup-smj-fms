package com.smj.app.ui.profile.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.databinding.ActivityProfileBinding
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.utils.session.SessionManager

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private var firebaseUser: FirebaseUser? = null
    private var usersRefrence: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)

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
        binding.tvLogout.setOnClickListener {
            showAppClosingDialog()
        }

        usersRefrence!!.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    val user: Users? = p0.getValue(Users::class.java)
                    if (user?.getPhoto()?.isNotEmpty() == true) {
                        binding.ivProfil.visibility = View.VISIBLE
                        binding.shimmerViewContainer.visibility = View.GONE
                        binding.tvName.text = user.getFullName()
                        binding.tvEmail.text = user.getEmail()
                        binding.tvPhone.text = "ID: ".plus(user.getIdNumber())
                        Glide.with(this@ProfileActivity)
                            .load(user.getPhoto())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(
                                ContextCompat.getDrawable(
                                    this@ProfileActivity,
                                    R.drawable.icon_user_light
                                )
                            )
                            .into(binding.ivProfil)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    private fun showAppClosingDialog() {
//        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
//        dialogBuilder.setCancelable(false)
//        dialogBuilder.setMessage(HtmlCompat.fromHtml("Do you want to close the application?<br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
//        dialogBuilder.setPositiveButton("Yes"){ _, _ ->
//            logout()
//        }
//        dialogBuilder.setNegativeButton("No") { dialog, _ ->
//            dialog.dismiss()
//        }
//
//        val alertDialog = dialogBuilder.create()
//        alertDialog.show()
//
//        val layoutParams = WindowManager.LayoutParams()
//        layoutParams.copyFrom(alertDialog.window!!.attributes)
//        layoutParams.width = 900
//        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
//        alertDialog.window!!.attributes = layoutParams
        val alertBuilder = android.app.AlertDialog.Builder(this)
        alertBuilder.setTitle("Keluar")
        alertBuilder.setMessage("Ingin keluar dari aplikasi ?")
        alertBuilder.setCancelable(false)
        alertBuilder.setPositiveButton("Lanjutkan"){_,_ ->
            logout()
        }
        alertBuilder.setNeutralButton("Batal"){_,_ ->
        }
        alertBuilder.show()

    }

    private fun logout() {
        if(SessionManager.getDataString(this, "signIn").equals("email")) {
            mGoogleSignInClient.signOut().addOnCompleteListener {
                this.let { it ->
                    SessionManager.clearData(it)
                    firebaseAuth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    NavigationHelper().navigateToActivity(this, intent)
                }
            }
        }
        else{
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            NavigationHelper().navigateToActivity(this, intent)
        }
    }
}