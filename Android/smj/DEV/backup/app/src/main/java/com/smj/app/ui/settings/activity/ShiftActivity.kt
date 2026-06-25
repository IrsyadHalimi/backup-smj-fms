package com.smj.app.ui.settings.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
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
import com.smj.app.databinding.ActivityShiftBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.Helper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.fleet.view.EditProductActivity
import com.smj.app.ui.settings.adapter.ShiftAdapter
import com.smj.app.ui.settings.model.ShiftList
import java.text.SimpleDateFormat
import java.util.Calendar

class ShiftActivity : AppCompatActivity(), ShiftAdapter.ShiftAdapterCallback {
    private lateinit var binding: ActivityShiftBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerViewShift: RecyclerView

    private var shiftList: ArrayList<ShiftList>? = null
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
        binding = ActivityShiftBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener {
            val intent = Intent()
            intent.putExtra("from", "ShiftActivity")
            setResult(Activity.RESULT_OK, intent)
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)

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

        recyclerViewShift = binding.recyclerViewShiftList
        recyclerViewShift.setHasFixedSize(true)
        recyclerViewShift.layoutManager = LinearLayoutManager(this)

        shiftList = ArrayList()

        val refContact = FirebaseDatabase.getInstance().reference.child("Shift")
        refContact.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (shiftList as ArrayList).clear()
                if(snapshot.exists()){
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewShift.visibility = View.VISIBLE
                    binding.llEmpty.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val shift = dataSnapshot.getValue(ShiftList::class.java)
                        (shiftList as ArrayList).add(shift!!)
                    }
                    val shiftAdapter = ShiftAdapter(this@ShiftActivity, shiftList!!, this@ShiftActivity)
                    recyclerViewShift.layoutManager = LinearLayoutManager(this@ShiftActivity)
                    recyclerViewShift.adapter = shiftAdapter
                }
                else{
                    binding.shimmerViewContainer.visibility = View.GONE
                    recyclerViewShift.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFormDialog() {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_form_shift, null)

        val nameShift = dialogView.findViewById<EditText>(R.id.et_name_shift)
        nameShift.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }
        val btnTimeStart = dialogView.findViewById<ImageView>(R.id.iv_time_start)
        val timeStart = dialogView.findViewById<TextView>(R.id.et_time_start)
        val timeEnd = dialogView.findViewById<TextView>(R.id.et_time_end)
        val btnTimeEnd = dialogView.findViewById<ImageView>(R.id.iv_time_end)
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val addShift = dialogView.findViewById<LinearLayout>(R.id.add_shift)

        btnTimeStart.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                timeStart.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnTimeEnd.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                timeEnd.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
        }

        addShift.setOnClickListener {
            addingShift(nameShift.text.toString(), timeStart.text.toString(), timeEnd.text.toString(), dialogView, alertDialog)
        }

    }

    @SuppressLint("CutPasteId")
    private fun addingShift(
        shift: String,
        start: String,
        end: String,
        dialogView: View,
        alertDialog: AlertDialog
    ) {
        if (shift == ""){
            dialogView.findViewById<TextView>(R.id.tv_name_shift).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tv_name_shift).text = "Field is required!"
        }
        if (start == ""){
            dialogView.findViewById<TextView>(R.id.tv_time_start).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tv_time_start).text = "Field is required!"
        }
        if (end == ""){
            dialogView.findViewById<TextView>(R.id.tv_time_end).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tv_time_end).text = "Field is required!"
        }
        if (shift != "" && start != "" && end != ""){
            binding.llProgressBar.preload.visibility = View.VISIBLE
            alertDialog.dismiss()
            val refShift = FirebaseDatabase.getInstance().reference
            val shiftId = refShift.push().key

            val shiftHashMap = HashMap<String, Any>()
            shiftHashMap["uid"] = firebaseAuth.currentUser!!.uid
            shiftHashMap["shiftId"] = shiftId!!
            shiftHashMap["shiftName"] = shift
            shiftHashMap["shiftTimeStart"] = start
            shiftHashMap["shiftTimeEnd"] = end
            shiftHashMap["createDate"] =  DateHelper().todayTime()

            databaseReference = refShift.child("Shift").child(shiftId)
            databaseReference.updateChildren(shiftHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Helper().showToast("Adding data is Successful!", this@ShiftActivity)
                        binding.llProgressBar.preload.visibility = View.GONE
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Product is Something Wrong!!!", this@ShiftActivity)
                    }
                }
        }
    }

    @SuppressLint("CutPasteId")
    private fun updateShift(
        shift: String,
        start: String,
        end: String,
        dialogView: View,
        alertDialog: AlertDialog,
        shiftId: String?
    ) {
        if (shift == ""){
            dialogView.findViewById<TextView>(R.id.tv_name_shift).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tv_name_shift).text = "Field is required!"
        }
        if (start == ""){
            dialogView.findViewById<TextView>(R.id.tv_time_start).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tv_time_start).text = "Field is required!"
        }
        if (end == ""){
            dialogView.findViewById<TextView>(R.id.tv_time_end).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.tv_time_end).text = "Field is required!"
        }
        if (shift != "" && start != "" && end != ""){
            binding.llProgressBar.preload.visibility = View.VISIBLE
            alertDialog.dismiss()
            val refShift = FirebaseDatabase.getInstance().reference
            val shiftId = shiftId

            val shiftHashMap = HashMap<String, Any>()
            shiftHashMap["uid"] = firebaseAuth.currentUser!!.uid
            shiftHashMap["shiftId"] = shiftId!!
            shiftHashMap["shiftName"] = shift
            shiftHashMap["shiftTimeStart"] = start
            shiftHashMap["shiftTimeEnd"] = end
            shiftHashMap["createDate"] =  DateHelper().todayTime()

            databaseReference = refShift.child("Shift").child(shiftId)
            databaseReference.updateChildren(shiftHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Helper().showToast("Update data is Successful!", this@ShiftActivity)
                        binding.llProgressBar.preload.visibility = View.GONE
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Shift is Something Wrong!!!", this@ShiftActivity)
                    }
                }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showEditialog(
        shiftId: String?,
        shiftName: String?,
        shiftTimeStart: String?,
        shiftTimeEnd: String?
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_form_shift, null)

        val nameShift = dialogView.findViewById<EditText>(R.id.et_name_shift)
        nameShift.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }
        val btnTimeStart = dialogView.findViewById<ImageView>(R.id.iv_time_start)
        val timeStart = dialogView.findViewById<TextView>(R.id.et_time_start)
        val timeEnd = dialogView.findViewById<TextView>(R.id.et_time_end)
        val btnTimeEnd = dialogView.findViewById<ImageView>(R.id.iv_time_end)
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)
        val addShift = dialogView.findViewById<LinearLayout>(R.id.add_shift)

        dialogView.findViewById<TextView>(R.id.txt_facebook).text = "Update Shift"
        timeStart.text = shiftTimeStart
        timeEnd.text = shiftTimeEnd
        nameShift.setText(shiftName)

        btnTimeStart.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                timeStart.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnTimeEnd.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                timeEnd.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
        }

        addShift.setOnClickListener {
            updateShift(nameShift.text.toString(), timeStart.text.toString(), timeEnd.text.toString(), dialogView, alertDialog, shiftId)
        }

    }

    override fun onDetail(adapterPosition: Int, shiftList: ArrayList<ShiftList>) {
        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (user?.getPosition() == "ADMIN" || user?.getPosition() == "root") {
                            showEditialog(
                                shiftList[adapterPosition].getShiftId(),
                                shiftList[adapterPosition].getShiftName(),
                                shiftList[adapterPosition].getShiftTimeStart(),
                                shiftList[adapterPosition].getshiftTimeEnd()
                            )
                        }
                        else{
                            val alertBuilder = android.app.AlertDialog.Builder(this@ShiftActivity)
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