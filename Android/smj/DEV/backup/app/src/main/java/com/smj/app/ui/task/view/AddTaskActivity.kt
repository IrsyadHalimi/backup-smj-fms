package com.smj.app.ui.task.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.databinding.ActivityAddTaskBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.FormatNumber
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.fleet.model.UnitList
import com.smj.app.ui.main.view.activity.MainActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding
    private var firebaseUser: FirebaseUser? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private val myCalendar: Calendar = Calendar.getInstance()
    private var formatNumber: FormatNumber? = null
    lateinit var radioButton: RadioButton

    private var usersRefrence: DatabaseReference? = null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    //start category
    private lateinit var selectedShift: String
    private var selectedShiftIndex: Int = 0
    private val shift = arrayOf(
        "Siang",
        "Malam",
    )
    //start reason
    private lateinit var selectedReason: String
    private var selectedReasonIndex: Int = 0
    private val reason = arrayOf(
        "Bad Timing",
        "Budget Issue",
        "Chose Competitor",
        "Client's Internal Issue",
        "No Response",
        "No Interested",
        "Not Qualified",
        "Product Doesn't Fit Client Need",
        "Others",
    )
    //start priority
    private lateinit var selectedPriority: String
    private var selectedPriorityIndex: Int = 0
    private val priority = arrayOf(
        "Low",
        "Medium",
        "Hight",
    )
    //start reminder
    private lateinit var selectedReminder: String
    private var selectedReminderIndex: Int = 0
    private val reminder = arrayOf(
        "5 Minutes Before",
        "15 Minutes Before",
        "1 Hour Before",
        "4 Hour Before",
        "1 Day Before",
        "2 Day Before",
        "1 Week Before",
    )
    //start status
    private lateinit var selectedStatus: String
    private var selectedStatusIndex: Int = 0
    private val status = arrayOf(
        "Todo",
        "Doing",
        "Done",
    )

    //start source
    private lateinit var selectedJarak: String
    private var selectedJarakIndex: Int = 0
    private val jarak = arrayOf(
        "0",
        "100",
        "200",
        "300",
        "400",
        "500",
        "600",
        "700",
        "800",
        "900",
        "1000",
        "1100",
        "1200",
        "1300",
        "1400",
        "1500",
        "1600",
        "1700",
        "1800",
        "1900",
        "2000",
        "2100",
        "2200",
        "2300",
        "2400",
        "2500",
        "2600",
        "2700",
        "2800",
        "2900",
        "3000",
        "3100",
        "3200",
        "3300",
        "3400",
        "3500",
    )

    //start timbunan
    private val timbunanResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            binding.llProgressBar.preload.visibility = View.GONE
            if (result.resultCode == Activity.RESULT_OK){
                binding.etTimbunan.text = result.data?.getStringExtra("locName")
                binding.etTimbunanCode.text = result.data?.getStringExtra("locId")
            }
        }

    //start galian
    private val galianResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            binding.llProgressBar.preload.visibility = View.GONE
            if (result.resultCode == Activity.RESULT_OK){
                binding.llProgressBar.preload.visibility = View.GONE
                binding.etGalian.text = result.data?.getStringExtra("locName")
                binding.etGalianCode.text = result.data?.getStringExtra("locId")
            }
        }

    //start loc
    private val locResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            binding.llProgressBar.preload.visibility = View.GONE
            if (result.resultCode == Activity.RESULT_OK){
                binding.llProgressBar.preload.visibility = View.GONE
                binding.etLoc.text = result.data?.getStringExtra("locName")
                binding.etLocCode.text = result.data?.getStringExtra("locId")
            }
        }

    //start shift
    private val shiftResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            binding.llProgressBar.preload.visibility = View.GONE
            if (result.resultCode == Activity.RESULT_OK){
                binding.llProgressBar.preload.visibility = View.GONE
                binding.etShift.text = result.data?.getStringExtra("shiftName")
                binding.etShiftId.text = result.data?.getStringExtra("shiftId")
                binding.etShiftTime.text = result.data?.getStringExtra("shiftTime")
            }
        }

    //start operator
    private val operatorResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            binding.llProgressBar.preload.visibility = View.GONE
            if (result.resultCode == Activity.RESULT_OK){
                binding.llProgressBar.preload.visibility = View.GONE
                binding.etOperator.text = result.data?.getStringExtra("userName")
                binding.etOperatorId.text = result.data?.getStringExtra("userId")
                binding.etOperatorPosition.text = result.data?.getStringExtra("userPosition")
            }
        }

    //start pengawas
    private val pengawasResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            binding.llProgressBar.preload.visibility = View.GONE
            if (result.resultCode == Activity.RESULT_OK){
                binding.llProgressBar.preload.visibility = View.GONE
                binding.etPengawas.text = result.data?.getStringExtra("userName")
                binding.etPengawasId.text = result.data?.getStringExtra("userId")
                binding.etPengawasPosition.text = result.data?.getStringExtra("userPosition")
            }
        }

    private var crossLoading: String? = null
    //start product
    private val exaResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            binding.llProgressBar.preload.visibility = View.GONE
            if (result.resultCode == Activity.RESULT_OK){
                binding.llProgressBar.preload.visibility = View.GONE
                binding.etExaCode.text = result.data?.getStringExtra("unitCode")
                binding.etExa.text = result.data?.getStringExtra("unitType")
                binding.etExaId.text = result.data?.getStringExtra("unitId")
            }
            if (result.data?.getStringExtra("sopirId")?.isNotEmpty() == true && result.data?.getStringExtra("sopir")?.isNotEmpty() == true) {
                binding.etOperator.text = result.data?.getStringExtra("sopir")
                binding.etOperator.isEnabled = false
                binding.etOperator.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_fill_grey)
                binding.etOperator.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.etOperator.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                binding.etOperatorId.text = result.data?.getStringExtra("sopirId")
                binding.etOperatorPosition.text = result.data?.getStringExtra("userPosition")
                binding.etOperatorPosition.visibility = View.GONE
                binding.etLoc.text = result.data?.getStringExtra("loc")
                binding.etLoc.isEnabled = false
                binding.etLoc.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_fill_grey)
                binding.etLoc.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.etLoc.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                binding.etLocCode.text = result.data?.getStringExtra("locode")
                binding.etGalian.text = result.data?.getStringExtra("galianName")
                binding.etGalian.isEnabled = false
                binding.etGalian.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_fill_grey)
                binding.etGalian.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.etGalian.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                binding.etGalianCode.text = result.data?.getStringExtra("galianCode")
                binding.etTimbunan.text = result.data?.getStringExtra("timbunanName")
                binding.etTimbunan.isEnabled = false
                binding.etTimbunan.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_fill_grey)
                binding.etTimbunan.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.etTimbunan.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                binding.etTimbunanCode.text = result.data?.getStringExtra("timbunaCode")
                binding.etJarak.setText(result.data?.getStringExtra("jarak"))
                binding.etJarak.isEnabled = false
                binding.etJarak.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_fill_grey)
                binding.etJarak.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.etTargetRit.setText("0")
                binding.etTargetRit.isEnabled = false
                binding.etTargetRit.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_fill_grey)
                binding.etTargetRit.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.etPlan.setText("0")
                binding.etPlan.isEnabled = false
                binding.etPlan.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_fill_grey)
                binding.etPlan.setTextColor(ContextCompat.getColor(this, R.color.grey))
                crossLoading = "CrossLoading"
            }
            else{
                binding.etOperator.text = ""
                binding.etOperator.isEnabled = true
                binding.etOperator.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_grey)
                binding.etOperator.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.etOperator.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.ic_search), null)
                binding.etOperatorId.text = ""
                binding.etOperatorPosition.text = ""
                binding.etOperatorPosition.visibility = View.VISIBLE
                binding.etLoc.text = ""
                binding.etLoc.isEnabled = true
                binding.etLoc.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_grey)
                binding.etLoc.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.etLoc.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.ic_search), null)
                binding.etLocCode.text = ""
                binding.etGalian.text = ""
                binding.etGalian.isEnabled = true
                binding.etGalian.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_grey)
                binding.etGalian.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.etGalian.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.ic_search), null)
                binding.etGalianCode.text = ""
                binding.etTimbunan.text = ""
                binding.etTimbunan.isEnabled = true
                binding.etTimbunan.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_grey)
                binding.etTimbunan.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.etTimbunan.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.ic_search), null)
                binding.etTimbunanCode.text = ""
                binding.etJarak.setText("")
                binding.etJarak.isEnabled = true
                binding.etJarak.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_grey)
                binding.etJarak.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.etTargetRit.setText("")
                binding.etTargetRit.isEnabled = true
                binding.etTargetRit.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_grey)
                binding.etTargetRit.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.etPlan.setText("")
                binding.etPlan.isEnabled = true
                binding.etPlan.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_grey)
                binding.etPlan.setTextColor(ContextCompat.getColor(this, R.color.black))
                crossLoading = null
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("PrivateResource", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseAuth = FirebaseAuth.getInstance()
        formatNumber = FormatNumber()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener {
            val intent = Intent()
            intent.putExtra("from", "AddTaskActivity")
            setResult(Activity.RESULT_OK, intent)
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        binding.etDate.text = currentDate

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

        binding.etShift.setOnClickListener {
            getShiftActivity()
        }

        binding.etPengawas.setOnClickListener {
            getPengawas()
        }

        binding.etOperator.setOnClickListener {
            getOperator()
        }

        binding.etExa.setOnClickListener {
            getExa()
        }

        binding.etLoc.setOnClickListener {
            getLoc()
        }

        binding.etGalian.setOnClickListener {
            getGalian()
        }

        binding.etTimbunan.setOnClickListener {
            getTimbunan()
        }

        val date = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, day)
            updateLabel()
        }
        binding.ivDate.setOnClickListener {
            DatePickerDialog(
                this@AddTaskActivity,
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnAddLocation.setOnClickListener {
            getLocation()
        }

        binding.btnClearLocation.setOnClickListener {
            binding.tvTime.text = ""
            binding.tvMapAddress.text = ""
            binding.showAddress.visibility = View.GONE
            binding.btnAddLocation.visibility = View.VISIBLE
            binding.btnClearLocation.visibility = View.GONE
            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
            mapFragment!!.getMapAsync { mMap ->
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

                mMap.clear() //clear old markers

                val latitude = "-2.548926"
                val longitude = "118.0148634"
                binding.etLatitude.text = latitude
                binding.etLongitude.text = longitude

                val googlePlex = CameraPosition.builder()
                    .target(LatLng(latitude.toDouble(), longitude.toDouble()))
                    .zoom(3f)
                    .bearing(0f)
                    .tilt(45f)
                    .build()

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null)
            }
        }

        binding.etReminder.setOnClickListener {
            getReminder()
        }

        binding.btnAddTask.setOnClickListener {
            addTask()
        }

        binding.etJarak.setOnClickListener {
            selectedJarak = jarak[selectedJarakIndex]
            MaterialAlertDialogBuilder(this)
                .setTitle("Select Jarak (Meter)")
                .setSingleChoiceItems(jarak, selectedJarakIndex) { _, which ->
                    selectedJarakIndex = which
                    selectedJarak = jarak[which]
                }
                .setPositiveButton("Choice") { _, _ ->
                    val mapData = HashMap<String, Any>()
                    mapData["source"] = "$selectedJarak"
                    binding.etJarak.text = "$selectedJarak"
                }
                .show()
        }

        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (
                            user?.getPosition() == "Foreman Produksi"
                            || user?.getPosition() == "Sr Foreman Produksi"
                            || user?.getPosition() == "Jr Foreman Produksi"
                            || user?.getPosition() == "SPV Produksi"
                            || user?.getPosition() == "Jr SPV Produksi"
                        ) {
                            binding.etPengawasId.text = user.getUid().toString()
                            binding.etPengawasPosition.text = user.getPosition()
                            binding.etPengawas.text = user.getFullName()
                            binding.etPengawas.isEnabled = false
                            binding.etPengawas.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                        }

                        if (user?.getDept() == "HRGA") {
                            binding.llJarak.visibility = View.GONE
                            binding.llGalian.visibility = View.GONE
                            binding.llTimbunan.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

    private fun addTask() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        Helper().hideKeyboard(this@AddTaskActivity)
//        Toast.makeText(this, binding.etGalian.text, Toast.LENGTH_LONG).show()
        if(binding.etShift.text.isNotEmpty()
            && binding.etDate.text.isNotEmpty()
            && binding.etShift.text.isNotEmpty()
            && binding.etPengawas.text.isNotEmpty()
            && binding.etOperator.text.isNotEmpty()
            && binding.etExaCode.text.isNotEmpty()
//            && binding.etPlan.text?.isNotEmpty() == true
//            && binding.etJarak.text?.isNotEmpty() == true
//            && binding.etTargetRit.text?.isNotEmpty() == true
            && binding.etLoc.text?.isNotEmpty() == true
//            && binding.etGalian.text?.isNotEmpty() == true
//            && binding.etTimbunan.text?.isNotEmpty() == true
        ) {
            val refTask = FirebaseDatabase.getInstance().reference
            val taskId = refTask.push().key

            val taskHashMap = HashMap<String, Any>()
            taskHashMap["uid"] = firebaseAuth.currentUser!!.uid
            taskHashMap["taskId"] = taskId!!
            if (crossLoading.equals("CrossLoading")) {
                taskHashMap["status"] = crossLoading.toString()
            }
            else {
                taskHashMap["status"] = intent.getStringExtra("status").toString()
            }
            taskHashMap["date"] = binding.etDate.text.toString()
            taskHashMap["shift"] = binding.etShift.text.toString()
            taskHashMap["shiftId"] = binding.etShiftId.text.toString()
            taskHashMap["shiftTime"] = binding.etShiftTime.text.toString()
            taskHashMap["pengawasName"] = binding.etPengawas.text.toString()
            taskHashMap["pengawasId"] = binding.etPengawasId.text.toString()
            taskHashMap["operatorName"] = binding.etOperator.text.toString()
            taskHashMap["operatorId"] = binding.etOperatorId.text.toString()
            taskHashMap["exaCode"] = binding.etExaCode.text.toString()
            taskHashMap["exaId"] = binding.etExaId.text.toString()
            taskHashMap["locationCode"] = binding.etLocCode.text.toString()
            taskHashMap["locationName"] = binding.etLoc.text.toString()
            taskHashMap["galianCode"] = binding.etGalianCode.text.toString()
            taskHashMap["galianName"] = binding.etGalian.text.toString()
            taskHashMap["timbunanCode"] = binding.etTimbunanCode.text.toString()
            taskHashMap["timbunanName"] = binding.etTimbunan.text.toString()
            taskHashMap["plan"] = binding.etPlan.text.toString()
            taskHashMap["jarak"] = binding.etJarak.text.toString()
            taskHashMap["targetRit"] = binding.etTargetRit.text.toString()
            taskHashMap["createBy"] = firebaseAuth.currentUser!!.uid
            taskHashMap["updateBy"] = ""
            taskHashMap["createDate"] =  DateHelper().todayTime()
            taskHashMap["updateDate"] =  ""

            databaseReference = refTask.child("TasksFleetPlan").child(taskId)
            databaseReference.setValue(taskHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val productRefrence = FirebaseDatabase.getInstance().reference
                            .child("Units")
                            .child(binding.etExaId.text.toString())

                        productRefrence.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists())
                                {
                                    val unit: UnitList? = snapshot.getValue(UnitList::class.java)

                                    val refUnit = FirebaseDatabase.getInstance().reference
                                    val unitHashMap = HashMap<String, Any>()
                                    unitHashMap["uid"] = unit?.getUid().toString()
                                    unitHashMap["unitId"] = binding.etExaId.text.toString()
                                    unitHashMap["unitCode"] = unit?.getUnitCode().toString()
                                    unitHashMap["unitType"] = unit?.getUnitType().toString()
                                    unitHashMap["merk"] = unit?.getMerk().toString()
                                    unitHashMap["yom"] =  unit?.getYom().toString()
                                    unitHashMap["status"] =  "USED"
                                    val databaseReference = refUnit.child("Units").child(binding.etExaId.text.toString())
                                    databaseReference.updateChildren(unitHashMap)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Helper().showToast("Adding data is Successful!", this@AddTaskActivity)
                                                binding.llProgressBar.preload.visibility = View.GONE
                                                val intent = Intent()
                                                intent.putExtra("from", "AddTaskActivity")
                                                setResult(Activity.RESULT_OK, intent)
                                                onBackPressedDispatcher.onBackPressed()
                                            }
                                            else{
                                                binding.llProgressBar.preload.visibility = View.GONE
                                                Helper().showToast("Data Product is Something Wrong!!!", this@AddTaskActivity)
                                            }
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Company is Something Wrong!!!", this@AddTaskActivity)
                    }
                }
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            Helper().showToast("All field is required!", this@AddTaskActivity)
        }
    }

    private fun getStatusTask() {
        selectedStatus = status[selectedStatusIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Status")
            .setSingleChoiceItems(status, selectedStatusIndex) { _, which ->
                selectedStatusIndex = which
                selectedStatus = status[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["status"] = "$selectedStatus"
//                binding.etStatus.text = "$selectedStatus"
            }
            .show()
    }

    private fun getReminder() {
        selectedReminder = reminder[selectedReminderIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Reminder")
            .setSingleChoiceItems(reminder, selectedReminderIndex) { _, which ->
                selectedReminderIndex = which
                selectedReminder = reminder[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["reminder"] = "$selectedReminder"
                binding.etReminder.text = "$selectedReminder"
            }
            .show()
    }

    private fun updateLabel() {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        binding.etDate.text = dateFormat.format(myCalendar.time)
    }

    private fun getPriority() {
        selectedPriority = priority[selectedPriorityIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Priority")
            .setSingleChoiceItems(priority, selectedPriorityIndex) { _, which ->
                selectedPriorityIndex = which
                selectedPriority = priority[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["priority"] = "$selectedPriority"
//                binding.etPriority.text = "$selectedPriority"
            }
            .show()
    }

    private fun getExa() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        binding.etExa.text = ""
        binding.etExaId.text = ""
        binding.etExaCode.text = ""
        crossLoading = null
        val intent = Intent(this, ProductListActivity::class.java)
        intent.putExtra("unitType", "EXCAVATOR")
        intent.putExtra("forUse", "pengawas")
        exaResult.launch(intent)
    }

    private fun getLoc() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val dataFrom = "loc"
        getUserById(firebaseAuth.currentUser?.uid.toString(), dataFrom)
    }

    private fun getTimbunan() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val dataFrom = "timbunan"
        getUserById(firebaseAuth.currentUser?.uid.toString(), dataFrom)
    }

    private fun getGalian() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val dataFrom = "galian"
        getUserById(firebaseAuth.currentUser?.uid.toString(), dataFrom)
    }

    private fun getPengawas() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val intent = Intent(this, ContactListActivity::class.java)
        intent.putExtra("position", "pengawas")
        intent.putExtra("taskId", "")
        pengawasResult.launch(intent)
    }

    private fun getOperator() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        crossLoading = ""
        val intent = Intent(this, ContactListActivity::class.java)
        intent.putExtra("position", "operator")
        intent.putExtra("forUse", "pengawas")
        intent.putExtra("taskId", "")
        operatorResult.launch(intent)
    }

    private fun getShiftActivity() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        val intent = Intent(this, ShiftListActivity::class.java)
        shiftResult.launch(intent)
    }


    // get location
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double) {
        Helper().showToast("getCompleteAddressString", this)
        val geocoder = Geocoder(this@AddTaskActivity, Locale.getDefault())
        geocoder.getFromLocation(LATITUDE, LONGITUDE, 1
        ) {p0 ->
            Log.i("getCompleteAddressString", p0.toString())
            showMap(LATITUDE, LONGITUDE)
            val sdf = SimpleDateFormat("dd-M-yyyy hh:mm:ss")
            val currentDate = sdf.format(Date())
            binding.showAddress.visibility = View.VISIBLE
            binding.tvTime.text = currentDate
            binding.tvMapAddress.text = p0[0].thoroughfare+", "+
                    p0[0].subAdminArea+", "+
                    p0[0].countryName+" "+
                    p0[0].postalCode
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        if (Build.VERSION.SDK_INT >= 33) {
                            getCompleteAddressStringOld(location.latitude, location.longitude)
                        }
                        else {
                            getCompleteAddressStringOld(location.latitude, location.longitude)
                        }
                    }
                }
            } else {
                Helper().showToast("Please turn on location", this)
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun getCompleteAddressStringOld(latitude: Double, longitude: Double) {
        binding.etLatitude.text = latitude.toString()
        binding.etLongitude.text = longitude.toString()
        val geocoder = Geocoder(this, Locale.getDefault())
        val list: List<Address> =
            geocoder.getFromLocation(latitude, longitude, 1) as List<Address>
        binding.apply {
            showMap(latitude, longitude)
            val sdf = SimpleDateFormat("dd-M-yyyy hh:mm:ss")
            val currentDate = sdf.format(Date())
            binding.showAddress.visibility = View.VISIBLE
            binding.tvTime.text = currentDate
            binding.tvMapAddress.text = list[0].getAddressLine(0)

            binding.showAddress.visibility = View.VISIBLE
            binding.btnAddLocation.visibility = View.GONE
            binding.btnClearLocation.visibility = View.VISIBLE
        }
    }

    //show map
    private fun showMap(latitude: Double, longitude: Double){
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment!!.getMapAsync { mMap ->
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

            mMap.clear() //clear old markers

            val latitude = latitude
            val longitude = longitude

            val googlePlex = CameraPosition.builder()
                .target(LatLng(latitude!!.toDouble(), longitude!!.toDouble()))
                .zoom(18f)
                .bearing(0f)
                .tilt(45f)
                .build()

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null)

            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
            )
        }
    }

    private fun getUserById(userId: String, dataFrom: String) {
        firebaseUser = firebaseAuth.currentUser
        usersRefrence = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        usersRefrence!!.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    val user: Users? = p0.getValue(Users::class.java)
                    if (dataFrom == "loc") {
                        val intent = Intent(this@AddTaskActivity, LocListActivity::class.java)
                        intent.putExtra("position", user?.getPosition())
                        locResult.launch(intent)
                    }
                    if (dataFrom == "galian") {
                        val intent = Intent(this@AddTaskActivity, GalianListActivity::class.java)
                        intent.putExtra("position", user?.getPosition())
                        galianResult.launch(intent)
                    }
                    if (dataFrom == "timbunan") {
                        val intent = Intent(this@AddTaskActivity, TimbunanListActivity::class.java)
                        intent.putExtra("position", user?.getPosition())
                        timbunanResult.launch(intent)
                    }
                }
                else{
                    binding.llProgressBar.preload.visibility = View.GONE
                    Toast.makeText(this@AddTaskActivity, "Something wrong..", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                binding.llProgressBar.preload.visibility = View.GONE
            }
        })
    }
}

