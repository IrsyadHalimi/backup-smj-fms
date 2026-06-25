package com.smj.app.ui.google.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.smj.app.R
import com.smj.app.databinding.ActivityMapBinding
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.view.LoginActivity
import com.smj.app.ui.contact.view.AddContactActivity
import com.smj.app.utils.session.SessionManager
import java.io.IOException
import java.util.*

/**
 * Created by MuhamadRiyadi on 01/01/2023
 * Phone: 08174100212
 * Website: www.prokonco.com
 */
class MapActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback,
    GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapClickListener {

    private lateinit var locationManager: LocationManager
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    private lateinit var binding: ActivityMapBinding
    private lateinit var firebaseAuth: FirebaseAuth

    var context: Context? = null
    var address: String? = null
    var feature: String? = null
    var admin: String? = null
    var subAdmin: String? = null
    var locality: String? = null
    var postalCode: String? = null
    var countryCode: String? = null
    var countryName: String? = null
    var latitude: String? = null
    var longitude: String? = null

    lateinit var mMap: GoogleMap
    private var point: LatLng? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SourceLockedOrientationActivity", "ClickableViewAccessibility",
        "PrivateResource"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        context = this.applicationContext
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        firebaseAuth = FirebaseAuth.getInstance()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkUserCurrent()
        getLocation()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        if(intent.getStringExtra("latitude")?.isNotEmpty() == true && intent.getStringExtra("longitude")?.isNotEmpty() == true) {
            intent.getStringExtra("latitude")?.toDouble()?.let {
                showMap(
                    it,
                    intent.getStringExtra("longitude")?.toDouble()!!
                )
            }
            binding.etAlamat.setText(
                intent.getStringExtra("address").toString()
            )
        }


        binding.etAlamat.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }
        binding.etAlamat.hint = "Type address here..."
        binding.btnCari.setOnClickListener {
                view -> onClick(view)
            Helper().hideKeyboard(this)
        }
        binding.btnSimpan.setOnClickListener { view -> onClick(view) }

        binding.tvKetuk.text = HtmlCompat.fromHtml("Ketuk pada area maps untuk memindahkan pin", HtmlCompat.FROM_HTML_MODE_LEGACY)

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
                com.google.android.material.R.color.mtrl_btn_transparent_bg_color
            )
        }.let {
            Helper().changeStatusNavColor(
                it, true,
                this
            )
        }
    }

    private fun checkUserCurrent() {
        if(firebaseAuth.currentUser?.uid == null){
            val intent = Intent(this@MapActivity, LoginActivity::class.java)
            NavigationHelper().navigateToActivityFlags(this@MapActivity, intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun onClick(view: View) {
        when(view.id){
            R.id.btn_cari->{
                intent.removeExtra("address")
                intent.removeExtra("featureName")
                intent.removeExtra("adminArea")
                intent.removeExtra("subAdminArea")
                intent.removeExtra("postalCode")
                intent.removeExtra("countryName")
                intent.removeExtra("locality")
                intent.removeExtra("latitude")
                intent.removeExtra("longitude")
                if (binding.etAlamat.text.toString().isNotEmpty()) {
                    point = this.getLocationFromAddress(this, binding.etAlamat.text.toString())
                    if (point!=null) {
                        mMap.clear()
                        point?.let { mMap.addMarker(MarkerOptions().position(it).title("Ketuk untuk memindahkan").draggable(true)) }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point!!, 18f))
                    }
                    else{
                        Toast.makeText(this, "Alamat tidak ditemukan, coba kembali", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    SessionManager.clearData(this@MapActivity)
                    this.recreate()
                }
            }
            R.id.btn_simpan->{
                val intent = Intent()
                intent.putExtra("address", binding.etAlamat.text.toString())
                intent.putExtra("featureName", SessionManager.getDataString(this@MapActivity, "featureName"))
                intent.putExtra("adminArea", SessionManager.getDataString(this@MapActivity, "adminArea"))
                intent.putExtra("subAdminArea", SessionManager.getDataString(this@MapActivity, "subAdminArea"))
                intent.putExtra("postalCode", SessionManager.getDataString(this@MapActivity, "postalCode"))
                intent.putExtra("countryCode", SessionManager.getDataString(this@MapActivity, "countryCode"))
                intent.putExtra("countryName", SessionManager.getDataString(this@MapActivity, "countryName"))
                intent.putExtra("locality", SessionManager.getDataString(this@MapActivity, "locality"))
                intent.putExtra("latitude", SessionManager.getDataString(this@MapActivity, "latitude"))
                intent.putExtra("longitude", SessionManager.getDataString(this@MapActivity, "longitude"))
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerDragListener(this)
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
        if (point != null) {
            Helper().showToast(point.toString(), this)
            mMap.addMarker(MarkerOptions().position(point!!).title("Ketuk untuk memindahkan").draggable(true))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point!!, 18f))
        }
    }

    override fun onMarkerDrag(p0: Marker) {
        point = p0.position
    }

    override fun onMarkerDragEnd(p0: Marker) {
        point = p0.position
    }

    override fun onMarkerDragStart(p0: Marker) {

    }

    override fun onMarkerClick(p0: Marker): Boolean {
        mMap.uiSettings.isScrollGesturesEnabled = false
        return false
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMapClick(p0: LatLng) {
        point = p0
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(point!!).title("Ketuk untuk memindahkan").draggable(true))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point!!, 18f))
        getCompleteAddressString(point!!.latitude, point!!.longitude)
        binding.etAlamat.setText(
            SessionManager.getDataString(this@MapActivity, "address")
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getLocation() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (checkPermission()) {
            statusCheck()
            try {
                if (Build.VERSION.SDK_INT >= 31) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 16f, this)
                }
//                else if(Build.VERSION.SDK_INT in 31..32){
//                    if (isLocationEnabled()) {
//                        mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
//                            val location: Location? = task.result
//                            if (location != null) {
//                                getCompleteAddressStringOld(location.latitude, location.longitude)
//                            }
//                        }
//                    } else {
//                        Helper().showToast("Please turn on location", this)
//                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                        startActivity(intent)
//                    }
//                }
                else{
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 16f, this)
                }
            }
            catch (ex: IOException) {
                Helper().showToast(ex.message.toString(), this)
            }

        }
        else {
            requestPermission()
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onLocationChanged(location: Location) {
        try {
            if(SessionManager.getDataString(this@MapActivity, "address") != null){
                latitude = SessionManager.getDataString(this@MapActivity, "latitude")
                longitude = SessionManager.getDataString(this@MapActivity, "longitude")
            }
            else{
                latitude = location.latitude.toString()
                longitude = location.longitude.toString()
            }
            point = if(latitude != null && longitude != null){
                LatLng(
                    latitude!!.toDouble(),
                    longitude!!.toDouble())
            }else {
                Helper().showToast("onLocationChanged", this@MapActivity)
                this.getLocationFromAddress(this, binding.etAlamat.text.toString())
            }
            mMap.clear()
            point?.let { mMap.addMarker(MarkerOptions().position(it).title("Ketuk untuk memindahkan").draggable(true)) }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point!!, 18f))
            getCompleteAddressString(point!!.latitude, point!!.longitude)

            if(intent.getStringExtra("address")?.isNotEmpty() == true){
                binding.etAlamat.setText(
                    intent.getStringExtra("address").toString()
                )
            }
            else{
                binding.etAlamat.setText(
                    SessionManager.getDataString(this@MapActivity, "address")
                )
            }
            binding.llProgressBar.preload.visibility = View.GONE
        }
        catch (ex: IOException) {
            Helper().showToast(ex.message.toString(), this)
        }
    }

    private fun getLocationFromAddress(context: Context, strAddress: String): LatLng? {

        val coder = Geocoder(context)
        val address: List<Address>?
        var p1: LatLng? = null

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 1)
            if (address == null) {
                return null
            }

            if(address.isNotEmpty()) {
                val location = address[0]
                p1 = LatLng(location.latitude, location.longitude)
                Log.i("getLocationFromAddress", location.toString())
                if(location.getAddressLine(0)!=null) {
                    SessionManager.saveAddress(
                        applicationContext,
                        location.getAddressLine(0).toString()
                    )
                }
                else{
                    SessionManager.saveAddress(
                        applicationContext,
                        ""
                    )
                }
                if(location.adminArea!=null) {
                    SessionManager.saveAdmin(applicationContext, location.adminArea.toString())
                }
                else{
                    SessionManager.saveAdmin(applicationContext, "")
                }
                if(location.subAdminArea!=null) {
                    SessionManager.saveSubAdmin(
                        applicationContext,
                        location.subAdminArea.toString()
                    )
                }
                else{
                    SessionManager.saveSubAdmin(
                        applicationContext,
                        ""
                    )
                }
                if(location.featureName!=null) {
                    SessionManager.saveFeature(applicationContext, location.featureName.toString())
                }
                else{
                    SessionManager.saveFeature(applicationContext, "")
                }
                if(location.locality!=null) {
                    SessionManager.saveLocality(applicationContext, location.locality.toString())
                }
                else{
                    SessionManager.saveLocality(applicationContext, "")
                }
                if(location.postalCode!=null) {
                    SessionManager.savePostalCode(
                        applicationContext,
                        location.postalCode.toString()
                    )
                }
                else{
                    SessionManager.savePostalCode(
                        applicationContext,
                        ""
                    )
                }
                if(location.countryCode!=null) {
                    SessionManager.saveCountryCode(
                        applicationContext,
                        location.countryCode.toString()
                    )
                }
                else{
                    SessionManager.saveCountryCode(
                        applicationContext,
                        ""
                    )
                }
                if(location.countryName!=null) {
                    SessionManager.saveCountryName(
                        applicationContext,
                        location.countryName.toString()
                    )
                }
                else{
                    SessionManager.saveCountryName(
                        applicationContext,
                        ""
                    )
                }
                if(location.latitude!=null) {
                    SessionManager.saveLatitude(applicationContext, location.latitude.toString())
                }
                else{
                    SessionManager.saveLatitude(applicationContext, "")
                }
                if(location.longitude!=null) {
                    SessionManager.saveLongitude(applicationContext, location.longitude.toString())
                }
                else{
                    SessionManager.saveLongitude(applicationContext, "")
                }

                if(binding.etAlamat.text?.isNotEmpty() == true){
                    if(intent.getStringExtra("address")?.isNotEmpty() == true){
                        binding.etAlamat.setText(
                            intent.getStringExtra("address").toString()
                        )
                    }
//                    else if(SessionManager.getDataString(this@MapActivity, "address") != null){
//                        binding.etAlamat.setText(
//                            SessionManager.getDataString(this@MapActivity, "address")
//                        )
//                    }
                    else{
                        binding.etAlamat.setText(
                            binding.etAlamat.text.toString()
                        )
                    }
                }

            }

        } catch (ex: IOException) {
            FirebaseCrashlytics.getInstance().recordException(ex)
            ex.printStackTrace()
        }

        return p1
    }

    private fun checkPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        requestPermissionLauncher.launch(requiredPermissions)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it.isNotEmpty()) {
            getLocation()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            (getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
        }
    }

    private fun statusCheck() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun buildAlertMessageNoGps() {
        MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(HtmlCompat.fromHtml("<b>Status GPS:</b><br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setMessage("GPS Anda tampaknya dinonaktifkan, apakah Anda ingin mengaktifkannya?")
            .setPositiveButton("Yes") { _, _ -> startActivity(
                Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            ) }
            .setNegativeButton("No") { _, _ -> buildAlertNoGps()}
            .show()
    }

    private fun buildAlertNoGps() {
        MaterialAlertDialogBuilder(this, R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(HtmlCompat.fromHtml("<b>Status GPS:</b><br/>", HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setMessage("GPS wajib aktif untuk mengakses halaman google map!")
            .setNegativeButton("Ok") { _, _ -> navigate()}
            .show()
    }

    private fun navigate(){
        val intent = Intent(context, AddContactActivity::class.java)
        NavigationHelper().navigateToActivityCallback(this, intent)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double) {
        val geocoder = Geocoder(this@MapActivity, Locale.getDefault())
        geocoder.getFromLocation(LATITUDE, LONGITUDE, 1
        ) {p0 ->
            Log.i("getCompleteAddressString", p0.toString())
            SessionManager.saveAddress(applicationContext, p0[0].getAddressLine(0).toString())
            SessionManager.saveAdmin(applicationContext, p0[0].adminArea.toString())
            SessionManager.saveSubAdmin(applicationContext, p0[0].subAdminArea.toString())
            SessionManager.saveFeature(applicationContext, p0[0].featureName.toString())
            SessionManager.saveLocality(applicationContext, p0[0].locality.toString())
            SessionManager.savePostalCode(applicationContext, p0[0].postalCode.toString())
            SessionManager.saveCountryCode(applicationContext, p0[0].countryCode.toString())
            SessionManager.saveCountryName(applicationContext, p0[0].countryName.toString())
            SessionManager.saveLatitude(applicationContext, p0[0].latitude.toString())
            SessionManager.saveLongitude(applicationContext, p0[0].longitude.toString())

            if(intent.getStringExtra("address")?.isNotEmpty() == true){
                Helper().showToast("getCompleteAddressString", this@MapActivity)
                binding.etAlamat.setText(
                    intent.getStringExtra("address").toString()
                )
            }
            else{
                binding.etAlamat.setText(
                    SessionManager.getDataString(this@MapActivity, "address")
                )
            }
        }
    }

    companion object {
        /**
         * App's required permissions.
         */
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun getCompleteAddressStringOld(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val list: List<Address> =
            geocoder.getFromLocation(latitude, longitude, 1) as List<Address>
        binding.apply {
            showMap(latitude, longitude)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showMap(lat: Double, long: Double){
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment!!.getMapAsync { mMap ->
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

            mMap.clear() //clear old markers

            latitude = lat.toString()
            longitude = long.toString()

            val googlePlex = CameraPosition.builder()
                .target(LatLng(latitude!!.toDouble(), longitude!!.toDouble()))
                .zoom(25f)
                .bearing(0f)
                .tilt(45f)
                .build()

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null)

            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude!!.toDouble(), longitude!!.toDouble()))
            )
        }
//        point = if(latitude != null && longitude != null){
//            LatLng(
//                latitude!!.toDouble(),
//                longitude!!.toDouble())
//        }else {
//            Helper().showToast("onLocationChanged", this@MapActivity)
//            this.getLocationFromAddress(this, binding.etAlamat.text.toString())
//        }
//        mMap.clear()
//        point?.let { mMap.addMarker(MarkerOptions().position(it).title("Ketuk untuk memindahkan").draggable(true)) }
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point!!, 18f))
//        getCompleteAddressString(latitude!!.toDouble(), longitude!!.toDouble())
        binding.llProgressBar.preload.visibility = View.GONE
    }


}