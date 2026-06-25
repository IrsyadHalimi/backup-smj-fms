package com.smj.app.ui.fleet.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.smj.app.databinding.ActivityEditProductBinding
import com.smj.app.helper.FormatNumber
import com.smj.app.helper.Helper
import com.smj.app.ui.fleet.model.UnitList
import com.smj.app.R

class EditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProductBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private var productRefrence: DatabaseReference? = null
    lateinit var dialogView: View
    private var formatNumber: FormatNumber? = null
    //start Unit Type
    private lateinit var selectedUnitType: String
    private var selectedUnitTypeIndex: Int = 0
    private val unit_type = arrayOf(
        "ARTICULATED DUMP TRUCK",
        "BULLDOZER",
        "COMPACTOR",
        "DRILL MACHINE",
        "DUMP TRUCK",
        "EXCAVATOR",
        "FORKLIFT",
        "FUEL TRUCK",
        "LIGHT VEHICLE",
        "LUBE TRUCK",
        "MANHAUL",
        "MOTOR GRADER",
        "RIGID TRUCK",
        "ROUGH TERRAIN CRANE",
        "WATER TRUCK"
    )

    //start Merk
    private lateinit var selectedMerk: String
    private var selectedMerkIndex: Int = 0
    private val merk = arrayOf(
        "VOLVO",
        "CATERPILLAR",
        "KOMATSU",
        "BOMAG",
        "SANDVIK",
        "ATLAS COPCO",
        "HINO",
        "SCANIA",
        "NISSAN",
        "KENWORTH",
        "TOYOTA ",
        "MITSUBISHI",
        "UD TRUCKS",
        "ISUZU"
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        formatNumber = FormatNumber()

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        val uid = intent.getStringExtra("uid")
        val unitId = intent.getStringExtra("unitId")

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_times)
        toolbar.setNavigationOnClickListener {
            val intent = Intent()
            intent.putExtra("from", "EditProductActivity")
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

        productRefrence = FirebaseDatabase.getInstance().reference
            .child("Units")
            .child(unitId!!)

        productRefrence!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    val unit: UnitList? = snapshot.getValue(UnitList::class.java)
                    binding.etUnitCode.setText(unit?.getUnitCode().toString())
                    binding.etUnitType.text = unit?.getUnitType().toString()
                    binding.etMerk.text = unit?.getMerk().toString()
                    binding.etYom.setText(unit?.getYom().toString())
                    binding.etStatus.text = unit?.getStatus().toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

//        binding.done.setOnClickListener {
//            addProduct(uid, productId)
//        }

        binding.btnAddUnit.setOnClickListener {
            addProduct(uid!!, unitId)
        }
    }

    private fun addProduct(uid: String, unitId: String) {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        Helper().hideKeyboard(this@EditProductActivity)
        if(binding.etUnitCode.text.isNotEmpty()
            && binding.etUnitType.text?.isNotEmpty() == true
            && binding.etMerk.text.isNotEmpty()
            && binding.etYom.text.isNotEmpty()
            && binding.etStatus.text.isNotEmpty()
        ) {
            val refUnit = FirebaseDatabase.getInstance().reference

            val unitHashMap = HashMap<String, Any>()
            unitHashMap["uid"] = firebaseAuth.currentUser!!.uid
            unitHashMap["unitId"] = unitId
            unitHashMap["unitCode"] = binding.etUnitCode.text.toString()
            unitHashMap["unitType"] = binding.etUnitType.text.toString()
            unitHashMap["merk"] = binding.etMerk.text.toString()
            unitHashMap["yom"] =  binding.etYom.text.toString()
            unitHashMap["status"] =  binding.etStatus.text.toString()

            databaseReference = refUnit.child("Units").child(unitId)
            databaseReference.updateChildren(unitHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Helper().showToast("Update data is Successful!", this@EditProductActivity)
                        binding.llProgressBar.preload.visibility = View.GONE
                        val intent = Intent()
                        intent.putExtra("from", "EditProductActivity")
                        setResult(Activity.RESULT_OK, intent)
                        onBackPressedDispatcher.onBackPressed()
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Product is Something Wrong!!!", this@EditProductActivity)
                    }
                }
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            Helper().showToast("Product Name, price and description is required!", this@EditProductActivity)
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
}