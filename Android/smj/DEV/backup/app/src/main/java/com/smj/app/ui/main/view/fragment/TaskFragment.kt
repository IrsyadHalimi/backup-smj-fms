package com.smj.app.ui.main.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smj.app.R
import com.smj.app.databinding.FragmentTaksBinding
import com.smj.app.helper.DateHelper
import com.smj.app.helper.FormatNumber
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.auth.model.Users
import com.smj.app.ui.contact.adapter.ShareUsersAdapter
import com.smj.app.ui.main.view.adapter.TabShiftAdapter
import com.smj.app.ui.task.model.TaskGroupList
import com.smj.app.ui.task.view.AddTaskActivity
import com.smj.app.utils.session.SessionManager

class TaskFragment : Fragment() {

    private lateinit var binding: FragmentTaksBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerViewUserList: RecyclerView
    private var firebaseUser: FirebaseUser? = null
    private var formatNumber: FormatNumber? = null

    private var context: Context? = null

    var positionTab: Int = 0
    lateinit var dialogView: View

    private var taskGroupList: ArrayList<TaskGroupList>? = null
    private var userList: ArrayList<Users>? = null
    private var shareUserList: ArrayList<Users>? = null

    private val addTaksResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == RESULT_OK){
            binding.llProgressBar.preload.visibility = View.GONE
        }
        else{
            binding.llProgressBar.preload.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaksBinding.inflate(layoutInflater)

        context = requireContext().applicationContext
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        context = requireContext().applicationContext
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        formatNumber = FormatNumber()

        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user: Users? = p0.getValue(Users::class.java)

                        if (context != null) {
                            if (user?.getPosition() == "Manager HRGA" || user?.getPosition() == "MCC Field" || user?.getPosition() == "Jr.Foreman MCC" || user?.getPosition() == "MT MCC") {
                                binding.llAddTasks.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        initViews()
        val tabLayout = binding.tabLayoutId
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onTabSelected(tab: TabLayout.Tab?) {
                positionTab = tab?.position!!
                if(tab.position == 0){
                    binding.llProgressBar.preload.visibility = View.GONE
                    binding.etSearchTodo.visibility = View.VISIBLE
                    binding.etSearchDoing.visibility = View.GONE
                    binding.etSearchDone.visibility = View.GONE

                    binding.tvFilter.visibility = View.VISIBLE
                    binding.tvFilterDoing.visibility = View.GONE
                    binding.tvFilterDone.visibility = View.GONE

                    binding.tvCategory.visibility = View.VISIBLE
                    binding.tvCategoryDoing.visibility = View.GONE
                    binding.tvCategoryDone.visibility = View.GONE
                }
                if(tab.position == 1){
                    binding.llProgressBar.preload.visibility = View.GONE
                    binding.etSearchTodo.visibility = View.GONE
                    binding.etSearchDoing.visibility = View.VISIBLE
                    binding.etSearchDone.visibility = View.GONE

                    binding.tvFilter.visibility = View.GONE
                    binding.tvFilterDoing.visibility = View.VISIBLE
                    binding.tvFilterDone.visibility = View.GONE

                    binding.tvCategory.visibility = View.GONE
                    binding.tvCategoryDoing.visibility = View.VISIBLE
                    binding.tvCategoryDone.visibility = View.GONE
                }
                if(tab.position == 2){
                    binding.llProgressBar.preload.visibility = View.GONE
                    binding.etSearchTodo.visibility = View.GONE
                    binding.etSearchDoing.visibility = View.GONE
                    binding.etSearchDone.visibility = View.VISIBLE

                    binding.tvFilter.visibility = View.GONE
                    binding.tvFilterDoing.visibility = View.GONE
                    binding.tvFilterDone.visibility = View.VISIBLE

                    binding.tvCategory.visibility = View.GONE
                    binding.tvCategoryDoing.visibility = View.GONE
                    binding.tvCategoryDone.visibility = View.VISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.i("TABPOSITION", tab?.position.toString())
            }

        })

        binding.addTaks.setOnClickListener {
            binding.llProgressBar.preload.visibility = View.VISIBLE
            val intent = Intent(requireContext(), AddTaskActivity::class.java)
            addTaksResult.launch(intent)
        }

        binding.ivShare.setOnClickListener {
            shareTasks()
        }

        taskGroupList = ArrayList()
        userList = ArrayList()
        shareUserList = ArrayList()

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun shareTasks() {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_share_tasks, null)

        val etEmail = dialogView.findViewById<EditText>(R.id.et_email)
        etEmail.setOnTouchListener { v, _ ->
            v.isFocusable = false
            v.isFocusableInTouchMode = true
            false
        }

        val shareTasks = dialogView.findViewById<LinearLayout>(R.id.share_tasks)
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
        }

        shareTasks.setOnClickListener {
            if(etEmail.text.isNotEmpty()) {
                binding.llProgressBar.preload.visibility = View.VISIBLE
                checkEmailExists(dialogView, alertDialog, etEmail.text)
            }
            else{
                val notification = dialogView.findViewById<TextView>(R.id.tv_notification)
                notification?.visibility = View.VISIBLE
                notification?.text = "Email is required!"
            }
        }

        recyclerViewUserList = dialogView.findViewById(R.id.recycler_view_user_list)
        recyclerViewUserList.setHasFixedSize(true)
        recyclerViewUserList.layoutManager = LinearLayoutManager(context)
        sharedRecipients(dialogView)
    }

    private fun sharedRecipients(dialogView: View?) {
        val refShareContact = FirebaseDatabase.getInstance().reference
        refShareContact.child("SharedRecipients")
            .child(firebaseUser?.uid.toString())
            .child("Tasks")
            .orderByChild("createDate")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.llProgressBar.preload.visibility = View.GONE
                    dialogView?.findViewById<RecyclerView>(R.id.recycler_view_user_list)?.visibility = View.VISIBLE
                    shareUserList?.clear()
                    if(snapshot.exists()) {
                        for (postSnapshot in snapshot.children) {
                            val shareUserDataList = postSnapshot.getValue(Users::class.java)
                            shareUserList?.add(shareUserDataList!!)
                        }
                        shareUserList?.sortByDescending { it.getCreateDate() }
                        val shareUsersAdapter = ShareUsersAdapter(requireContext(), shareUserList!!)
                        recyclerViewUserList.layoutManager = LinearLayoutManager(activity)
                        recyclerViewUserList.adapter = shareUsersAdapter
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        dialogView?.findViewById<RecyclerView>(R.id.recycler_view_user_list)?.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.llProgressBar.preload.visibility = View.GONE
                    val notification = dialogView?.findViewById<TextView>(R.id.tv_notification)
                    notification?.visibility = View.VISIBLE
                    notification?.text = error.message
                    Helper().showToast(error.message, requireActivity())
                }

            })
    }

    private fun checkEmailExists(dialogView: View?, alertDialog: AlertDialog, email: Editable?) {
        val notification = dialogView?.findViewById<TextView>(R.id.tv_notification)
        val imm = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = requireActivity().currentFocus
        if (view == null) {
            view = View(requireActivity())
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        notification?.visibility = View.GONE
        notification?.text = ""
        val refUser = FirebaseDatabase.getInstance().reference
        refUser.child("Users")
            .orderByChild("email")
            .equalTo(email.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.llProgressBar.preload.visibility = View.GONE
                    if(snapshot.exists()) {
                        userList?.clear()
                        for (postSnapshot in snapshot.children) {
                            val currentUser = postSnapshot.getValue(Users::class.java)
                            userList?.add(currentUser!!)
                        }
                        if(userList?.get(0)?.getEmail() != firebaseAuth.currentUser?.email){
                            binding.llProgressBar.preload.visibility = View.VISIBLE
                            binding.llProgressBar.preload.bringToFront()
                            getTasks(dialogView, alertDialog)
                        }
                        else{
                            val notification = dialogView?.findViewById<TextView>(R.id.tv_notification)
                            notification?.visibility = View.VISIBLE
                            notification?.text = "The system does not allow sending email to self"
                        }
                    }
                    else{
                        val notification = dialogView?.findViewById<TextView>(R.id.tv_notification)
                        notification?.visibility = View.VISIBLE
                        notification?.text = "Unregistered e-mail!"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.llProgressBar.preload.visibility = View.GONE
                    val notification = dialogView?.findViewById<TextView>(R.id.tv_notification)
                    notification?.visibility = View.VISIBLE
                    notification?.text = error.message
                    Helper().showToast(error.message, requireActivity())
                }

            })
    }

    private fun getTasks(dialogView: View?, alertDialog: AlertDialog) {
        val refTasks = FirebaseDatabase.getInstance().reference
            .child("Tasks")
            .child(firebaseUser!!.uid)
        refTasks.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (taskGroupList as ArrayList).clear()
                if(snapshot.exists()){
                    for (dataSnapshot in snapshot.children) {
                        val task = dataSnapshot.getValue(TaskGroupList::class.java)
                        (taskGroupList as ArrayList).add(task!!)
                    }
                    addTasks(dialogView, alertDialog)
                }
                else{
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun addTasks(dialogView: View?, alertDialog: AlertDialog) {
        Helper().hideKeyboard(requireActivity())
        val senderUid = firebaseUser?.uid.toString()
        val recipientUid = userList?.get(0)?.getUid().toString()
        val dataTasks: ArrayList<TaskGroupList>? = taskGroupList
        val refTasks = FirebaseDatabase.getInstance().reference
        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = firebaseAuth.currentUser!!.uid
        hashMap["photo"] = userList?.get(0)?.getPhoto().toString()
        hashMap["fullName"] = userList?.get(0)?.getFullName().toString()
        hashMap["email"] = userList?.get(0)?.getEmail().toString()
        hashMap["idNumber"] = userList?.get(0)?.getIdNumber().toString()
        hashMap["status"] = userList?.get(0)?.getStatus().toString()
        hashMap["gender"] = userList?.get(0)?.getGender().toString()
        hashMap["birthDay"] = userList?.get(0)?.getBirthDay().toString()
        hashMap["latitude"] = userList?.get(0)?.getLatitude().toString()
        hashMap["longitude"] = userList?.get(0)?.getLongitude().toString()
        hashMap["createDate"] = DateHelper().todayTime()
        databaseReference = refTasks
            .child("SharedRecipients")
            .child(senderUid)
            .child("Tasks")
            .child(recipientUid)
//        databaseReference.updateChildren(hashMap)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful)
//                {
//                    for (task in dataTasks!!){
//                        val taskHashMap = HashMap<String, Any>()
//                        taskHashMap["uid"] = firebaseAuth.currentUser!!.uid
//                        taskHashMap["taskId"] = task.getTaskId().toString()
//                        taskHashMap["category"] = task.getCategory().toString()
//                        taskHashMap["reasonRejected"] = task.getReasonRejected().toString()
//                        taskHashMap["contactId"] = task.getContactId().toString()
//                        taskHashMap["productId"] = task.getProductId().toString()
//                        val nominal = task.getNominal().toString()
//                        taskHashMap["nominal"] = nominal.replace(",","")
//                        taskHashMap["priority"] = task.getPriority()!!.toInt()
//                        taskHashMap["dueDate"] = task.getDueDate().toString()
//                        taskHashMap["reminder"] = task.getReminder().toString()
//                        taskHashMap["status"] = task.getStatus()!!.toInt()
//                        taskHashMap["note"] = task.getNote().toString()
//                        taskHashMap["createDate"] =  DateHelper().todayTime()
//                        taskHashMap["mapTime"] = task.getMapTime().toString()
//                        taskHashMap["mapAddress"] = task.getMapAddress().toString()
//                        taskHashMap["latitude"] = task.getLatitude().toString().ifEmpty { "-2.548926" }
//                        taskHashMap["longitude"] = task.getLongitude().toString().ifEmpty { "118.0148634" }
//
//                        databaseReference = refTasks
//                            .child("Shared")
//                            .child(recipientUid)
//                            .child("Tasks")
//                            .child(task.getTaskId().toString())
//                        databaseReference.updateChildren(taskHashMap)
//                            .addOnCompleteListener { task ->
//                                if (task.isSuccessful) {
//                                    Helper().showToast("Adding data is Successful!", requireActivity())
//                                    sharedRecipients(dialogView)
//                                }
//                                else{
//                                    binding.llProgressBar.preload.visibility = View.GONE
//                                    Helper().showToast("Data Task is Something Wrong!!!", requireActivity())
//                                }
//                            }
//                    }
//                }
//                else
//                {
//                    binding.llProgressBar.preload.visibility = View.GONE
//                    Helper().showToast("Data Task is Something Wrong!!!", requireActivity())
//                }
//            }
    }

    private fun getLocation() {
        binding.llProgressBar.preload.visibility = View.VISIBLE
        if (checkPermission()) {
            LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation.addOnCompleteListener(requireActivity()) { task ->
                val location: Location? = task.result
                Log.i("LocationGET", location.toString())
                if (location != null) {
                    SessionManager.saveLatitude(requireContext(), location.latitude.toString())
                    SessionManager.saveLongitude(requireContext(), location.longitude.toString())
                    val intent = Intent(requireContext(), AddTaskActivity::class.java)
                    NavigationHelper().navigateToActivityCallback(requireActivity(), intent)
                }
                else{
                    (activity?.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                }
            }
        }
        else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getLocation()
        } else {
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            (activity?.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
        }
    }

    private fun initViews() {
        initTabLayout()
    }

    private fun initTabLayout() {
        val tabLayoutMediator = TabLayoutMediator(binding.tabLayoutId, binding.viewPagerId) { tab, position ->
            when (position) {
                0 -> tab.text = HtmlCompat.fromHtml("<b>"+ TAB.SIANG.displayName+"</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)
                1 -> tab.text = HtmlCompat.fromHtml("<b>"+ TAB.MALAM.displayName+"</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        }
        binding.viewPagerId.adapter = TabShiftAdapter(requireActivity() as AppCompatActivity)
        tabLayoutMediator.attach()
    }

    enum class TAB(val displayName: String) {
        SIANG("Shift Siang"),
        MALAM("Shift Malam"),
    }
}

