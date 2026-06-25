package com.smj.app.ui.main.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.smj.app.databinding.FragmentSettingsBinding
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.ui.main.view.adapter.TabSettingsAdapter
import com.smj.app.utils.session.SessionManager
import com.smj.app.R

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private var firebaseUser: FirebaseUser? = null
    private var usersRefrence: DatabaseReference? = null

    private val logoutResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { _ ->
        requireActivity().finish()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(requireActivity(),gso)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        initViews()

        usersRefrence!!.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    val user: Users? = p0.getValue(Users::class.java)

                    if (context!=null)
                    {
                        if (user?.getPhoto()?.isNotEmpty() == true) {
                            if(isAdded) {
                                binding.ivProfil.visibility = View.VISIBLE
                                binding.shimmerViewContainer.visibility = View.GONE
                                binding.tvName.text = user.getFullName()
                                binding.tvEmail.text = user.getEmail()
                                binding.tvPhone.text = "ID: ".plus(user.getIdNumber())
                                Glide.with(requireActivity())
                                    .load(user.getPhoto())
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .placeholder(
                                        ContextCompat.getDrawable(
                                            requireContext(),
                                            R.drawable.ic_worker
                                        )
                                    )
                                    .into(binding.ivProfil)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        binding.tvLogout.setOnClickListener {
            showAppClosingDialog()
        }

        return binding.root
    }

    private fun initViews() {
        initTabLayout()
    }

    private fun initTabLayout() {
        val tabLayoutMediator = TabLayoutMediator(binding.tabLayoutId, binding.viewPagerId) { tab, position ->
            when (position) {
                0 -> tab.text = HtmlCompat.fromHtml("<b>"+ TAB.SETTINGS.displayName+"</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)
                1 -> tab.text = HtmlCompat.fromHtml("<b>"+ TAB.SUPPORT.displayName+"</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        }
        binding.viewPagerId.adapter = TabSettingsAdapter(requireActivity() as AppCompatActivity)
        tabLayoutMediator.attach()
    }

    enum class TAB(val displayName: String) {
        SETTINGS("Settings"),
        SUPPORT("Support")
    }

    companion object {
    }

    private fun showAppClosingDialog() {
//        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
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
        val alertBuilder = android.app.AlertDialog.Builder(requireContext())
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
        if(SessionManager.getDataString(requireContext(), "signIn").equals("email")) {
            mGoogleSignInClient.signOut().addOnCompleteListener {
                context?.let {
                    SessionManager.clearData(it)
                    firebaseAuth.signOut()
                    val intent = Intent(context, LoginActivity::class.java)
                    logoutResult.launch(intent)
                }
            }
        }
        else{
            firebaseAuth.signOut()
            val intent = Intent(context, LoginActivity::class.java)
//            NavigationHelper().navigateToActivityFlags(requireActivity(), intent)
            logoutResult.launch(intent)
        }
    }
}