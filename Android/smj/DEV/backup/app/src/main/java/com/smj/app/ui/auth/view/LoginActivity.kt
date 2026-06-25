package com.smj.app.ui.auth.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.smj.app.R
import com.smj.app.databinding.ActivityLoginBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.viewModel.UserViewModel
import com.smj.app.ui.main.view.activity.MainActivity
import com.smj.app.utils.response.BaseResponseFirebase
import com.smj.app.utils.session.SessionManager


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private var context: Context? = null

    private val viewUserModel by viewModels<UserViewModel>()

    var firebaseUser: FirebaseUser? = null

    lateinit var email: String
    lateinit var givenName: String
    lateinit var displayName: String
    lateinit var photoUrl: String

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
        finishAffinity()
        finishAfterTransition()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("PrivateResource", "SourceLockedOrientationActivity",
        "ClickableViewAccessibility"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this , MainActivity::class.java)
            NavigationHelper().navigateToActivity(this@LoginActivity, intent)
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

        binding.etEmail.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }

        binding.etPassword.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }

        binding.login.setOnClickListener {
            Helper().hideKeyboard(this)
            loginUser()
        }

        binding.google.setOnClickListener{
            binding.llProgressBar.preload.visibility = View.VISIBLE
            signInGoogle()
        }

        binding.tvForgot.setOnClickListener {
//            val intent = Intent(this@LoginActivity, ForgotActivity::class.java)
//            NavigationHelper().navigateToActivityCallback(this@LoginActivity, intent)
            sendMessage()
        }

        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            NavigationHelper().navigateToActivityCallback(this, intent)
        }

        binding.tvHelp.setOnClickListener {
            sendMessage()
        }

        viewUserModel.firebaseDatabaseUserResult?.observe(this){
            when (it) {
                is BaseResponseFirebase.Success -> {
                    val data = it.value
                    if(data?.getPhoto()?.isEmpty() == true){
                        saveUsers()
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        NavigationHelper().navigateToActivity(this@LoginActivity, intent)
                    }
                }
                is BaseResponseFirebase.Failed -> {
                    if(SessionManager.getDataString(this@LoginActivity, "signIn").equals("email")) {
                        saveUsers()
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        val intent = Intent(this@LoginActivity, LoginActivity::class.java)
                        NavigationHelper().navigateToActivityFlags(this@LoginActivity, intent)
                    }
                }
                else -> {
                }
            }
        }

        viewUserModel.firebaseDatabaseLoginResult?.observe(this){
            when (it) {
                is BaseResponseFirebase.LoginSuccess -> {
                    checkDatabaseExists()

                }
                is BaseResponseFirebase.LoginFailure -> {
                    val data = it.value
                    binding.tvNotif.text = data.toString()
                    binding.llProgressBar.preload.visibility = View.GONE
                }
                else -> {
                }
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveUsers() {
        val photosRef = FirebaseDatabase.getInstance().reference
        val mDbRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseAuth.currentUser!!.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = firebaseAuth.currentUser!!.uid
        hashMap["photo"] = photoUrl
        hashMap["fullName"] = displayName
        hashMap["email"] = email
        hashMap["phoneNumber"] = ""
        hashMap["status"] = ""
        hashMap["gender"] = ""
        hashMap["birthDay"] = ""
        hashMap["latitude"] = SessionManager.getDataString(this, "latitude").toString()
        hashMap["longitude"] = SessionManager.getDataString(this, "longitude").toString()
        hashMap["createDate"] = DateHelper().todayTime()

        mDbRef.updateChildren(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    val photosHashMap = HashMap<String, Any?>()
                    photosHashMap["uid"] = firebaseAuth.currentUser!!.uid
                    photosHashMap["no"] = "1"
                    photosHashMap["photo"] = photoUrl
                    photosHashMap["timestamp"] = DateHelper().todayTime()
                    photosRef.child("Photos").child(firebaseAuth.currentUser!!.uid).child("1").setValue(photosHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                binding.llProgressBar.preload.visibility = View.GONE
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                NavigationHelper().navigateToActivity(this@LoginActivity, intent)
                            }
                        }
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun signInGoogle() {
        val signInIntent:Intent=mGoogleSignInClient.signInIntent
        signInGoogleResultLauncher.launch(signInIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private var signInGoogleResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            binding.llProgressBar.preload.visibility = View.VISIBLE
            handleResult(task)
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
            else{
                binding.llProgressBar.preload.visibility = View.GONE
            }
        } catch (e: ApiException){
            binding.llProgressBar.preload.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun UpdateUI(account: GoogleSignInAccount){
        val credential= GoogleAuthProvider.getCredential(account.idToken,null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {task->
            if(task.isSuccessful) {
                email = account.email.toString()
                displayName = account.displayName.toString()
                photoUrl = account.photoUrl.toString()

                SessionManager.saveSignInBy(this@LoginActivity, "email")

                firebaseUser = FirebaseAuth.getInstance().currentUser
                firebaseDatabaseUser()
            }
        }
    }

    private fun firebaseDatabaseUser() {
        val child = "Users"
        val path = "uid"
        val uid = firebaseAuth.currentUser!!.uid
        viewUserModel.firebaseDatabaseUser(firebaseDatabase, child, path, uid)
    }

    private fun loginUser() {
        val email: String = binding.etEmail.text.toString().plus("@gmail.com")
        val password: String = binding.etPassword.text.toString()

        if (email == "")
        {
            Helper().showToast("Please write ID Number", this@LoginActivity)
        }
        else if (password == "")
        {
            Helper().showToast("Please write password", this@LoginActivity)
        }
        else
        {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            viewUserModel.login(firebaseAuth, email, password)
        }
    }

    private fun checkDatabaseExists() {
        val firebaseUser = firebaseAuth.currentUser
        val child = "Users"
        val path = "email"
        viewUserModel.firebaseDatabaseUser(firebaseDatabase, child, path, firebaseUser?.email.toString())
    }

    private fun sendMessage() {
        val uri = Uri.parse("smsto:6281190019568")
        val sendIntent = Intent(Intent.ACTION_SENDTO, uri)

        val shareIntent = Intent.createChooser(sendIntent, null)
        NavigationHelper().navigateToActivityCallback(this, shareIntent)
    }

    private fun shareWith(message:String){
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "")
            setPackage("com.whatsapp")
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
}