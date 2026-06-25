package com.smj.app.ui.settings.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.databinding.ActivityTimbunanBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.Helper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.settings.adapter.LocationAdapter
import com.smj.app.ui.settings.model.LocationList

class TimbunanActivity : AppCompatActivity(), LocationAdapter.LocationAdapterCallback {
    private lateinit var binding: ActivityTimbunanBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerViewLocation: RecyclerView

    private var locationList: ArrayList<LocationList>? = null
    lateinit var dialogView: View
    private var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimbunanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener { val intent = Intent()
            intent.putExtra("from", "ShiftActivity")
            setResult(Activity.RESULT_OK, intent)
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

                        if (user?.getPosition() == "ADMIN" || user?.getPosition() == "root") {
                            binding.ivAdd.visibility = View.VISIBLE
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

        binding.ivAdd.setOnClickListener {
            showFormDialog()
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
                            this@TimbunanActivity,
                            locationList!!,
                            this@TimbunanActivity,
                            position
                        )
                    recyclerViewLocation.layoutManager = LinearLayoutManager(this@TimbunanActivity)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun showFormDialog() {
        val dialogBuilder = MaterialAlertDialogBuilder(
            this,
            R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
        )
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_form_location, null)

        val nameLocation = dialogView.findViewById<EditText>(R.id.et_name_location)
        nameLocation.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val addLocation = dialogView.findViewById<LinearLayout>(R.id.add_location)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
        }

        addLocation.setOnClickListener {
            addingLocation(nameLocation.text.toString(), dialogView, alertDialog)
        }

    }

    @SuppressLint("CutPasteId")
    private fun addingLocation(
        location: String,
        dialogView: View,
        alertDialog: AlertDialog
    ) {
        if (location == "") {
            dialogView.findViewById<TextView>(R.id.tv_name_location).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tv_name_location).text = "Field is required!"
        }
        if (location != "") {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            alertDialog.dismiss()
            val refLocation = FirebaseDatabase.getInstance().reference
            val locationId = refLocation.push().key

            val locationHashMap = HashMap<String, Any>()
            locationHashMap["uid"] = firebaseAuth.currentUser!!.uid
            locationHashMap["locationId"] = locationId!!
            locationHashMap["locationName"] = location
            locationHashMap["createDate"] = DateHelper().todayTime()

            databaseReference = refLocation.child("Timbunan").child(locationId)
            databaseReference.updateChildren(locationHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Helper().showToast("Adding data is Successful!", this@TimbunanActivity)
                        binding.llProgressBar.preload.visibility = View.GONE
                    } else {
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast(
                            "Data Product is Something Wrong!!!",
                            this@TimbunanActivity
                        )
                    }
                }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showEditialog(
        locationId: String?,
        locationName: String?
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(
            this,
            R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
        )
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_form_location, null)

        val nameLocation = dialogView.findViewById<EditText>(R.id.et_name_location)
        nameLocation.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val addShift = dialogView.findViewById<LinearLayout>(R.id.add_location)

        dialogView.findViewById<TextView>(R.id.txt_facebook).text = "Update Location"
        nameLocation.setText(locationName)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
        }

        addShift.setOnClickListener {
            updateLocation(nameLocation.text.toString(), dialogView, alertDialog, locationId)
        }

    }

    @SuppressLint("CutPasteId")
    private fun updateLocation(
        location: String,
        dialogView: View,
        alertDialog: AlertDialog,
        locationId: String?
    ) {
        if (location == "") {
            dialogView.findViewById<TextView>(R.id.tv_name_location).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tv_name_location).text = "Field is required!"
        }
        if (location != "") {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            alertDialog.dismiss()
            val refShift = FirebaseDatabase.getInstance().reference
            val locationId = locationId

            val shiftHashMap = HashMap<String, Any>()
            shiftHashMap["uid"] = firebaseAuth.currentUser!!.uid
            shiftHashMap["locationId"] = locationId!!
            shiftHashMap["locationName"] = location
            shiftHashMap["createDate"] = DateHelper().todayTime()

            databaseReference = refShift.child("Timbunan").child(locationId)
            databaseReference.updateChildren(shiftHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Helper().showToast("Update data is Successful!", this@TimbunanActivity)
                        binding.llProgressBar.preload.visibility = View.GONE
                    } else {
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast(
                            "Data Location is Something Wrong!!!",
                            this@TimbunanActivity
                        )
                    }
                }
        }
    }

    override fun onDetail(adapterPosition: Int, locationList: ArrayList<LocationList>) {
        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (user?.getPosition() == "ADMIN" || user?.getPosition() == "root") {
                            showEditialog(
                                locationList[adapterPosition].getLocationId(),
                                locationList[adapterPosition].getLocationName()
                            )
                        }
                        else{
                            val alertBuilder = android.app.AlertDialog.Builder(this@TimbunanActivity)
                            alertBuilder.setTitle("Akses dibatasi")
                            alertBuilder.setMessage("Anda tidak memiliki akses!")
                            alertBuilder.setCancelable(false)
                            alertBuilder.setPositiveButton("OK"){_,_ ->

                            }
                            alertBuilder.show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}