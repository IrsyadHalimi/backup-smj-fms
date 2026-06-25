package com.smj.app.ui.auth.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.databinding.ActivitySignupBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.main.view.activity.MainActivity
import com.smj.app.ui.main.view.activity.PendingActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var usersRefrence: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null
    private lateinit var mDbRef: DatabaseReference

    private var resources: Resources? = null
    private var context: Context? = null
    private var firebaseUserID: String = ""

    var email: String? = ""
    @SuppressLint("MissingSuperCall")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        val intent = Intent(this, LoginActivity::class.java)
        NavigationHelper().navigateToActivity(this, intent)
    }

    @SuppressLint("SourceLockedOrientationActivity", "PrivateResource",
        "ClickableViewAccessibility"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        firebaseAuth = FirebaseAuth.getInstance()

        if(intent.getStringExtra("putEmail")?.isNotEmpty() == true){
            email = intent.getStringExtra("putEmail")
        }

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

        binding.etFullname.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }

        binding.etEmail.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }

        binding.etEmail.setText(email.toString())

        binding.etPassword.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }

        binding.signup.setOnClickListener {
            signUp()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun signUp() {
        val fullName: String = binding.etFullname.text.toString()
        val codeCountry = binding.phoneTv.selectedCountryCodeWithPlus
        val phoneNumber: String = binding.etPhoneNumber.text?.trim().toString()
        val id: String = binding.etEmail.text.toString()
        val email: String = binding.etEmail.text.toString().plus("@gmail.com")
        val password: String = binding.etPassword.text.toString()

        if (fullName == "")
        {
            Toast.makeText(this@SignupActivity, context!!.getString(R.string.please_write_fullname), Toast.LENGTH_LONG).show()
        }
        if (email == "")
        {
            Toast.makeText(this@SignupActivity, context!!.getString(R.string.please_write_email), Toast.LENGTH_LONG).show()
        }
        if (password == "")
        {
            Toast.makeText(this@SignupActivity, context!!.getString(R.string.please_write_password), Toast.LENGTH_LONG).show()
        }
        if (phoneNumber == "")
        {
            Toast.makeText(this@SignupActivity, context!!.getString(R.string.please_write_phone), Toast.LENGTH_LONG).show()
        }
        if (fullName != "" && email != "" && password != "" && phoneNumber != ""){
            binding.llProgressBar.preload.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful)
                    {
                        firebaseUserID = firebaseAuth.currentUser!!.uid
                        val refUser = FirebaseDatabase.getInstance().reference
                        val userKey = refUser.push().key
                        mDbRef = refUser.child("Users").child(firebaseUserID!!)

                        val hashMap = HashMap<String, Any>()
                        hashMap["fullName"] = fullName
                        hashMap["idNumber"] = id
                        hashMap["email"] = email
                        hashMap["phoneNumber"] = codeCountry+cleanPhoneNumber(phoneNumber.replace("-", ""))
                        hashMap["photo"] = "https://firebasestorage.googleapis.com/v0/b/siapnikah-app.appspot.com/o/profile.png?alt=media&token=9dabafa6-50b0-4472-aa22-a5e06b673674"
                        hashMap["status"] = "pending"
                        hashMap["position"] = "admin"
                        hashMap["gender"] = ""
                        hashMap["birthDay"] = ""
                        hashMap["latitude"] = ""
                        hashMap["longitude"] = ""
                        hashMap["userKey"] = userKey!!
                        hashMap["uid"] = firebaseUserID
                        hashMap["createBy"] = firebaseUserID
                        hashMap["updateBy"] = ""
                        hashMap["createDate"] =  DateHelper().todayTime()
                        hashMap["updateDate"] =  ""

                        mDbRef.updateChildren(hashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful)
                                {
                                    firebaseAuth = FirebaseAuth.getInstance()
                                    firebaseUser = firebaseAuth.currentUser
                                    usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

                                    usersRefrence!!.addValueEventListener(object :
                                        ValueEventListener {
                                        @SuppressLint("SetTextI18n")
                                        override fun onDataChange(p0: DataSnapshot) {
                                            if (p0.exists())
                                            {
                                                val user: Users? = p0.getValue(Users::class.java)

                                                if (context!=null)
                                                {
                                                    if (user?.getStatus() == "pending") {
                                                        val intent = Intent(this@SignupActivity, PendingActivity::class.java)
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                    if (user?.getStatus() == "active") {
                                                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(p0: DatabaseError) {

                                        }
                                    })
                                }
                            }
                    }
                    else {
                        binding.llProgressBar.preload.visibility = View.GONE
                        if (task.exception!!.message.toString() == "The email address is already in use by another account.") {
                            binding.registerFailedTv.text = "ID Number has been used by other accounts"
                        }else {
                            binding.registerFailedTv.text = task.exception!!.message.toString()
                        }
                    }
                }
        }
    }

    private fun cleanPhoneNumber(phoneNumber: String): Any {
        val length = phoneNumber.length
        if ((phoneNumber[0] == '0')) {
            if (length > 1) {
                return phoneNumber.substring(1, length)
            }
        }
        else{
            return phoneNumber
        }
        return false
    }
}