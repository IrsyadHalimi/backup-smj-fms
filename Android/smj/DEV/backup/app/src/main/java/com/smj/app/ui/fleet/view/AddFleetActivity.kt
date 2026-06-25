package com.smj.app.ui.fleet.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.smj.app.R
import com.smj.app.databinding.ActivityAddFleetBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.view.LoginActivity

class AddFleetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFleetBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var context: Context? = null

    //start Unit Type
    private lateinit var selectedUnitType: String
    private var selectedUnitTypeIndex: Int = 0
    private val unit_type = arrayOf(
        "DT",
        "ADT",
        "OHT",
        "HD",
        "WT",
        "MH",
        "GRADER",
        "EXCAVATOR",
        "BULLDOZER",
        "RIGID TRUCK"
//        "ARTICULATED DUMP TRUCK",
//        "BULLDOZER",
//        "COMPACTOR",
//        "DRILL MACHINE",
//        "DUMP TRUCK",
//        "EXCAVATOR",
//        "FORKLIFT",
//        "FUEL TRUCK",
//        "LIGHT VEHICLE",
//        "LUBE TRUCK",
//        "MANHAUL",
//        "MOTOR GRADER",
//        "RIGID TRUCK",
//        "ROUGH TERRAIN CRANE",
//        "WATER TRUCK"
    )

    //start Merk
    private lateinit var selectedMerk: String
    private var selectedMerkIndex: Int = 0
    private val merk = arrayOf(
        "VOLVO",
        "CATERPILLAR",
        "HINO",
        "SCANIA",
        "NISSAN",
        "KOMATSU",
        "ISUZU",
        "BOMAG",
        "SANDVIK",
        "ATLAS COPCO",
        "KENWORTH",
        "TOYOTA",
        "MITSUBISHI",
        "UD TRUCKS"
    )

    //start status
    private lateinit var selectedStatus: String
    private var selectedStatusIndex: Int = 0
    private val status = arrayOf(
        "RFU",
        "BD",
        "SCP",
        "AT TH-MODIFY CRANE",
        "LONG BD",
        "ACCIDENT"
    )

    //start status
    private lateinit var selectedPit: String
    private var selectedPitIndex: Int = 0
    private val pit = arrayOf(
        "50",
        "55"
    )

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.",
        ReplaceWith("onBackPressedDispatcher.onBackPressed()")
    )
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

    @SuppressLint("PrivateResource", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFleetBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        firebaseAuth = FirebaseAuth.getInstance()

        checkUserCurrent()

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener {
            val intent = Intent()
            intent.putExtra("from", "AddFleetActivity")
            setResult(Activity.RESULT_OK, intent)
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

        binding.etUnitType.setOnClickListener {
            getUnitType()
        }

        binding.etMerk.setOnClickListener {
            getMerk()
        }

        binding.etStatus.setOnClickListener {
            getStatus()
        }

        binding.etPit.setOnClickListener {
            getPit()
        }

        binding.btnAddUnit.setOnClickListener {
            addUnit()
        }
    }

    private fun addUnit() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        Helper().hideKeyboard(this@AddFleetActivity)
        if(binding.etUnitCode.text.isNotEmpty()
            && binding.etUnitType.text.isNotEmpty()
            && binding.etMerk.text.isNotEmpty()
            && binding.etYom.text.isNotEmpty()
            && binding.etStatus.text.isNotEmpty()
            && binding.etPit.text.isNotEmpty()
        ) {
            val refUnit = FirebaseDatabase.getInstance().reference
            val unitId = refUnit.push().key

            val unitHashMap = HashMap<String, Any>()
            unitHashMap["uid"] = firebaseAuth.currentUser!!.uid
            unitHashMap["unitId"] = unitId!!
            unitHashMap["unitCode"] = binding.etUnitCode.text.toString()
            unitHashMap["unitType"] = binding.etUnitType.text.toString()
            unitHashMap["merk"] = binding.etMerk.text.toString()
            unitHashMap["yom"] =  binding.etYom.text.toString()
            unitHashMap["status"] =  binding.etStatus.text.toString()
            unitHashMap["pit"] =  binding.etPit.text.toString()
            unitHashMap["createDate"] =  DateHelper().todayTime()

            databaseReference = refUnit.child("Units").child(unitId)
            databaseReference.updateChildren(unitHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Helper().showToast("Adding data is Successful!", this@AddFleetActivity)
                        binding.llProgressBar.preload.visibility = View.GONE
                        onBackPressedDispatcher.onBackPressed()
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Product is Something Wrong!!!", this@AddFleetActivity)
                    }
                }
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            Helper().showToast("Product Name, price and description is required!", this@AddFleetActivity)
        }
    }

    private fun checkUserCurrent() {
        if(firebaseAuth.currentUser?.uid == null){
            val intent = Intent(this@AddFleetActivity, LoginActivity::class.java)
            NavigationHelper().navigateToActivityFlags(this@AddFleetActivity, intent)
        }
    }

    private fun getUnitType() {
        selectedUnitType = unit_type[selectedUnitTypeIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Unit Type")
            .setSingleChoiceItems(unit_type, selectedUnitTypeIndex) { _, which ->
                selectedUnitTypeIndex = which
                selectedUnitType = unit_type[which]
            }
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Choice") { _, _ ->
                val mapData = java.util.HashMap<String, Any>()
                mapData["unit_type"] = "$selectedUnitType"
                binding.etUnitType.text = "$selectedUnitType"
            }
            .show()
    }

    private fun getMerk() {
        selectedMerk = merk[selectedMerkIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Merk")
            .setSingleChoiceItems(merk, selectedMerkIndex) { _, which ->
                selectedMerkIndex = which
                selectedMerk = merk[which]
            }
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Choice") { _, _ ->
                val mapData = java.util.HashMap<String, Any>()
                mapData["merk"] = "$selectedMerk"
                binding.etMerk.text = "$selectedMerk"
            }
            .show()
    }

    private fun getStatus() {
        selectedStatus = status[selectedStatusIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Status")
            .setSingleChoiceItems(status, selectedStatusIndex) { _, which ->
                selectedStatusIndex = which
                selectedStatus = status[which]
            }
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Choice") { _, _ ->
                val mapData = java.util.HashMap<String, Any>()
                mapData["status"] = "$selectedStatus"
                binding.etStatus.text = "$selectedStatus"
            }
            .show()
    }

    private fun getPit() {
        selectedPit = pit[selectedPitIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select PIT")
            .setSingleChoiceItems(pit, selectedPitIndex) { _, which ->
                selectedPitIndex = which
                selectedPit = pit[which]
            }
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Choice") { _, _ ->
                val mapData = java.util.HashMap<String, Any>()
                mapData["pit"] = "$selectedPit"
                binding.etPit.text = "$selectedPit"
            }
            .show()
    }
}