package com.smj.app.ui.fleet.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.databinding.ActivityDetailProductBinding
import com.smj.app.helper.FormatNumber
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.fleet.model.UnitList

class DetailProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProductBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private var firebaseUser: FirebaseUser? = null

    private var formatNumber: FormatNumber? = null

    private var unitRefrence: DatabaseReference? = null
    lateinit var dialogView: View

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        formatNumber = FormatNumber()

        val uid = intent.getStringExtra("uid")
        val unitId = intent.getStringExtra("unitId")

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener {
            val intent = Intent()
            intent.putExtra("from", "DetailProductActivity")
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

        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (user?.getPosition() == "admin") {
                            binding.delete.visibility = View.VISIBLE
                            binding.edit.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        unitRefrence = FirebaseDatabase.getInstance().reference
            .child("Units")
            .child(unitId!!)

        unitRefrence!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    val product: UnitList? = snapshot.getValue(UnitList::class.java)
                    binding.etUnitCode.setText(product?.getUnitCode().toString())
                    binding.etUnitType.setText(product?.getUnitType().toString())
                    binding.etMerk.setText(product?.getMerk().toString())
                    binding.etYom.setText(product?.getYom().toString())
                    binding.etStatus.setText(product?.getStatus().toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        binding.delete.setOnClickListener {
            if(unitId.isNotEmpty()) {
                productDeleted()
            }
            else{
                Helper().showToast("Data is Null!!", this)
            }
        }

        binding.edit.setOnClickListener {
            if(unitId.isNotEmpty()) {
                val intent = Intent(this, EditProductActivity::class.java)
                intent.putExtra("uid", uid)
                intent.putExtra("unitId", unitId)
                NavigationHelper().navigateToActivityCallback(this, intent)
            }
            else{
                Helper().showToast("Data is Null!!", this)
            }
        }

    }

    private fun productDeleted() {
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
            unitRefrence?.removeValue()
            alertDialog.dismiss()
            onBackPressedDispatcher.onBackPressed()
        }
    }
}