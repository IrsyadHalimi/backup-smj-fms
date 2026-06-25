package com.smj.app.ui.contact.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.smj.app.R
import com.smj.app.databinding.ActivityNotificationContactsBinding
import com.smj.app.ui.contact.adapter.ContactConfirmAdapter
import com.smj.app.ui.contact.model.ContactList
import com.smj.app.ui.contact.viewModel.ContactsViewModel
import com.smj.app.utils.response.BaseResponseFirebase

class NotificationContactsActivity : AppCompatActivity(), ContactConfirmAdapter.ContactConfirmAdapterCallback {

    private lateinit var binding: ActivityNotificationContactsBinding
    private var context: Context? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerViewContact: RecyclerView
    private lateinit var recyclerViewUserList: RecyclerView

    private var firebaseUser: FirebaseUser? = null
    private val viewContactsModel by viewModels<ContactsViewModel>()

    private var contactsList: ArrayList<ContactList>? = null

    lateinit var dialogView: View

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_times)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)

        contactsList = ArrayList()

        recyclerViewContact = binding.recyclerContactList
        recyclerViewContact.setHasFixedSize(true)
        recyclerViewContact.layoutManager = LinearLayoutManager(context)

        viewContactsModel.fDbContactsResult?.observe(this){
            when (it) {
                is BaseResponseFirebase.ContactShowSuccess -> {
                    (contactsList as ArrayList).clear()
                    if (it.value?.isNotEmpty() == true) {
                        binding.shimmerViewContainer.visibility = View.GONE
                        recyclerViewContact.visibility = View.VISIBLE
                        binding.llEmpty.visibility = View.GONE
                        for (data in it.value) {
                            (contactsList as ArrayList).add(data)
                        }

                        val contactConfirmAdapter = ContactConfirmAdapter(
                            this,
                            contactsList!!,
                            this@NotificationContactsActivity
                        )
                        recyclerViewContact.layoutManager = LinearLayoutManager(context)
                        recyclerViewContact.adapter = contactConfirmAdapter
                    } else {
                        binding.shimmerViewContainer.visibility = View.GONE
                        recyclerViewContact.visibility = View.GONE
                        binding.llEmpty.visibility = View.VISIBLE
                    }
                }
                is BaseResponseFirebase.Failed -> {
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewContact.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
                else -> {
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewContact.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
            }
        }

        contactListShow()

        binding.done.setOnClickListener {
            importContact()
        }
    }

    private fun importContact() {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setMessage(HtmlCompat.fromHtml("The data will be imported to your account list<br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
        dialogBuilder.setPositiveButton("Yes"){ dialog, _ ->
            addContacts(dialog)
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

    private fun addContacts(dialog: DialogInterface) {
        dialog.dismiss()
        binding.llProgressBar.preload.visibility = View.VISIBLE
    }

    private fun contactListShow() {
        val refContact = FirebaseDatabase.getInstance().reference
            .child("Shared")
            .child(firebaseUser!!.uid)
            .child("Contacts")
        viewContactsModel.show(refContact)
    }
}