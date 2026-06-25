package com.smj.app.ui.main.view.tab

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.HtmlCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.smj.app.R
import com.smj.app.databinding.FragmentTabSettingsBinding
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.ui.settings.activity.GalianActivity
import com.smj.app.ui.settings.activity.LabelingLostTimeActivity
import com.smj.app.ui.settings.activity.LokasiActivity
import com.smj.app.ui.settings.activity.ShiftActivity
import com.smj.app.ui.settings.activity.TimbunanActivity
import com.smj.app.utils.session.SessionManager

class TabSettingsFragment : Fragment() {

    private lateinit var binding: FragmentTabSettingsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val callResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == RESULT_OK){
            binding.llProgressBar.preload.visibility = View.GONE
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabSettingsBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(requireActivity(),gso)

        binding.tvShift.setOnClickListener {
            openShift()
        }

        binding.tvLokasi.setOnClickListener {
            openLokasi()
        }

        binding.tvGalian.setOnClickListener {
            openGalian()
        }

        binding.tvTimbunan.setOnClickListener {
            openTimbunan()
        }

        binding.tvLostTime.setOnClickListener {
            val filter = "General"
            openLabelingLostTime(filter)
        }

        binding.tvLostTimeUnit.setOnClickListener {
            val filter = "Kerusakan Unit"
            openLabelingLostTime(filter)
        }

        binding.tvLostTimeKondisi.setOnClickListener {
            val filter = "Kondisi"
            openLabelingLostTime(filter)
        }

        return binding.root
    }

    private fun openShift() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val intent = Intent(requireContext(), ShiftActivity::class.java)
        intent.putExtra("fragmentToLoad", "SettingsFragment")
        callResult.launch(intent)
    }

    private fun openLokasi() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val intent = Intent(requireContext(), LokasiActivity::class.java)
        intent.putExtra("fragmentToLoad", "SettingsFragment")
        callResult.launch(intent)
    }

    private fun openGalian() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val intent = Intent(requireContext(), GalianActivity::class.java)
        intent.putExtra("fragmentToLoad", "SettingsFragment")
        callResult.launch(intent)
    }

    private fun openTimbunan() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val intent = Intent(requireContext(), TimbunanActivity::class.java)
        intent.putExtra("fragmentToLoad", "SettingsFragment")
        callResult.launch(intent)
    }

    private fun openLabelingLostTime(filter: String) {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val intent = Intent(requireContext(), LabelingLostTimeActivity::class.java)
        intent.putExtra("fragmentToLoad", "SettingsFragment")
        intent.putExtra("filter", filter)
        callResult.launch(intent)
    }

    private fun showAppClosingDialog() {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setMessage(HtmlCompat.fromHtml("Do you want to close the application?<br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
        dialogBuilder.setPositiveButton("Yes"){ _, _ ->
            logout()
        }
        dialogBuilder.setNegativeButton("No") { dialog, _ ->
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

    private fun logout() {
        if(SessionManager.getDataString(requireContext(), "signIn").equals("email")) {
            mGoogleSignInClient.signOut().addOnCompleteListener {
                context?.let { it ->
                    SessionManager.clearData(it)
                    firebaseAuth.signOut()
                    val intent = Intent(context, LoginActivity::class.java)
                    NavigationHelper().navigateToActivity(requireActivity(), intent)
                }
            }
        }
        else{
            firebaseAuth.signOut()
            val intent = Intent(context, LoginActivity::class.java)
            NavigationHelper().navigateToActivity(requireActivity(), intent)
        }
    }

    companion object {
    }
}