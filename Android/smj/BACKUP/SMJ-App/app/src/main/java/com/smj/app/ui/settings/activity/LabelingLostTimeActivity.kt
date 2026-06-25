package com.smj.app.ui.settings.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.smj.app.databinding.ActivityLabelingLostTimeBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.Helper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.settings.adapter.LabelingLostTimeAdapter
import com.smj.app.ui.settings.model.LabelingLostTimeList

class LabelingLostTimeActivity : AppCompatActivity(), LabelingLostTimeAdapter.LabelingLostTimeAdapterCallback {
    private lateinit var binding: ActivityLabelingLostTimeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerViewLabelingLostTime: RecyclerView

    var userRole: String? = null

    private var labelingLostTimeList: ArrayList<LabelingLostTimeList>? = null
    lateinit var dialogView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabelingLostTimeBinding.inflate(layoutInflater)
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

        binding.tvLostTime.text = "Lost Time "+intent.getStringExtra("filter").toString()

        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)
                        userRole = user?.getPosition()
                        if (userRole == "ADMIN" || userRole == "root") {
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

        recyclerViewLabelingLostTime = binding.recyclerViewLabelingLostTime
        recyclerViewLabelingLostTime.setHasFixedSize(true)
        recyclerViewLabelingLostTime.layoutManager = LinearLayoutManager(this)

        labelingLostTimeList = ArrayList()

        val refContact = FirebaseDatabase.getInstance().reference.child("LabelingLostTime")
            .orderByChild("labelFilter")
            .equalTo(intent.getStringExtra("filter").toString())
        refContact.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (labelingLostTimeList as ArrayList).clear()
                if (snapshot.exists()) {
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewLabelingLostTime.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val labeling = dataSnapshot.getValue(LabelingLostTimeList::class.java)
                        (labelingLostTimeList as ArrayList).add(labeling!!)
                    }
                    val labelingLostTimeAdapter =
                        LabelingLostTimeAdapter(
                            this@LabelingLostTimeActivity,
                            labelingLostTimeList!!,
                            this@LabelingLostTimeActivity,
                            userRole
                        )
                    recyclerViewLabelingLostTime.layoutManager = LinearLayoutManager(this@LabelingLostTimeActivity)
                    recyclerViewLabelingLostTime.adapter = labelingLostTimeAdapter
                } else {
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewLabelingLostTime.visibility = View.GONE
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
        dialogView = inflater.inflate(R.layout.layout_form_label_lost_time, null)

        val nameLabel = dialogView.findViewById<EditText>(R.id.et_name_label)
        nameLabel.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val addLabel = dialogView.findViewById<LinearLayout>(R.id.add_label)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
        }

        addLabel.setOnClickListener {
            addingLabelLostTime(nameLabel.text.toString(), dialogView, alertDialog)
        }

    }

    @SuppressLint("CutPasteId")
    private fun addingLabelLostTime(nameLabel: String, dialogView: View, alertDialog: AlertDialog) {
        if (nameLabel == "") {
            dialogView.findViewById<TextView>(R.id.tv_name_label).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tv_name_label).text = "Field is required!"
        }
        if (nameLabel != "") {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            alertDialog.dismiss()
            val refLocation = FirebaseDatabase.getInstance().reference
            val labelId = refLocation.push().key

            val labelHashMap = HashMap<String, Any>()
            labelHashMap["uid"] = firebaseAuth.currentUser!!.uid
            labelHashMap["labelingLostId"] = labelId!!
            labelHashMap["labelingLostName"] = nameLabel
            labelHashMap["labelFilter"] = intent.getStringExtra("filter").toString()
            labelHashMap["createDate"] = DateHelper().todayTime()

            databaseReference = refLocation.child("LabelingLostTime").child(labelId)
            databaseReference.updateChildren(labelHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Helper().showToast("Adding data is Successful!", this@LabelingLostTimeActivity)
                        binding.llProgressBar.preload.visibility = View.GONE
                    } else {
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast(
                            "Data Label Lost Time is Something Wrong!!!",
                            this@LabelingLostTimeActivity
                        )
                    }
                }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showEditialog(labelingLostId: String?, labelingLostName: String?) {
        val dialogBuilder = MaterialAlertDialogBuilder(
            this,
            R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog
        )
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_form_label_lost_time, null)

        val nameLabel = dialogView.findViewById<EditText>(R.id.et_name_label)
        nameLabel.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val addLabel = dialogView.findViewById<LinearLayout>(R.id.add_label)

        dialogView.findViewById<TextView>(R.id.txt_facebook).text = "Update Labe Lost Time"
        nameLabel.setText(labelingLostName)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
        }

        addLabel.setOnClickListener {
            updateLabel(nameLabel.text.toString(), dialogView, alertDialog, labelingLostId)
        }
    }

    @SuppressLint("CutPasteId", "SetTextI18n")
    private fun updateLabel(
        nameLabel: String,
        dialogView: View,
        alertDialog: AlertDialog,
        labelingLostId: String?
    ) {
        if (nameLabel == "") {
            dialogView.findViewById<TextView>(R.id.tv_name_label).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tv_name_label).text = "Field is required!"
        }
        if (nameLabel != "") {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            alertDialog.dismiss()
            val refShift = FirebaseDatabase.getInstance().reference

            val labelHashMap = HashMap<String, Any>()
            labelHashMap["uid"] = firebaseAuth.currentUser!!.uid
            labelHashMap["labelingLostId"] = labelingLostId!!
            labelHashMap["labelingLostName"] = nameLabel
            labelHashMap["labelFilter"] = intent.getStringExtra("filter").toString()
            labelHashMap["createDate"] = DateHelper().todayTime()

            databaseReference = refShift.child("LabelingLostTime").child(labelingLostId)
            databaseReference.updateChildren(labelHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Helper().showToast(
                            "Update data is Successful!",
                            this@LabelingLostTimeActivity
                        )
                        binding.llProgressBar.preload.visibility = View.GONE
                    } else {
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast(
                            "Data Label is Something Wrong!!!",
                            this@LabelingLostTimeActivity
                        )
                    }
                }
        }
    }

    override fun onDetail(
        adapterPosition: Int,
        labelingLostTimeList: ArrayList<LabelingLostTimeList>
    ) {
        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (user?.getPosition() == "ADMIN" || user?.getPosition() == "root") {
                            showEditialog(
                                labelingLostTimeList[adapterPosition].getLabelingLostId(),
                                labelingLostTimeList[adapterPosition].getLabelingLostName()
                            )
                        }
                        else{
                            val alertBuilder = android.app.AlertDialog.Builder(this@LabelingLostTimeActivity)
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