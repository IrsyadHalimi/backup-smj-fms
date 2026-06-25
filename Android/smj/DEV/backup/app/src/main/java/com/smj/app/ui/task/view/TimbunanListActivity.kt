package com.smj.app.ui.task.view

import android.app.Activity
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
import com.smj.app.databinding.ActivityTimbunanListBinding
import com.smj.app.helper.Helper
import com.smj.app.ui.settings.adapter.LocationAdapter
import com.smj.app.ui.settings.model.LocationList

class TimbunanListActivity : AppCompatActivity(), LocationAdapter.LocationAdapterCallback {
    private lateinit var binding: ActivityTimbunanListBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerViewLocation: RecyclerView

    private var locationList: ArrayList<LocationList>? = null
    lateinit var dialogView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimbunanListBinding.inflate(layoutInflater)
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

        val refContact = FirebaseDatabase.getInstance().reference.child("Timbunan")
        refContact.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (locationList as ArrayList).clear()
                if (snapshot.exists()) {
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewLocation.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val location = dataSnapshot.getValue(LocationList::class.java)
                        (locationList as ArrayList).add(location!!)
                    }
                    val locationAdapter =
                        LocationAdapter(
                            this@TimbunanListActivity,
                            locationList!!,
                            this@TimbunanListActivity,
                            position
                        )
                    recyclerViewLocation.layoutManager = LinearLayoutManager(this@TimbunanListActivity)
                    recyclerViewLocation.adapter = locationAdapter
                } else {
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