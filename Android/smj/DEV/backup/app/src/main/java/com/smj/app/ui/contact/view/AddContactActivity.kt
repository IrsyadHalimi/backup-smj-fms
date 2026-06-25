package com.smj.app.ui.contact.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.smj.app.R
import com.smj.app.databinding.ActivityAddContactBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.ui.google.view.MapActivity
import com.smj.app.utils.session.SessionManager
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContactBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mDbRef: DatabaseReference
    private var context: Context? = null
    private lateinit var phoneNumberUtil: PhoneNumberUtil

    lateinit var dialogView: View

    lateinit var radioButton: RadioButton

    private var latitudeStore: String? = null
    private var longitudeStore: String? = null
    private var firebaseUserID: String = ""

    lateinit var mMap: GoogleMap
    private var point: LatLng? = null

    private val myCalendar: Calendar = Calendar.getInstance()

    var lastChar = " "
    var i:Int = 0

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.",
        ReplaceWith("onBackPressedDispatcher.onBackPressed()")
    )
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

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

    //start contact
    private val contactResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK){
                binding.etFirstname.setText(result.data?.getStringExtra("name"))
                val number = PhoneNumberUtils.formatNumber(result.data?.getStringExtra("number"), Locale.getDefault().country)
                val phone = number.split(" ")
                val countryIsoCode = getCountryIsoCode(number)
                binding.phoneTv.setCountryForNameCode(countryIsoCode)
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
            }
        }

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("PrivateResource", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        firebaseAuth = FirebaseAuth.getInstance()

        phoneNumberUtil = PhoneNumberUtil.createInstance(this)

        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue = tm.networkCountryIso
        binding.phoneTv.setCountryForNameCode(countryCodeValue)

        checkUserCurrent()

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener {
            val intent = Intent()
            intent.putExtra("from", "AddContactActivity")
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
        SessionManager.clearData(this@AddContactActivity)

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

        firebaseUser = FirebaseAuth.getInstance().currentUser

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
        
        binding.tvPhoneBook.setOnClickListener {
            if (checkPermissions()) {
                binding.llProgressBar.preload.visibility = View.GONE
                val intent = Intent(this, PhoneBookActivity::class.java)
                contactResult.launch(intent)
            }
            else{
                requestPermissions()
            }
        }

        binding.btnLocation.setOnClickListener {
            val intent = Intent(context, MapActivity::class.java)
            if(SessionManager.getDataString(this@AddContactActivity, "latitude") != null) {
                intent.putExtra("address", binding.etCompanyAddress.text.toString())
                intent.putExtra(
                    "featureName",
                    SessionManager.getDataString(this@AddContactActivity, "featureName")
                )
                intent.putExtra(
                    "adminArea",
                    SessionManager.getDataString(this@AddContactActivity, "adminArea")
                )
                intent.putExtra(
                    "subAdminArea",
                    SessionManager.getDataString(this@AddContactActivity, "subAdminArea")
                )
                intent.putExtra(
                    "postalCode",
                    SessionManager.getDataString(this@AddContactActivity, "postalCode")
                )
                intent.putExtra(
                    "countryCode",
                    SessionManager.getDataString(this@AddContactActivity, "countryCode")
                )
                intent.putExtra(
                    "countryName",
                    SessionManager.getDataString(this@AddContactActivity, "countryName")
                )
                intent.putExtra(
                    "locality",
                    SessionManager.getDataString(this@AddContactActivity, "locality")
                )
                intent.putExtra(
                    "latitude",
                    SessionManager.getDataString(this@AddContactActivity, "latitude")
                )
                intent.putExtra(
                    "longitude",
                    SessionManager.getDataString(this@AddContactActivity, "longitude")
                )
            }
            else {
                intent.putExtra("latitude", latitudeStore)
                intent.putExtra("longitude", longitudeStore)
            }
            secondActivityWithResult.launch(intent)
        }

        binding.etSource.setOnClickListener {
            getDataSource()
        }

        binding.etPosition.setOnClickListener {
            getDataPosition()
        }

        binding.etStatus.setOnClickListener {
            getDataStatus()
        }

        binding.etReligion.setOnClickListener {
            getDataReligion()
        }

        val date = OnDateSetListener { _, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                updateLabel()
            }
        binding.etBirthday.setOnClickListener(View.OnClickListener {
            DatePickerDialog(
                this@AddContactActivity,
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        })

        binding.btnAddContact.setOnClickListener {
//            addContact()
//            signUp()
            verifyPassword()
        }

        initMobile()
        initCompanyPhone()

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissions() {
        requestPermissionLauncher.launch(requiredPermissions)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        val intent = Intent(this, PhoneBookActivity::class.java)
        intentActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun intentActivity(intent: Intent) {
        val isReadContact = isReadContact(Manifest.permission.READ_CONTACTS)

        if (isReadContact){
            binding.llProgressBar.preload.visibility = View.GONE
            NavigationHelper().navigateToActivityCallback(this, intent)
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            SessionManager.saveRejected(this, i)
            if(SessionManager.getDataString(this, "rejected")?.toInt()!! > 1) {
                alertRejected(intent)
            }
            else{
                buildAlertMessageLocation(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buildAlertMessageLocation(intent: Intent) {
        MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(HtmlCompat.fromHtml("<b>Permission</b><br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setMessage(HtmlCompat.fromHtml("Permission is required to view the list of numbers in the phone book<br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("OK") { _, _ ->
                i=SessionManager.getDataString(this, "rejected")!!.toInt()+1
                SessionManager.saveRejected(this, i)
                binding.llProgressBar.preload.visibility = View.GONE
                if(SessionManager.getDataString(this, "rejected")?.toInt()!! > 2) {
                    alertRejected(intent)
                }
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun alertRejected(intent: Intent) {
        MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(HtmlCompat.fromHtml("<b>Account Rejected</b><br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setMessage(HtmlCompat.fromHtml("Your account access is Rejected for unusual activity<br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("OK") { _, _ ->
                (getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
            }
            .show()
    }

    private fun isReadContact(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
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

    private fun addContact() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        Helper().hideKeyboard(this@AddContactActivity)

        if(binding.etFirstname.text.isNotEmpty()
            && binding.etMobile.text?.trim()?.isNotEmpty() == true
            && binding.etCompanyState.text.isNotEmpty()
            && binding.etCompanyCity.text.isNotEmpty()
            && findViewById<RadioButton>(binding.etProspect.checkedRadioButtonId) != null
        ) {
            val refContact = FirebaseDatabase.getInstance().reference
            val contactId = refContact.push().key
            val refCompany = FirebaseDatabase.getInstance().reference
            val companyId = refCompany.push().key

            val contactHashMap = HashMap<String, Any>()
            contactHashMap["uid"] = firebaseAuth.currentUser!!.uid
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

            databaseReference = refContact.child("Contact").child(firebaseAuth.currentUser!!.uid).child(contactId)
            databaseReference.updateChildren(contactHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        val companyHashMap = HashMap<String, Any>()
                        companyHashMap["companyId"] = companyId
                        companyHashMap["companyName"] = binding.etCompanyName.text.toString()
                        companyHashMap["companyPhone"] = binding.etCompanyPhone.text.toString()
                        companyHashMap["companyAddress"] = binding.etCompanyAddress.text.toString()
                        companyHashMap["companyState"] = binding.etCompanyState.text.toString()
                        companyHashMap["companyCity"] = binding.etCompanyCity.text.toString()
                        companyHashMap["postalCode"] = binding.etPostalCode.text.toString()
                        companyHashMap["latitude"] = SessionManager.getDataString(this@AddContactActivity, "latitude").toString()
                        companyHashMap["longitude"] = SessionManager.getDataString(this@AddContactActivity, "longitude").toString()
                        contactHashMap["createDate"] =  DateHelper().todayTime()

                        refContact.child("Company").child(companyId)
                            .updateChildren(companyHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Helper().showToast("Adding data is Successful!", this@AddContactActivity)
                                    binding.llProgressBar.preload.visibility = View.GONE
                                    val intent = Intent()
                                    intent.putExtra("from", "AddContactActivity")
                                    setResult(Activity.RESULT_OK, intent)
                                    onBackPressedDispatcher.onBackPressed()
                                }
                                else{
                                    binding.llProgressBar.preload.visibility = View.GONE
                                    Helper().showToast("Data Company is Something Wrong!!!", this@AddContactActivity)
                                }
                            }
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Data Contact is Something Wrong!!!", this@AddContactActivity)
                    }
                }
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
            Helper().showToast("Name, mobile, Prospect and company information is required!", this@AddContactActivity)
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

    private fun checkUserCurrent() {
        if(firebaseAuth.currentUser?.uid == null){
            val intent = Intent(this@AddContactActivity, LoginActivity::class.java)
            NavigationHelper().navigateToActivityFlags(this@AddContactActivity, intent)
        }
    }

    private fun cleanPhoneNumber(phoneNumber: String): Any {
        val length = phoneNumber.length
        if ((phoneNumber[0] == '0')) {
            if (length > 1) {
                return phoneNumber.substring(1, length)
            }
        }
        else{
            return phoneNumber
        }
        return false
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

    companion object {
        /**
         * App's required permissions.
         */
        val requiredPermissions = arrayOf(
            Manifest.permission.READ_CONTACTS
        )
    }

    @SuppressLint("SetTextI18n")
    private fun signUp(
        emailExist: String,
        uidExist: String,
        passwordExist: String,
        dialogView: View
    ) {
        val fullName: String = binding.etFirstname.text.toString()
        val codeCountry = binding.phoneTv.selectedCountryCodeWithPlus
        val id: String = binding.etEmail.text.toString()
        val email: String = binding.etEmail.text.toString().plus("@gmail.com")
        val phoneNumber: String = binding.etMobile.text?.trim().toString()
        val position: String = binding.etPosition.text.toString()
        val password = "Smj123Mcc"

        if (fullName == "")
        {
            binding.llProgressBar.preload.visibility = View.GONE
            Toast.makeText(this@AddContactActivity, context!!.getString(R.string.please_write_fullname), Toast.LENGTH_LONG).show()
        }
        if (email == "")
        {
            binding.llProgressBar.preload.visibility = View.GONE
            Toast.makeText(this@AddContactActivity, context!!.getString(R.string.please_write_id), Toast.LENGTH_LONG).show()
        }
        if (phoneNumber == "")
        {
            Toast.makeText(this@AddContactActivity, context!!.getString(R.string.please_write_phone), Toast.LENGTH_LONG).show()
        }
        if (fullName != "" && email != "" && phoneNumber != "")
        {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful)
                    {
                        firebaseUserID = firebaseAuth.currentUser!!.uid

                        val refUser = FirebaseDatabase.getInstance().reference
                        val userKey = refUser.push().key
                        mDbRef = refUser.child("Users").child(firebaseUserID)

                        val hashMap = HashMap<String, Any>()
                        hashMap["fullName"] = fullName
                        hashMap["idNumber"] = id
                        hashMap["email"] = email
                        hashMap["phoneNumber"] = codeCountry+cleanPhoneNumber(phoneNumber.replace("-", ""))
                        hashMap["photo"] = "https://firebasestorage.googleapis.com/v0/b/smj-app-94dec.appspot.com/o/worker.png?alt=media&token=a5ea2480-6479-4f5b-a32e-93fdd0380afa"
                        hashMap["status"] = "pending"
                        hashMap["position"] = position
                        hashMap["gender"] = ""
                        hashMap["birthDay"] = ""
                        hashMap["latitude"] = ""
                        hashMap["longitude"] = ""
                        hashMap["userKey"] = userKey!!
                        hashMap["uid"] = firebaseUserID
                        hashMap["createBy"] = uidExist
                        hashMap["updateBy"] = ""
                        hashMap["createDate"] =  DateHelper().todayTime()
                        hashMap["updateDate"] =  ""

                        mDbRef.updateChildren(hashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful)
                                {
                                    firebaseAuth.signOut()
                                    firebaseAuth.signInWithEmailAndPassword(emailExist, passwordExist)
                                        .addOnCompleteListener {
                                            if(it.isSuccessful) {
                                                Helper().showToast("Adding data is Successful!", this@AddContactActivity)
                                                binding.llProgressBar.preload.visibility = View.GONE
                                                val intent = Intent()
                                                intent.putExtra("from", "AddContactActivity")
                                                setResult(Activity.RESULT_OK, intent)
                                                onBackPressedDispatcher.onBackPressed()
                                            }
                                            else{
                                                binding.llProgressBar.preload.visibility = View.GONE
                                                Helper().showToast("Data is Something Wrong!!!", this@AddContactActivity)
                                            }
                                        }
                                        .addOnFailureListener {e ->
                                            dialogView.visibility = View.VISIBLE
                                        }
                                }
                            }
                    }
                    else {
                        binding.llProgressBar.preload.visibility = View.GONE
                        if (task.exception!!.message.toString() == "The email address is already in use by another account.") {
                            binding.registerFailedTv.text = "ID Number has been used by other accounts"
                        }else {
                            binding.registerFailedTv.text = task.exception!!.message.toString()
                        }
                    }
                }
        }
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
                            signUp(firebaseAuth.currentUser?.email.toString(), firebaseAuth.currentUser?.uid.toString(), etPassword.text.toString(), dialogView)
                        }
                        else{
                            binding.llProgressBar.preload.visibility = View.GONE
                            Helper().showToast("Data is Something Wrong!!!", this@AddContactActivity)
                        }
                    }
                    .addOnFailureListener {e ->
                        alertDialog.show()
                    }
            }
            else{
                val tvVerification = dialogView.findViewById<TextView>(R.id.tv_verification)
                tvVerification?.visibility = View.VISIBLE
                tvVerification?.text = "Password is required!"
            }
        }
    }
}