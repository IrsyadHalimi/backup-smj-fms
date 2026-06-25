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
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
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
import com.google.firebase.database.*
import com.smj.app.databinding.ActivityEditTaskBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.FormatNumber
import com.smj.app.helper.Helper
import com.smj.app.ui.task.model.TaskGroupList
import java.text.SimpleDateFormat
import java.util.*
import com.smj.app.R

class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTaskBinding
    private var firebaseUser: FirebaseUser? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var taskRefrence: DatabaseReference? = null
    private val myCalendar: Calendar = Calendar.getInstance()
    private var formatNumber: FormatNumber? = null
    lateinit var radioButton: RadioButton

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    //start category
    private lateinit var selectedCategory: String
    private var selectedCategoryIndex: Int = 0
    private val category = arrayOf(
        "Canvasing",
        "Prospecting",
        "Telemarketing",
        "Email",
        "Quotation",
        "Meeting",
        "Monitoring",
        "Dealing Win",
        "Dealing Lose",
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

    //start contact
    private val contactResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                binding.etContact.text = result.data?.getStringExtra("fullName")
                binding.etContactId.text = result.data?.getStringExtra("contactId")
            }
        }

    //start product
    private val productResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                binding.etProduct.text = result.data?.getStringExtra("productName")
                binding.etNominal.setText(result.data?.getStringExtra("productPrice"))
                binding.etProductId.text = result.data?.getStringExtra("productId")
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseAuth = FirebaseAuth.getInstance()
        formatNumber = FormatNumber()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val uid = intent.getStringExtra("uid")
        val taskId = intent.getStringExtra("taskId")

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener {
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

        dataTask(uid, taskId)

        binding.etCategory.setOnClickListener {
            getCategoryActivity()
        }

        binding.etReasonRejected.setOnClickListener {
            getReason()
        }

        binding.etContact.setOnClickListener {
            getContact()
        }

        binding.etProduct.setOnClickListener {
            getProduct()
        }

        val date = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, day)
            updateLabel()
        }
        binding.etDueDate.setOnClickListener {
            DatePickerDialog(
                this@EditTaskActivity,
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
                    .target(LatLng(latitude!!.toDouble(), longitude!!.toDouble()))
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

        binding.finish.setOnClickListener {
            addTask(uid, taskId)
        }

        binding.btnAddTask.setOnClickListener {
            addTask(uid, taskId)
        }
    }

    private fun dataTask(uid: String?, taskId: String?) {
        val refTask = FirebaseDatabase.getInstance().reference
            .child("TasksFleetPlan")
            .child(taskId.toString())
        refTask.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NewApi")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val taskGroup: TaskGroupList? = snapshot.getValue(TaskGroupList::class.java)
                    binding.etDate.text = taskGroup?.getDate().toString()
                    binding.etShiftTime.text = taskGroup?.getShiftTime().toString()
                    binding.etShift.text = taskGroup?.getShift().toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("taaksGET", "onCancelled")
            }

        })
    }

    private fun addTask(uid: String?, taskId: String?) {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        Helper().hideKeyboard(this@EditTaskActivity)
        if(binding.etCategory.text.isNotEmpty()
            && binding.etContact.text.isNotEmpty()
            && binding.etProduct.text.isNotEmpty()
            && binding.etNominal.text?.isNotEmpty() == true
            && binding.etPriority.isNotEmpty()
            && binding.etDueDate.text.isNotEmpty()
            && binding.etReminder.text.isNotEmpty()
            && binding.etStatus.isNotEmpty()
            && binding.tvTime.text.isNotEmpty()
            && binding.tvMapAddress.text.isNotEmpty()
        ) {
            val refTask = FirebaseDatabase.getInstance().reference

            val taskHashMap = HashMap<String, Any>()
            taskHashMap["uid"] = uid.toString()
            taskHashMap["taskId"] = taskId!!
            taskHashMap["category"] = binding.etCategory.text.toString()
            taskHashMap["reasonRejected"] = binding.etReasonRejected.text.toString()
            taskHashMap["contactId"] = binding.etContactId.text.toString()
            taskHashMap["productId"] = binding.etProductId.text.toString()
            val nominal = binding.etNominal.text.toString()
            taskHashMap["nominal"] = nominal.replace(",","")
            taskHashMap["nominal"] = binding.etNominal.text.toString()

            val checkedOption: Int = binding.etPriority.checkedRadioButtonId
            radioButton = findViewById(checkedOption)
            when(radioButton.text){
                "Low" -> taskHashMap["priority"] = 1
                "Medium" -> taskHashMap["priority"] = 2
                "High" -> taskHashMap["priority"] = 3
            }

            taskHashMap["dueDate"] = binding.etDueDate.text.toString()
            taskHashMap["reminder"] = binding.etReminder.text.toString()

            val checkedStatus: Int = binding.etStatus.checkedRadioButtonId
            radioButton = findViewById(checkedStatus)
            when(radioButton.text){
                "Todo" -> taskHashMap["status"] = 1
                "Doing" -> taskHashMap["status"] = 2
                "Done" -> taskHashMap["status"] = 3
            }

            taskHashMap["note"] = binding.etNote.text.toString()
            taskHashMap["createDate"] =  DateHelper().todayTime()
            taskHashMap["mapTime"] = binding.tvTime.text.toString()
            taskHashMap["mapAddress"] = binding.tvMapAddress.text.toString()

            taskHashMap["latitude"] = binding.etLatitude.text.toString().ifEmpty { "-2.548926" }
            taskHashMap["longitude"] = binding.etLongitude.text.toString().ifEmpty { "118.0148634" }

            databaseReference = refTask.child("Tasks").child(uid.toString()).child(taskId)
            databaseReference.updateChildren(taskHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.tvMapAddress.clearComposingText()
                        Helper().showToast("Adding data is Successful!", this@EditTaskActivity)
                        binding.llProgressBar.preload.visibility = View.GONE
                        onBackPressedDispatcher.onBackPressed()
//                        val intent = Intent(this, MainActivity::class.java)
//                        intent.putExtra("fragmentToLoad", "TaskFragment")
//                        intent.putExtra("uid", uid)
//                        intent.putExtra("taskId", taskId)
//                        NavigationHelper().navigateToActivityCallback(this, intent)
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Company is Something Wrong!!!", this@EditTaskActivity)
                    }
                }
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            Helper().showToast("All field is required!", this@EditTaskActivity)
        }
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
        binding.etDueDate.text = dateFormat.format(myCalendar.time)
    }

    private fun getProduct() {
        val intent = Intent(this, ProductListActivity::class.java)
        productResult.launch(intent)
    }

    private fun getContact() {
        val intent = Intent(this, ContactListActivity::class.java)
        contactResult.launch(intent)
    }

    private fun getReason() {
        selectedReason = reason[selectedReasonIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Reason")
            .setSingleChoiceItems(reason, selectedReasonIndex) { _, which ->
                selectedReasonIndex = which
                selectedReason = reason[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["reason"] = "$selectedReason"
                binding.etReasonRejected.text = "$selectedReason"
            }
            .show()
    }

    private fun getCategoryActivity() {
        selectedCategory = category[selectedCategoryIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Category")
            .setSingleChoiceItems(category, selectedCategoryIndex) { _, which ->
                selectedCategoryIndex = which
                selectedCategory = category[which]
            }
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["category"] = "$selectedCategory"
                binding.etCategory.text = "$selectedCategory"
                if(selectedCategory == "Dealing Lose"){
                    binding.llReason.visibility = View.VISIBLE
                }
                else{
                    binding.etReasonRejected.text = ""
                    binding.llReason.visibility = View.GONE
                }
            }
            .show()
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
        val list: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) as List<Address>
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
        mapFragment?.getMapAsync { mMap ->
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
                    .position(LatLng(latitude.toDouble(), longitude.toDouble()))
            )
        }
    }

    fun removeFragment(fragment: Fragment?) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction =
            fragmentManager.beginTransaction()
        if (fragment != null) {
            fragmentTransaction.remove(fragment)
        }
        fragmentTransaction.commit()
    }
}