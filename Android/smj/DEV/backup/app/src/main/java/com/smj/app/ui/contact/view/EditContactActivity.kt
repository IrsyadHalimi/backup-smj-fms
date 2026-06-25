package com.smj.app.ui.contact.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
import com.smj.app.databinding.ActivityEditContactBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.FormatNumber
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.ui.google.view.MapActivity
import com.smj.app.utils.session.SessionManager
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.text.SimpleDateFormat
import java.util.*
import com.smj.app.R


class EditContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditContactBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var context: Context? = null

    private var formatNumber: FormatNumber? = null
    private lateinit var phoneNumberUtil: PhoneNumberUtil

    private lateinit var databaseReference: DatabaseReference

    private var firebaseUser: FirebaseUser? = null
    private var usersRefrence: DatabaseReference? = null
    private var contactRefrence: DatabaseReference? = null
    private var companyRefrence: DatabaseReference? = null
    private var contactsSharedRefrence: DatabaseReference? = null
    lateinit var dialogView: View

    lateinit var radioButton: RadioButton

    private var latitudeStore: String? = null
    private var longitudeStore: String? = null

    lateinit var mMap: GoogleMap
    private var point: LatLng? = null

    private val myCalendar: Calendar = Calendar.getInstance()

    var lastChar = " "

    //start source
    private lateinit var selectedSource: String
    private var selectedSourceIndex: Int = 0
    private val source = arrayOf(
        "Advertisement",
        "Event",
        "Multichannel Whatsapp",
        "Referral",
        "Telepon",
        "Website",
        "Others",
    )

    //start status
    private lateinit var selectedStatus: String
    private var selectedStatusIndex: Int = 0
    private val statusCustomer = arrayOf(
        "Lead",
        "Opportunity",
        "Negotiation",
        "Proposal",
        "Accept",
        "Reject"
    )

    //start religion
    private lateinit var selectedReligion: String
    private var selectedReligionIndex: Int = 0
    private val religion = arrayOf(
        "Moslem",
        "Christian",
        "Catholic",
        "Buddha",
        "Hindu",
        "Konghucu",
        "Other"
    )

    //start source
    private lateinit var selectedPosition: String
    private var selectedPositionIndex: Int = 0
    private val position = arrayOf(
        "ADMIN",
        "ADMIN HR",
        "ADMIN RM",
        "ADMIN MCC",
        "DIREKTUR",
        "GM Produksi",
        "Manager Produksi",
        "SPI Produksi",
        "Jr.SPI Produksi",
        "SPV Produksi",
        "Jr SPV Produksi",
        "Sr Foreman Produksi",
        "Jr Foreman Produksi",
        "Foreman Produksi",
        "MT MCC Produksi",
        "MCC Field",
        "Operator PC 200",
        "Operator PC 750",
        "Operator PC 1250",
        "Operator EC 395",
        "Operator EC 480",
        "Operator EC 750",
        "Operator CAT 395",
        "Operator CAT 773E",
        "Operator CAT 773E & HD 465",
        "Operator HD 465",
        "Operator ADT",
        "Operator DT2809",
        "Operator D8",
        "Operator D8 T",
        "Operator DZ 375",
        "Operator DZ 85 SS",
        "Operator GD 705",
        "Operator GD 825",
        "Operator GD 14 M",
        "Driver Hino 500",
        "Driver WT",
        "Driver Isuzu",
        "Jr. SPI Maintenance Road"
    )

    var latitude = ""
    var longitude = ""
    var uid = ""
    var contactId = ""
    var companyId = ""
    var userKey = ""
    var firebaseUserID = ""
    var createBy = ""
    var createDate = ""

    private val secondActivityWithResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                binding.etCompanyAddress.setText(result.data?.getStringExtra("address"))
                binding.etCompanyState.setText(result.data?.getStringExtra("countryName"))
                binding.etCompanyCity.setText(result.data?.getStringExtra("subAdminArea"))
                binding.etPostalCode.setText(result.data?.getStringExtra("postalCode"))
                point = if(result.data?.getStringExtra("latitude")!!.isNotEmpty()
                    && result.data?.getStringExtra("longitude")!!.isNotEmpty()){
                    LatLng(
                        result.data?.getStringExtra("latitude")!!.toDouble(),
                        result.data?.getStringExtra("longitude")!!.toDouble()
                    )
                }
                else{
                    null
                }
                val mapFragment =
                    supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
                mapFragment!!.getMapAsync { mMap ->
                    mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

                    mMap.clear() //clear old markers

                    val googlePlex = CameraPosition.builder()
                        .target(LatLng(result.data?.getStringExtra("latitude")!!.toDouble(), result.data?.getStringExtra("longitude")!!.toDouble()))
                        .zoom(18f)
                        .bearing(0f)
                        .tilt(45f)
                        .build()

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 10000, null)

                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(result.data?.getStringExtra("latitude")!!.toDouble(), result.data?.getStringExtra("longitude")!!.toDouble()))
//                            .title("Spider Man")
//                            .icon(bitmapDescriptorFromVector(activity, R.drawable.spider))
                    )
//
//                    mMap.addMarker(
//                        MarkerOptions()
//                            .position(LatLng(37.4629101, -122.2449094))
//                            .title("Iron Man")
//                            .snippet("His Talent : Plenty of money")
//                    )
//
//                    mMap.addMarker(
//                        MarkerOptions()
//                            .position(LatLng(37.3092293, -122.1136845))
//                            .title("Captain America")
//                    )
                }
            }
        }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        firebaseAuth = FirebaseAuth.getInstance()
        formatNumber = FormatNumber()
        phoneNumberUtil = PhoneNumberUtil.createInstance(this)

        checkUserCurrent()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        uid = intent.getStringExtra("uid").toString()
        contactId = intent.getStringExtra("contactId").toString()
        companyId = intent.getStringExtra("companyId").toString()
        userKey = intent.getStringExtra("userKey").toString()
        firebaseUserID = intent.getStringExtra("firebaseUserID").toString()
        createBy = intent.getStringExtra("createBy").toString()
        createDate = intent.getStringExtra("createDate").toString()

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_times)
        toolbar.setNavigationOnClickListener {
            val intent = Intent()
            intent.putExtra("from", "EditContactActivity")
            setResult(Activity.RESULT_OK, intent)
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)

        intent.removeExtra("address")
        intent.removeExtra("featureName")
        intent.removeExtra("adminArea")
        intent.removeExtra("subAdminArea")
        intent.removeExtra("postalCode")
        intent.removeExtra("countryName")
        intent.removeExtra("locality")
        intent.removeExtra("latitude")
        intent.removeExtra("longitude")
        SessionManager.clearData(this@EditContactActivity)

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

        binding.etPosition.setOnClickListener {
            getDataPosition()
        }

        binding.btnAddContact.setOnClickListener {
            verifyPassword()
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersRefrence = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(uid)
        contactRefrence = FirebaseDatabase.getInstance().reference
            .child("Contact")
            .child(uid)
            .child(contactId)
        companyRefrence = FirebaseDatabase.getInstance().reference
            .child("Company")
        contactsSharedRefrence = FirebaseDatabase.getInstance().reference
            .child("ContactsShared")
            .child(contactId)

        usersRefrence!!
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists())
                    {
                        val user: Users? = p0.getValue(Users::class.java)
                        binding.etFirstname.setText(user?.getFullName())
                        binding.etEmail.setText(user?.getIdNumber())
                        val number = PhoneNumberUtils.formatNumber(user?.getPhoneNumber().toString(), Locale.getDefault().country)
                        val countryIsoCode = getCountryIsoCode(number)
                        binding.phoneTv.setCountryForNameCode(countryIsoCode)
                        val phone = number.split(" ")
                        if(phone.size in 2..2 && phone.isNotEmpty()){
                            binding.etMobile.setText(phone[1])
                        }
                        else if(phone.size >= 2 && phone.isNotEmpty()){
                            val tes = phone[1]+phone[2]
                            binding.etMobile.setText(tes)
                        }
                        else{
                            binding.etMobile.setText(phone[0].substring(2, phone[0].length))
                        }
                        binding.etPosition.text = user?.getPosition()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

        binding.btnLocation.setOnClickListener {
            val intent = Intent(context, MapActivity::class.java)
            intent.putExtra("latitude", latitude)
            intent.putExtra("longitude", longitude)
            intent.putExtra("address", binding.etCompanyAddress.text.toString())
            secondActivityWithResult.launch(intent)
        }

        binding.etSource.setOnClickListener {
            getDataSource()
        }

        binding.etStatus.setOnClickListener {
            getDataStatus()
        }

        binding.etReligion.setOnClickListener {
            getDataReligion()
        }

        val date = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, day)
            updateLabel()
        }
        binding.etBirthday.setOnClickListener(View.OnClickListener {
            DatePickerDialog(
                this@EditContactActivity,
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        })

        binding.done.setOnClickListener {
            addContact(uid, contactId, companyId)
        }

//        binding.btnAddContact.setOnClickListener {
//            addContact(uid, contactId, companyId)
//        }

        initMobile()
        initCompanyPhone()

    }

    private fun initCompanyPhone() {
        binding.etCompanyPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                val digits: Int = binding.etCompanyPhone.text.toString().length
                if (digits > 1) lastChar = binding.etCompanyPhone.text.toString().substring(digits - 1)
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val digits: Int = binding.etCompanyPhone.text.toString().length
                Log.d("LENGTH", "" + digits)
                if (lastChar != "-") {
                    if (digits == 3 || digits == 8) {
                        binding.etCompanyPhone.append("-")
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun initMobile() {
        binding.etMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                val digits: Int = binding.etMobile.text.toString().length
                if (digits > 1) lastChar = binding.etMobile.text.toString().substring(digits - 1)
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val digits: Int = binding.etMobile.text.toString().length
                Log.d("LENGTH", "" + digits)
                if (lastChar != "-") {
                    if (digits == 3 || digits == 8) {
                        binding.etMobile.append("-")
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun setMapExists(companyLatitude: String?, companyLongitude: String?) {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment?.getMapAsync { mMap ->
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

            mMap.clear() //clear old markers

            latitude = companyLatitude.toString()
            longitude = companyLongitude.toString()

            val googlePlex = CameraPosition.builder()
                .target(LatLng(latitude.toDouble(), longitude.toDouble()))
                .zoom(18f)
                .bearing(0f)
                .tilt(45f)
                .build()

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 10000, null)

            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude.toDouble(), longitude.toDouble()))
            )
        }
    }

    private fun addContact(uid: String, contactId: String, companyId: String) {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        Helper().hideKeyboard(this@EditContactActivity)
        if(binding.etFirstname.text.isNotEmpty()
            && binding.etMobile.text?.trim()?.isNotEmpty() == true
            && binding.etCompanyState.text.isNotEmpty()
            && binding.etCompanyCity.text.isNotEmpty()
        ) {
            val refContact = FirebaseDatabase.getInstance().reference
            val contactId = contactId
            val refCompany = FirebaseDatabase.getInstance().reference
            val companyId = companyId

            val contactHashMap = HashMap<String, Any>()
            contactHashMap.clear()
            contactHashMap["uid"] = uid
            contactHashMap["contactId"] = contactId!!
            contactHashMap["companyId"] = companyId!!
            contactHashMap["jobTitle"] = binding.etJobTitle.text.toString()
            contactHashMap["firstName"] = binding.etFirstname.text.toString()
            contactHashMap["lastName"] = binding.etLastname.text.toString()
            contactHashMap["email"] = binding.etEmail.text.toString()
            val codeCountry = binding.phoneTv.selectedCountryCodeWithPlus
            val phoneNumber: String = binding.etMobile.text?.trim().toString()
            contactHashMap["mobile"] = codeCountry+cleanPhoneNumber(phoneNumber.replace("-", ""))
            contactHashMap["source"] = binding.etSource.text.toString()
            contactHashMap["status"] = binding.etStatus.text.toString()
            contactHashMap["reason"] = binding.etReason.text.toString()
            val checkedOption: Int = binding.etProspect.checkedRadioButtonId
            radioButton = findViewById(checkedOption)
            contactHashMap["prospect"] = radioButton.text.toString()
            contactHashMap["birthday"] = binding.etBirthday.text.toString()
            contactHashMap["religion"] = binding.etReligion.text.toString()
            contactHashMap["note"] = binding.etNote.text.toString()
            contactHashMap["createDate"] =  DateHelper().todayTime()

            val companyHashMap = HashMap<String, Any>()
            companyHashMap.clear()
            companyHashMap["companyId"] = companyId
            companyHashMap["companyName"] = binding.etCompanyName.text.toString()
            companyHashMap["companyPhone"] = binding.etCompanyPhone.text.toString()
            companyHashMap["companyAddress"] = binding.etCompanyAddress.text.toString()
            companyHashMap["companyState"] = binding.etCompanyState.text.toString()
            companyHashMap["companyCity"] = binding.etCompanyCity.text.toString()
            companyHashMap["postalCode"] = binding.etPostalCode.text.toString()

            databaseReference = refContact.child("Contact").child(uid).child(contactId)
            databaseReference.updateChildren(contactHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        if(SessionManager.getDataString(this@EditContactActivity, "latitude")?.isNotEmpty() == true) {
                            companyHashMap["latitude"] =
                                SessionManager.getDataString(this@EditContactActivity, "latitude")
                                    .toString()
                        }
                        else{
                            companyHashMap["latitude"] = latitude
                        }
                        if(SessionManager.getDataString(this@EditContactActivity, "longitude")?.isNotEmpty() == true) {
                            companyHashMap["longitude"] =
                                SessionManager.getDataString(this@EditContactActivity, "longitude")
                                    .toString()
                        }
                        else{
                            companyHashMap["longitude"] = longitude
                        }
                        companyHashMap["createDate"] =  DateHelper().todayTime()

                        refCompany.child("Company").child(companyId)
                            .updateChildren(companyHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Helper().showToast("Update data is Successful!", this@EditContactActivity)
                                    binding.llProgressBar.preload.visibility = View.GONE
                                    onBackPressedDispatcher.onBackPressed()
//                                    val intent = Intent(this, DetailContactActivity::class.java)
//                                    intent.putExtra("uid", uid)
//                                    intent.putExtra("contactId", contactId)
//                                    NavigationHelper().navigateToActivityFlags(this, intent)
                                }
                                else{
                                    binding.llProgressBar.preload.visibility = View.GONE
                                    Helper().showToast("Data Company is Something Wrong!!!", this@EditContactActivity)
                                }
                            }
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Contact is Something Wrong!!!", this@EditContactActivity)
                    }
                }
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            Helper().showToast("Name, mobile and company information is required!", this@EditContactActivity)
        }
    }

    private fun updateLabel() {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        binding.etBirthday.text = dateFormat.format(myCalendar.time)
    }

    private fun getDataReligion() {
        selectedReligion = religion[selectedReligionIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Religion")
            .setSingleChoiceItems(religion, selectedReligionIndex) { _, which ->
                selectedReligionIndex = which
                selectedReligion = religion[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["religion"] = "$selectedReligion"
                binding.etReligion.text = "$selectedReligion"
            }
            .show()
    }

    private fun getDataStatus() {
        selectedStatus = statusCustomer[selectedStatusIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Status Customer")
            .setSingleChoiceItems(statusCustomer, selectedStatusIndex) { _, which ->
                selectedStatusIndex = which
                selectedStatus = statusCustomer[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["status"] = "$selectedStatus"
                binding.etStatus.text = "$selectedStatus"
                if(selectedStatus == "Reject"){
                    binding.llReason.visibility = View.VISIBLE
                }
                else{
                    binding.llReason.visibility = View.GONE
                }
            }
            .show()
    }

    private fun getDataSource() {
        selectedSource = source[selectedSourceIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Source")
            .setSingleChoiceItems(source, selectedSourceIndex) { _, which ->
                selectedSourceIndex = which
                selectedSource = source[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["source"] = "$selectedSource"
                binding.etSource.text = "$selectedSource"
            }
            .show()
    }

    private fun checkUserCurrent() {
        if(firebaseAuth.currentUser?.uid == null){
            val intent = Intent(this@EditContactActivity, LoginActivity::class.java)
            NavigationHelper().navigateToActivityFlags(this@EditContactActivity, intent)
        }
    }

    private fun cleanPhoneNumber(phoneNumber: String): Any {
        val length = phoneNumber.length
        if (phoneNumber[0] == '0') {
            if (length > 1) {
                return phoneNumber.substring(1, length)
            }
        }
        else if (phoneNumber[0] == '+') {
            if (length > 3) {
                return phoneNumber.substring(3, length)
            }
        }
        else{
            return phoneNumber
        }
        return false
    }

    private fun getDataPosition() {
        selectedPosition = position[selectedPositionIndex]
        MaterialAlertDialogBuilder(this)
            .setTitle("Select JOB")
            .setSingleChoiceItems(position, selectedPositionIndex) { _, which ->
                selectedPositionIndex = which
                selectedPosition = position[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["source"] = "$selectedPosition"
                binding.etPosition.text = "$selectedPosition"
            }
            .show()
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams", "SetTextI18n")
    private fun verifyPassword() {
        val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_verify_password, null)

        val etPassword = dialogView.findViewById<EditText>(R.id.et_password)
        etPassword.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }

        val verification = dialogView.findViewById<LinearLayout>(R.id.verification)
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
        }

        verification.setOnClickListener {
            if(etPassword.text.isNotEmpty()) {
                binding.llProgressBar.preload.visibility = View.VISIBLE
                alertDialog.dismiss()
                firebaseAuth.signInWithEmailAndPassword(firebaseAuth.currentUser?.email.toString(), etPassword.text.toString())
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            val hashMap = HashMap<String, Any>()
                            hashMap["fullName"] = binding.etFirstname.text.toString()
                            hashMap["idNumber"] = binding.etEmail.text.toString()
                            hashMap["email"] = binding.etEmail.text.toString().plus("@gmail.com")
                            hashMap["phoneNumber"] = binding.phoneTv.selectedCountryCodeWithPlus+cleanPhoneNumber(binding.etMobile.text.toString().replace("-", ""))
                            hashMap["photo"] = "https://firebasestorage.googleapis.com/v0/b/siapnikah-app.appspot.com/o/profile.png?alt=media&token=9dabafa6-50b0-4472-aa22-a5e06b673674"
                            hashMap["status"] = "pending"
                            hashMap["position"] = binding.etPosition.text.toString()
                            hashMap["gender"] = ""
                            hashMap["birthDay"] = ""
                            hashMap["latitude"] = ""
                            hashMap["longitude"] = ""
                            hashMap["userKey"] = userKey
                            hashMap["uid"] = firebaseUserID
                            hashMap["createBy"] = createBy
                            hashMap["updateBy"] = firebaseAuth.currentUser?.uid.toString()
                            hashMap["createDate"] =  createDate.toInt()
                            hashMap["updateDate"] =  DateHelper().todayTime()

                            val refUser = FirebaseDatabase.getInstance().reference
                            databaseReference = refUser.child("Users").child(uid)
                            databaseReference.updateChildren(hashMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Helper().showToast("Update data is Successful!", this@EditContactActivity)
                                        binding.llProgressBar.preload.visibility = View.GONE
                                        val intent = Intent()
                                        intent.putExtra("from", "AddContactActivity")
                                        setResult(Activity.RESULT_OK, intent)
                                        onBackPressedDispatcher.onBackPressed()
                                    }
                                    else{
                                        binding.llProgressBar.preload.visibility = View.GONE
                                        Helper().showToast("Data User is Something Wrong!!!", this@EditContactActivity)
                                    }
                                }
                        }
                        else{
                            Helper().showToast(uid, this)
                            binding.llProgressBar.preload.visibility = View.GONE
                            Helper().showToast("Data is Something Wrong!!!", this@EditContactActivity)
                        }
                    }
                    .addOnFailureListener {e ->
                        alertDialog.show()
                    }
            }
            else{
                Helper().showToast("ERROR", this)
                val tvVerification = dialogView.findViewById<TextView>(R.id.tv_verification)
                tvVerification?.visibility = View.VISIBLE
                tvVerification?.text = "Password is required!"
            }
        }
    }

    private fun getCountryIsoCode(number: String): String? {
        val validatedNumber = if (number.startsWith("+")) number else "+$number"

        val phoneNumber = try {
            phoneNumberUtil.parse(validatedNumber, null)
        } catch (e: NumberParseException) {
            Log.e("ERROR", "error during parsing a number")
            null
        } ?: return null

        return phoneNumberUtil.getRegionCodeForCountryCode(phoneNumber.countryCode)
    }
}