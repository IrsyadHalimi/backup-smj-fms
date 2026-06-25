package com.smj.app.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.smj.app.databinding.FragmentDashboardBinding
import com.smj.app.helper.Helper
import com.smj.app.helper.NavigationHelper
import com.smj.app.ui.contact.model.ContactList
import com.smj.app.ui.contact.view.NotificationContactsActivity
import com.smj.app.ui.task.model.TaskGroupList
import com.smj.app.ui.task.view.NotificationTasksActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.smj.app.R

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference
    private var contactsList: ArrayList<ContactList>? = null
    private var contactsLast: ArrayList<ContactList>? = null
    private var taskGroupList: ArrayList<TaskGroupList>? = null
    private var taskGroupLast: ArrayList<TaskGroupList>? = null
    private var taskTelemarketingList: ArrayList<TaskGroupList>? = null
    private var taskTelemarketingLast: ArrayList<TaskGroupList>? = null

    private val myCalendar: Calendar = Calendar.getInstance()

    lateinit var dialogView: View

    // on below line we are creating
    // variables for our bar chart
    lateinit var barChart: BarChart

    // on below line we are creating
    // a variable for bar data
    lateinit var barData: BarData

    // on below line we are creating a
    // variable for bar data set
    lateinit var barDataSet: BarDataSet

    // on below line we are creating array list for bar data
    lateinit var barEntriesList: ArrayList<BarEntry>

    //start time
    private lateinit var selectedFilter: String
    private var selectedFilterIndex: Int = 0
    private val statusFilter = arrayOf(
        "Today"
    )
    private var selectedTime: String? = null

    @SuppressLint("PrivateResource", "SetTextI18n", "SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        // on below line we are initializing
        // our variable with their ids.
        barChart = binding.idBarChart

        // on below line we are calling get bar
        // chart data to add data to our array list
        getBarChartData()

        // on below line we are initializing our bar data set
        barDataSet = BarDataSet(barEntriesList, "Bar Chart Data")

        // on below line we are initializing our bar data
        barData = BarData(barDataSet)

        // on below line we are setting data to our bar chart
        barChart.data = barData

        // on below line we are setting colors for our bar chart text
        barDataSet.valueTextColor = Color.BLACK

        // on below line we are setting color for our bar data set
        barDataSet.color = ContextCompat.getColor(requireContext(), R.color.blue)

        // on below line we are setting text size
        barDataSet.valueTextSize = 16f

        // on below line we are enabling description as false
        barChart.description.isEnabled = false

        context?.let {
            ContextCompat.getColor(
                it,
                R.color.green_light
            )
        }?.let {
            Helper().changeStatusBarColor(
                it, true,
                requireActivity()
            )
        }

        context?.let {
            ContextCompat.getColor(
                it,
                R.color.green_light
            )
        }?.let {
            Helper().changeStatusNavColor(
                it, true,
                requireActivity()
            )
        }

        notification()

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        binding.tvDate.text = currentDate

        val date = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, day)
            updateLabel()
        }
        binding.ivDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        binding.tvDate.setOnClickListener(View.OnClickListener {
            DatePickerDialog(
                requireContext(),
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        })

        binding.tvNotifCount.setOnClickListener {
            if(binding.tvNotifCount.text.isNotEmpty()){
                binding.llProgressBar.preload.visibility = View.VISIBLE
                notificationConfirm()
            }
            else{
                Helper().showToast("Notification is Empty!", requireActivity())
            }
        }

        binding.tvSelectTime.setOnClickListener {
            filterTime()
        }

        binding.ivSettings.setOnClickListener {
            bottomSheetSettings()
        }

        setDefaultFilter()

        taskCount()

        binding.tvDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,before: Int, count: Int) {
                taskCount()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        return binding.root
    }

    private fun updateLabel() {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        binding.tvDate.text = dateFormat.format(myCalendar.time)
    }

    private fun setDefaultFilter() {
        val refFilter = FirebaseDatabase.getInstance().reference
            .child("SettingUpTime")
            .child(firebaseUser!!.uid)
        refFilter.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    binding.tvSelectTime.text = snapshot.child("filterBy").value.toString()

                    selectedTime = binding.tvSelectTime.text.toString()
                    contactCount(selectedTime!!)
                    tasksCount(selectedTime!!)
                }
                else{
                    selectedTime = binding.tvSelectTime.text.toString()
                    contactCount(selectedTime!!)
                    tasksCount(selectedTime!!)
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onCancelled(error: DatabaseError) {
                binding.tvSelectTime.text = "Today"
                contactCount(selectedTime!!)
                tasksCount(selectedTime!!)
            }
        })
    }

    private fun bottomSheetSettings() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog_time, null)

        val btnClose = view.findViewById<Button>(R.id.btn_seting_up_default_time)
        btnClose.setOnClickListener {
            val selectedId: Int = view.findViewById<RadioGroup>(R.id.radio_group_time).checkedRadioButtonId
            val radioButton = view.findViewById(selectedId) as RadioButton
            setUpDefaultFilterTime(dialog, radioButton)
        }

        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun setUpDefaultFilterTime(dialog: BottomSheetDialog, radioButton: RadioButton) {
        if (radioButton.text.isNotEmpty()){
            dialog.dismiss()
            binding.llProgressBar.preload.visibility = View.VISIBLE

            val refSettingUp = FirebaseDatabase.getInstance().reference
            val settingUpContact = HashMap<String, Any>()
            settingUpContact["uid"] = firebaseUser!!.uid
            settingUpContact["filterBy"] = radioButton.text.toString()

            databaseReference = refSettingUp.child("SettingUpTime").child(firebaseAuth.currentUser!!.uid)
            databaseReference.updateChildren(settingUpContact)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        binding.llProgressBar.preload.visibility = View.GONE
                        binding.tvSelectTime.text = radioButton.text.toString()

                        selectedTime = binding.tvSelectTime.text.toString()
                        tasksCount(selectedTime!!)
                        contactCount(selectedTime!!)
                    }
                    else{
                        binding.llProgressBar.preload.visibility = View.GONE
                        Helper().showToast("Something Wrong!!!", requireActivity())
                    }
                }
        }
    }

    private fun contactCount(selectedTime: String) {
        contactsList = ArrayList()
        contactsLast = ArrayList()
        val refTask = FirebaseDatabase.getInstance().reference
            .child("Contact")
            .child(firebaseUser!!.uid)
        refTask.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                (contactsList as ArrayList).clear()
                if(snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val contact = dataSnapshot.getValue(ContactList::class.java)
                        (contactsList as ArrayList).add(contact!!)
                    }
                    binding.tvContact.text = contactsList!!.size.toString()
                    binding.tvLastMonthContact.text = "Target Plan: "+contactsLast!!.size.toString()
                    binding.tvContactLabel.text = "Worker - $selectedTime"

                    if(contactsLast!!.size != 0) {
                        val contactPercent =
                            ((contactsList!!.size - contactsLast!!.size) / contactsLast!!.size) * 100
                        binding.tvContactPercent.text = "$contactPercent %"
                    }
                    else{
                        val contactPercent =
                            ((contactsList!!.size - 0) / 1) * 100
                        binding.tvContactPercent.text = "$contactPercent %"
                    }
                }
                else{
                    binding.tvContact.text = "0"
                    binding.tvLastMonthContact.text = "Target Plan: 0"
                    binding.tvContactLabel.text = "Worker - $selectedTime"
                    binding.tvContactPercent.text = "0 %"
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onCancelled(error: DatabaseError) {
                binding.tvContact.text = "0"
                binding.tvLastMonthContact.text = "Target Plan: 0"
                binding.tvContactLabel.text = "Worker - $selectedTime"
                binding.tvContactPercent.text = "0 %"
            }

        })
    }

    private fun tasksCount(selectedTime: String) {
        taskGroupList = ArrayList()
        taskGroupLast = ArrayList()
        taskTelemarketingList = ArrayList()
        taskTelemarketingLast = ArrayList()
        val refTask = FirebaseDatabase.getInstance().reference
            .child("Tasks")
            .child(firebaseUser!!.uid)
//        refTask.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                (taskGroupList as ArrayList).clear()
//                (taskGroupLast as ArrayList).clear()
//                (taskTelemarketingList as ArrayList).clear()
//                (taskTelemarketingLast as ArrayList).clear()
//                if(snapshot.exists()) {
//                    for (dataSnapshot in snapshot.children) {
//                        val tasks = dataSnapshot.getValue(TaskGroupList::class.java)
//                        val dateExist = DateHelper().timeStimeToDateTime(tasks?.getCreateDate()!!.toInt())
//                        val dateTaskDiff = DateHelper().covertTimeToDateDiff(dateExist)
//                        if(selectedTime == "Today"){
//                            if(TimeUnit.MILLISECONDS.toHours(dateTaskDiff!!) < 24){
//                                (taskGroupList as ArrayList).add(tasks)
//                                if(tasks.getCategory() == "Telemarketing"){
//                                    (taskTelemarketingList as ArrayList).add(tasks)
//                                }
//                            }
//                        }
//                        else if(selectedTime == "This Week"){
//                            if(TimeUnit.MILLISECONDS.toDays(dateTaskDiff!!) < 7){
//                                (taskGroupList as ArrayList).add(tasks)
//                                if(tasks.getCategory() == "Telemarketing"){
//                                    (taskTelemarketingList as ArrayList).add(tasks)
//                                }
//                            }
//                        }
//                        else if(selectedTime == "This Month"){
//                            if(TimeUnit.MILLISECONDS.toDays(dateTaskDiff!!) < 30){
//                                (taskGroupList as ArrayList).add(tasks)
//                                if(tasks.getCategory() == "Telemarketing"){
//                                    (taskTelemarketingList as ArrayList).add(tasks)
//                                }
//                            }
//                        }
//                        else if (selectedTime == "This Year") {
//                            if(TimeUnit.MILLISECONDS.toDays(dateTaskDiff!!) < 360){
//                                (taskGroupList as ArrayList).add(tasks)
//                                if(tasks.getCategory() == "Telemarketing"){
//                                    (taskTelemarketingList as ArrayList).add(tasks)
//                                }
//                            }
//                        }
//
//                        if(TimeUnit.MILLISECONDS.toDays(dateTaskDiff!!) in 31..60){
//                            (taskGroupLast as ArrayList).add(tasks)
//                            if(tasks.getCategory() == "Telemarketing"){
//                                (taskTelemarketingLast as ArrayList).add(tasks)
//                            }
//                        }
//                    }
//                    binding.tvCalls.text = taskTelemarketingList!!.size.toString()
//                    binding.tvTasks.text = taskGroupList!!.size.toString()
//                    binding.tvLastMonth.text = "Target Plan: "+taskGroupLast!!.size.toString()
//                    binding.tvLastMonthCall.text = "Target plan: "+taskTelemarketingLast!!.size.toString()
//                    binding.tvTasksLabel.text = "Fleet Plans - $selectedTime"
//                    binding.tvCallLabel.text = "Fleet Units - $selectedTime"
//
//                    if(taskGroupLast!!.size != 0) {
//                        val tasksPercent =
//                            ((taskGroupList!!.size - taskGroupLast!!.size) / taskGroupLast!!.size) * 100
//                        binding.tvTasksPercent.text = "$tasksPercent %"
//                    }
//                    else{
//                        val tasksPercent =
//                            ((taskGroupList!!.size - 0) / 1) * 100
//                        binding.tvTasksPercent.text = "$tasksPercent %"
//                    }
//
//                    if(taskTelemarketingLast!!.size != 0) {
//                        val tasksCallPercent =
//                            ((taskTelemarketingList!!.size - taskTelemarketingLast!!.size) / taskTelemarketingLast!!.size) * 100
//                        binding.tvCallPercent.text = "$tasksCallPercent %"
//                    }
//                    else{
//                        val tasksCallPercent =
//                            ((taskTelemarketingList!!.size - 0) / 1) * 100
//                        binding.tvCallPercent.text = "$tasksCallPercent %"
//                    }
//                }
//                else{
//                    binding.tvCalls.text = "0"
//                    binding.tvTasks.text = "0"
//                    binding.tvLastMonth.text = "Target Plan: 0"
//                    binding.tvLastMonthCall.text = "Target Plan: 0"
//                    binding.tvTasksLabel.text = "Fleet Plans - $selectedTime"
//                    binding.tvCallLabel.text = "Fleet Units - $selectedTime"
//                    binding.tvTasksPercent.text = "0 %"
//                    binding.tvCallPercent.text = "0 %"
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                binding.tvCalls.text = "0"
//                binding.tvTasks.text = "0"
//                binding.tvLastMonth.text = "Target Plan: 0"
//                binding.tvLastMonthCall.text = "Target Plan: 0"
//                binding.tvTasksLabel.text = "Fleet Plans - $selectedTime"
//                binding.tvCallLabel.text = "Fleet Units - $selectedTime"
//                binding.tvTasksPercent.text = "0 %"
//                binding.tvCallPercent.text = "0 %"
//            }
//
//        })
    }

    private fun filterTime() {
        selectedFilter = statusFilter[selectedFilterIndex]
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Time Selection")
            .setCancelable(false)
            .setSingleChoiceItems(statusFilter, selectedFilterIndex) { _, which ->
                selectedFilterIndex = which
                selectedFilter = statusFilter[which]
            }
            .setPositiveButton("Choice") { _, _ ->
                val mapData = HashMap<String, Any>()
                mapData["status"] = "$selectedFilter"
                binding.tvSelectTime.text = "$selectedFilter"
                selectedTime = binding.tvSelectTime.text.toString()
                tasksCount(selectedTime!!)
                contactCount(selectedTime!!)
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun notificationConfirm() {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.Body_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.layout_notification, null)
        val close = dialogView.findViewById<LinearLayout>(R.id.icon_close)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        val alertDialog = dialogBuilder.create()
        binding.llProgressBar.preload.visibility = View.GONE
        alertDialog.show()

        close.setOnClickListener {
            alertDialog.dismiss()
        }

        notificationContacts(dialogView)
        notificationTasks(dialogView)
    }

    private fun notificationTasks(dialogView: View) {
        val refTask = FirebaseDatabase.getInstance().reference
            .child("Shared")
            .child(firebaseUser!!.uid)
            .child("Tasks")
        refTask.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount > 0){
                    val boxTask = dialogView.findViewById<LinearLayout>(R.id.box_tasks)
                    boxTask.visibility = View.VISIBLE
                    val tvTasksCount = dialogView.findViewById<TextView>(R.id.tv_tasks_count)
                    tvTasksCount.text = snapshot.childrenCount.toString()

                    boxTask.setOnClickListener {
                        val intent = Intent(requireContext(), NotificationTasksActivity::class.java)
                        NavigationHelper().navigateToActivityCallback(requireActivity(), intent)
                    }
                }
                else{
                    binding.llProgressBar.preload.visibility = View.GONE
                    val boxTask = dialogView.findViewById<LinearLayout>(R.id.box_tasks)
                    boxTask.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                val boxTask = dialogView.findViewById<LinearLayout>(R.id.box_tasks)
                boxTask.visibility = View.GONE
            }

        })
    }

    private fun notificationContacts(dialogView: View) {
        val refTask = FirebaseDatabase.getInstance().reference
            .child("Shared")
            .child(firebaseUser!!.uid)
            .child("Contacts")
        refTask.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount > 0){
                    val boxContact = dialogView.findViewById<LinearLayout>(R.id.box_contact)
                    boxContact.visibility = View.VISIBLE
                    val tvContactCount = dialogView.findViewById<TextView>(R.id.tv_contact_count)
                    tvContactCount.text = snapshot.childrenCount.toString()

                    boxContact.setOnClickListener {
                        val intent = Intent(requireContext(), NotificationContactsActivity::class.java)
                        NavigationHelper().navigateToActivityCallback(requireActivity(), intent)
                    }
                }
                else{
                    val boxContact = dialogView.findViewById<LinearLayout>(R.id.box_contact)
                    boxContact.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                val boxContact = dialogView.findViewById<LinearLayout>(R.id.box_contact)
                boxContact.visibility = View.GONE
            }

        })
    }

    private fun notification() {
        val refTask = FirebaseDatabase.getInstance().reference
            .child("Shared")
            .child(firebaseUser!!.uid)
        refTask.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount > 0){
                    binding.tvNotifCount.visibility = View.VISIBLE
                    binding.tvNotifCount.text = snapshot.childrenCount.toString()
                }
                else{
                    binding.tvNotifCount.visibility = View.GONE
                    binding.tvNotifCount.text = ""
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.tvNotifCount.visibility = View.GONE
                binding.tvNotifCount.text = ""
            }

        })
    }

    private fun getBarChartData() {
        barEntriesList = ArrayList()

        // on below line we are adding data
        // to our bar entries list
        barEntriesList.add(BarEntry(1f, 1f))
        barEntriesList.add(BarEntry(2f, 2f))
        barEntriesList.add(BarEntry(3f, 3f))
        barEntriesList.add(BarEntry(4f, 4f))
        barEntriesList.add(BarEntry(5f, 5f))
    }

    private fun taskCount() {
        val refTask = FirebaseDatabase.getInstance().reference.child("TasksFleetPlan")
            .orderByChild("date")
            .equalTo(binding.tvDate.text.toString())
        refTask.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    binding.tvTasks.text = snapshot.childrenCount.toString()
                }
                else{
                    binding.tvTasks.text = "0"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() : DashboardFragment{
            val fragment = DashboardFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}