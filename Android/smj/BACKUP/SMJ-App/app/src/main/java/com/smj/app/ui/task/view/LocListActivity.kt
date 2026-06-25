package com.smj.app.ui.task.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import com.smj.app.databinding.ActivityLocListBinding
import com.smj.app.helper.Helper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.settings.adapter.LocationAdapter
import com.smj.app.ui.settings.model.LocationList

class LocListActivity : AppCompatActivity(), LocationAdapter.LocationAdapterCallback {
    private lateinit var binding: ActivityLocListBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerViewLocation: RecyclerView

    private var locationList: ArrayList<LocationList>? = null
    lateinit var dialogView: View
    private var context: Context? = null

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.",
        ReplaceWith("onBackPressedDispatcher.onBackPressed()")
    )
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val position = intent.getStringExtra("position")

        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (user?.getPosition() == "admin") {
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

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

        recyclerViewLocation = binding.recyclerViewLocationList
        recyclerViewLocation.setHasFixedSize(true)
        recyclerViewLocation.layoutManager = LinearLayoutManager(this)

        locationList = ArrayList()

        val refContact = FirebaseDatabase.getInstance().reference.child("Locations")
        refContact.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (locationList as ArrayList).clear()
                if(snapshot.exists()){
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewLocation.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val location = dataSnapshot.getValue(LocationList::class.java)
                        (locationList as ArrayList).add(location!!)
                    }
                    val locationAdapter = LocationAdapter(this@LocListActivity, locationList!!, this@LocListActivity, position)
                    recyclerViewLocation.layoutManager = LinearLayoutManager(this@LocListActivity)
                    recyclerViewLocation.adapter = locationAdapter
                }
                else{
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewLocation.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onDetail(adapterPosition: Int, mData: ArrayList<LocationList>) {
        val intent = Intent()
        intent.putExtra("locName", mData[adapterPosition].getLocationName())
        intent.putExtra("id", mData[adapterPosition].getId())
        intent.putExtra("locId", mData[adapterPosition].getLocationId())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}