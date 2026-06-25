package com.smj.app.ui.task.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.databinding.ActivityContactListBinding
import com.smj.app.databinding.LayoutProgressBinding
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.ui.contact.adapter.ContactAdapter
import com.smj.app.ui.contact.model.ContactList

class ContactListActivity : AppCompatActivity(), ContactAdapter.ContactAdapterCallback {

    private lateinit var binding: ActivityContactListBinding
    private lateinit var recyclerViewContact: RecyclerView
    private var firebaseUser: FirebaseUser? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private var usersRefrence: DatabaseReference? = null

    private var contactsList: ArrayList<ContactList>? = null
    var positionName: String = ""

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser

        val forUse = intent.getStringExtra("forUse")
        val taskId = intent.getStringExtra("taskId")
        val position = intent.getStringExtra("position")
        if (position.equals("Sopir")) {
            positionName = "Operator Hauler"
        }
        if (position.equals("pengawas")) {
            positionName = "foreman Produksi"
        }
        if (position.equals("operator")) {
            positionName = "Operator Exavator"
        }

        binding.tvTitle.text = positionName.toCapitalizeEachWord()

        recyclerViewContact = binding.recyclerViewContactlist
        recyclerViewContact.setHasFixedSize(true)
        recyclerViewContact.layoutManager = LinearLayoutManager(this)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setTitle(positionName)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.title = positionName

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

        binding.etSearch.queryHint = "Search"
        binding.etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                showDataSearch(position, query.toString(), forUse)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                showDataSearch(position, newText.toString(), forUse)
                return true
            }

        })

        contactsList = ArrayList()

        showData(position, forUse)

    }

    private fun String.toCapitalizeEachWord() : String{
        return this.split(" ").joinToString(" ") {
            if (it.contains("/")) {
                it.split("/").joinToString("/") { word ->
                    word.replaceFirstChar { firstChar -> firstChar.uppercase() }
                }
            } else {
                it.replaceFirstChar { firstChar -> firstChar.uppercase() }
            }
        }
    }

    private fun showDataSearch(position: String?, search: String, forUse: String?) {
        val refContact = FirebaseDatabase.getInstance().reference.child("Users")
        refContact.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (contactsList as ArrayList).clear()
                if(snapshot.exists()){
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewContact.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val contact = dataSnapshot.getValue(ContactList::class.java)
                        if (position == "pengawas") {
                            if (
                                contact?.getPosition() == "SPV Produksi"
                                || contact?.getPosition() == "Jr SPV Produksi"
                                || contact?.getPosition() == "Sr Foreman Produksi"
                                || contact?.getPosition() == "Jr Foreman Produksi"
                                || contact?.getPosition() == "Foreman Produksi"
                            ) {
                                if (contact.getFullName()?.lowercase()
                                        ?.startsWith(search.lowercase()) == true
                                    || contact.getFullName()?.lowercase()
                                        ?.endsWith(search.lowercase()) == true
                                    || contact.getFullName()?.lowercase()
                                        ?.contains(search.lowercase()) == true
                                ) {
                                    (contactsList as ArrayList).add(contact)
                                }
                            }
                        }
                        if (position == "operator") {
                            if (
                                contact?.getPosition()?.startsWith("Operator") == true
                            ) {
                                if (contact.getFullName()?.lowercase()
                                        ?.startsWith(search.lowercase()) == true
                                    || contact.getFullName()?.lowercase()
                                        ?.endsWith(search.lowercase()) == true
                                    || contact.getFullName()?.lowercase()
                                        ?.contains(search.lowercase()) == true
                                ) {
                                    (contactsList as ArrayList).add(contact)
                                }
                            }
                        }
                        if (position == "Sopir") {
                            if (contact?.getPosition()?.startsWith("Operator") == true) {
                                if (contact.getFullName()?.lowercase()
                                        ?.startsWith(search.lowercase()) == true
                                    || contact.getFullName()?.lowercase()
                                        ?.endsWith(search.lowercase()) == true
                                    || contact.getFullName()?.lowercase()
                                        ?.contains(search.lowercase()) == true
                                ) {
                                    (contactsList as ArrayList).add(contact)
                                }
                            }
                        }
                    }
                    checkUserCurrent(
                        this@ContactListActivity,
                        contactsList!!,
                        this@ContactListActivity,
                        position,
                        forUse,
                        binding.llProgressBar
                    )
                }
                else{
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewContact.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun showData(position: String?, forUse: String?) {
        val refContact = FirebaseDatabase.getInstance().reference.child("Users")
        refContact.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (contactsList as ArrayList).clear()
                if(snapshot.exists()){
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewContact.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val contact = dataSnapshot.getValue(ContactList::class.java)
                        if (position == "pengawas") {
                            if (
                                contact?.getPosition() == "SPV Produksi"
                                || contact?.getPosition() == "Jr SPV Produksi"
                                || contact?.getPosition() == "Sr Foreman Produksi"
                                || contact?.getPosition() == "Jr Foreman Produksi"
                                || contact?.getPosition() == "Foreman Produksi"
                            ) {
                                (contactsList as ArrayList).add(contact)
                            }
                        }
                        if (position == "operator") {
                            if (
                                contact?.getPosition() == "Operator PC 1250"
                                || contact?.getPosition() == "Operator EC 395"
                                || contact?.getPosition() == "Operator PC 750"
                                || contact?.getPosition() == "Operator EC 480"
                                || contact?.getPosition() == "Operator PC 200"
                            ) {
                                (contactsList as ArrayList).add(contact)
                            }
                        }
                        if (position == "Sopir") {
                            if (
                                contact?.getPosition() == "Operator ADT"
                                || contact?.getPosition() == "Operator D8 T"
                                || contact?.getPosition() == "Operator DZ 375"
                                || contact?.getPosition() == "Operator DZ 85 SS"
                                || contact?.getPosition() == "Operator GD 705"
                                || contact?.getPosition() == "Operator GD 14 M"
                                || contact?.getPosition() == "Operator HD 465"
                                || contact?.getPosition() == "Driver Hino 500"
                            ) {
                                (contactsList as ArrayList).add(contact)
                            }
                        }
                    }
                    checkUserCurrent(
                        this@ContactListActivity,
                        contactsList!!,
                        this@ContactListActivity,
                        position,
                        forUse,
                        binding.llProgressBar
                    )
//                    val contactAdapter = ContactAdapter(
//                        this@ContactListActivity,
//                        contactsList!!,
//                        this@ContactListActivity,
//                        position,
//                        forUse,
//                        binding.llProgressBar,
//                        user.getPosition().toString()
//                    )
//                    recyclerViewContact.layoutManager = LinearLayoutManager(this@ContactListActivity)
//                    recyclerViewContact.adapter = contactAdapter
                }
                else{
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewContact.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun OnDetail(bindingAdapterPosition: Int, mData: ArrayList<ContactList>) {
        val intent = Intent()
        intent.putExtra("userName", mData[bindingAdapterPosition].getFullName())
        intent.putExtra("userId", mData[bindingAdapterPosition].getUid())
        intent.putExtra("userPosition", mData[bindingAdapterPosition].getPosition())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun WhatsApp(bindingAdapterPosition: Int, mData: java.util.ArrayList<ContactList>) {

    }

    override fun Call(bindingAdapterPosition: Int, mData: java.util.ArrayList<ContactList>) {

    }

    override fun Email(bindingAdapterPosition: Int, mData: java.util.ArrayList<ContactList>) {

    }

    override fun Authorized(bindingAdapterPosition: Int, mData: java.util.ArrayList<ContactList>) {

    }

    override fun Unauthorized(
        bindingAdapterPosition: Int,
        mData: java.util.ArrayList<ContactList>
    ) {

    }

    private fun checkUserCurrent(
        contactListActivity: ContactListActivity,
        contactsList: ArrayList<ContactList>,
        contactListActivity1: ContactListActivity,
        position: String?,
        forUse: String?,
        llProgressBar: LayoutProgressBinding
    ) {
        if(firebaseAuth.currentUser?.uid == null){
            val intent = Intent(this, LoginActivity::class.java)
            NavigationHelper().navigateToActivityFlags(this, intent)
        }
        else{
            firebaseUser = firebaseAuth.currentUser
            usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

            usersRefrence!!.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user: Users? = snapshot.getValue(Users::class.java)
                        val contactAdapter = ContactAdapter(
                            contactListActivity,
                            contactsList,
                            contactListActivity1,
                            position,
                            forUse,
                            llProgressBar,
                            user?.getPosition().toString(),
                            intent.getStringExtra("taskId")!!
                        )
                        recyclerViewContact.layoutManager = LinearLayoutManager(this@ContactListActivity)
                        recyclerViewContact.adapter = contactAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }
}