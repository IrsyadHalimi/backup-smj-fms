package com.smj.app.ui.contact.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.smj.app.databinding.ActivityDetailContactBinding
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.contact.model.CompanyList
import com.smj.app.ui.contact.model.ContactList
import com.smj.app.R

class DetailContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailContactBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private var firebaseUser: FirebaseUser? = null
    private var usersRefrence: DatabaseReference? = null
    private var contactRefrence: DatabaseReference? = null
    private var companyRefrence: DatabaseReference? = null
    private var contactsSharedRefrence: DatabaseReference? = null
    lateinit var dialogView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        val uid = intent.getStringExtra("uid")
        val contactId = intent.getStringExtra("contactId")
        val companyId = intent.getStringExtra("companyId")

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

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersRefrence = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(firebaseUser!!.uid)
        contactRefrence = FirebaseDatabase.getInstance().reference
            .child("Contact")
            .child(uid!!)
            .child(contactId!!)
        companyRefrence = FirebaseDatabase.getInstance().reference
            .child("Company")
        contactsSharedRefrence = FirebaseDatabase.getInstance().reference
            .child("ContactsShared")
            .child(contactId)

        contactRefrence!!
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    val contact: ContactList? = p0.getValue(ContactList::class.java)
                    binding.etFullname.setText(contact?.getFullName())
                    binding.etEmail.setText(contact?.getEmail())
                    binding.etStatus.setText(contact?.getStatus())

                    companyRefrence!!
                        .child(contact?.getId().toString())
                        .addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.exists()){
                                    Log.i("POSTALCODE", snapshot.value.toString())
                                    val company: CompanyList? = snapshot.getValue(CompanyList::class.java)
                                    binding.etCompanyName.setText(company?.getCompanyName().toString())
                                    binding.etCompanyPhone.setText(company?.getCompanyPhone().toString())
                                    binding.etCompanyAddress.setText(company?.getCompanyAddress().toString())
                                    binding.etCompanyState.setText(company?.getCompanyState().toString())
                                    binding.etCompanyCity.setText(company?.getCompanyCity().toString())
                                    binding.etCompanyPostalCode.setText(company?.getPostalCode().toString())
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        binding.delete.setOnClickListener {
            if(uid.isNotEmpty() && contactId.isNotEmpty()) {
                contactDeleted(contactId)
            }
            else{
                Helper().showToast("Data is Null!!", this)
            }
        }

        binding.edit.setOnClickListener {
            if(uid.isNotEmpty() && contactId.isNotEmpty()) {
                val intent = Intent(this, EditContactActivity::class.java)
                intent.putExtra("uid", uid)
                intent.putExtra("contactId", contactId)
                intent.putExtra("companyId", companyId)
                NavigationHelper().navigateToActivityCallback(this, intent)
            }
            else{
                Helper().showToast("Data is Null!!", this)
            }
        }
    }

    private fun contactDeleted(contactId: String) {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_remove_contact, null)

        val remove = dialogView.findViewById<LinearLayout>(R.id.remove)
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
        }

        remove.setOnClickListener {
            contactRefrence?.removeValue()
            alertDialog.dismiss()
            onBackPressedDispatcher.onBackPressed()
        }

    }
}